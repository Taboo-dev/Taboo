package xyz.chalky.taboo.events;

import com.github.topislavalinkplugins.topissourcemanagers.ISRCAudioTrack;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import lavalink.client.player.LavalinkPlayer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import xyz.chalky.taboo.central.Taboo;
import xyz.chalky.taboo.database.model.SearchHistory;
import xyz.chalky.taboo.database.repository.SearchHistoryRepository;
import xyz.chalky.taboo.music.AudioScheduler;
import xyz.chalky.taboo.music.GuildAudioPlayer;

import java.time.Instant;

import static xyz.chalky.taboo.util.MiscUtil.toMinutesAndSeconds;

@Component
public class MusicEvents extends ListenerAdapter {

    @Autowired private SearchHistoryRepository searchHistoryRepository;

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        event.deferEdit().queue();
        String componentId = event.getComponentId();
        Guild guild = event.getGuild();
        if (componentId.startsWith("music:pause")) {
            // componentId = music:pause:<channelId>:<trackIdentifier>
            String[] split = componentId.split(":");
            long channelId = Long.parseLong(split[2]);
            if (!(event.getChannel().getIdLong() == channelId)) return;
            String trackIdentifier = split[3];
            GuildAudioPlayer guildAudioPlayer = Taboo.getInstance().getAudioManager().getAudioPlayer(guild.getIdLong());
            AudioScheduler scheduler = guildAudioPlayer.getScheduler();
            LavalinkPlayer lavalinkPlayer = scheduler.getPlayer();
            AudioTrack track = lavalinkPlayer.getPlayingTrack();
            AudioTrackInfo info = track.getInfo();
            String duration = toMinutesAndSeconds(info.length);
            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("Now Playing:")
                    .setDescription(String.format("[%s](%s) by %s", info.title,
                            info.uri, info.author))
                    .addField("Duration:", duration, true)
                    .setColor(0x9F90CF)
                    .setTimestamp(Instant.now());
            if (track instanceof YoutubeAudioTrack) {
                embed.setThumbnail(String.format("https://img.youtube.com/vi/%s/mqdefault.jpg", track.getInfo().identifier));
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
        } else if (componentId.startsWith("music:save")) {
            // componentId = music:save:<channelId>:<trackIdentifier>
            String[] split = componentId.split(":");
            long channelId = Long.parseLong(split[2]);
            if (!(event.getChannel().getIdLong() == channelId)) return;
            GuildAudioPlayer guildAudioPlayer = Taboo.getInstance().getAudioManager().getAudioPlayer(guild.getIdLong());
            AudioScheduler scheduler = guildAudioPlayer.getScheduler();
            LavalinkPlayer lavalinkPlayer = scheduler.getPlayer();
            AudioTrack track = lavalinkPlayer.getPlayingTrack();
            AudioTrackInfo info = track.getInfo();
            SearchHistory history = new SearchHistory(event.getUser().getIdLong(), info.title, info.uri, track.getIdentifier());
            searchHistoryRepository.save(history);
        }
    }

}
