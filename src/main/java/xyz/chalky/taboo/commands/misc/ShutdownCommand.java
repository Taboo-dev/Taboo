package xyz.chalky.taboo.commands.misc;

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

    public static final MessageEmbed shutdownEmbed = new EmbedBuilder()
            .setTitle("Shutting down Taboo...")
            .setColor(Color.RED)
            .setTimestamp(Instant.now())
            .build();

    public ShutdownCommand() {
        super("shutdown", "Shuts down the bot", "shutdown");
        addCommandFlags(CommandFlag.DEVELOPER_ONLY, CommandFlag.PRIVATE);
        addAllowedGuilds(Taboo.getInstance().getConfig().getGuildId());
    }

    @Override
    public void executeCommand(@NotNull MessageReceivedEvent event, CommandContext ctx) {
        ScheduledExecutorService executor = Taboo.getInstance().getScheduledExecutor();
        event.getMessage().replyEmbeds(shutdownEmbed).queue();
        Taboo.getLogger().info("Shutting down Taboo...");
        Taboo.getInstance().getShardManager().shutdown();
        Taboo.getLogger().info("Goodbye!");
        executor.schedule(() -> {
            System.exit(0);
        }, 10, TimeUnit.SECONDS);
    }

}
