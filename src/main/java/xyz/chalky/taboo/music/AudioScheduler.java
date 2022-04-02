package xyz.chalky.taboo.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import lavalink.client.player.IPlayer;
import lavalink.client.player.LavalinkPlayer;
import lavalink.client.player.event.PlayerEventListenerAdapter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;
import java.time.Instant;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

public class AudioScheduler extends PlayerEventListenerAdapter {

    private final SlashCommandInteractionEvent event;
    private final LavalinkPlayer player;
    private final LinkedList<AudioTrack> queue;
    private final GuildAudioPlayer guildAudioPlayer;
    private final long guildId;
    private boolean repeat = false;
    private boolean shuffle = false;
    private AudioTrack lastTrack;

    public AudioScheduler(SlashCommandInteractionEvent event, LavalinkPlayer player, GuildAudioPlayer guildAudioPlayer, long guildId) {
        this.guildId = guildId;
        this.player = player;
        this.guildAudioPlayer = guildAudioPlayer;
        this.queue = new LinkedList<>();
        this.event = event;
    }

    public void queue(AudioTrack track) {
        if (player.getPlayingTrack() != null) {
            queue.offer(track);
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

    public LinkedList<AudioTrack> getQueue() {
        return queue;
    }

    public LavalinkPlayer getPlayer() {
        return player;
    }

    @Override
    public void onTrackStart(IPlayer player, AudioTrack track) {
        long length = track.getInfo().length;
        long minutes = length / 60000;
        long seconds = length % 60;
        MessageEmbed embed = new EmbedBuilder()
                .setTitle("Now Playing:")
                .setDescription(track.getInfo().title)
                .addField("Duration:", String.format("%02d:%02d", minutes, seconds), false)
                .setImage(String.format("https://img.youtube.com/vi/%s/mqdefault.jpg", track.getInfo().identifier))
                .setColor(0x9F90CF)
                .setTimestamp(Instant.now())
                .build();
        event.getHook().sendMessageEmbeds(embed).queue(message -> {
            message.delete().queueAfter(length, TimeUnit.MILLISECONDS);
        });
    }

    @Override
    public void onTrackEnd(IPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (endReason.mayStartNext) {
            if (repeat) {
                player.playTrack(lastTrack.makeClone());
            } else {
                MessageEmbed embed = new EmbedBuilder()
                        .setTitle("Track Ended:")
                        .setDescription(track.getInfo().title)
                        .setColor(0x9F90CF)
                        .setTimestamp(Instant.now())
                        .build();
                event.getHook().sendMessageEmbeds(embed).queue();
                nextTrack();
            }
        }
    }

    @Override
    public void onTrackException(IPlayer player, AudioTrack track, Exception exception) {
        MessageEmbed embed = new EmbedBuilder()
                .setTitle("An error occurred while playing the track:")
                .setDescription(track.getInfo().title)
                .setColor(Color.RED)
                .setTimestamp(Instant.now())
                .build();
        event.getHook().sendMessageEmbeds(embed).queue();
    }

    public long getGuildId() {
        return guildId;
    }

    public void destroy() {
        queue.clear();
        lastTrack = null;
    }

}
