package dev.taboo.taboo

import com.jagrosh.jdautilities.command.CommandClientBuilder
import com.jagrosh.jdautilities.commons.waiter.EventWaiter
import dev.taboo.taboo.commands.*
import dev.taboo.taboo.commands.owner.Shutdown
import dev.taboo.taboo.database.DatabaseManager
import dev.taboo.taboo.database.PrefixManager
import dev.taboo.taboo.events.GuildJoinHandler
import dev.taboo.taboo.events.MessageLog
import dev.taboo.taboo.util.PropertiesManager
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
        val TABOO_LOG: Logger = LoggerFactory.getLogger(Taboo::class.java)
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
                    TABOO_LOG.info("Config file doesn't exist! Creating one now!")
                    exitProcess(0)
                }
            } catch (e: Exception) {
                TABOO_LOG.error("Could not create config file!")
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
                Prefix(),
                Help(),
                Invite(),
                Support()
            ).addCommands(
                Ping(),
                Shutdown(),
                Stats(),
                Prefix(),
                Help(),
                Invite(),
                Support()
            ).build()
        jda.addEventListener(waiter, commands)
        DatabaseManager.startDb()
        this.jda = jda
        this.waiter = waiter
    }

}