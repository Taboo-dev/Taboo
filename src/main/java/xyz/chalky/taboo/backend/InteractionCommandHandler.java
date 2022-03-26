package xyz.chalky.taboo.backend;

import mu.KotlinLogging;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.slf4j.Logger;
import xyz.chalky.taboo.Taboo;
import xyz.chalky.taboo.commands.*;
import xyz.chalky.taboo.util.PropertiesManager;
import xyz.chalky.taboo.util.ResponseHelper;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class InteractionCommandHandler {

    private final PropertiesManager propertiesManager;
    private final static List<GenericCommand> registeredCommands = new ArrayList<>();
    private final static ConcurrentHashMap<Long, List<GenericCommand>> registeredGuildCommands = new ConcurrentHashMap<>();
    private CommandListUpdateAction commandUpdateAction;
    private static final Logger LOGGER = KotlinLogging.INSTANCE.logger("InteractionCommandHandler");

    public InteractionCommandHandler(PropertiesManager propertiesManager) {
        this.propertiesManager = propertiesManager;
    }

    public void initialize() {
        commandUpdateAction = Taboo.getInstance().getShardManager().getShards().get(0).updateCommands();
        registerAllCommands();
    }

    private void registerAllCommands() {
        registerCommand(new InfoSlashCommand());
        registerCommand(new PingSlashCommand());
        registerCommand(new PlaySlashCommand(propertiesManager));
        registerCommand(new ConfigSlashCommand());
        registerCommand(new BookmarkContextMenuMessageCommand());
    }

    public void updateCommands(Consumer<List<Command>> success, Consumer<Throwable> failure) {
        if (!Taboo.getInstance().isDebug()) {
            commandUpdateAction.queue(success, failure);

            for (Map.Entry<Long, List<GenericCommand>> entrySet : registeredGuildCommands.entrySet()) {
                Long guildId = entrySet.getKey();
                List<GenericCommand> slashCommands = entrySet.getValue();
                if (guildId == null || slashCommands == null) continue;
                if (slashCommands.isEmpty()) continue;
                Guild guild = Taboo.getInstance().getShardManager().getGuildById(guildId);
                if (guild == null) continue;
                CommandListUpdateAction guildCommandUpdateAction = guild.updateCommands();
                for (GenericCommand cmd : slashCommands) {
                    guildCommandUpdateAction = guildCommandUpdateAction.addCommands(cmd.getData());
                }
                if (slashCommands.size() > 0) guildCommandUpdateAction.queue();
            }
        } else {
            List<GenericCommand> commands = registeredGuildCommands.get(propertiesManager.getGuildId());
            if (commands != null && !commands.isEmpty()) {
                Guild guild = Taboo.getInstance().getShardManager().getGuildById(propertiesManager.getGuildId());
                if (guild == null) return;
                CommandListUpdateAction commandListUpdateAction = guild.updateCommands();
                for (GenericCommand cmd : commands) {
                    commandListUpdateAction.addCommands(cmd.getData());
                }
                commandListUpdateAction.queue(success, failure);
            }
        }
    }

    private void registerCommand(GenericCommand command) {
        if (!command.isGlobal() && !Taboo.getInstance().isDebug()) {
            if (command.getEnabledGuilds().isEmpty()) return;
            for (Long guildId : command.getEnabledGuilds()) {
                Guild guild = Taboo.getInstance().getShardManager().getGuildById(guildId);
                if (guild == null) continue;
                List<GenericCommand> alreadyRegistered = registeredGuildCommands.containsKey(guildId) ? registeredGuildCommands.get(guildId) : new ArrayList<>();
                alreadyRegistered.add(command);
                registeredGuildCommands.put(guildId, alreadyRegistered);
            }
            return;
        }
        if (Taboo.getInstance().isDebug()) {
            Guild guild = Taboo.getInstance().getShardManager().getGuildById(propertiesManager.getGuildId());
            if (guild != null) {
                List<GenericCommand> alreadyRegistered = registeredGuildCommands.containsKey(propertiesManager.getGuildId()) ? registeredGuildCommands.get(propertiesManager.getGuildId()) : new ArrayList<>();
                alreadyRegistered.add(command);
                registeredGuildCommands.put(propertiesManager.getGuildId(), alreadyRegistered);
            }
            return;
        }
        commandUpdateAction.addCommands(command.getData());
        registeredCommands.add(command);
        LOGGER.info("Registered commands");
    }

    public void handleSlashCommand(SlashCommandInteractionEvent event, Member member) {
        Runnable r = () -> {
            try {
                if (!event.isFromGuild()) return;
                Guild guild = event.getGuild();
                SlashCommand command = null;
                long guildId = guild.getIdLong();
                if (registeredGuildCommands.containsKey(guildId)) {
                    List<SlashCommand> guildCommands = registeredGuildCommands.get(guildId)
                            .stream()
                            .filter(cmd -> cmd instanceof SlashCommand)
                            .map(cmd -> (SlashCommand) cmd)
                            .toList();
                    SlashCommand guildCommand = guildCommands.stream()
                            .filter(cmd -> cmd.getCommandName().equalsIgnoreCase(event.getName()))
                            .findFirst()
                            .orElse(null);
                    if (guildCommand != null) command = guildCommand;
                }
                if (command == null) {
                    SlashCommand globalCommand = getRegisteredSlashCommands()
                            .stream()
                            .filter(cmd -> cmd.getCommandName().equalsIgnoreCase(event.getName()))
                            .findFirst()
                            .orElse(null);
                    if (globalCommand != null) command = globalCommand;
                }
                if (command != null) {
                    SlashCommandContext ctx = new SlashCommandContext(event);
                    List<Permission> neededPermissions = command.getRequiredUserPermissions();
                    List<Permission> neededBotPermissions = command.getRequiredBotPermissions();
                    if (neededPermissions != null && !member.hasPermission((GuildChannel) event.getChannel(), neededPermissions)) {
                        event.replyEmbeds(ResponseHelper.createEmbed(null, "You do not have the required permissions to use this command!", Color.RED, event.getUser()).build()).queue();
                        return;
                    }
                    if (neededBotPermissions != null && !guild.getSelfMember().hasPermission((GuildChannel) event.getChannel(), neededBotPermissions)) {
                        event.replyEmbeds(ResponseHelper.createEmbed(null, "I do not have the required permissions to use this command!", Color.RED, event.getUser()).build()).queue();
                        return;
                    }
                    if (command.getCommandFlags().contains(CommandFlag.MUST_BE_IN_VC)) {
                        GuildVoiceState guildVoiceState = member.getVoiceState();
                        if (guildVoiceState == null || !guildVoiceState.inAudioChannel()) {
                            event.replyEmbeds(ResponseHelper.createEmbed(null, "You must be in a voice channel to use this command!", Color.RED, event.getUser()).build()).queue();
                            return;
                        }
                    }
                    if (command.getCommandFlags().contains(CommandFlag.MUST_BE_IN_SAME_VC)) {
                        GuildVoiceState guildVoiceState = member.getVoiceState();
                        AudioManager audioManager = event.getGuild().getAudioManager();
                        if (guildVoiceState == null || guildVoiceState.getChannel() == null || !audioManager.getConnectedChannel().equals(guildVoiceState.getChannel())) {
                            event.replyEmbeds(ResponseHelper.createEmbed(null, "You must be in the same voice channel as me to use this command!", Color.RED, event.getUser()).build()).queue();
                            return;
                        }
                    }
                    event.deferReply().queue();
                    command.executeCommand(event, member, ctx);
                }
            } catch (Exception e) {
                if (event.isAcknowledged()) {
                    event.getHook().sendMessage("An error occurred while handling the command!").queue();
                    LOGGER.error("Could not execute slash-command!", e);
                } else {
                    event.reply("An error occurred while handling the command!").queue();
                    LOGGER.error("Could not execute slash-command!", e);
                }
            }
        };
        Taboo.getInstance().getCommandExecutor().execute(r);
    }

    public void handleAutoComplete(CommandAutoCompleteInteractionEvent event) {
        if (!event.isFromGuild()) return;
        Runnable r = () -> {
            try {
                SlashCommand command = null;
                long guildId = event.getGuild().getIdLong();
                if (registeredGuildCommands.containsKey(guildId)) {
                    List<SlashCommand> guildCommands = registeredGuildCommands.get(guildId)
                            .stream()
                            .filter(cmd -> cmd instanceof SlashCommand)
                            .map(cmd -> (SlashCommand) cmd)
                            .toList();
                    SlashCommand guildCommand = guildCommands.stream()
                            .filter(cmd -> cmd.getCommandName().equalsIgnoreCase(event.getName()))
                            .findFirst()
                            .orElse(null);
                    if (guildCommand != null) command = guildCommand;
                }
                if (command == null) {
                    SlashCommand globalCommand = getRegisteredSlashCommands()
                            .stream()
                            .filter(cmd -> cmd instanceof SlashCommand)
                            .map(cmd -> (SlashCommand) cmd)
                            .filter(cmd -> cmd.getCommandName().equalsIgnoreCase(event.getName()))
                            .findFirst()
                            .orElse(null);
                    if (globalCommand != null) command = globalCommand;
                }
                command.handleAutoComplete(event);
            } catch (Exception e) {
                LOGGER.error("An error occurred while handling autocomplete!", e);
                event.replyChoices(Collections.emptyList()).queue();
            }
        };
        Taboo.getInstance().getCommandExecutor().execute(r);
    }

    public void handleMessageContextCommand(MessageContextInteractionEvent event) {
        if (!event.isFromGuild()) return;
        Guild guild = event.getGuild();
        long guildId = guild.getIdLong();
        Member member = event.getMember();
        MessageContextCommand command = null;
        if (registeredGuildCommands.containsKey(guildId)) {
            List<MessageContextCommand> guildCommands = registeredGuildCommands.get(guildId)
                    .stream()
                    .filter(cmd -> cmd instanceof MessageContextCommand)
                    .map(cmd -> (MessageContextCommand) cmd)
                    .toList();
            MessageContextCommand guildCommand = guildCommands.stream()
                    .filter(cmd -> cmd.getCommandName().equalsIgnoreCase(event.getName()))
                    .findFirst()
                    .orElse(null);
            if (guildCommand != null) command = guildCommand;
        }
        if (command == null) {
            MessageContextCommand globalCommand = getRegisteredMessageContextCommands()
                    .stream()
                    .filter(cmd -> cmd.getData().getName().equalsIgnoreCase(event.getName()))
                    .findFirst()
                    .orElse(null);
            if (globalCommand != null) command = globalCommand;
            if (command == null) return;
            List<Permission> neededPermissions = command.getRequiredUserPermissions();
            List<Permission> neededBotPermissions = command.getRequiredBotPermissions();
            if (neededPermissions != null && !member.hasPermission((GuildChannel) event.getChannel(), neededPermissions)) {
                event.reply("You don't have the required permissions to use this command.").queue();
                return;
            }
            if (neededBotPermissions != null && !guild.getSelfMember().hasPermission((GuildChannel) event.getChannel(), neededBotPermissions)) {
                event.reply("I don't have the required permissions to use this command.").queue();
                return;
            }
            if (command.getCommandFlags().contains(CommandFlag.MUST_BE_IN_VC)) {
                GuildVoiceState guildVoiceState = member.getVoiceState();
                if (guildVoiceState == null || !guildVoiceState.inAudioChannel()) {
                    event.reply("You must be in a voice channel to use this command!").queue();
                    return;
                }
            }
            if (command.getCommandFlags().contains(CommandFlag.MUST_BE_IN_SAME_VC)) {
                GuildVoiceState guildVoiceState = member.getVoiceState();
                AudioManager audioManager = event.getGuild().getAudioManager();
                if (guildVoiceState == null || guildVoiceState.getChannel() == null || !audioManager.getConnectedChannel().equals(guildVoiceState.getChannel())) {
                    event.reply("You must be in the same voice channel as me to use this command!").queue();
                    return;
                }
            }
            MessageContextCommand finalCommand = command;
            Runnable r = () -> {
                try {
                    finalCommand.executeCommand(event);
                } catch (Exception e) {
                    LOGGER.error("An error occurred while executing a message context command!", e);
                    event.reply("An error occurred while executing this command!").queue();
                }
            };
            Taboo.getInstance().getCommandExecutor().submit(r);
        }
    }

    private List<SlashCommand> getRegisteredSlashCommands() {
        return registeredCommands.stream()
            .filter(cmd -> cmd instanceof SlashCommand)
            .map(cmd -> (SlashCommand) cmd)
            .toList();
    }

    private List<MessageContextCommand> getRegisteredMessageContextCommands() {
        return registeredCommands.stream()
            .filter(cmd -> cmd instanceof MessageContextCommand)
            .map(cmd -> (MessageContextCommand) cmd)
            .toList();
    }

    public static ConcurrentHashMap<Long, List<GenericCommand>> getRegisteredGuildCommands() {
        return registeredGuildCommands;
    }

    public static List<GenericCommand> getRegisteredCommands() {
        return registeredCommands;
    }

}
