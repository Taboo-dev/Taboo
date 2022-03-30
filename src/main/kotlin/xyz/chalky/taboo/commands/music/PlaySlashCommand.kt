package xyz.chalky.taboo.commands.music

import dev.minn.jda.ktx.interactions.getOption
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import xyz.chalky.taboo.backend.CommandFlag.*
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import xyz.chalky.taboo.backend.SlashCommand
import xyz.chalky.taboo.music.AudioHandler

class PlaySlashCommand : SlashCommand() {

    init {
        setCommandData(
            Commands.slash("play", "Plays a song.")
                .addOptions(OptionData(OptionType.STRING, "song", "The song to play.", true))
        )
        addCommandFlags(MUST_BE_IN_VC, MUST_BE_IN_SAME_VC)
    }

    override fun executeCommand(event: SlashCommandInteractionEvent) {
        val input = event.getOption<String>("song")!!
        val member = event.member!!
        val voiceState = member.voiceState
        val query = if (input.startsWith("http://") || input.startsWith("https://")) {
            input
        } else {
            "ytsearch:$input"
        }
        val audioHandler = AudioHandler(event)
        audioHandler.connect(voiceState!!.channel)
        audioHandler.loadItem(query, event)
    }

}