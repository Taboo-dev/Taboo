package dev.taboo.taboo.commands;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.commands.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;

import java.time.Instant;

public class Invite extends SlashCommand {

    public Invite() {
        this.name = "invite";
        this.aliases = new String[] { "inv", "invite" };
        this.help = "Invite the bot to your server!";
        this.guildOnly = true;
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        User user = event.getUser();
        InteractionHook hook = event.getHook();
        event.deferReply(true).queue();
        hook.sendMessageEmbeds(inviteEmbed(user)).mentionRepliedUser(false).setEphemeral(false).queue();
    }

    @Override
    protected void execute(CommandEvent event) {
        User author = event.getAuthor();
        Message message = event.getMessage();
        message.replyEmbeds(inviteEmbed(author)).mentionRepliedUser(false).queue();
    }

    private MessageEmbed inviteEmbed(User user) {
        return new EmbedBuilder()
                .setTitle("Invite the bot to your server!")
                .setDescription(
                        "[Click here to invite the bot to your server!]" +
                        "(https://discord.com/api/oauth2/authorize?client_id=892077333878566962&permissions=8&scope=bot%20applications.commands)"
                ).setColor(0x9F90CF)
                .setFooter("Requested by " + user.getAsTag(), user.getEffectiveAvatarUrl())
                .setTimestamp(Instant.now())
                .build();
    }

}
