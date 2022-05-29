package xyz.chalky.taboo.commands.misc;

import club.minnced.discord.webhook.external.JDAWebhookClient;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import xyz.chalky.taboo.central.Taboo;
import xyz.chalky.taboo.core.Command;
import xyz.chalky.taboo.core.CommandContext;
import xyz.chalky.taboo.core.CommandFlag;

import java.awt.*;
import java.time.Instant;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ShutdownCommand extends Command {

    public ShutdownCommand() {
        super("shutdown", "Shuts down the bot", "shutdown");
        addCommandFlags(CommandFlag.DEVELOPER_ONLY, CommandFlag.PRIVATE);
        addAllowedGuilds(Taboo.getInstance().getConfig().getGuildId());
    }

    @Override
    public void executeCommand(@NotNull MessageReceivedEvent event, CommandContext ctx) {
        ScheduledExecutorService executor = Taboo.getInstance().getScheduledExecutor();
        JDAWebhookClient webhookClient = Taboo.getInstance().getWebhookClient();
        MessageEmbed embed = new EmbedBuilder()
                .setTitle("Shutting down Taboo...")
                .setColor(Color.RED)
                .setTimestamp(Instant.now())
                .build();
        event.getMessage().replyEmbeds(embed).queue();
        Taboo.getLogger().info("Shutting down Taboo...");
        Taboo.getInstance().getShardManager().shutdown();
        Taboo.getLogger().info("Goodbye!");
        webhookClient.send(embed);
        executor.schedule(() -> {
            System.exit(0);
        }, 10, TimeUnit.SECONDS);
    }

}
