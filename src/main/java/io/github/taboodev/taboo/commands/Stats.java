package io.github.taboodev.taboo.commands;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import io.github.taboodev.taboo.util.Constants;
import io.github.taboodev.taboo.util.DateDifference;
import io.github.taboodev.taboo.util.ParseBytes;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDAInfo;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.time.Instant;

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
        Runtime runtime = Runtime.getRuntime();
        long memory = runtime.totalMemory() - runtime.freeMemory();
        String memoryUsage = ParseBytes.parseBytes(memory / 1024) + "/" + ParseBytes.parseBytes(runtime.totalMemory() / 1024);
        User owner = jda.getUserById(Constants.OWNER_ID);
        return new EmbedBuilder()
                .setTitle("Taboo")
                .addField("Author:", owner.getAsTag(), true)
                .addField("Source:", "[View source on GitHub](https://github.com/Taboo-dev/Taboo)", true)
                .addField("Library:", "[JDA " + JDAInfo.VERSION + "](" + JDAInfo.GITHUB + ")", true)
                .addField("Uptime:", DateDifference.timeSince(Instant.now().toEpochMilli() - startTime.toEpochMilli()), true)
                .addField("Servers:", String.valueOf(jda.getGuildCache().size()), true)
                .addField("Memory:", memoryUsage, true)
                .setColor(0x9F90CF)
                .setFooter("Requested by " + user.getAsTag(), user.getEffectiveAvatarUrl())
                .build();
    }

}
