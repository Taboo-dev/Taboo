package dev.taboo.taboo.commands

import com.jagrosh.jdautilities.command.CommandEvent
import com.jagrosh.jdautilities.command.SlashCommand
import dev.minn.jda.ktx.Embed
import dev.taboo.taboo.database.DatabaseManager
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.commands.SlashCommandEvent
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant

class Help: SlashCommand() {

    init {
        name = "help"
        help = "Displays all commands."
        aliases = arrayOf("h", "?", "commands", "cmds")
        guildOnly = true
    }

    override fun execute(event: SlashCommandEvent) {
        val user = event.user
        val guild = event.guild!!
        val guildId = guild.id
        val hook = event.hook
        event.deferReply(true).queue()
        transaction {
            hook.sendMessageEmbeds(helpEmbed(user, DatabaseManager.PrefixManager.getPrefixFromGuild(guildId)))
                .mentionRepliedUser(false)
                .queue()
        }
    }

    override fun execute(event: CommandEvent) {
        val author = event.author
        val guild = event.guild
        val guildId = guild.id
        val message = event.message
        transaction {
            message.replyEmbeds(helpEmbed(author, DatabaseManager.PrefixManager.getPrefixFromGuild(guildId)))
                .mentionRepliedUser(false)
                .queue()
        }
    }

    private fun helpEmbed(user: User, prefix: String): MessageEmbed {
        return Embed {
            title = "Help"
            color = 0x9F90CF
            description = "All commands are listed below:"
            field {
                name = "${prefix}help"
                value = "Displays this message."
                inline = false
            }
            field {
                name = "${prefix}ping"
                value = "Pong!"
                inline = false
            }
            field {
                name = "${prefix}info"
                value = "Displays information about me."
                inline = false
            }
            field {
                name = "${prefix}stats"
                value = "Displays my statistics."
                inline = false
            }
            field {
                name = "${prefix}invite"
                value = "Displays my invite link."
                inline = false
            }
            field {
                name = "${prefix}support"
                value = "Displays a link to my support server."
                inline = false
            }
            field {
                name = "${prefix}suggest"
                value = "Suggest a feature for me."
                inline = false
            }
            field {
                name = "${prefix}report"
                value = "Report a bug or a feature."
                inline = false
            }
            footer {
                name = "Requested by ${user.asTag}"
                iconUrl = user.effectiveAvatarUrl
            }
            timestamp = Instant.now()
        }
    }

}