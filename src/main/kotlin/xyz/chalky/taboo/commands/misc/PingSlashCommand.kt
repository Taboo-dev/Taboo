package xyz.chalky.taboo.commands.misc

import dev.minn.jda.ktx.EmbedBuilder
import dev.minn.jda.ktx.InlineEmbed
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.interactions.commands.build.Commands
import xyz.chalky.taboo.backend.SlashCommand
import java.awt.SystemColor.text
import java.time.Instant

class PingSlashCommand : SlashCommand() {
    init {
        setCommandData(Commands.slash("ping", "Pong!"))
    }

    override fun executeCommand(event: SlashCommandInteractionEvent) {
        val jda = event.jda
        val user = event.user
        val hook = event.hook
        val pingEmbed = EmbedBuilder {
            title = "Ping!"
            description = "Pong! Shows the bot's ping!"
            color = 0x9F90CF
            footer {
                name = "Requested by ${user.asTag}"
                iconUrl = user.effectiveAvatarUrl
            }
            timestamp = Instant.now()
        }
        hook.sendMessageEmbeds(pingEmbed.build()).queue { msg: Message ->
            jda.restPing.queue { restPing: Long? ->
                val gatewayPing = jda.gatewayPing
                pingEmbed.description = """
                    Pong!
                    Rest Ping: $restPing ms
                    Gateway Ping: $gatewayPing ms
                """.trimIndent()
                msg.editMessageEmbeds(pingEmbed.build()).queue()
            }
        }
    }
}