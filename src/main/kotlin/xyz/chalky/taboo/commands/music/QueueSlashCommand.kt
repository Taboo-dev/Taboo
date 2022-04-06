package xyz.chalky.taboo.commands.music

import dev.minn.jda.ktx.Embed
import lavalink.client.player.track.AudioTrack
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import xyz.chalky.taboo.backend.CommandFlag
import xyz.chalky.taboo.backend.SlashCommand
import xyz.chalky.taboo.music.GuildAudioPlayer
import xyz.chalky.taboo.util._reply
import xyz.chalky.taboo.util.onSubCommand
import java.time.Instant
import java.util.*

class QueueSlashCommand : SlashCommand() {

    init {
        setCommandData(
            Commands.slash("queue", "Queue a song")
                .addSubcommands(
                    SubcommandData(
                        "list", "List the current queue."
                    ),
                    SubcommandData(
                        "clear", "Clear the current queue."
                    )
                )
        )
        addCommandFlags(CommandFlag.MUST_BE_IN_VC, CommandFlag.MUST_BE_IN_SAME_VC)
    }

    override fun executeCommand(event: SlashCommandInteractionEvent) {
        val guildAudioPlayer = GuildAudioPlayer(event)
        val queue: Queue<AudioTrack> = guildAudioPlayer.scheduler.queue
        event.onSubCommand("list") {
            val builder = StringBuilder()
            queue.forEach {
                builder.append("${it.info.title} - ${it.info.author} - ${it.info.length} - ${it.info.uri}\n")
            }
            // Still broken
            val embed = Embed {
                title = "Queue"
                description = "Queue Size: ${queue.size}"
                color = 0x9F90CF
                timestamp = Instant.now()
            }
            event._reply(embed).queue()
        }
        event.onSubCommand("clear") {
            queue.clear()
            val embed = Embed {
                title = "Queue"
                description = "Queue cleared."
                color = 0x9F90CF
                timestamp = Instant.now()
            }
            event._reply(embed).queue()
        }
    }

}