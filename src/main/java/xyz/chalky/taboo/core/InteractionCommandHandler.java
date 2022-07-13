package xyz.chalky.taboo.core;

import io.sentry.Breadcrumb;
import io.sentry.Sentry;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import xyz.chalky.taboo.central.Application;
import xyz.chalky.taboo.central.Taboo;
import xyz.chalky.taboo.config.TabooConfigProperties;
import xyz.chalky.taboo.database.repository.ConfigRepository;
import xyz.chalky.taboo.util.ResponseHelper;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static xyz.chalky.taboo.util.Constants.SUPPORT_SERVER_URL;

@Component
public class InteractionCommandHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(InteractionCommandHandler.class);
    private final List<GenericCommand> registeredCommands;
    private final ConcurrentHashMap<Long, List<GenericCommand>> registeredGuildCommands;
    private CommandListUpdateAction commandUpdateAction;
    private final TabooConfigProperties config;
    private final ApplicationContext context;
    private final ConfigRepository configRepository;

    public InteractionCommandHandler(ConfigRepository configRepository) {
        this.registeredCommands = Collections.synchronizedList(new ArrayList<>());
        this.registeredGuildCommands = new ConcurrentHashMap<>();
        this.config = Taboo.getInstance().getConfig();
        this.context = Application.getProvider().getApplicationContext();
        this.configRepository = configRepository;
    }

    public void initialize() {
        commandUpdateAction = Taboo.getInstance().getShardManager().getShards().get(0).updateCommands();
        registerAllCommands(this.context);
    }

    public void registerAllCommands(@NotNull ApplicationContext context) {
        context.getBeansOfType(GenericCommand.class).values().forEach(this::registerCommand);
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
                for (GenericCommand command : slashCommands) {
                    guildCommandUpdateAction = guildCommandUpdateAction.addCommands(command.getData());
                }
                if (slashCommands.size() > 0) guildCommandUpdateAction.queue();
            }
        } else {
            List<GenericCommand> commands = registeredGuildCommands.get(config.getGuildId());
            if (commands != null && !commands.isEmpty()) {
                Guild guild = Taboo.getInstance().getShardManager().getGuildById(config.getGuildId());
                if (guild == null) return;
                CommandListUpdateAction commandListUpdateAction = guild.updateCommands();
                for (GenericCommand command : commands)
                    commandListUpdateAction.addCommands(command.getData());
                commandListUpdateAction.queue();
            }
        }
    }

    private void registerCommand(@NotNull GenericCommand command) {
        if (!command.isGlobal() && !Taboo.getInstance().isDebug()) {
            if (command.getEnabledGuilds() == null || command.getEnabledGuilds().isEmpty()) return;
            for (Long guildId : command.getEnabledGuilds()) {
                Guild guild = Taboo.getInstance().getShardManager().getGuildById(guildId);
                if (guild == null) continue;
                List<GenericCommand> alreadyRegistered = registeredGuildCommands.containsKey(guildId) ?
                        registeredGuildCommands.get(guildId) : new ArrayList<>();
                alreadyRegistered.add(command);
                registeredGuildCommands.put(guildId, alreadyRegistered);
            }
            return;
        }
        if (Taboo.getInstance().isDebug()) {
            Guild guild = Taboo.getInstance().getShardManager().getGuildById(config.getGuildId());
            if (guild != null) {
                List<GenericCommand> alreadyRegistered = registeredGuildCommands.containsKey(config.getGuildId()) ?
                        registeredGuildCommands.get(config.getGuildId()) : new ArrayList<>();
                alreadyRegistered.add(command);
                registeredGuildCommands.put(config.getGuildId(), alreadyRegistered);
            }
            return;
        }
        commandUpdateAction.addCommands(command.getData());
        registeredCommands.add(command);
    }

    public void handleAutoComplete(@NotNull CommandAutoCompleteInteractionEvent event) {
        if (event.getGuild() == null) return;
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
                    SlashCommand guildCommand = guildCommands
                            .stream()
                            .filter(cmd -> cmd.getCommandName().equalsIgnoreCase(event.getName()))
                            .findFirst()
                            .orElse(null);
                    if (guildCommand != null)
                        command = guildCommand;
            }
            if (command == null) {
                SlashCommand globalCommand = registeredCommands
                        .stream()
                        .filter(cmd -> cmd instanceof SlashCommand)
                        .map(cmd -> (SlashCommand) cmd)
                        .filter(cmd -> cmd.getCommandName().equalsIgnoreCase(event.getName()))
                        .findFirst()
                        .orElse(null);
                if (globalCommand != null)
                    command = globalCommand;
            }
            if (command != null)
                command.handleAutoComplete(event);
            } catch (Exception e) {
                LOGGER.warn("Error while handling auto complete", e);
                event.replyChoices(Collections.emptyList()).queue();
            }
        };
        Taboo.getInstance().getCommandExecutor().execute(r);
    }

    public void handleMessageContextCommand(@NotNull MessageContextInteractionEvent event) {
        if (!event.isFromGuild()) return;
        Guild guild = event.getGuild();
        Member member = event.getMember();
        MessageContextCommand command = null;
        if (registeredGuildCommands.containsKey(guild.getIdLong())) {
            List<MessageContextCommand> guildCommands = registeredGuildCommands.get(guild.getIdLong())
                    .stream()
                    .filter(cmd -> cmd instanceof MessageContextCommand)
                    .map(cmd -> (MessageContextCommand) cmd)
                    .toList();
            MessageContextCommand guildCommand = guildCommands.stream().filter(cmd -> cmd.getData().getName().equalsIgnoreCase(event.getName()))
                    .findFirst()
                    .orElse(null);
            if (guildCommand != null)
                command = guildCommand;
        }
        if (command == null) {
            MessageContextCommand globalCommand = getRegisteredMessageContextCommands()
                    .stream()
                    .filter(cmd -> cmd.getData().getName().equalsIgnoreCase(event.getName()))
                    .findFirst()
                    .orElse(null);
            if (globalCommand != null)
                command = globalCommand;
        }
        if (command == null) return;
        List<Permission> neededPermissions = command.getRequiredUserPermissions();
        List<Permission> neededBotPermissions = command.getRequiredBotPermissions();
        if (neededPermissions != null && !member.hasPermission((GuildChannel) event.getChannel(), neededPermissions)) {
            EmbedBuilder embed = ResponseHelper.createEmbed(null, "You don't have the required permissions to execute this command.",
                    Color.RED, null);
            event.replyEmbeds(embed.build()).setEphemeral(true).queue();
            return;
        }
        if (neededBotPermissions != null && !event.getGuild().getSelfMember().hasPermission((GuildChannel) event.getChannel(), neededBotPermissions)) {
            EmbedBuilder embed = ResponseHelper.createEmbed(null, "I don't have the required permissions to execute this command.",
                    Color.RED, null);
            event.replyEmbeds(embed.build()).setEphemeral(true).queue();
            return;
        }
        if (command.getCommandFlags().contains(CommandFlag.DEVELOPER_ONLY)) {
            long idLong = event.getUser().getIdLong();
            if (idLong != config.getOwnerId()) {
                EmbedBuilder embed = ResponseHelper.createEmbed(null, "This command is only available for the developer.",
                        Color.RED, null);
                event.replyEmbeds(embed.build()).setEphemeral(true).queue();
                return;
            }
        }
        if (command.getCommandFlags().contains(CommandFlag.DISABLED)) {
            EmbedBuilder embed = ResponseHelper.createEmbed(null, "This command is currently disabled.",
                    Color.RED, null);
            event.replyEmbeds(embed.build()).setEphemeral(true).queue();
            return;
        }
        MessageContextCommand finalCommand = command;
        Runnable r = () -> {
            try {
                event.deferReply(finalCommand.isEphemeral()).queue();
                finalCommand.executeCommand(event);
            } catch (Exception e) {
                LOGGER.warn("Error while executing command", e);
                EmbedBuilder embed = ResponseHelper.createEmbed(null, "An error occurred while executing this command. " +
                                "This has automatically been reported to the developer.", Color.RED, null);
                ActionRow row = ActionRow.of(Button.link(SUPPORT_SERVER_URL, "Support Server"));
                Sentry.captureException(e, scope -> scope.addBreadcrumb(Breadcrumb.user("MessageContextCommand", "handleMessageContextCommand")));
                if (event.isAcknowledged()) {
                    event.getHook().sendMessageEmbeds(embed.build()).addActionRows(row).setEphemeral(true).queue();
                } else {
                    event.replyEmbeds(embed.build()).addActionRows(row).setEphemeral(true).queue();
                }
            }
        };
        Taboo.getInstance().getCommandExecutor().submit(r);
    }

    public void handleUserContextCommand(@NotNull UserContextInteractionEvent event) {
        if (!event.isFromGuild()) return;
        Guild guild = event.getGuild();
        Member member = event.getMember();
        UserContextCommand command = null;
        if (registeredGuildCommands.containsKey(guild.getIdLong())) {
            List<UserContextCommand> guildCommands = registeredGuildCommands.get(guild.getIdLong())
                    .stream()
                    .filter(cmd -> cmd instanceof UserContextCommand)
                    .map(cmd -> (UserContextCommand) cmd)
                    .toList();
            UserContextCommand guildCommand = guildCommands
                    .stream()
                    .filter(cmd -> cmd.getData().getName().equalsIgnoreCase(event.getName()))
                    .findFirst()
                    .orElse(null);
            if (guildCommand != null)
                command = guildCommand;
        }
        if (command == null) {
            UserContextCommand globalCommand = getRegisteredUserContextCommands()
                    .stream()
                    .filter(cmd -> cmd.getData().getName().equalsIgnoreCase(event.getName()))
                    .findFirst()
                    .orElse(null);
            if (globalCommand != null)
                command = globalCommand;
        }
        if (command == null)
            return;
        List<Permission> neededPermissions = command.getRequiredUserPermissions();
        List<Permission> neededBotPermissions = command.getRequiredBotPermissions();
        if (neededPermissions != null && !member.hasPermission((GuildChannel) event.getChannel(), neededPermissions)) {
            EmbedBuilder embed = ResponseHelper.createEmbed(null, "You don't have the required permissions to execute this command.",
                    Color.RED, null);
            event.replyEmbeds(embed.build()).setEphemeral(true).queue();
            return;
        }
        if (neededBotPermissions != null && !event.getGuild().getSelfMember().hasPermission((GuildChannel) event.getChannel(), neededBotPermissions)) {
            EmbedBuilder embed = ResponseHelper.createEmbed(null, "I don't have the required permissions to execute this command.",
                    Color.RED, null);
            event.replyEmbeds(embed.build()).setEphemeral(true).queue();
            return;
        }
        if (command.getCommandFlags().contains(CommandFlag.DEVELOPER_ONLY)) {
            long idLong = event.getUser().getIdLong();
            if (idLong != config.getOwnerId()) {
                EmbedBuilder embed = ResponseHelper.createEmbed(null, "This command is only available for the developer.",
                        Color.RED, null);
                event.replyEmbeds(embed.build()).setEphemeral(true).queue();
                return;
            }
        }
        if (command.getCommandFlags().contains(CommandFlag.DISABLED)) {
            EmbedBuilder embed = ResponseHelper.createEmbed(null, "This command is currently disabled.",
                    Color.RED, null);
            event.replyEmbeds(embed.build()).setEphemeral(true).queue();
            return;
        }
        UserContextCommand finalCommand = command;
        Runnable r = () -> {
            try {
                event.deferReply(finalCommand.isEphemeral()).queue();
                finalCommand.executeCommand(event);
            } catch (Exception e) {
                LOGGER.warn("An error occurred while executing a user context command.", e);
                EmbedBuilder embed = ResponseHelper.createEmbed(null, "An error occurred while executing this command. " +
                        "This has automatically been reported to the developer.", Color.RED, null);
                ActionRow row = ActionRow.of(Button.link(SUPPORT_SERVER_URL, "Support Server"));
                Sentry.captureException(e, scope -> scope.addBreadcrumb(Breadcrumb.user("UserContextCommand", "handleUserContextCommand")));
                if (event.isAcknowledged()) {
                    event.getHook().sendMessageEmbeds(embed.build()).addActionRows(row).setEphemeral(true).queue();
                } else {
                    event.replyEmbeds(embed.build()).addActionRows(row).setEphemeral(true).queue();
                }
            }
        };
        Taboo.getInstance().getCommandExecutor().submit(r);
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
                    SlashCommand guildCommand = guildCommands
                            .stream()
                            .filter(cmd -> cmd.getCommandName().equalsIgnoreCase(event.getName()))
                            .findFirst()
                            .orElse(null);
                    if (guildCommand != null)
                        command = guildCommand;
                }
                if (command == null) {
                    SlashCommand globalCommand = getRegisteredSlashCommands()
                            .stream()
                            .filter(cmd -> cmd.getCommandName().equalsIgnoreCase(event.getName()))
                            .findFirst()
                            .orElse(null);
                    if (globalCommand != null)
                        command = globalCommand;
                }
                if (command != null) {
                    List<Permission> neededPermissions = command.getRequiredUserPermissions();
                    List<Permission> neededBotPermissions = command.getRequiredBotPermissions();
                    if (neededPermissions != null && !member.hasPermission((GuildChannel) event.getChannel(), neededPermissions)) {
                        EmbedBuilder embed = ResponseHelper.createEmbed(null, "You don't have the required permissions to execute this command.",
                                Color.RED, null);
                        event.replyEmbeds(embed.build()).setEphemeral(true).queue();
                        return;
                    }
                    if (neededBotPermissions != null && !event.getGuild().getSelfMember().hasPermission((GuildChannel) event.getChannel(), neededBotPermissions)) {
                        EmbedBuilder embed = ResponseHelper.createEmbed(null, "I don't have the required permissions to execute this command.",
                                Color.RED, null);
                        event.replyEmbeds(embed.build()).setEphemeral(true).queue();
                        return;
                    }
                    if (command.getCommandFlags().contains(CommandFlag.MUSIC)) {
                        if (event.getChannelType().equals(ChannelType.VOICE)) {
                            GuildVoiceState guildVoiceState = member.getVoiceState();
                            if (guildVoiceState == null || !guildVoiceState.inAudioChannel()) {
                                EmbedBuilder embed = ResponseHelper.createEmbed(null, "You must be in a voice channel to execute this command.",
                                        Color.RED, null);
                                event.replyEmbeds(embed.build()).setEphemeral(true).queue();
                                return;
                            }
                            AudioManager manager = event.getGuild().getAudioManager();
                            if (manager.isConnected()) {
                                if (!manager.getConnectedChannel().equals(guildVoiceState.getChannel())) {
                                    EmbedBuilder embed = ResponseHelper.createEmbed(null, "You must be in the same voice channel as me to execute this command.",
                                            Color.RED, null);
                                    event.replyEmbeds(embed.build()).setEphemeral(true).queue();
                                    return;
                                }
                            }
                        } else {
                            EmbedBuilder embed = ResponseHelper.createEmbed(null, "You can only execute this command in voice channels.",
                                    Color.RED, null);
                            event.replyEmbeds(embed.build()).setEphemeral(true).queue();
                            return;
                        }
                    }
                    if (command.getCommandFlags().contains(CommandFlag.DEVELOPER_ONLY)) {
                        long idLong = event.getUser().getIdLong();
                        if (idLong != config.getOwnerId()) {
                            EmbedBuilder embed = ResponseHelper.createEmbed(null, "This command is only available for the developer.",
                                    Color.RED, null);
                            event.replyEmbeds(embed.build()).setEphemeral(true).queue();
                            return;
                        }
                    }
                    if (command.getCommandFlags().contains(CommandFlag.DISABLED)) {
                        EmbedBuilder embed = ResponseHelper.createEmbed(null, "This command is currently disabled.",
                                Color.RED, null);
                        event.replyEmbeds(embed.build()).setEphemeral(true).queue();
                        return;
                    }
                    event.deferReply(command.isEphemeral()).queue();
                    command.executeCommand(event);
                }
            } catch (Exception e) {
                LOGGER.warn("Error while executing slash command", e);
                EmbedBuilder embed = ResponseHelper.createEmbed(null, "An error occurred while executing this command. " +
                        "This has automatically been reported to the developer.", Color.RED, null);
                ActionRow row = ActionRow.of(Button.link(SUPPORT_SERVER_URL, "Support Server"));
                Sentry.captureException(e, scope -> scope.addBreadcrumb(Breadcrumb.user("SlashCommand", "handleSlashCommand")));
                if (event.isAcknowledged()) {
                    event.getHook().sendMessageEmbeds(embed.build()).addActionRows(row).setEphemeral(true).queue();
                } else {
                    event.replyEmbeds(embed.build()).addActionRows(row).setEphemeral(true).queue();
                }
            }
        };
        Taboo.getInstance().getCommandExecutor().execute(r);
    }

    public List<GenericCommand> getRegisteredCommands() {
        return registeredCommands;
    }

    public List<SlashCommand> getRegisteredSlashCommands() {
        return registeredCommands
                .stream()
                .filter(cmd -> cmd instanceof SlashCommand)
                .map(cmd -> (SlashCommand) cmd)
                .toList();
    }

    public List<MessageContextCommand> getRegisteredMessageContextCommands() {
        return registeredCommands
                .stream()
                .filter(cmd -> cmd instanceof MessageContextCommand)
                .map(cmd -> (MessageContextCommand) cmd)
                .toList();
    }

    public List<UserContextCommand> getRegisteredUserContextCommands() {
        return registeredCommands
                .stream()
                .filter(cmd -> cmd instanceof UserContextCommand)
                .map(cmd -> (UserContextCommand) cmd)
                .toList();
    }

    public ConcurrentHashMap<Long, List<GenericCommand>> getRegisteredGuildCommands() {
        return registeredGuildCommands;
    }

}
