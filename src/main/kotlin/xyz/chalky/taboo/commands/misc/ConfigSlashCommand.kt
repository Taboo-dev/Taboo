package xyz.chalky.taboo.commands.misc

import dev.minn.jda.ktx.Embed
import dev.minn.jda.ktx.interactions.getOption
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import xyz.chalky.taboo.backend.CommandFlag
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
                    ).addOptions(
                        OptionData(OptionType.CHANNEL, "log", "Set the channel to log actions to.", true),
                        OptionData(OptionType.CHANNEL, "music", "Sets the music channel.", true)
                    ), SubcommandData(
                        "clear", "Clear this server's config."
                    ), SubcommandData(
                        "view", "View this server's config."
                    )
                )
        )
        addCommandFlags(CommandFlag.MODERATOR_ONLY)
    }

    override fun executeCommand(event: SlashCommandInteractionEvent) {
        event.deferReply(true).queue()
        event.onSubCommand("set") {
            val logChannel = event.getOption<TextChannel>("log")
            val musicChannel = event.getOption<TextChannel>("music")
            val runCatching = runCatching {
                transaction {
                    Config.insert {
                        it[guildId] = event.guild!!.idLong
                        it[log] = logChannel!!.idLong
                        it[music] = musicChannel!!.idLong
                    }
                }
            }
            runCatching.onFailure {
                DatabaseManager.logger.warn { "Failed to set config for guild ${event.guild!!.name}" }
                event._reply(
                    Embed {
                        title = "Failed to set config!"
                        description = "You may already have a config set! " +
                                "If this issue persists, please contact my owner."
                        color = Color.RED.hashCode()
                        timestamp = Instant.now()
                    }
                ).queue()
            }
            runCatching.onSuccess {
                event._reply(
                    Embed {
                        title = "Config set!"
                        description = """
                            Log channel set to ${logChannel!!.asMention}
                            Music channel set to ${musicChannel!!.asMention}
                        """.trimIndent()
                        color = 0x9F90CF
                        timestamp = Instant.now()
                    }
                ).queue()
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
                event._reply(
                    Embed {
                        title = "Failed to clear config!"
                        description = "If this issue persists, please contact my owner."
                        color = Color.RED.hashCode()
                        timestamp = Instant.now()
                    }
                ).queue()
            }
            runCatching.onSuccess {
                event._reply(
                    Embed {
                        title = "Config cleared!"
                        color = 0x9F90CF
                        timestamp = Instant.now()
                    }
                ).queue()
            }
        }
        event.onSubCommand("view") {
            transaction {
                Config.select {
                    Config.guildId eq event.guild!!.idLong
                }.forEach {
                    event._reply(
                        Embed {
                            title = "Config for ${event.guild!!.name}"
                            description = """
                            Log channel: ${event.guild!!.getTextChannelById(it[Config.log])!!.asMention}
                            Join/leave log channel: ${event.guild!!.getTextChannelById(it[Config.log])!!.asMention}
                            """.trimIndent()
                            color = 0x9F90CF
                            timestamp = Instant.now()
                        }
                    ).queue()
                }
            }
        }
    }
}
