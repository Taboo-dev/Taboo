package xyz.chalky.taboo.commands.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;
import xyz.chalky.taboo.Taboo;
import xyz.chalky.taboo.core.CommandFlag;
import xyz.chalky.taboo.core.SlashCommand;
import xyz.chalky.taboo.music.AudioScheduler;
import xyz.chalky.taboo.music.GuildAudioPlayer;

import java.awt.*;
import java.time.Instant;
import java.util.concurrent.BlockingQueue;

public class SkipToSlashCommand extends SlashCommand {

    public SkipToSlashCommand() {
        setCommandData(Commands.slash("skipto", "Skips to a song in the queue")
                .addOption(OptionType.INTEGER, "number", "The number of the song to skip to", true));
        addCommandFlags(CommandFlag.MUSIC);
        setEphemeral(false);
    }

    @Override
    public void executeCommand(@NotNull SlashCommandInteractionEvent event) {
        GuildAudioPlayer guildAudioPlayer = Taboo.getInstance().getAudioManager().getAudioPlayer(event.getGuild().getIdLong());
        AudioScheduler scheduler = guildAudioPlayer.getScheduler();
        BlockingQueue<AudioTrack> queue = scheduler.getQueue();
        int number = event.getOption("number").getAsInt(); // discord - starts index at 1
        int index = number - 1;
        if (index > queue.size()) {
            MessageEmbed embed = new EmbedBuilder()
                    .setDescription(String.format("The number is too high, the queue only has %s songs", queue.size()))
                    .setColor(Color.RED)
                    .setTimestamp(Instant.now())
                    .build();
            event.getHook().sendMessageEmbeds(embed).queue();
            return;
        }
        scheduler.skipTo(index);
        AudioTrack playingTrack = scheduler.getPlayer().getPlayingTrack();
        MessageEmbed embed = new EmbedBuilder()
                .setDescription(String.format("Skipped to %s: [%s](%s)", number, playingTrack.getInfo().title, playingTrack.getInfo().uri))
                .setColor(0x9F90CF)
                .setTimestamp(Instant.now())
                .build();
        event.getHook().sendMessageEmbeds(embed).queue();
    }

}
