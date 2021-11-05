package dev.taboo.taboo.commands;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.commands.SlashCommandEvent;

import java.time.Instant;

public class Support extends SlashCommand {

    public Support() {
        this.name = "support";
        this.aliases = new String[] { "server" };
        this.help = "Get support for Taboo.";
        this.guildOnly = true;
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        User user = event.getUser();
        event.replyEmbeds(supportEmbed(user)).mentionRepliedUser(false).setEphemeral(false).queue();
    }

    @Override
    protected void execute(CommandEvent event) {
        User user = event.getAuthor();
        Message message = event.getMessage();
        message.replyEmbeds(supportEmbed(user)).mentionRepliedUser(false).queue();
    }

    private MessageEmbed supportEmbed(User user) {
        return new EmbedBuilder()
                .setTitle("Support")
                .setDescription(
                        "If you need help with Taboo, please join the support server.\n" +
                        "https://discord.gg/XWQWX2X" // TODO: Add support server link
                ).setColor(0x9F90CF)
                .setFooter("Requested by " + user.getAsTag(), user.getEffectiveAvatarUrl())
                .setTimestamp(Instant.now())
                .build();
    }

}
