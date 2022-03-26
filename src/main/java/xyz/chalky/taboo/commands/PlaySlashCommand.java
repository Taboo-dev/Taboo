package xyz.chalky.taboo.commands;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.managers.AudioManager;
import xyz.chalky.taboo.backend.CommandFlag;
import xyz.chalky.taboo.backend.SlashCommand;
import xyz.chalky.taboo.backend.SlashCommandContext;
import xyz.chalky.taboo.music.PlayerManager;
import xyz.chalky.taboo.util.PropertiesManager;

public class PlaySlashCommand extends SlashCommand {

    private final PropertiesManager propertiesManager;

    public PlaySlashCommand(PropertiesManager propertiesManager) {
        setCommandData(Commands.slash("play", "Plays a song.")
                .addOptions(new OptionData(OptionType.STRING, "song", "The song to play.", true)/*.setAutoComplete(true)*/));
        addCommandFlags(CommandFlag.MUST_BE_IN_VC);
        this.propertiesManager = propertiesManager;
    }

    @Override
    public void executeCommand(SlashCommandInteractionEvent event, Member sender, SlashCommandContext ctx) {
        InteractionHook hook = event.getHook();
        Guild guild = event.getGuild();
        String input = event.getOption("song").getAsString();
        Member member = event.getMember();
        GuildVoiceState voiceState = member.getVoiceState();
        AudioManager selfAudioManager = guild.getAudioManager();
        String query;
        if (input.startsWith("http://") || input.startsWith("https://")) {
            query = input;
        } else {
            query = "ytsearch:" + input;
        }
        selfAudioManager.openAudioConnection(voiceState.getChannel());
        // selfAudioManager.setSelfDeafened(true);
        PlayerManager.getInstance().loadAndPlay(event, query);
    }

    /*@Override
    public void handleAutoComplete(CommandAutoCompleteInteractionEvent event) {
        if (event.getFocusedOption().getName().equals("song")) {
            AutoCompleteQuery song = event.getFocusedOption();
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url("https://www.googleapis.com/youtube/v3/search?part=snippet&maxResults=20&q=" + song.getValue() + "&type=video&key=" + propertiesManager.getYouTubeApiKey())
                    .build();
            try (Response response = client.newCall(request).execute()) {
                DataObject responseBody = DataObject.fromJson(response.body().string());
                List<String> results = new ArrayList<>();
                DataArray items = responseBody.getArray("items");
                DataObject snippet = items.getObject(0).getObject("snippet");
                items.forEach(o -> {

                });
                event.replyChoices(


                ).queue();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }*/

}
