/*
package xyz.chalky.taboo.commands.moderation

import dev.minn.jda.ktx.interactions.components.getOption
import dev.minn.jda.ktx.messages.Embed
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.exceptions.HierarchyException
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import xyz.chalky.taboo.core.SlashCommand
import xyz.chalky.taboo.database.Config
import xyz.chalky.taboo.util._reply
import java.awt.Color
import java.time.Instant

class KickSlashCommand : SlashCommand() {

    init {
        setCommandData(Commands.slash("kick", "Kick a user from the server.")
            .addOption(OptionType.USER, "user", "The user to kick.", true)
            .addOption(OptionType.STRING, "reason", "The reason for kicking the user.", false))
        isEphemeral = true
    }

    override fun executeCommand(event: SlashCommandInteractionEvent) {
        val target = event.getOption<Member>("user")
        val reason = if (event.getOption<String>("reason") == null) "No reason provided." else event.getOption<String>("reason")
        try {
            target!!.kick(reason).queue ({
                transaction {
                    val logId = Config.select {
                        Config.guildId eq event.guild!!.idLong
                    }.firstOrNull()?.getOrNull(Config.log) ?: return@transaction
                    val log = event.guild!!.getTextChannelById(logId)
                    val embed = Embed {
                        title = "Kicked a member"
                        author {
                            name = "Moderator: ${event.user.asTag}"
                            iconUrl = event.user.effectiveAvatarUrl
                        }
                        field {
                            name = "Kicked:"
                            value = "${target.asMention} ${target.user.asTag}"
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
                    event._reply(embed).setEphemeral(true).queue {
                        log!!.sendMessageEmbeds(embed).queue()
                    }
                }
            }, {
                error(event)
            })
        } catch (e: HierarchyException) {
            error(event)
        }
    }

    private fun error(event: SlashCommandInteractionEvent) {
        event._reply(
            Embed {
                title = "Failed to kick member"
                description = "The member could not be kicked."
                color = Color.RED.hashCode()
                timestamp = Instant.now()
            }
        ).setEphemeral(true).queue()
    }

}*/
