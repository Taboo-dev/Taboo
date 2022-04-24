package xyz.chalky.taboo.commands.moderation

import dev.minn.jda.ktx.Embed
import dev.minn.jda.ktx.interactions.getOption
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.exceptions.HierarchyException
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import xyz.chalky.taboo.backend.CommandFlag
import xyz.chalky.taboo.backend.SlashCommand
import xyz.chalky.taboo.database.Config
import xyz.chalky.taboo.util._reply
import java.awt.Color
import java.time.Instant

class BanSlashCommand : SlashCommand() {

    init {
        setCommandData(
            Commands.slash("ban", "Bans a user from the server.")
                .addOption(OptionType.USER, "user", "The user to ban.", true)
                .addOption(OptionType.INTEGER, "del-days", "The amount of days to delete messages from the user.", false)
                .addOption(OptionType.STRING, "reason", "The reason for the ban.", false)
        )
        addCommandFlags(CommandFlag.MODERATOR_ONLY)
        isEphemeral = true
    }

    override fun executeCommand(event: SlashCommandInteractionEvent) {
        val target = event.getOption<User>("user")
        val delDays = if (event.getOption<Int>("del-days") == null) 0 else event.getOption<Int>("del-days")
        val reason = if (event.getOption<String>("reason") == null) "No reason provided." else event.getOption<String>("reason")
        try {
            event.guild!!.ban(target!!, delDays!!, reason).queue({ // Success
                transaction {
                    val logId = Config.select {
                        Config.guildId eq event.guild!!.idLong
                    }.firstOrNull()?.getOrNull(Config.log) ?: return@transaction
                    val log = event.guild!!.getTextChannelById(logId)
                    val embed = Embed {
                        title = "Banned a member"
                        color = Color.CYAN.hashCode()
                        author {
                            name = "Moderator: ${event.user.asTag}"
                            iconUrl = event.user.effectiveAvatarUrl
                        }
                        field {
                            name = "Banned:"
                            value = "${target.asMention} ${target.asTag}"
                            inline = false
                        }
                        field {
                            name = "Days of messages deleted:"
                            value = "$delDays"
                            inline = false
                        }
                        field {
                            name = "Reason:"
                            value = reason!!
                            inline = false
                        }
                        footer {
                            name = "User ID: ${target.id}"
                        }
                        timestamp = Instant.now()
                    }
                    event._reply(embed).setEphemeral(true)
                        .flatMap { log!!.sendMessageEmbeds(embed) }
                        .queue()
                }
            }, { // Failure
                error(event)
            })
        } catch (e: HierarchyException) {
            error(event)
        }
    }

    private fun error(event: SlashCommandInteractionEvent) {
        event._reply(
            Embed {
                title = "Failed to ban member"
                description = "The member could not be banned."
                color = Color.RED.hashCode()
                timestamp = Instant.now()
            }
        ).setEphemeral(true).queue()
    }

}