package dev.taboo.taboo

import com.jagrosh.jdautilities.command.CommandClientBuilder
import com.jagrosh.jdautilities.commons.waiter.EventWaiter
import dev.taboo.taboo.commands.*
import dev.taboo.taboo.database.DatabaseManager
import dev.taboo.taboo.events.Events
import dev.taboo.taboo.events.GuildJoinHandler
import dev.taboo.taboo.interactions.Bookmark
import dev.taboo.taboo.util.PropertiesManager
import io.sentry.Sentry
import io.sentry.SentryOptions
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder
import net.dv8tion.jda.api.sharding.ShardManager
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
        val LOGGER: Logger = LoggerFactory.getLogger(Taboo::class.java)
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
                    LOGGER.info("Config file doesn't exist! Creating one now!")
                    exitProcess(0)
                }
            } catch (e: Exception) {
                LOGGER.error("Could not create config file!")
                exitProcess(0)
            }
        }
        Sentry.init { options ->
            options.dsn = PropertiesManager.sentryDsn
            options.tracesSampleRate = 1.0
            options.tracesSampler = SentryOptions.TracesSamplerCallback { 1.0 }
            options.setDebug(true)
        }
        val waiter = EventWaiter()
        val commands = CommandClientBuilder()
            .setHelpConsumer(null)
            .setPrefix("<@!${PropertiesManager.botId}> ")
            /*.setPrefixFunction {
                transaction {
                    DatabaseManager.PrefixManager.getPrefixFromGuild(it.guild.id)
                }
            }*/
            .setStatus(OnlineStatus.ONLINE)
            .setActivity(null)
            .setOwnerId(PropertiesManager.ownerId)
            .forceGuildOnly(PropertiesManager.guildId)
            .addSlashCommands(
                Ping(),
                Shutdown(),
                Stats(),
                Help(),
                Invite(),
                Support(),
                // Settings(),
                Suggest()
            ).addCommands(
                Ping(),
                Shutdown(),
                Stats(),
                Help(),
                Invite(),
                Support(),
                // Settings(),
                Suggest()
            ).build()
        val jda = DefaultShardManagerBuilder.createLight(PropertiesManager.token)
            .setEnabledIntents(GatewayIntent.GUILD_MESSAGES)
            .setRawEventsEnabled(true)
            .setAutoReconnect(true)
            .setEventManagerProvider { Events().manager }
            .addEventListeners(waiter, commands, GuildJoinHandler(), Bookmark())
            .setShardsTotal(-1)
            .build()
        DatabaseManager.startDb()
        this.jda = jda
        this.waiter = waiter
    }

}