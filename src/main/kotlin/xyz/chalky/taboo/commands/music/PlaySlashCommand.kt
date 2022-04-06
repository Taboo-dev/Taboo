package xyz.chalky.taboo.commands.music

import dev.minn.jda.ktx.interactions.getOption
import net.dv8tion.jda.api.entities.VoiceChannel
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.exceptions.PermissionException
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import xyz.chalky.taboo.backend.CommandFlag.MUST_BE_IN_SAME_VC
import xyz.chalky.taboo.backend.CommandFlag.MUST_BE_IN_VC
import xyz.chalky.taboo.backend.SlashCommand
import xyz.chalky.taboo.music.AudioLoadHandler
import xyz.chalky.taboo.music.GuildAudioPlayer
import xyz.chalky.taboo.util._reply
import xyz.chalky.taboo.util.isUrl

class PlaySlashCommand() : SlashCommand() {

    init {
        setCommandData(
            Commands.slash("play", "Plays a song.")
                .addOptions(OptionData(OptionType.STRING, "song", "The song to play.", true))
        )
        addCommandFlags(MUST_BE_IN_VC, MUST_BE_IN_SAME_VC)
    }

    override fun executeCommand(event: SlashCommandInteractionEvent) {
        val guild = event.guild
        val guildAudioPlayer = GuildAudioPlayer(event)
        val link = guildAudioPlayer.link
        val player = guildAudioPlayer.player
        val input = event.getOption<String>("song")!!
        val member = event.member!!
        val voiceState = member.voiceState
        val manager = guild!!.audioManager
        val query = if (isUrl(input)) {
            input
        } else {
            "ytsearch:$input"
        }
        if (manager.connectedChannel == null) {
            try {
                link.connect(voiceState?.channel as VoiceChannel)
                link.restClient.loadItem(query, AudioLoadHandler(event, player, guildAudioPlayer))
            } catch (e: PermissionException) {
                event._reply("I don't have permission to connect to your voice channel.").queue()
                return
            }
        }
    }

}