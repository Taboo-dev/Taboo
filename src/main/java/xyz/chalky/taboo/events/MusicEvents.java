package xyz.chalky.taboo.events;

import lavalink.client.player.LavalinkPlayer;
import lavalink.client.player.track.AudioTrack;
import lavalink.client.player.track.AudioTrackInfo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import xyz.chalky.taboo.central.Taboo;
import xyz.chalky.taboo.music.AudioScheduler;
import xyz.chalky.taboo.music.GuildAudioPlayer;

import java.time.Instant;

import static xyz.chalky.taboo.util.MiscUtil.getArtworkUrl;
import static xyz.chalky.taboo.util.MiscUtil.toMinutesAndSeconds;

public class MusicEvents extends ListenerAdapter {

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        JDA jda = event.getJDA();
        String componentId = event.getComponentId();
        if (componentId.startsWith("music:")) {
            event.deferEdit().queue();
        }
        Guild guild = event.getGuild();
        String[] split = componentId.split(":");
        final long channelId = Long.parseLong(split[2]);
        final String trackIdentifier = split[3];
        if (!(event.getChannel().getIdLong() == channelId)) return;
        GuildAudioPlayer guildAudioPlayer = Taboo.getInstance().getAudioManager().getAudioPlayer(guild.getIdLong());
        AudioScheduler scheduler = guildAudioPlayer.getScheduler();
        LavalinkPlayer lavalinkPlayer = scheduler.getPlayer();
        AudioTrack track = lavalinkPlayer.getPlayingTrack();
        AudioTrackInfo info = track.getInfo();
        switch (split[1]) {
            case "pause" -> {
                // componentId = music:pause:<channelId>:<trackIdentifier>
                String duration = toMinutesAndSeconds(info.getLength());
                EmbedBuilder embed = new EmbedBuilder()
                        .setTitle("Now Playing:")
                        .setDescription(String.format("[%s](%s) by %s", info.getTitle(),
                                info.getUri(), info.getAuthor()))
                        .addField("Duration:", duration, true)
                        .setThumbnail(getArtworkUrl(track))
                        .setColor(0x9F90CF)
                        .setTimestamp(Instant.now());
                if (!info.getIdentifier().equals(trackIdentifier)) {
                    event.getHook().sendMessage("That track is currently not playing!").setEphemeral(true).queue();
                    return;
                }
                if (lavalinkPlayer.isPaused()) {
                    lavalinkPlayer.setPaused(false);
                    embed.addField("Paused", "False", true);
                    event.getHook().editOriginalEmbeds(embed.build()).queue();
                } else {
                    lavalinkPlayer.setPaused(true);
                    embed.addField("Paused", "True", true);
                    event.getHook().editOriginalEmbeds(embed.build()).queue();
                }
            }
            case "skip" -> {
                // componentId = music:skip:<channelId>:<trackIdentifier>
                scheduler.nextTrack();
                MessageEmbed embed = new EmbedBuilder()
                        .setTitle("Skipped the current song.")
                        .setColor(0x9F90CF)
                        .setTimestamp(Instant.now())
                        .build();
                event.getHook().sendMessageEmbeds(embed).queue();
            }
            case "stop" -> {
                // componentId = music:stop:<channelId>:<trackIdentifier>
                scheduler.destroy();
                jda.getDirectAudioController().disconnect(guild);
                MessageEmbed embed = new EmbedBuilder()
                        .setTitle("Stopped the music player.")
                        .setColor(0x9F90CF)
                        .setTimestamp(Instant.now())
                        .build();
                event.getHook().sendMessageEmbeds(embed).queue();
            }
            case "loop" -> {
                // componentId = music:loop:<channelId>:<trackIdentifier>
                boolean repeat = scheduler.isRepeat();
                if (repeat) {
                    scheduler.setRepeat(false);
                    MessageEmbed embed = new EmbedBuilder()
                            .setTitle("No longer looping")
                            .setDescription("Looping is now disabled")
                            .setColor(0x9F90CF)
                            .setTimestamp(Instant.now())
                            .build();
                    event.getHook().sendMessageEmbeds(embed).queue();
                } else {
                    scheduler.setRepeat(true);
                    MessageEmbed embed = new EmbedBuilder()
                            .setTitle("Looping")
                            .setDescription("Looping is now enabled")
                            .setColor(0x9F90CF)
                            .setTimestamp(Instant.now())
                            .build();
                    event.getHook().sendMessageEmbeds(embed).queue();
                }
            }
            case "shuffle" -> {
                scheduler.shuffle();
                MessageEmbed embed = new EmbedBuilder()
                        .setTitle("Shuffled the queue.")
                        .setColor(0x9F90CF)
                        .setTimestamp(Instant.now())
                        .build();
                event.getHook().sendMessageEmbeds(embed).queue();
            }
        }
    }

}
