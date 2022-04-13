package xyz.chalky.taboo.commands

import dev.minn.jda.ktx.Embed
import dev.minn.jda.ktx.interactions.getOption
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import xyz.chalky.taboo.backend.SlashCommand
import xyz.chalky.taboo.database.Config
import xyz.chalky.taboo.database.DatabaseManager
import xyz.chalky.taboo.util._reply
import xyz.chalky.taboo.util.onSubCommand
import java.awt.Color
import java.time.Instant

class ConfigSlashCommand : SlashCommand() {

    init {
        setCommandData(
            Commands.slash("config", "Server config")
                .addSubcommands(
                    SubcommandData(
                        "set", "Sets this server's config."
                    ).addOption(
                        OptionType.CHANNEL, "action-log", "Set the channel to log actions to.", true
                    ).addOption(
                        OptionType.CHANNEL, "join-leave-log", "Set the channel to log join/leave messages to.", true
                    ), SubcommandData(
                        "clear", "Clear this server's config."
                    ), SubcommandData(
                        "view", "View this server's config."
                    )
                )
        )
    }

    override fun executeCommand(event: SlashCommandInteractionEvent) {
        event.onSubCommand("set") {
            val actionLogChannel = event.getOption<TextChannel>("action-log")
            val joinLeaveLogChannel = event.getOption<TextChannel>("join-leave-log")
            val runCatching = runCatching {
                transaction {
                    Config.insert {
                        it[guildId] = event.guild!!.idLong
                        it[actionLog] = actionLogChannel!!.idLong
                        it[joinLeaveLog] = joinLeaveLogChannel!!.idLong
                    }
                }
            }
            runCatching.onFailure {
                DatabaseManager.logger.warn { "Failed to set config for guild ${event.guild!!.name}" }
                val embed = Embed {
                    title = "Failed to set config!"
                    description = "You may already have a config set! " +
                            "If this issue persists, please contact my owner."
                    color = Color.RED.hashCode()
                    timestamp = Instant.now()
                }
                event._reply(embed).queue()
            }
            runCatching.onSuccess {
                val embed = Embed {
                    title = "Config set!"
                    description = """
                        Action log channel set to ${actionLogChannel!!.asMention}
                        Join/leave log channel set to ${joinLeaveLogChannel!!.asMention}
                    """.trimIndent()
                    color = 0x9F90CF
                    timestamp = Instant.now()
                }
                event._reply(embed).queue()
            }
        }
        event.onSubCommand("clear") {
            val runCatching = runCatching {
                transaction {
                    Config.deleteWhere {
                        Config.guildId eq event.guild!!.idLong
                    }
                }
            }
            runCatching.onFailure {
                DatabaseManager.logger.warn { "Failed to clear config for guild ${event.guild!!.name}" }
                val embed = Embed {
                    title = "Failed to clear config!"
                    description = "If this issue persists, please contact my owner."
                    color = Color.RED.hashCode()
                    timestamp = Instant.now()
                }
                event._reply(embed).queue()
            }
            runCatching.onSuccess {
                val embed = Embed {
                    title = "Config cleared!"
                    color = 0x9F90CF
                    timestamp = Instant.now()
                }
                event._reply(embed).queue()
            }
        }
        event.onSubCommand("view") {
            transaction {
                Config.select {
                    Config.guildId eq event.guild!!.idLong
                }.forEach {
                    val embed = Embed {
                        title = "Config for ${event.guild!!.name}"
                        description = """
                            Action log channel: ${event.guild!!.getTextChannelById(it[Config.actionLog])!!.asMention}
                            Join/leave log channel: ${event.guild!!.getTextChannelById(it[Config.joinLeaveLog])!!.asMention}
                        """.trimIndent()
                        color = 0x9F90CF
                        timestamp = Instant.now()
                    }
                    event._reply(embed).queue()
                }
            }
        }
    }
}
