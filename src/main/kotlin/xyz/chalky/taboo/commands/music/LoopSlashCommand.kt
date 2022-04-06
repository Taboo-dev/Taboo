package xyz.chalky.taboo.commands.music

import dev.minn.jda.ktx.Embed
import dev.minn.jda.ktx.interactions.getOption
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import xyz.chalky.taboo.backend.CommandFlag
import xyz.chalky.taboo.backend.SlashCommand
import xyz.chalky.taboo.music.GuildAudioPlayer
import xyz.chalky.taboo.util._reply
import java.time.Instant

class LoopSlashCommand : SlashCommand() {

    init {
        setCommandData(
            Commands.slash("loop", "loop the current song")
                .addOptions(OptionData(OptionType.BOOLEAN, "loop", "Whether to loop the current song.", true))
        )
        addCommandFlags(CommandFlag.MUST_BE_IN_VC, CommandFlag.MUST_BE_IN_SAME_VC)
    }

    override fun executeCommand(event: SlashCommandInteractionEvent) {
        val guildAudioPlayer = GuildAudioPlayer(event)
        val scheduler = guildAudioPlayer.scheduler
        val loop = event.getOption<Boolean>("loop")!!
        if (loop) {
            scheduler.isRepeat = true
            val embed = Embed {
                title = "Looping"
                color = 0x9F90CF
                description = "Looping is now enabled"
                timestamp = Instant.now()
            }
            event._reply(embed).queue()
        } else {
            scheduler.isRepeat = false
            val embed = Embed {
                title = "No longer looping"
                color = 0x9F90CF
                description = "Looping is now disabled"
                timestamp = Instant.now()
            }
            event._reply(embed).queue()
        }
    }

}