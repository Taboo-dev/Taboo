package xyz.chalky.taboo.commands.music;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import lavalink.client.io.jda.JdaLink;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.AutoCompleteQuery;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import xyz.chalky.taboo.central.Taboo;
import xyz.chalky.taboo.core.CommandFlag;
import xyz.chalky.taboo.core.SlashCommand;
import xyz.chalky.taboo.database.model.SearchHistory;
import xyz.chalky.taboo.database.repository.SearchHistoryRepository;
import xyz.chalky.taboo.music.AudioScheduler;
import xyz.chalky.taboo.music.GuildAudioPlayer;

import java.awt.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static xyz.chalky.taboo.util.MiscUtil.isUrl;

@Component
public class PlaySlashCommand extends SlashCommand {

    private final SearchHistoryRepository searchHistoryRepository;

    public PlaySlashCommand(SearchHistoryRepository searchHistoryRepository) {
        setCommandData(Commands.slash("play", "Plays a song.").addOptions(
                new OptionData(OptionType.STRING, "song", "The song to play.", true, true),
                new OptionData(OptionType.STRING, "provider", "Provider to search in. (Ignore if link)", false)
                        .addChoice("YouTube (Default)", "ytsearch")
                        .addChoice("Spotify", "spsearch")
                        .addChoice("SoundCloud", "scsearch")
                        .addChoice("YouTube Music", "ytmsearch")));
        addCommandFlags(CommandFlag.MUSIC);
        setEphemeral(false);
        this.searchHistoryRepository = searchHistoryRepository;
    }

    @Override
    public void executeCommand(@NotNull SlashCommandInteractionEvent event) {
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
            scheduler.setChannelId(event.getChannel().getIdLong());
            link.connect(voiceState.getChannel());
            link.getRestClient().loadItem(query, new AudioLoadResultHandler() {
                private static final Logger LOGGER = LoggerFactory.getLogger(AudioLoadResultHandler.class);
                @Override
                public void trackLoaded(AudioTrack track) {
                    scheduler.queue(track);
                    handle(track);
                    LOGGER.debug("Track loaded: {}", track.getInfo().title);
                }

                @Override
                public void playlistLoaded(@NotNull AudioPlaylist playlist) {
                    if (playlist.isSearchResult()) {
                        AudioTrack track = playlist.getTracks().get(0);
                        scheduler.queue(track);
                        handle(track);
                        LOGGER.debug("Track loaded: {}", track.getInfo().title);
                    } else {
                        playlist.getTracks().forEach(scheduler::queue);
                        handlePlaylist(playlist);
                        LOGGER.debug("Playlist loaded: {}", playlist.getName());
                    }
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
                public void loadFailed(@NotNull FriendlyException exception) {
                    MessageEmbed embed = new EmbedBuilder()
                            .setTitle("An error occurred while loading the track")
                            .setColor(Color.RED)
                            .setTimestamp(Instant.now())
                            .build();
                    event.getHook().sendMessageEmbeds(embed).queue();
                    LOGGER.debug("An error occurred while loading the track: {}", exception.getMessage());
                }

                private void handle(@NotNull AudioTrack track) {
                    AudioTrackInfo info = track.getInfo();
                    MessageEmbed embed = new EmbedBuilder()
                            .setTitle("Added to queue:")
                            .setDescription(String.format("[%s](%s) by %s", info.title, info.uri, info.author))
                            .setColor(0x9F90CF)
                            .setTimestamp(Instant.now())
                            .build();
                    event.getHook().sendMessageEmbeds(embed).queue();
                    SearchHistory history = new SearchHistory(event.getUser().getIdLong(), info.title, info.uri, track.getIdentifier());
                    searchHistoryRepository.save(history);
                }

                private void handlePlaylist(@NotNull AudioPlaylist playlist) {
                    StringBuilder description = new StringBuilder("Tracks:\n");
                    int trackList = playlist.getTracks().size();
                    int trackCount = Math.min(trackList, 10);
                    for (int i = 0; i < trackCount; i++) {
                        AudioTrack track = playlist.getTracks().get(i);
                        AudioTrackInfo info = track.getInfo();
                        description.append("`#")
                                .append(i + 1)
                                .append("` [")
                                .append(info.title)
                                .append("](")
                                .append(info.uri)
                                .append(") by ")
                                .append(info.author)
                                .append("\n");
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
                    List<SearchHistory> histories = new ArrayList<>();
                    playlist.getTracks().forEach(audioTrack -> {
                        AudioTrackInfo info = audioTrack.getInfo();
                        histories.add(new SearchHistory(event.getUser().getIdLong(), info.title, info.uri, audioTrack.getIdentifier()));
                        searchHistoryRepository.saveAll(histories);
                    });
                }
            });
        }
    }

    @Override
    public void handleAutoComplete(@NotNull CommandAutoCompleteInteractionEvent event) {
        AutoCompleteQuery focusedOption = event.getFocusedOption();
        String value = focusedOption.getValue();
        if (focusedOption.getName().equals("song")) {
            Set<Command.Choice> choices;
            long userId = event.getUser().getIdLong();
            if (value.isEmpty()) {
                choices = searchHistoryRepository.findByUserId(userId)
                        .stream()
                        .limit(25)
                        .map(history -> new Command.Choice(history.getName(), history.getUrl()))
                        .collect(Collectors.toSet());
            } else {
                choices = searchHistoryRepository.findByUserId(userId)
                        .stream()
                        .filter(history -> history.getName().toLowerCase().contains(value.toLowerCase()))
                        .limit(25)
                        .map(history -> new Command.Choice(history.getName(), history.getUrl()))
                        .collect(Collectors.toSet());
            }
            event.replyChoices(choices).queue();
        }
    }

}
