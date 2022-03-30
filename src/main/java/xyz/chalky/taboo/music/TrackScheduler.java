package xyz.chalky.taboo.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedList;

public class TrackScheduler extends AudioEventAdapter {

    private final AudioPlayer player;
    public final LinkedList<AudioTrack> queue;
    private boolean looping;
    private SlashCommandInteractionEvent event;

    public TrackScheduler(AudioPlayer player, SlashCommandInteractionEvent event) {
        this.player = player;
        player.addListener(this);
        this.queue = new LinkedList<>();
        looping = false;
        this.event = event;
    }

    public void shuffle() {
        Collections.shuffle(queue);
    }

    public void nextTrack() {
        AudioTrack poll = queue.poll();
        System.out.println(poll.getInfo().title);
        player.playTrack(poll);
    }

    public AudioTrack remove(int pos) {
        if (pos > getQueueSize()) return null;
        return queue.remove(pos - 1);
    }

    public void queue(AudioTrack track) {
        if (!player.startTrack(track, true)) {
            queue.offer(track);
        }
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
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
        event.getHook().sendMessageEmbeds(embed).queue();
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        System.out.println("Track ended: " + track.getInfo().title);
        if (endReason.mayStartNext) {
            if (looping) {
                player.playTrack(track.makeClone());
            } else {
                nextTrack();
            }
        }
    }

    public void stop() {
        player.stopTrack();
        looping = false;
        queue.clear();
    }

    public void setLooping(boolean loop) {
        looping = loop;
    }

    public boolean getLooping() {
        return looping;
    }

    public int getQueueSize() {
        return queue.size();
    }

    public LinkedList<AudioTrack> getQueue() {
        return queue;
    }

}
