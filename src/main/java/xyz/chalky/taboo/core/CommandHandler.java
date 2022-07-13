package xyz.chalky.taboo.core;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.chalky.taboo.central.Taboo;
import xyz.chalky.taboo.commands.misc.ShutdownCommand;
import xyz.chalky.taboo.config.TabooConfigProperties;

import java.awt.*;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;

public class CommandHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandHandler.class);
    private final ConcurrentMap<String, Command> registeredCommands = new ConcurrentSkipListMap<>(String.CASE_INSENSITIVE_ORDER);
    private TabooConfigProperties config;

    public CommandHandler() {
        this.config = Taboo.getInstance().getConfig();
        registerCommand(new ShutdownCommand());
    }

    private void registerCommand(@NotNull Command command) {
        String name = command.getName();
        if (registeredCommands.containsKey(name)) {
            LOGGER.error("Command \"{}\" could not be registered because a command (or alias) with this name already exists!", name);
            return;
        }
        registeredCommands.put(name, command);
        List<String> commandAliases = command.getAliases();
        for (String alias : commandAliases) {
            if (registeredCommands.containsKey(alias))
                LOGGER.error("Alias \"{}\" could not be registered because a command (or alias) with this name already exists!", alias);
            else {
                registeredCommands.put(alias, command);
            }
        }
    }

    public List<Command> getRegisteredCommands() {
        return registeredCommands.values().stream().distinct().toList();
    }

    public List<Command> getRegisteredCommands(long guildId) {
        return registeredCommands.values().stream().distinct()
                .filter(command -> command.isAvailableIn(guildId)).toList();
    }

    public List<Command> getGuildCommands(long guildId) {
        return registeredCommands.values().stream().distinct()
                .filter(command -> command.hasCommandFlag(CommandFlag.PRIVATE) && command.getAllowedGuilds().contains(guildId)).toList();
    }

    public void handleCommand(@NotNull MessageReceivedEvent event) {
        Taboo.getInstance().getCommandExecutor().submit(() -> {
            try {
                CommandArgument arguments = new CommandArgument(event.getMessage().getContentRaw(), config.getPrefix());
                String name = arguments.getCommandName();
                if (!registeredCommands.containsKey(name)) {
                    return;
                }
                Command command = registeredCommands.get(name);
                if (command.hasCommandFlag(CommandFlag.PRIVATE)) {
                    if (!command.isAvailableIn(event.getGuild().getIdLong())) {
                        return;
                    }
                }
                if (command.hasCommandFlag(CommandFlag.DEVELOPER_ONLY)) {
                    long id = event.getMember().getIdLong();
                    if (!(id == config.getOwnerId())) {
                        return;
                    }
                }
                CommandContext context = new CommandContext(event, command, event.getMember(), arguments);
                command.executeCommand(event, context);
            } catch (Exception e) {
                LOGGER.error("An error occurred while executing a command", e);
                MessageEmbed embed = new EmbedBuilder()
                        .setTitle("An error occurred while executing a command")
                        .setColor(Color.RED)
                        .build();
                event.getMessage().replyEmbeds(embed).queue();
            }
        });
    }

}
