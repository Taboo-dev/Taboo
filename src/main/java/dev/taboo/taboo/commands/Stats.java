package dev.taboo.taboo.commands;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import dev.taboo.taboo.util.DateDifference;
import dev.taboo.taboo.util.ParseBytes;
import dev.taboo.taboo.util.PropertiesManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDAInfo;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.commands.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.OperatingSystem;

import java.time.Instant;

public class Stats extends SlashCommand {

    private static final Instant startTime = Instant.now();

    public Stats() {
        this.name = "stats";
        this.help = "Shows some bot statistics.";
        this.aliases = new String[] { "botstats", "statistics" };
        this.defaultEnabled = true;
        this.guildOnly = true;
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        JDA jda = event.getJDA();
        User user = event.getUser();
        InteractionHook hook = event.getHook();
        event.deferReply(true).queue();
        hook.sendMessageEmbeds(statsEmbed(jda, user)).mentionRepliedUser(false).queue();
    }

    @Override
    protected void execute(CommandEvent event) {
        JDA jda = event.getJDA();
        User author = event.getAuthor();
        Message message = event.getMessage();
        message.replyEmbeds(statsEmbed(jda, author)).mentionRepliedUser(false).queue();
    }

    private MessageEmbed statsEmbed(JDA jda, User user) {
        EmbedBuilder statsEmbed = new EmbedBuilder();
        jda.retrieveUserById(PropertiesManager.getOwnerId()).submit()
                .thenAcceptAsync(botOwner -> {
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
                    statsEmbed.setTitle("Taboo");
                    statsEmbed.addField("Author:", botOwner.getAsTag(), true);
                    statsEmbed.addField("Source:", "[View source on GitHub](https://github.com/Taboo-dev/Taboo)", true);
                    statsEmbed.addField("Library:", "[JDA " + JDAInfo.VERSION + "](" + JDAInfo.GITHUB + ")", true);
                    statsEmbed.addField("Uptime:", DateDifference.timeSince(Instant.now().toEpochMilli() - startTime.toEpochMilli()), true);
                    statsEmbed.addField("Servers:", String.valueOf(jda.getGuildCache().size()), true);
                    statsEmbed.addField("CPU:", cpuName, true);
                    statsEmbed.addField("Cores:", String.valueOf(cores), true);
                    statsEmbed.addField("Threads:", String.valueOf(threads), true);
                    statsEmbed.addField("Total RAM:", ParseBytes.parseBytes(total), true);
                    statsEmbed.addField("Available RAM:", ParseBytes.parseBytes(available), true);
                    statsEmbed.addField("Used RAM:", ParseBytes.parseBytes(usedMemory), true);
                    statsEmbed.addField("OS:", String.valueOf(operatingSystem), true);
                    statsEmbed.setColor(0x9F90CF);
                    statsEmbed.setFooter("Requested by " + user.getAsTag(), user.getEffectiveAvatarUrl());
                });
        return statsEmbed.build();
    }

}
