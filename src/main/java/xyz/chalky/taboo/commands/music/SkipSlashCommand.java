package xyz.chalky.taboo.commands.music;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import xyz.chalky.taboo.Taboo;
import xyz.chalky.taboo.backend.CommandFlag;
import xyz.chalky.taboo.backend.SlashCommand;
import xyz.chalky.taboo.music.AudioScheduler;
import xyz.chalky.taboo.music.GuildAudioPlayer;

import java.time.Instant;

public class SkipSlashCommand extends SlashCommand {

    public SkipSlashCommand() {
        setCommandData(Commands.slash("skip", "Skips the current song."));
        addCommandFlags(CommandFlag.MUST_BE_IN_VC, CommandFlag.MUST_BE_IN_SAME_VC);
    }

    @Override
    public void executeCommand(SlashCommandInteractionEvent event) {
        GuildAudioPlayer guildAudioPlayer = Taboo.getInstance().getAudioManager().getAudioPlayer(event.getGuild().getIdLong());
        AudioScheduler scheduler = guildAudioPlayer.getScheduler();
        scheduler.nextTrack();
        MessageEmbed embed = new EmbedBuilder()
                .setTitle("Skipped the current song.")
                .setColor(0x9F90CF)
                .setTimestamp(Instant.now())
                .build();
        event.getHook().sendMessageEmbeds(embed).queue();
    }

}