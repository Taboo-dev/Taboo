package xyz.chalky.taboo;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import lavalink.client.io.jda.JdaLavalink;
import mu.KotlinLogging;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.slf4j.Logger;
import xyz.chalky.taboo.backend.GenericCommand;
import xyz.chalky.taboo.backend.InteractionCommandHandler;
import xyz.chalky.taboo.database.DatabaseManager;
import xyz.chalky.taboo.events.EventManager;
import xyz.chalky.taboo.util.PropertiesManager;

import java.io.FileInputStream;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Taboo {

    private static final Logger LOGGER = KotlinLogging.INSTANCE.logger("Taboo");
    private static Taboo instance;
    private final ShardManager shardManager;
    private final boolean isDebug;

    private final ExecutorService commandExecutor =
            Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(),
                    new ThreadFactoryBuilder()
                            .setNameFormat("Taboo Command Thread %d")
                            .setUncaughtExceptionHandler((thread, throwable) -> {
                                LOGGER.error("An uncaught error occurred on the command thread-pool! (Thread {})", thread.getName(), throwable);
                            }).build());

    private final ScheduledExecutorService scheduledExecutor =
            Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder()
                    .setNameFormat("Taboo Scheduled Executor Thread")
                    .setUncaughtExceptionHandler((thread, throwable) -> {
                        LOGGER.error("An uncaught error occurred on the scheduled executor!", throwable);
                    }).build());

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor(new ThreadFactoryBuilder()
                    .setNameFormat("Taboo Executor Thread")
                    .setUncaughtExceptionHandler((thread, throwable) -> {
                        LOGGER.error("An uncaught error occurred on the executor!", throwable);
                    }).build());

    private final InteractionCommandHandler interactionCommandHandler;
    private final EventWaiter eventWaiter;
    private final JdaLavalink lavalink;

    Taboo() throws Exception {
        instance = this;
        Properties properties = new Properties();
        properties.load(new FileInputStream("config.properties"));
        PropertiesManager propertiesManager = new PropertiesManager(properties);
        interactionCommandHandler = new InteractionCommandHandler(propertiesManager);
        isDebug = propertiesManager.getDebugState();
        eventWaiter = new EventWaiter();
        lavalink = new JdaLavalink(null, 1, null);
        EventManager eventManager = new EventManager(propertiesManager);
        eventManager.init();
        DatabaseManager.INSTANCE.startDatabase();
        shardManager = DefaultShardManagerBuilder.createDefault(propertiesManager.getToken())
                .enableIntents(GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.GUILD_EMOJIS)
                .enableCache(CacheFlag.VOICE_STATE)
                .setShardsTotal(-1)
                .setStatus(OnlineStatus.ONLINE)
                .setVoiceDispatchInterceptor(lavalink.getVoiceInterceptor())
                .setEventManagerProvider(i -> eventManager)
                .build();
    }

    public static Taboo getInstance() {
        return instance;
    }

    public ExecutorService getExecutor() {
        return executor;
    }

    public ExecutorService getCommandExecutor() {
        return commandExecutor;
    }

    public ScheduledExecutorService getScheduledExecutor() {
        return scheduledExecutor;
    }

    public boolean isDebug() {
        return isDebug;
    }

    public ShardManager getShardManager() {
        return shardManager;
    }

    public InteractionCommandHandler getInteractionCommandHandler() {
        return interactionCommandHandler;
    }

    public EventWaiter getEventWaiter() {
        return eventWaiter;
    }

    public JdaLavalink getLavalink() {
        return lavalink;
    }

    public static Logger getLogger() {
        return LOGGER;
    }

    public void initCommandCheck(PropertiesManager propertiesManager) {
        LOGGER.info("Checking for outdated command cache...");
        getCommandExecutor().submit(() -> {
            if (getInstance().isDebug()) {
                Guild guild = getInstance().getShardManager().getGuildById(propertiesManager.getGuildId());
                if (guild == null) {
                    LOGGER.error("Debug guild does not exist!");
                    return;
                }
                guild.retrieveCommands().queue(discordCommands -> {
                    List<GenericCommand> localCommands = getInstance().getInteractionCommandHandler()
                            .getRegisteredGuildCommands()
                            .get(guild.getIdLong());
                    handleCommandUpdates(discordCommands, localCommands);
                });
                return;
            }
            getInstance().getShardManager().getShards().get(0).retrieveCommands().queue(discordCommands -> {
                List<GenericCommand> localCommands = getInstance().getInteractionCommandHandler()
                        .getRegisteredCommands()
                        .stream()
                        .filter(GenericCommand::isGlobal)
                        .toList();
                handleCommandUpdates(discordCommands, localCommands);
            });
        });
    }

    private static void handleCommandUpdates(Collection<? extends Command> discordCommands, Collection<? extends GenericCommand> localCommands) {
        boolean commandRemovedOrAdded = localCommands.size() != discordCommands.size();
        if (commandRemovedOrAdded) {
            if (localCommands.size() > discordCommands.size()) {
                LOGGER.warn("New command(s) has/have been added! Updating Discord's cache...");
            } else {
                LOGGER.warn("Command(s) has/have been removed! Updating Discord's cache...");
            }
            getInstance().getInteractionCommandHandler().updateCommands(commands -> {
                LOGGER.info("Updated {} commands!", commands.size());
            }, throwable -> {});
            return;
        }
        boolean outdated = false;
        for (GenericCommand localCommand : localCommands) {
            Command discordCommand = discordCommands.stream()
                    .filter(command -> command.getName().equalsIgnoreCase(localCommand.getData().getName()))
                    .findFirst()
                    .orElse(null);
            CommandData localCommandData = localCommand.getData();
            CommandData discordCommandData = CommandData.fromCommand(discordCommand);
            if (!localCommandData.equals(discordCommandData)) {
                outdated = true;
                break;
            }
        }
        if (outdated) {
            getInstance().getInteractionCommandHandler().updateCommands(commands -> {
                LOGGER.info("Updated {} commands!", commands.size());
            }, throwable -> {});
        } else {
            LOGGER.info("No outdated commands found!");
        }
    }

}
