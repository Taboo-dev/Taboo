package xyz.chalky.taboo.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import xyz.chalky.taboo.backend.SlashCommand;
import xyz.chalky.taboo.backend.SlashCommandContext;

import java.time.Instant;

public class PingSlashCommand extends SlashCommand {

    public PingSlashCommand() {
        setCommandData(Commands.slash("ping", "Pong!"));
    }

    @Override
    public void executeCommand(SlashCommandInteractionEvent event, Member sender, SlashCommandContext ctx) {
        JDA jda = event.getJDA();
        User user = sender.getUser();
        InteractionHook hook = event.getHook();
        EmbedBuilder pingEmbed = new EmbedBuilder()
                .setTitle("Ping!")
                .setDescription("Pong! Shows the bot's ping!")
                .setColor(0x9F90CF)
                .setFooter("Requested by " + user.getAsTag(), user.getEffectiveAvatarUrl())
                .setTimestamp(Instant.now());
        hook.sendMessageEmbeds(pingEmbed.build()).queue(msg -> {
            jda.getRestPing().queue(restPing -> {
                long gatewayPing = jda.getGatewayPing();
                pingEmbed.setDescription(String.format(
                        """
                        Pong! Showing the bot's ping!
                        Rest Ping: %s ms
                        Gateway Ping: %s ms
                        """
                , restPing, gatewayPing));
                msg.editMessageEmbeds(pingEmbed.build()).queue();
            });
        });
    }

}
