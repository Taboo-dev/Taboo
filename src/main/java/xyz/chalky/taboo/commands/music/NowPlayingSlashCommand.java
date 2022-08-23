package xyz.chalky.taboo.commands.music;

import lavalink.client.player.LavalinkPlayer;
import lavalink.client.player.track.AudioTrack;
import lavalink.client.player.track.AudioTrackInfo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import xyz.chalky.taboo.central.Taboo;
import xyz.chalky.taboo.core.CommandFlag;
import xyz.chalky.taboo.core.SlashCommand;
import xyz.chalky.taboo.music.GuildAudioPlayer;

import java.time.Instant;

import static xyz.chalky.taboo.util.MiscUtil.getArtworkUrl;
import static xyz.chalky.taboo.util.MiscUtil.toMinutesAndSeconds;

@Component
public class NowPlayingSlashCommand extends SlashCommand {

    public NowPlayingSlashCommand() {
        setCommandData(Commands.slash("now-playing", "Queries the current song playing."));
        addCommandFlags(CommandFlag.MUSIC);
        setEphemeral(false);
    }

    @Override
    public void executeCommand(@NotNull SlashCommandInteractionEvent event) {
        GuildAudioPlayer guildAudioPlayer = Taboo.getInstance().getAudioManager().getAudioPlayer(event.getGuild().getIdLong());
        LavalinkPlayer player = guildAudioPlayer.getScheduler().getPlayer();
        AudioTrack track = player.getPlayingTrack();
        if (track == null) {
            MessageEmbed embed = new EmbedBuilder()
                    .setTitle("No song is currently playing.")
                    .setColor(0x9F90CF)
                    .setTimestamp(Instant.now())
                    .build();
            event.getHook().sendMessageEmbeds(embed).queue();
        } else {
            AudioTrackInfo info = track.getInfo();
            long position = player.getTrackPosition();
            long length = info.getLength();
            String trackPosition = toMinutesAndSeconds(position);
            String duration = toMinutesAndSeconds(length);
            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("Now Playing:")
                    .setDescription(String.format("[%s](%s) by %s", info.getTitle(), info.getUri(), info.getAuthor()))
                    .addField("Duration:", String.format("%s / %s", trackPosition, duration), true)
                    .setThumbnail(getArtworkUrl(track))
                    .setColor(0x9F90CF)
                    .setTimestamp(Instant.now());
            event.getHook().sendMessageEmbeds(embed.build()).queue();
        }
    }

}
