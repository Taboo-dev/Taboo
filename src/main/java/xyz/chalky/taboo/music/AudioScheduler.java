package xyz.chalky.taboo.music;

import lavalink.client.io.jda.JdaLink;
import lavalink.client.player.IPlayer;
import lavalink.client.player.LavalinkPlayer;
import lavalink.client.player.event.PlayerEventListenerAdapter;
import lavalink.client.player.track.AudioTrack;
import lavalink.client.player.track.AudioTrackEndReason;
import lavalink.client.player.track.AudioTrackInfo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;
import xyz.chalky.taboo.central.Taboo;

import java.awt.*;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static xyz.chalky.taboo.util.MiscUtil.getArtworkUrl;
import static xyz.chalky.taboo.util.MiscUtil.toMinutesAndSeconds;

public class AudioScheduler extends PlayerEventListenerAdapter {

    private final LavalinkPlayer player;
    private final JdaLink link;
    private final BlockingQueue<AudioTrack> queue;
    private final long guildId;
    private long channelId;
    private boolean repeat = false;

    public AudioScheduler(@NotNull LavalinkPlayer player, long guildId) {
        this.guildId = guildId;
        this.queue = new LinkedBlockingQueue<>();
        this.link = Taboo.getInstance().getLavalink().getLink(guildId);
        this.player = link.getPlayer();
        player.addListener(this);
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
            if (queue.size() == 0) {
                VoiceChannel channel = Taboo.getInstance().getShardManager().getVoiceChannelById(channelId);
                MessageEmbed embed = new EmbedBuilder()
                        .setDescription("There are no more tracks in the queue.")
                        .setColor(Color.RED)
                        .setTimestamp(Instant.now())
                        .build();
                channel.sendMessageEmbeds(embed).queue();
            }
        }
    }

    public void skipTo(int index) {
        AudioTrack[] tracks = queue.toArray(new AudioTrack[0]);
        AudioTrack track = tracks[index];
        for (int i = 0; i < (index + 1); i++) {
            queue.poll();
        }
        if (track != null) {
            player.playTrack(track);
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
    public void onTrackStart(IPlayer player, @NotNull AudioTrack track) {
        VoiceChannel channel = Taboo.getInstance().getShardManager().getVoiceChannelById(channelId);
        AudioTrackInfo info = track.getInfo();
        long length = info.getLength();
        String duration = toMinutesAndSeconds(length);
        if (!repeat) {
            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("Now Playing:")
                    .setDescription(String.format("[%s](%s) by %s", info.getTitle(), info.getUri(), info.getAuthor()))
                    .addField("Duration:", duration, true)
                    .setThumbnail(getArtworkUrl(track))
                    .setColor(0x9F90CF)
                    .setTimestamp(Instant.now());
            String identifier = info.getIdentifier();
            String id = String.format("music:[]:%s:%s", channelId, identifier);
            channel.sendMessageEmbeds(embed.build()).setActionRow(
                        Button.secondary(id.replace("[]", "pause"), "Play/Pause"),
                        Button.secondary(id.replace("[]", "skip"), "Skip"),
                        Button.secondary(id.replace("[]", "stop"), "Stop"),
                        Button.secondary(id.replace("[]", "loop"), "Loop"),
                        Button.secondary(id.replace("[]", "shuffle"), "Shuffle")
            ).queue();
        }
    }

    @Override
    public void onTrackEnd(IPlayer player, AudioTrack track, @NotNull AudioTrackEndReason endReason) {
        VoiceChannel channel = Taboo.getInstance().getShardManager().getVoiceChannelById(channelId);
        if (endReason.mayStartNext) {
            AudioTrackInfo info = track.getInfo();
            String description = String.format("[%s](%s) by %s", info.getTitle(), info.getUri(), info.getAuthor());
            if (repeat) {
                player.playTrack(track);
                MessageEmbed embed = new EmbedBuilder()
                        .setTitle("Looping:")
                        .setDescription(description)
                        .setColor(0x9F90CF)
                        .setTimestamp(Instant.now())
                        .build();
                channel.sendMessageEmbeds(embed).queue();
            } else {
                MessageEmbed embed = new EmbedBuilder()
                        .setTitle("Track Ended:")
                        .setDescription(description)
                        .setColor(0x9F90CF)
                        .setTimestamp(Instant.now())
                        .build();
                channel.sendMessageEmbeds(embed).queue();
                nextTrack();
            }
        }
    }

    @Override
    public void onTrackException(IPlayer player, @NotNull AudioTrack track, Exception exception) {
        VoiceChannel channel = Taboo.getInstance().getShardManager().getVoiceChannelById(channelId);
        AudioTrackInfo info = track.getInfo();
        MessageEmbed embed = new EmbedBuilder()
                .setTitle("An error occurred while playing the track:")
                .setDescription(info.getTitle())
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
