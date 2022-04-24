package xyz.chalky.taboo.music;

import com.github.topislavalinkplugins.topissourcemanagers.ISRCAudioTrack;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import mu.KotlinLogging;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.awt.*;
import java.time.Instant;

public class AudioResultHandler implements AudioLoadResultHandler {

    private final Logger LOGGER = KotlinLogging.INSTANCE.logger("AudioLoadResultHandler");
    private final SlashCommandInteractionEvent event;
    private final AudioScheduler scheduler;

    public AudioResultHandler(SlashCommandInteractionEvent event, AudioScheduler scheduler) {
        this.event = event;
        this.scheduler = scheduler;
    }

    @Override
    public void trackLoaded(AudioTrack track) {
        scheduler.queue(track);
        handle(track);
        LOGGER.debug("Track loaded: {}", track.getInfo().title);
    }

    @Override
    public void playlistLoaded(@NotNull AudioPlaylist playlist) {
        if (playlist.isSearchResult()) {
            AudioTrack track = playlist.getTracks().get(0);
            scheduler.queue(track);
            handle(track);
            LOGGER.debug("Track loaded: {}", track.getInfo().title);
        } else {
            playlist.getTracks().forEach(scheduler::queue);
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
    public void loadFailed(@NotNull FriendlyException exception) {
        MessageEmbed embed = new EmbedBuilder()
                .setTitle("An error occurred while loading the track")
                .setColor(Color.RED)
                .setTimestamp(Instant.now())
                .build();
        event.getHook().sendMessageEmbeds(embed).queue();
        LOGGER.debug("An error occurred while loading the track: {}", exception.getMessage());
    }

    private void handle(@NotNull AudioTrack track) {
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Added to queue:")
                .setDescription(String.format("[%s](%s) by %s", track.getInfo().title,
                        track.getInfo().uri, track.getInfo().author))
                .setColor(0x9F90CF)
                .setTimestamp(Instant.now());
        if (track instanceof YoutubeAudioTrack) {
            embed.setThumbnail(String.format("https://img.youtube.com/vi/%s/mqdefault.jpg", track.getInfo().identifier));
        } else if (track instanceof ISRCAudioTrack isrcAudioTrack) {
            embed.setThumbnail(isrcAudioTrack.getArtworkURL());
        }
        event.getHook().sendMessageEmbeds(embed.build()).queue();
    }

    private void handlePlaylist(@NotNull AudioPlaylist playlist) {
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
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Added to queue:")
                .setDescription(description)
                .setColor(0x9F90CF)
                .setTimestamp(Instant.now());
        if (trackList == 0) {
            embed.setDescription("The queue is empty.");
        } else {
            embed.setDescription(description);
        }
        event.getHook().sendMessageEmbeds(embed.build()).queue();
    }

}
