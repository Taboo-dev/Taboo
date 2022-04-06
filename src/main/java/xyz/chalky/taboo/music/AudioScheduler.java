package xyz.chalky.taboo.music;

import lavalink.client.player.IPlayer;
import lavalink.client.player.LavalinkPlayer;
import lavalink.client.player.event.PlayerEventListenerAdapter;
import lavalink.client.player.track.AudioTrack;
import lavalink.client.player.track.AudioTrackEndReason;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;
import java.time.Instant;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class AudioScheduler extends PlayerEventListenerAdapter {

    private final SlashCommandInteractionEvent event;
    private final LavalinkPlayer player;
    private final BlockingQueue<AudioTrack> queue;
    private final GuildAudioPlayer guildAudioPlayer;
    private final long guildId;
    private boolean repeat = false;
    private boolean shuffle = false;
    private AudioTrack lastTrack;

    public AudioScheduler(SlashCommandInteractionEvent event, LavalinkPlayer player, GuildAudioPlayer guildAudioPlayer, long guildId) {
        this.guildId = guildId;
        this.player = player;
        this.guildAudioPlayer = guildAudioPlayer;
        this.queue = new LinkedBlockingQueue<>();
        this.event = event;
    }

    public void queue(AudioTrack track) {
        if (player.getPlayingTrack() != null) {
            queue.add(track);
        } else {
            player.playTrack(track);
        }
    }

    public void nextTrack() {
        AudioTrack track = queue.poll();
        if (track != null) {
            player.playTrack(track);
        } else {
            player.stopTrack();
        }
    }

    public boolean isRepeat() {
        return repeat;
    }

    public void setRepeat(boolean repeat) {
        this.repeat = repeat;
    }

    public boolean isShuffle() {
        return shuffle;
    }

    public void setShuffle(boolean shuffle) {
        this.shuffle = shuffle;
    }

    public BlockingQueue<AudioTrack> getQueue() {
        return queue;
    }

    public LavalinkPlayer getPlayer() {
        return player;
    }

    @Override
    public void onTrackStart(IPlayer player, AudioTrack track) {
        System.out.println("start fired");
        long length = track.getInfo().getLength();
        long minutes = length / 60000;
        long seconds = length % 60;
        if (!repeat) {
            MessageEmbed embed = new EmbedBuilder()
                    .setTitle("Now Playing:")
                    .setDescription(String.format("[%s](%s) by %s", track.getInfo().getTitle(),
                            track.getInfo().getUri(), track.getInfo().getAuthor()))
                    .addField("Duration:", String.format("%02d:%02d", minutes, seconds), false)
                    .setColor(0x9F90CF)
                    .setTimestamp(Instant.now())
                    .build();
            event.getChannel().sendMessageEmbeds(embed).queue(message -> {
                message.delete().queueAfter(length, TimeUnit.MILLISECONDS);
            });
        }
    }

    @Override
    public void onTrackEnd(IPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        System.out.println("end fired");
        if (endReason.mayStartNext) {
            if (repeat) {
                player.playTrack(track);
                MessageEmbed embed = new EmbedBuilder()
                        .setTitle("Looping:")
                        .setDescription(String.format("[%s](%s) by %s", track.getInfo().getTitle(),
                                track.getInfo().getUri(), track.getInfo().getUri()))
                        .setColor(0x9F90CF)
                        .setTimestamp(Instant.now())
                        .build();
                event.getChannel().sendMessageEmbeds(embed).queue();
            } else {
                MessageEmbed embed = new EmbedBuilder()
                        .setTitle("Track Ended:")
                        .setDescription(String.format("[%s](%s) by %s", track.getInfo().getTitle(),
                                track.getInfo().getUri(), track.getInfo().getAuthor()))
                        .setColor(0x9F90CF)
                        .setTimestamp(Instant.now())
                        .build();
                event.getChannel().sendMessageEmbeds(embed).queue();
                nextTrack();
            }
        }
    }

    @Override
    public void onTrackException(IPlayer player, AudioTrack track, Exception exception) {
        MessageEmbed embed = new EmbedBuilder()
                .setTitle("An error occurred while playing the track:")
                .setDescription(track.getInfo().getTitle())
                .setColor(Color.RED)
                .setTimestamp(Instant.now())
                .build();
        event.getChannel().sendMessageEmbeds(embed).queue();
    }

    public long getGuildId() {
        return guildId;
    }

    public void destroy() {
        queue.clear();
        lastTrack = null;
    }

}
