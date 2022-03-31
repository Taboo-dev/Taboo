package xyz.chalky.taboo.commands

import dev.minn.jda.ktx.interactions.getOption
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.transactions.transaction
import xyz.chalky.taboo.backend.SlashCommand
import xyz.chalky.taboo.database.Config
import xyz.chalky.taboo.util._reply
import xyz.chalky.taboo.util.onSubCommand

class ConfigSlashCommand : SlashCommand() {

    init {
        setCommandData(
            Commands.slash("config", "Server config")
                .addSubcommands(
                    SubcommandData(
                        "set", "Sets this server's config"
                    ).addOption(
                        OptionType.CHANNEL, "action-log", "Set the channel to log actions to", true
                    )
                ).addSubcommands(
                    SubcommandData("clear", "Clear this server's config.")
                )
        )
    }

    override fun executeCommand(event: SlashCommandInteractionEvent) {
        event.onSubCommand("set") {
            val actionLogChannel = event.getOption<TextChannel>("action-log")
            val runCatching = runCatching {
                transaction {
                    Config.insertIgnore {
                        it[guildId] = event.guild!!.idLong
                        it[actionLog] = actionLogChannel!!.idLong
                    }
                }
            }
            runCatching.onFailure {
                event._reply("Failed to set config: ${it.message}").queue()
            }
            runCatching.onSuccess {
                event._reply("Config set!").queue()
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
                event._reply("Failed to clear config: ${it.message}").queue()
            }
            runCatching.onSuccess {
                event._reply("Config cleared!").queue()
            }
        }
    }
}
