package io.github.taboodev.taboo.commands;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import io.github.taboodev.taboo.util.Constants;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDAInfo;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.OperatingSystem;

import java.time.Instant;

import static io.github.taboodev.taboo.util.DateDifference.timeSince;
import static io.github.taboodev.taboo.util.ParseBytes.parseBytes;

public class Stats extends SlashCommand {

    private static final Instant startTime = Instant.now();

    public Stats() {
        this.name = "stats";
        this.help = "Shows some bot statistics.";
        this.defaultEnabled = true;
        this.guildOnly = true;
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        JDA jda = event.getJDA();
        User user = event.getUser();
        event.replyEmbeds(statsEmbed(jda, user)).mentionRepliedUser(false).setEphemeral(false).queue();
    }

    @Override
    protected void execute(CommandEvent event) {
        Message message = event.getMessage();
        JDA jda = event.getJDA();
        User author = message.getAuthor();
        message.replyEmbeds(statsEmbed(jda, author)).mentionRepliedUser(false).queue();
    }

    private MessageEmbed statsEmbed(JDA jda, User user) {
        User owner = jda.getUserById(Constants.OWNER_ID);
        SystemInfo systemInfo = new SystemInfo();
        HardwareAbstractionLayer hardware = systemInfo.getHardware();
        CentralProcessor processor = hardware.getProcessor();
        String cpuName = processor.getProcessorIdentifier().getName();
        int cores = processor.getPhysicalProcessorCount();
        int threads = processor.getLogicalProcessorCount();
        GlobalMemory memory1 = hardware.getMemory();
        double available = (double) memory1.getAvailable();
        double total = (double) memory1.getTotal();
        double usedMemory = total - available;
        OperatingSystem operatingSystem = systemInfo.getOperatingSystem();
        return new EmbedBuilder()
                .setTitle("Taboo")
                .addField("Author:", owner.getAsTag(), true)
                .addField("Source:", "[View source on GitHub](https://github.com/Taboo-dev/Taboo)", true)
                .addField("Library:", "[JDA " + JDAInfo.VERSION + "](" + JDAInfo.GITHUB + ")", true)
                .addField("Uptime:", timeSince(Instant.now().toEpochMilli() - startTime.toEpochMilli()), true)
                .addField("Servers:", String.valueOf(jda.getGuildCache().size()), true)
                .addField("CPU:", cpuName, true)
                .addField("Cores:", String.valueOf(cores), true)
                .addField("Threads:", String.valueOf(threads), true)
                .addField("Total RAM:", parseBytes(total), true)
                .addField("Available RAM:", parseBytes(available), true)
                .addField("Used RAM:", parseBytes(usedMemory), true)
                .addField("OS:", String.valueOf(operatingSystem), true)
                .setColor(0x9F90CF)
                .setFooter("Requested by " + user.getAsTag(), user.getEffectiveAvatarUrl())
                .build();
    }

}
