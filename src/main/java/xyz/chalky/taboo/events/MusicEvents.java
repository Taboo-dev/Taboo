package xyz.chalky.taboo.events;

import com.github.topislavalinkplugins.topissourcemanagers.ISRCAudioTrack;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import lavalink.client.player.LavalinkPlayer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import xyz.chalky.taboo.central.Taboo;
import xyz.chalky.taboo.database.repository.SearchHistoryRepository;
import xyz.chalky.taboo.music.AudioScheduler;
import xyz.chalky.taboo.music.GuildAudioPlayer;

import java.time.Instant;

import static xyz.chalky.taboo.util.MiscUtil.toMinutesAndSeconds;

@Component
public class MusicEvents extends ListenerAdapter {

    private final SearchHistoryRepository searchHistoryRepository;

    public MusicEvents(SearchHistoryRepository searchHistoryRepository) {
        this.searchHistoryRepository = searchHistoryRepository;
    }

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
                String duration = toMinutesAndSeconds(info.length);
                EmbedBuilder embed = new EmbedBuilder()
                        .setTitle("Now Playing:")
                        .setDescription(String.format("[%s](%s) by %s", info.title,
                                info.uri, info.author))
                        .addField("Duration:", duration, true)
                        .setColor(0x9F90CF)
                        .setTimestamp(Instant.now());
                if (track instanceof YoutubeAudioTrack) {
                    embed.setThumbnail(String.format("https://img.youtube.com/vi/%s/mqdefault.jpg", info.identifier));
                } else if (track instanceof ISRCAudioTrack isrcAudioTrack) {
                    embed.setThumbnail(isrcAudioTrack.getArtworkURL());
                }
                if (!info.identifier.equals(trackIdentifier)) {
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
