package dev.taboo.taboo.commands;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.awt.*;
import java.time.Instant;

public class Ping extends SlashCommand {

    public Ping() {
        this.name = "ping";
        this.help = "Pings the bot to se if it's alive.";
        this.defaultEnabled = true;
        this.guildOnly = true;
    }

    @Override
    public void execute(SlashCommandEvent event) {
        User user = event.getUser();
        JDA jda = event.getJDA();
        event.replyEmbeds(initialPingEmbed(user)).mentionRepliedUser(false).setEphemeral(false).queue(hook -> {
            jda.getRestPing().queue(restPing -> {
                long gatewayPing = jda.getGatewayPing();
                hook.editOriginalEmbeds(finalPingEmbed(user, restPing, gatewayPing)).queue();
            });
        });
    }

    @Override
    protected void execute(CommandEvent event) {
        User user = event.getMember().getUser();
        JDA jda = event.getJDA();
        Message message = event.getMessage();
        message.replyEmbeds(initialPingEmbed(user)).mentionRepliedUser(false).queue(response -> {
            jda.getRestPing().queue(restPing -> {
                long gatewayPing = event.getJDA().getGatewayPing();
                response.editMessageEmbeds(finalPingEmbed(user, restPing, gatewayPing)).queue();
            });
        });
    }

    private MessageEmbed initialPingEmbed(User user) {
        return new EmbedBuilder()
                .setTitle("Ping!")
                .setDescription("`Pong!` Shows the bot's ping!")
                .setColor(Color.ORANGE)
                .setFooter("Requested by " + user.getAsTag(), user.getEffectiveAvatarUrl())
                .setTimestamp(Instant.now())
                .build();
    }

    private MessageEmbed finalPingEmbed(User user, long restPing, long gatewayPing) {
        return new EmbedBuilder()
                .setTitle("Ping!")
                .setDescription(String.format(
                        """
                        `Pong!` Showing the bot's ping!
                        Gateway Ping: `%s ms`
                        Rest Ping: `%s ms`
                        """, gatewayPing, restPing))
                .setColor(Color.ORANGE)
                .setFooter("Requested by " + user.getAsTag(), user.getEffectiveAvatarUrl())
                .setTimestamp(Instant.now())
                .build();
    }

}
