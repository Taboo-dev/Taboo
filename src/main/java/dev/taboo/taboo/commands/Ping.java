package dev.taboo.taboo.commands;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.commands.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;

import java.time.Instant;

public class Ping extends SlashCommand {

    public Ping() {
        this.name = "ping";
        this.help = "Pings the bot to see if it's alive.";
        this.defaultEnabled = true;
        this.guildOnly = true;
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        User user = event.getUser();
        JDA jda = event.getJDA();
        InteractionHook hook = event.getHook();
        event.deferReply(true).queue();
        hook.sendMessageEmbeds(initialPingEmbed(user)).mentionRepliedUser(false).queue(m -> {
            jda.getRestPing().queue(restPing -> {
                long gatewayPing = jda.getGatewayPing();
                m.editMessageEmbeds(finalPingEmbed(user, restPing, gatewayPing)).queue();
            });
        });
    }

    @Override
    protected void execute(CommandEvent event) {
        User author = event.getAuthor();
        JDA jda = event.getJDA();
        Message message = event.getMessage();
        message.replyEmbeds(initialPingEmbed(author)).mentionRepliedUser(false).queue(m -> {
            jda.getRestPing().queue(restPing -> {
                long gatewayPing = jda.getGatewayPing();
                m.editMessageEmbeds(finalPingEmbed(author, restPing, gatewayPing)).queue();
            });
        });
    }

    private MessageEmbed initialPingEmbed(User user) {
        return new EmbedBuilder()
                .setTitle("Ping")
                .setDescription("Pong! Shows the bot's ping!")
                .setColor(0x9F90CF)
                .setFooter("Requested by " + user.getAsTag(), user.getEffectiveAvatarUrl())
                .setTimestamp(Instant.now())
                .build();
    }

    private MessageEmbed finalPingEmbed(User user, long restPing, long gatewayPing) {
        return new EmbedBuilder()
                .setTitle("Ping")
                .setDescription(String.format(
                        """
                        Pong! Showing the bot's ping!
                        Gateway Ping: `%s ms`
                        Rest Ping: `%s ms`
                        """, gatewayPing, restPing))
                .setColor(0x9F90CF)
                .setFooter("Requested by " + user.getAsTag(), user.getEffectiveAvatarUrl())
                .setTimestamp(Instant.now())
                .build();
    }


}
