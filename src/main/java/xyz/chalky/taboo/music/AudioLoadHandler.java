package xyz.chalky.taboo.music;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import lavalink.client.player.LavalinkPlayer;
import mu.KotlinLogging;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.slf4j.Logger;

import java.awt.*;
import java.time.Instant;

public class AudioLoadHandler implements AudioLoadResultHandler {

    private final Logger LOGGER = KotlinLogging.INSTANCE.logger("AudioLoadHandler");
    private final AudioScheduler audioScheduler;
    private final SlashCommandInteractionEvent event;

    public AudioLoadHandler(SlashCommandInteractionEvent event, LavalinkPlayer player, GuildAudioPlayer guildAudioPlayer) {
        long guildId = event.getIdLong();
        this.event = event;
        this.audioScheduler = new AudioScheduler(event, player, guildAudioPlayer, guildId);
    }

    @Override
    public void trackLoaded(AudioTrack track) {
        audioScheduler.queue(track);
        handle(track);
        LOGGER.debug("Track loaded: {}", track.getInfo().title);
    }

    @Override
    public void playlistLoaded(AudioPlaylist playlist) {
        if (playlist.isSearchResult()) {
            AudioTrack track = playlist.getTracks().get(0);
            audioScheduler.queue(track);
            handle(track);
            LOGGER.debug("Track loaded: {}", track.getInfo().title);
        } else {
            playlist.getTracks().forEach(audioScheduler::queue);
            handlePlaylist(playlist);
            LOGGER.debug("Playlist loaded: {}", playlist.getName());
        }
    }

    @Override
    public void noMatches() {
        MessageEmbed embed = new EmbedBuilder()
                .setTitle("No matches found")
                .setColor(Color.RED)
                .setTimestamp(Instant.now())
                .build();
        event.getHook().sendMessageEmbeds(embed).queue();
        LOGGER.debug("No matches found");
    }

    @Override
    public void loadFailed(FriendlyException exception) {
        MessageEmbed embed = new EmbedBuilder()
                .setTitle("An error occurred while loading the track")
                .setColor(Color.RED)
                .setTimestamp(Instant.now())
                .build();
        event.getHook().sendMessageEmbeds(embed).queue();
        LOGGER.debug("An error occurred while loading the track: {}", exception.getMessage());
    }

    public void handle(AudioTrack track) {
        MessageEmbed embed = new EmbedBuilder()
                .setTitle("Added to queue:")
                .setDescription(track.getInfo().title)
                .setColor(0x9F90CF)
                .setTimestamp(Instant.now())
                .build();
        event.getHook().sendMessageEmbeds(embed).queue();
    }

    public void handlePlaylist(AudioPlaylist playlist) {
        StringBuilder description = new StringBuilder("Tracks:\n");
        int trackList = playlist.getTracks().size();
        int trackCount = Math.min(trackList, 10);
        for (int i = 0; i < trackCount; i++) {
            AudioTrack track = playlist.getTracks().get(i);
            AudioTrackInfo info = track.getInfo();
            description.append("`#")
                    .append(i + 1)
                    .append("` ")
                    .append(info.title)
                    .append(" [")
                    .append(info.author)
                    .append("]\n");
        }
        if (trackList > trackCount) {
            description.append("And ")
                    .append("`")
                    .append(trackList - trackCount)
                    .append("`")
                    .append(" more tracks...");
        }
        MessageEmbed embed = new EmbedBuilder()
                .setTitle("Added to queue:")
                .setDescription(description)
                .setColor(0x9F90CF)
                .setTimestamp(Instant.now())
                .build();
        event.getHook().sendMessageEmbeds(embed).queue();
    }

}
