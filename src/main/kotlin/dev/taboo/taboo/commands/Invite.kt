package dev.taboo.taboo.commands

import com.jagrosh.jdautilities.command.CommandEvent
import com.jagrosh.jdautilities.command.SlashCommand
import dev.minn.jda.ktx.Embed
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.commands.SlashCommandEvent
import java.time.Instant

class Invite: SlashCommand() {

    init {
        name = "invite"
        aliases = arrayOf("inv")
        help = "Invite Taboo to your server"
        guildOnly = true
    }

    override fun execute(event: SlashCommandEvent) {
        val user = event.user
        val hook = event.hook
        event.deferReply(true).queue()
        hook.sendMessageEmbeds(inviteEmbed(user)).mentionRepliedUser(false).queue()
    }

    override fun execute(event: CommandEvent) {
        val author = event.author
        val message = event.message
        message.replyEmbeds(inviteEmbed(author)).mentionRepliedUser(false).queue()
    }

    private fun inviteEmbed(user: User): MessageEmbed {
        val link = "https://discord.com/api/oauth2/authorize?client_id=892077333878566962&permissions=8&scope=bot%20applications.commands"
        return Embed {
            title = "Invite me to your server!"
            description = "[Click here to invite me to your server!]($link)"
            color = 0x9F90CF
            footer {
                name = "Requested by ${user.asTag}"
                iconUrl = user.effectiveAvatarUrl
            }
            timestamp = Instant.now()
        }
    }
}