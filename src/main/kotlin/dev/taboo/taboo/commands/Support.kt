package dev.taboo.taboo.commands

import com.jagrosh.jdautilities.command.CommandEvent
import com.jagrosh.jdautilities.command.SlashCommand
import dev.minn.jda.ktx.Embed
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.commands.SlashCommandEvent
import java.time.Instant

class Support: SlashCommand() {

    init {
        name = "support"
        aliases = arrayOf("server")
        help = "Get support for Taboo."
        guildOnly = true
    }

    override fun execute(event: SlashCommandEvent) {
        val user = event.user
        val hook = event.hook
        event.deferReply(true).queue()
        hook.sendMessageEmbeds(supportEmbed(user)).mentionRepliedUser(false).queue()
    }

    override fun execute(event: CommandEvent) {
        val user = event.author
        val message = event.message
        message.replyEmbeds(supportEmbed(user)).mentionRepliedUser(false).queue()
    }

    private fun supportEmbed(user: User): MessageEmbed {
        return Embed {
            name = "Support"
            description =
                """
                If you need help with Taboo, please join the support server.
                https://discord.gg/XWQWX2X
                """.trimIndent() // TODO: Add support server link
            color = 0x9F90CF
            footer {
                name = "Requested by ${user.asTag}"
                iconUrl = user.effectiveAvatarUrl
            }
            timestamp = Instant.now()
        }
    }

}