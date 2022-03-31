package xyz.chalky.taboo.commands.misc

import dev.minn.jda.ktx.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.Commands
import xyz.chalky.taboo.backend.SlashCommand
import xyz.chalky.taboo.util._edit
import xyz.chalky.taboo.util._reply
import java.time.Instant

class PingSlashCommand : SlashCommand() {

    init {
        setCommandData(Commands.slash("ping", "Pong!"))
    }

    override fun executeCommand(event: SlashCommandInteractionEvent) {
        val jda = event.jda
        val user = event.user
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
        event._reply(pingEmbed.build()).queue { msg ->
            jda.restPing.queue { restPing ->
                val gatewayPing = jda.gatewayPing
                pingEmbed.description = """
                    Pong!
                    Rest Ping: $restPing ms
                    Gateway Ping: $gatewayPing ms
                """.trimIndent()
                msg._edit(pingEmbed.build()).queue()
            }
        }
    }

}