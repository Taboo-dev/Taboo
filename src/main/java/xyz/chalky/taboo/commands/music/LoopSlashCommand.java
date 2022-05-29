package xyz.chalky.taboo.commands.music;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;
import xyz.chalky.taboo.central.Taboo;
import xyz.chalky.taboo.core.CommandFlag;
import xyz.chalky.taboo.core.SlashCommand;
import xyz.chalky.taboo.music.AudioScheduler;
import xyz.chalky.taboo.music.GuildAudioPlayer;

import java.time.Instant;

public class LoopSlashCommand extends SlashCommand {

    public LoopSlashCommand() {
        setCommandData(
                Commands.slash("loop", "Loop the current song.")
                        .addOption(OptionType.BOOLEAN, "loop",
                                "Whether to loop the current song.", true)
        );
        addCommandFlags(CommandFlag.MUSIC);
        setEphemeral(false);
    }

    @Override
    public void executeCommand(@NotNull SlashCommandInteractionEvent event) {
        GuildAudioPlayer guildAudioPlayer = Taboo.getInstance().getAudioManager().getAudioPlayer(event.getGuild().getIdLong());
        AudioScheduler scheduler = guildAudioPlayer.getScheduler();
        boolean loop = event.getOption("loop").getAsBoolean();
        if (loop) {
            scheduler.setRepeat(true);
            MessageEmbed embed = new EmbedBuilder()
                    .setTitle("Looping")
                    .setDescription("Looping is now enabled")
                    .setColor(0x9F90CF)
                    .setTimestamp(Instant.now())
                    .build();
            event.getHook().sendMessageEmbeds(embed).queue();
        } else {
            scheduler.setRepeat(false);
            MessageEmbed embed = new EmbedBuilder()
                    .setTitle("No longer looping")
                    .setDescription("Looping is now disabled")
                    .setColor(0x9F90CF)
                    .setTimestamp(Instant.now())
                    .build();
            event.getHook().sendMessageEmbeds(embed).queue();
        }
    }

}
