package xyz.chalky.taboo.commands.misc;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import xyz.chalky.taboo.core.SlashCommand;

import java.time.Instant;

@Component
public class PingSlashCommand extends SlashCommand {

    public PingSlashCommand() {
        setCommandData(Commands.slash("ping", "Pong!"));
        setEphemeral(true);
    }

    @Override
    public void executeCommand(@NotNull SlashCommandInteractionEvent event) {
        JDA jda = event.getJDA();
        User user = event.getUser();
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Ping!")
                .setDescription("Pong! Shows the bot's ping!")
                .setColor(0x9F90CF)
                .setFooter(String.format("Requested by %s", user.getAsTag()), user.getEffectiveAvatarUrl())
                .setTimestamp(Instant.now());
        event.getHook().sendMessageEmbeds(embed.build()).queue(msg -> {
            jda.getRestPing().queue(restPing -> {
                long gatewayPing = jda.getGatewayPing();
                embed.setDescription(String.format("""
                        Pong!
                        Rest Ping: %s ms
                        Gateway Ping: %s ms
                        """, restPing, gatewayPing));
                msg.editMessageEmbeds(embed.build()).queue();
            });
        });
    }

}
