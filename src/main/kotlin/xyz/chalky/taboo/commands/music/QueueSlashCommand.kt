package xyz.chalky.taboo.commands.music

import dev.minn.jda.ktx.Embed
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import xyz.chalky.taboo.backend.CommandFlag.MUST_BE_IN_SAME_VC
import xyz.chalky.taboo.backend.CommandFlag.MUST_BE_IN_VC
import xyz.chalky.taboo.backend.SlashCommand
import xyz.chalky.taboo.music.AudioHandler
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
                    ),
                    SubcommandData(
                        "add", "Add a song to the queue."
                    ).addOptions(OptionData(
                        OptionType.STRING, "song", "The song to add to the queue.", true
                    ))
                )
        )
        addCommandFlags(MUST_BE_IN_VC, MUST_BE_IN_SAME_VC)
    }

    override fun executeCommand(event: SlashCommandInteractionEvent) {
        event.onSubCommand("list") {
            val trackScheduler = AudioHandler(event).trackScheduler
            val queue = trackScheduler.getQueue()
            val desc = StringBuilder("Tracks:\n")
            val trackList = queue.size
            val trackCount = trackList.coerceAtMost(10)
            for (i in 0 until trackCount) {
                val track = queue[i]
                val info = track.info
                desc.append("`#")
                    .append(i + 1)
                    .append("` ")
                    .append(info.title)
                    .append(" [")
                    .append(info.author)
                    .append("]\n")
            }
            if (trackList > trackCount) {
                desc.append("And ")
                    .append("`")
                    .append(trackList - trackCount)
                    .append("`")
                    .append(" more tracks...")
            }
            if (trackList == 0) {
                desc.append("No tracks in queue.")
            }
            val embed = Embed {
                title = "Queue"
                description = desc.toString()
                color = 0x9F90CF
                timestamp = Instant.now()
            }
            event._reply(embed).queue()
        }
        event.onSubCommand("clear") {
            val trackScheduler = AudioHandler(event).trackScheduler
            trackScheduler.queue.clear()
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