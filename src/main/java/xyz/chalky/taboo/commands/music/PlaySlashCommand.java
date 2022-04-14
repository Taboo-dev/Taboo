package xyz.chalky.taboo.commands.music;

import lavalink.client.io.FriendlyException;
import lavalink.client.io.LoadResultHandler;
import lavalink.client.io.jda.JdaLink;
import lavalink.client.player.track.AudioPlaylist;
import lavalink.client.player.track.AudioTrack;
import lavalink.client.player.track.AudioTrackInfo;
import mu.KotlinLogging;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.managers.AudioManager;
import org.slf4j.Logger;
import xyz.chalky.taboo.Taboo;
import xyz.chalky.taboo.backend.CommandFlag;
import xyz.chalky.taboo.backend.SlashCommand;
import xyz.chalky.taboo.music.AudioScheduler;
import xyz.chalky.taboo.music.GuildAudioPlayer;

import java.awt.*;
import java.time.Instant;
import java.util.List;

import static xyz.chalky.taboo.util.ExtensionsKt.isUrl;

public class PlaySlashCommand extends SlashCommand {

    public PlaySlashCommand() {
        setCommandData(Commands.slash("play", "Plays a song.").addOptions(
                new OptionData(OptionType.STRING, "song", "The song to play.", true),
                new OptionData(OptionType.STRING, "provider", "Provider to search in. (Ignore if link)", false)
                        .addChoice("YouTube (Default)", "ytsearch")
                        .addChoice("Spotify", "spsearch")
                        .addChoice("SoundCloud", "scsearch")
                        .addChoice("YouTube Music", "ytmsearch")));
        addCommandFlags(CommandFlag.MUST_BE_IN_VC, CommandFlag.MUST_BE_IN_SAME_VC);
    }

    @Override
    public void executeCommand(SlashCommandInteractionEvent event) {
        GuildAudioPlayer guildAudioPlayer = Taboo.getInstance().getAudioManager().getAudioPlayer(event.getGuild().getIdLong());
        AudioScheduler scheduler = guildAudioPlayer.getScheduler();
        JdaLink link = guildAudioPlayer.getScheduler().getLink();
        String input = event.getOption("song").getAsString();
        OptionMapping providerOption = event.getOption("provider");
        Member member = event.getMember();
        GuildVoiceState voiceState = member.getVoiceState();
        AudioManager manager = event.getGuild().getAudioManager();
        String query;
        String provider;
        if (providerOption == null) {
            provider = "ytsearch";
        } else {
            provider = providerOption.getAsString();
        }
        if (isUrl(input)) {
            query = input;
        } else {
            query = String.format("%s:%s", provider, input);
        }
        if (manager.getConnectedChannel() == null) {
            scheduler.setChannelId(event.getChannel().getIdLong()); // Cursed
            link.connect((VoiceChannel) voiceState.getChannel());
            link.getRestClient().loadItem(query, new LoadResultHandler() {
                private final Logger LOGGER = KotlinLogging.INSTANCE.logger("AudioLoadResultHandler");
                @Override
                public void playlistLoaded(AudioPlaylist playlist) {
                    playlist.getTracks().forEach(scheduler::queue);
                    handlePlaylist(playlist);
                    LOGGER.debug("Playlist loaded: {}", playlist.getName());
                }

                @Override
                public void searchResultLoaded(List<AudioTrack> tracks) {
                    AudioTrack track = tracks.get(0);
                    scheduler.queue(track);
                    handle(track);
                    LOGGER.debug("Track loaded: {}", track.getInfo().getTitle());
                }

                @Override
                public void trackLoaded(AudioTrack track) {
                    scheduler.queue(track);
                    handle(track);
                    LOGGER.debug("Track loaded: {}", track.getInfo().getTitle());
                }

                @Override
                public void noMatches() {
                    MessageEmbed embed = new EmbedBuilder()
                            .setTitle("No matches found")
                            .setColor(Color.RED)
                            .setTimestamp(Instant.now())
                            .build();
                    event.getHook().sendMessageEmbeds(embed).queue();
                    LOGGER.debug("No matches found");
                }

                @Override
                public void loadFailed(FriendlyException exception) {
                    MessageEmbed embed = new EmbedBuilder()
                            .setTitle("An error occurred while loading the track")
                            .setColor(Color.RED)
                            .setTimestamp(Instant.now())
                            .build();
                    event.getHook().sendMessageEmbeds(embed).queue();
                    LOGGER.debug("An error occurred while loading the track: {}", exception.getMessage());
                }

                public void handle(AudioTrack track) {
                    MessageEmbed embed = new EmbedBuilder()
                            .setTitle("Added to queue:")
                            .setDescription(String.format("[%s](%s) by %s", track.getInfo().getTitle(),
                                    track.getInfo().getUri(), track.getInfo().getAuthor()))
                            .setColor(0x9F90CF)
                            .setTimestamp(Instant.now())
                            .build();
                    event.getHook().sendMessageEmbeds(embed).queue();
                }

                public void handlePlaylist(AudioPlaylist playlist) {
                    StringBuilder description = new StringBuilder("Tracks:\n");
                    int trackList = playlist.getTracks().size();
                    int trackCount = Math.min(trackList, 10);
                    for (int i = 0; i < trackCount; i++) {
                        AudioTrack track = playlist.getTracks().get(i);
                        AudioTrackInfo info = track.getInfo();
                        description.append("`#")
                                .append(i + 1)
                                .append("` ")
                                .append(info.getTitle())
                                .append(" [")
                                .append(info.getAuthor())
                                .append("]\n");
                    }
                    if (trackList > trackCount) {
                        description.append("And ")
                                .append("`")
                                .append(trackList - trackCount)
                                .append("`")
                                .append(" more tracks...");
                    }
                    EmbedBuilder embed = new EmbedBuilder()
                            .setTitle("Added to queue:")
                            .setDescription(description)
                            .setColor(0x9F90CF)
                            .setTimestamp(Instant.now());
                    if (trackList == 0) {
                        embed.setDescription("The queue is empty.");
                    } else {
                        embed.setDescription(description);
                    }
                    event.getHook().sendMessageEmbeds(embed.build()).queue();
                }
            });
        }
    }

}
