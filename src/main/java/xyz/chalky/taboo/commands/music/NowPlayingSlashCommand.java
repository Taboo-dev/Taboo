package xyz.chalky.taboo.commands.music;

import kotlin.Pair;
import lavalink.client.player.LavalinkPlayer;
import lavalink.client.player.track.AudioTrack;
import lavalink.client.player.track.AudioTrackInfo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import xyz.chalky.taboo.Taboo;
import xyz.chalky.taboo.backend.SlashCommand;
import xyz.chalky.taboo.music.GuildAudioPlayer;
import xyz.chalky.taboo.util.ExtensionsKt;

import java.time.Instant;

public class NowPlayingSlashCommand extends SlashCommand {

    public NowPlayingSlashCommand() {
        setCommandData(Commands.slash("now-playing", "Queries the current song playing."));
    }

    @Override
    public void executeCommand(SlashCommandInteractionEvent event) {
        GuildAudioPlayer guildAudioPlayer = Taboo.getInstance().getAudioManager().getAudioPlayer(event.getGuild().getIdLong());
        LavalinkPlayer player = guildAudioPlayer.getScheduler().getPlayer();
        AudioTrack playingTrack = player.getPlayingTrack();
        if (playingTrack == null) {
            MessageEmbed embed = new EmbedBuilder()
                    .setTitle("No song is currently playing.")
                    .setColor(0x9F90CF)
                    .setTimestamp(Instant.now())
                    .build();
            event.getHook().sendMessageEmbeds(embed).queue();
        } else {
            AudioTrackInfo info = playingTrack.getInfo();
            long trackPosition = player.getTrackPosition();
            long length = info.getLength();
            Pair<Long, Long> parseTrackPosition = ExtensionsKt.parseLength(trackPosition);
            Pair<Long, Long> parseLength = ExtensionsKt.parseLength(length);
            String durationString = String.format(
                    "%02d:%02d / %02d:%02d",
                    parseTrackPosition.getFirst(), parseTrackPosition.getSecond(),
                    parseLength.getFirst(), parseLength.getSecond()
            );
            MessageEmbed embed = new EmbedBuilder()
                    .setTitle("Now Playing:")
                    .setDescription(String.format("[%s](%s) by %s", info.getTitle(), info.getUri(), info.getAuthor()))
                    .addField("Duration:", durationString, false)
                    .setColor(0x9F90CF)
                    .setTimestamp(Instant.now())
                    .build();
            event.getHook().sendMessageEmbeds(embed).queue();
        }
    }

}
