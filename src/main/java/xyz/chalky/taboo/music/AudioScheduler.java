package xyz.chalky.taboo.music;

import kotlin.Pair;
import lavalink.client.io.jda.JdaLink;
import lavalink.client.player.IPlayer;
import lavalink.client.player.LavalinkPlayer;
import lavalink.client.player.event.PlayerEventListenerAdapter;
import lavalink.client.player.track.AudioTrack;
import lavalink.client.player.track.AudioTrackEndReason;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import xyz.chalky.taboo.Taboo;
import xyz.chalky.taboo.util.ExtensionsKt;

import java.awt.*;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class AudioScheduler extends PlayerEventListenerAdapter {

    private final LavalinkPlayer player;
    private final JdaLink link;
    private final BlockingQueue<AudioTrack> queue;
    private final GuildAudioPlayer guildAudioPlayer;
    private final long guildId;
    private long channelId;
    private boolean repeat = false;

    public AudioScheduler(LavalinkPlayer player, GuildAudioPlayer guildAudioPlayer, long guildId) {
        this.guildId = guildId;
        this.guildAudioPlayer = guildAudioPlayer;
        this.queue = new LinkedBlockingQueue<>();
        this.link = Taboo.getInstance().getLavalink().getLink(guildId);
        this.player = link.getPlayer();
        player.addListener(this);
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

    public void shuffle() {
        AudioTrack[] tracks = queue.toArray(new AudioTrack[0]);
        for (int i = tracks.length - 1; i > 0; i--) {
            int index = (int) (Math.random() * (i + 1));
            AudioTrack tmp = tracks[index];
            tracks[index] = tracks[i];
            tracks[i] = tmp;
        }
        queue.clear();
        queue.addAll(List.of(tracks));
    }

    public boolean isRepeat() {
        return repeat;
    }

    public void setRepeat(boolean repeat) {
        this.repeat = repeat;
    }

    public BlockingQueue<AudioTrack> getQueue() {
        return queue;
    }

    public LavalinkPlayer getPlayer() {
        return player;
    }

    // Cursed - someone please fix this
    public void setChannelId(long channelId) {
        this.channelId = channelId;
    }

    public long getChannelId() {
        return channelId;
    }

    public JdaLink getLink() {
        return link;
    }

    @Override
    public void onTrackStart(IPlayer player, AudioTrack track) {
        TextChannel channel = Taboo.getInstance().getShardManager().getTextChannelById(channelId);
        long length = track.getInfo().getLength();
        Pair<Long, Long> parseLength = ExtensionsKt.parseLength(length);
        if (!repeat) {
            MessageEmbed embed = new EmbedBuilder()
                    .setTitle("Now Playing:")
                    .setDescription(String.format("[%s](%s) by %s", track.getInfo().getTitle(),
                            track.getInfo().getUri(), track.getInfo().getAuthor()))
                    .addField("Duration:", String.format("%02d:%02d", parseLength.getFirst(), parseLength.getSecond()), false)
                    .setColor(0x9F90CF)
                    .setTimestamp(Instant.now())
                    .build();
            channel.sendMessageEmbeds(embed).queue(message -> {
                message.delete().queueAfter(length, TimeUnit.MILLISECONDS);
            });
        }
    }

    @Override
    public void onTrackEnd(IPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        TextChannel channel = Taboo.getInstance().getShardManager().getTextChannelById(channelId);
        if (endReason.mayStartNext) {
            if (repeat) {
                player.playTrack(track);
                MessageEmbed embed = new EmbedBuilder()
                        .setTitle("Looping:")
                        .setDescription(String.format("[%s](%s) by %s", track.getInfo().getTitle(),
                                track.getInfo().getUri(), track.getInfo().getAuthor()))
                        .setColor(0x9F90CF)
                        .setTimestamp(Instant.now())
                        .build();
                channel.sendMessageEmbeds(embed).queue();
            } else {
                MessageEmbed embed = new EmbedBuilder()
                        .setTitle("Track Ended:")
                        .setDescription(String.format("[%s](%s) by %s", track.getInfo().getTitle(),
                                track.getInfo().getUri(), track.getInfo().getAuthor()))
                        .setColor(0x9F90CF)
                        .setTimestamp(Instant.now())
                        .build();
                channel.sendMessageEmbeds(embed).queue();
                nextTrack();
            }
        }
    }

    @Override
    public void onTrackException(IPlayer player, AudioTrack track, Exception exception) {
        TextChannel channel = Taboo.getInstance().getShardManager().getTextChannelById(channelId);
        MessageEmbed embed = new EmbedBuilder()
                .setTitle("An error occurred while playing the track:")
                .setDescription(track.getInfo().getTitle())
                .setColor(Color.RED)
                .setTimestamp(Instant.now())
                .build();
        channel.sendMessageEmbeds(embed).queue();
    }

    public long getGuildId() {
        return guildId;
    }

    public void destroy() {
        player.stopTrack();
        queue.clear();
    }

}
