package xyz.chalky.taboo.commands.music

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import dev.minn.jda.ktx.Embed
import dev.minn.jda.ktx.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import xyz.chalky.taboo.Taboo
import xyz.chalky.taboo.backend.CommandFlag
import xyz.chalky.taboo.backend.SlashCommand
import xyz.chalky.taboo.util._reply
import xyz.chalky.taboo.util.onSubCommand
import java.time.Instant

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
        addCommandFlags(CommandFlag.MUSIC)
    }

    override fun executeCommand(event: SlashCommandInteractionEvent) {
        val guildAudioPlayer = Taboo.getInstance().audioManager.getAudioPlayer(event.guild!!.idLong)
        val queue = guildAudioPlayer.scheduler.queue
        event.onSubCommand("list") {
            val description = StringBuilder("Queue:\n")
            val trackSize = queue.size
            val trackCount = trackSize.coerceAtMost(10)
            val trackList: List<AudioTrack> = queue.toList()
            for (i in 0 until trackCount) {
                val track = trackList[i]
                val info = track.info
                description.append("`#")
                    .append(i + 1)
                    .append("` ")
                    .append(info.title)
                    .append(" [")
                    .append(info.author)
                    .append("]\n")
            }
            if (trackSize > trackCount) {
                description.append("And ")
                    .append("`")
                    .append(trackSize - trackCount)
                    .append("`")
                    .append(" more tracks...")
            }
            val embed = EmbedBuilder {
                title = "Queue"
                color = 0x9F90CF
                timestamp = Instant.now()
            }
            if (trackSize == 0) {
                embed.description = "The queue is empty."
            } else {
                embed.description = description.toString()
            }
            event._reply(embed.build()).queue()
        }
        event.onSubCommand("clear") {
            queue.clear()
            event._reply(
                Embed {
                    title = "Queue"
                    description = "Queue cleared."
                    color = 0x9F90CF
                    timestamp = Instant.now()
                }
            ).queue()
        }
    }

}