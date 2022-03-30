package xyz.chalky.taboo.music;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.AudioChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.managers.AudioManager;

import java.time.Instant;

public class AudioHandler {

    private final AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
    private final AudioPlayerSendHandler audioPlayerSendHandler;
    private final TrackScheduler trackScheduler;
    private AudioManager audioManager;
    private final AudioPlayer player;

    public AudioHandler(SlashCommandInteractionEvent event) {
        AudioSourceManagers.registerRemoteSources(playerManager);
        player = playerManager.createPlayer();
        trackScheduler = new TrackScheduler(player, event);
        audioPlayerSendHandler = new AudioPlayerSendHandler(player);
    }

    public TrackScheduler getTrackScheduler() {
        return trackScheduler;
    }

    // Remove when switch to Lavalink
    public void connect(AudioChannel audioChannel) {
        if (audioManager == null || !audioManager.isConnected()) {
            audioManager = audioChannel.getGuild().getAudioManager();
            audioManager.openAudioConnection(audioChannel);
            audioManager.setSelfDeafened(true);
            audioManager.setSendingHandler(audioPlayerSendHandler);
        }
    }

    public void stopAndDisconnect() {
        trackScheduler.stop();
        audioManager.closeAudioConnection();
    }

    public void loadItem(String identifier, SlashCommandInteractionEvent event) {
        InteractionHook hook = event.getHook();
        playerManager.loadItem(identifier, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                trackScheduler.queue(track);
                handle(track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                if (playlist.isSearchResult()) {
                    AudioTrack track = playlist.getTracks().get(0);
                    trackScheduler.queue(track);
                    handle(track);
                } else {
                    playlist.getTracks().forEach(trackScheduler::queue);
                    handlePlaylist(playlist);
                }
            }

            @Override
            public void noMatches() {

            }

            @Override
            public void loadFailed(FriendlyException exception) {

            }

            public void handle(AudioTrack track) {
                MessageEmbed embed = new EmbedBuilder()
                        .setTitle("Added to queue:")
                        .setDescription(track.getInfo().title)
                        .setColor(0x9F90CF)
                        .setTimestamp(Instant.now())
                        .build();
                hook.sendMessageEmbeds(embed).queue();
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
                hook.sendMessageEmbeds(embed).queue();
            }

        });
    }

}
