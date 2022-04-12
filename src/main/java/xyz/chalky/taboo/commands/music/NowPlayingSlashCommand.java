package xyz.chalky.taboo.commands.music;

import lavalink.client.player.LavalinkPlayer;
import lavalink.client.player.track.AudioTrack;
import lavalink.client.player.track.AudioTrackInfo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import xyz.chalky.taboo.Taboo;
import xyz.chalky.taboo.backend.SlashCommand;
import xyz.chalky.taboo.music.GuildAudioPlayer;

import java.time.Instant;

public class NowPlayingSlashCommand extends SlashCommand {

    public NowPlayingSlashCommand() {
        setCommandData(Commands.slash("now-playing", "Queries the current song playing."));
    }

    @Override
    public void executeCommand(SlashCommandInteractionEvent event) {
        GuildAudioPlayer guildAudioPlayer = Taboo.getInstance().getAudioManager().getAudioPlayer(event.getGuild().getIdLong());
        LavalinkPlayer player = guildAudioPlayer.getScheduler().getPlayer();
        AudioTrack playingTrack = player.getPlayingTrack();
        if (playingTrack == null) {
            MessageEmbed embed = new EmbedBuilder()
                    .setTitle("No song is currently playing.")
                    .setColor(0x9F90CF)
                    .setTimestamp(Instant.now())
                    .build();
            event.getHook().sendMessageEmbeds(embed).queue();
        } else {
            AudioTrackInfo info = playingTrack.getInfo();
            MessageEmbed embed = new EmbedBuilder()
                    .setTitle("Now Playing:")
                    .setDescription(String.format("[%s](%s) by %s", info.getTitle(), info.getUri(), info.getAuthor()))
                    .setColor(0x9F90CF)
                    .setTimestamp(Instant.now())
                    .build();
            event.getHook().sendMessageEmbeds(embed).queue();
        }
    }

}
