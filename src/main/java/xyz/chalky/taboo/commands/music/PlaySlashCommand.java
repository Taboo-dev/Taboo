package xyz.chalky.taboo.commands.music;

import lavalink.client.io.jda.JdaLink;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.NotNull;
import xyz.chalky.taboo.Taboo;
import xyz.chalky.taboo.backend.CommandFlag;
import xyz.chalky.taboo.backend.SlashCommand;
import xyz.chalky.taboo.music.AudioResultHandler;
import xyz.chalky.taboo.music.AudioScheduler;
import xyz.chalky.taboo.music.GuildAudioPlayer;
import xyz.chalky.taboo.util.ExtensionsKt;

public class PlaySlashCommand extends SlashCommand {

    public PlaySlashCommand() {
        setCommandData(Commands.slash("play", "Plays a song.").addOptions(
                new OptionData(OptionType.STRING, "song", "The song to play.", true),
                new OptionData(OptionType.STRING, "provider", "Provider to search in. (Ignore if link)", false)
                        .addChoice("YouTube (Default)", "ytsearch")
                        .addChoice("Spotify", "spsearch")
                        .addChoice("SoundCloud", "scsearch")
                        .addChoice("YouTube Music", "ytmsearch")));
        addCommandFlags(CommandFlag.MUSIC);
    }

    @Override
    public void executeCommand(@NotNull SlashCommandInteractionEvent event) {
        event.deferReply().queue();
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
        if (ExtensionsKt.isUrl(input)) {
            query = input;
        } else {
            query = String.format("%s:%s", provider, input);
        }
        if (manager.getConnectedChannel() == null) {
            scheduler.setChannelId(event.getChannel().getIdLong()); // Cursed
            link.connect(voiceState.getChannel());
            link.getRestClient().loadItem(query, new AudioResultHandler(event, scheduler));
        }
    }

}
