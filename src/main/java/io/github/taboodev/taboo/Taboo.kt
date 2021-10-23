package io.github.taboodev.taboo

import com.jagrosh.jdautilities.command.CommandClientBuilder
import com.jagrosh.jdautilities.commons.waiter.EventWaiter
import io.github.taboodev.taboo.commands.Ping
import io.github.taboodev.taboo.commands.Prefix
import io.github.taboodev.taboo.commands.Stats
import io.github.taboodev.taboo.commands.owner.Shutdown
import io.github.taboodev.taboo.database.DatabaseManager
import io.github.taboodev.taboo.events.GuildJoinHandler
import io.github.taboodev.taboo.events.MessageLog
import io.github.taboodev.taboo.util.PrefixManager
import io.github.taboodev.taboo.util.PropertiesManager
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder
import net.dv8tion.jda.api.sharding.ShardManager
import net.dv8tion.jda.api.utils.ChunkingFilter
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import javax.security.auth.login.LoginException
import kotlin.system.exitProcess

class Taboo {

    @JvmField
    val jda: ShardManager
    @JvmField
    val waiter: EventWaiter

    companion object {
        @JvmField
        var INSTANCE: Taboo? = null
        @JvmField
        val LOG_TABOO: Logger = LoggerFactory.getLogger(Taboo::class.java)
        @Throws(LoginException::class)
        @JvmStatic
        fun main(args: Array<String>) {
            INSTANCE = Taboo()
        }
    }

    init {
        val properties = Properties()
        try {
            properties.load(FileInputStream("config.properties"))
            PropertiesManager.loadProperties(properties)
        } catch (e: FileNotFoundException) {
            val config = Path.of("config.properties")
            try {
                if (Files.notExists(config)) {
                    Files.createFile(config)
                    LOG_TABOO.info("Config file doesn't exist! Creating one now!")
                    exitProcess(0)
                }
            } catch (e: Exception) {
                LOG_TABOO.error("Could not create config file!")
                exitProcess(0)
            }
        }
        val jda = DefaultShardManagerBuilder.createLight(PropertiesManager.token)
            .setEnabledIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MEMBERS)
            .setChunkingFilter(ChunkingFilter.ALL)
            .setRawEventsEnabled(true)
            .setAutoReconnect(true)
            .addEventListeners(GuildJoinHandler(), MessageLog())
            .setShardsTotal(-1)
            .build()
        val waiter = EventWaiter()
        val prefix = String.format("<@!%s> ", PropertiesManager.botId)
        val commands = CommandClientBuilder()
            .setHelpConsumer(null)
            .setPrefix(prefix)
            .setPrefixFunction {
                transaction {
                    PrefixManager.getPrefixFromGuild(it.guild.id)
                }
            }
            .setStatus(OnlineStatus.ONLINE)
            .setActivity(null)
            .setOwnerId(PropertiesManager.ownerId)
            .forceGuildOnly(PropertiesManager.guildId)
            .addSlashCommands(
                Ping(),
                Shutdown(),
                Stats(),
                Prefix()
            ).addCommands(
                Ping(),
                Shutdown(),
                Stats(),
                Prefix()
            )
            .build()
        jda.addEventListener(waiter)
        jda.addEventListener(commands)
        DatabaseManager.startDb()
        this.jda = jda
        this.waiter = waiter
    }
}