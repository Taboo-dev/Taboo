package xyz.chalky.taboo.backend;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.util.*;

public abstract class MessageContextCommand implements GenericCommand {

    private CommandData commandData;
    private final List<Permission> requiredUserPermissions;
    private final List<Permission> requiredBotPermissions;
    private boolean isGlobal;
    private boolean isEphemeral;
    private final List<Long> enabledGuilds;
    private final Set<CommandFlag> commandFlags;

    public String getCommandName() {
        return commandData.getName();
    }

    @Override
    public CommandData getData() {
        return commandData;
    }

    public void setCommandData(CommandData commandData) {
        if (commandData.getType() != Command.Type.MESSAGE) {
            throw new IllegalArgumentException("CommandData must be of type MESSAGE");
        }
        this.commandData = commandData;
    }

    public List<Permission> getRequiredUserPermissions() {
        return requiredUserPermissions;
    }

    public void setRequiredUserPermissions(Permission... permissions) {
        this.requiredUserPermissions.addAll(Arrays.asList(permissions));
    }

    public Set<CommandFlag> getCommandFlags() {
        return commandFlags;
    }

    public List<Permission> getRequiredBotPermissions() {
        return requiredBotPermissions;
    }

    public void setRequiredBotPermissions(Permission... permissions) {
        this.requiredBotPermissions.addAll(Arrays.asList(permissions));
    }

    @Override
    public boolean isGlobal() {
        return isGlobal;
    }

    public void setGlobal(boolean global) {
        isGlobal = global;
    }

    public boolean isEphemeral() {
        return isEphemeral;
    }

    public void setEphemeral(boolean ephemeral) {
        isEphemeral = ephemeral;
    }

    @Override
    public List<Long> getEnabledGuilds() {
        return enabledGuilds;
    }

    public void setEnabledGuilds(Long... enabledGuilds) {
        this.enabledGuilds.addAll(Arrays.asList(enabledGuilds));
    }

    public void addCommandFlags(CommandFlag... flags) {
        commandFlags.addAll(Set.of(flags));
    }

    public MessageContextCommand() {
        this.requiredBotPermissions = new ArrayList<>();
        this.requiredUserPermissions = new ArrayList<>();
        this.commandData = null;
        this.isGlobal = true;
        this.isEphemeral = false;
        this.enabledGuilds = new ArrayList<>();
        this.commandFlags = new HashSet<>();
    }

    /**
     * Executes the requested context menu command
     * @param event The MessageContextInteractionEvent
     */
    public abstract void executeCommand(MessageContextInteractionEvent event);

}
