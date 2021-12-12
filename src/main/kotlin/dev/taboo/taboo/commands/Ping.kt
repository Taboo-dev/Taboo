package dev.taboo.taboo.commands

import com.jagrosh.jdautilities.command.CommandEvent
import com.jagrosh.jdautilities.command.SlashCommand
import dev.minn.jda.ktx.Embed
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.commands.SlashCommandEvent
import java.time.Instant

class Ping: SlashCommand() {

    init {
        name = "ping"
        help = "Pings the bot to see if it's alive"
        defaultEnabled = true
        guildOnly = true
    }

    override fun execute(event: SlashCommandEvent) {
        val user = event.user
        val jda = event.jda
        val hook = event.hook
        event.deferReply(true).queue()
        hook.sendMessageEmbeds(initialPingEmbed(user)).mentionRepliedUser(false).queue { msg ->
            jda.restPing.queue { restPing ->
                val gatewayPing = jda.gatewayPing
                msg.editMessageEmbeds(finalPingEmbed(user, restPing, gatewayPing)).queue()
            }
        }
    }

    override fun execute(event: CommandEvent) {
        val author = event.author
        val jda = event.jda
        val message = event.message
        message.replyEmbeds(initialPingEmbed(author)).mentionRepliedUser(false).queue { msg ->
            jda.restPing.queue { restPing ->
                val gatewayPing = jda.gatewayPing
                msg.editMessageEmbeds(finalPingEmbed(author, restPing, gatewayPing)).queue()
            }
        }
    }

    private fun initialPingEmbed(user: User): MessageEmbed {
        return Embed {
            title = "Ping"
            description = "Pong! Shows the bot's ping!"
            color = 0x9F90CF
            footer {
                name = "Requested by ${user.asTag}"
                iconUrl = user.effectiveAvatarUrl
            }
            timestamp = Instant.now()
        }
    }

    private fun finalPingEmbed(user: User, restPing: Long, gatewayPing: Long): MessageEmbed {
        return Embed {
            title = "Ping"
            description =
                """
                    Pong! Showing the bot's ping!
                    Gateway Ping: $gatewayPing ms
                    Rest Ping: $restPing ms
                """.trimIndent()
            color = 0x9F90CF
            footer {
                name = "Requested by ${user.asTag}"
                iconUrl = user.effectiveAvatarUrl
            }
            timestamp = Instant.now()
        }
    }

}