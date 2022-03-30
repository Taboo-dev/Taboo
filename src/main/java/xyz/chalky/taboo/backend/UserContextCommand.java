package xyz.chalky.taboo.backend;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.util.*;

public abstract class UserContextCommand implements GenericCommand {

    private CommandData commandData;
    private final List<Permission> requiredUserPermissions;
    private final List<Permission> requiredBotPermissions;
    private boolean isGlobal;
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
        if (commandData.getType() != Command.Type.USER) {
            throw new IllegalArgumentException("CommandData must be of type USER");
        }
        this.commandData = commandData;
    }

    public List<Permission> getRequiredUserPermissions() {
        return requiredUserPermissions;
    }

    public Set<CommandFlag> getCommandFlags() {
        return commandFlags;
    }

    public void setRequiredUserPermissions(Permission... permissions) {
        this.requiredUserPermissions.addAll(Arrays.asList(permissions));
    }

    public List<Permission> getRequiredBotPermissions() {
        return requiredBotPermissions;
    }

    public void setRequiredBotPermissions(Permission... permissions) {
        this.requiredBotPermissions.addAll(Arrays.asList(permissions));
    }

    public boolean isGlobal() {
        return isGlobal;
    }

    public void setGlobal(boolean global) {
        isGlobal = global;
    }

    public List<Long> getEnabledGuilds() {
        return enabledGuilds;
    }

    public void setEnabledGuilds(Long... guilds) {
        this.enabledGuilds.addAll(Arrays.asList(guilds));
    }

    public void addCommandFlags(CommandFlag... flags) {
        commandFlags.addAll(Set.of(flags));
    }

    public UserContextCommand() {
        this.requiredBotPermissions = new ArrayList<>();
        this.requiredUserPermissions = new ArrayList<>();
        this.commandData = null;
        this.isGlobal = true;
        this.enabledGuilds = new ArrayList<>();
        this.commandFlags = new HashSet<>();
    }

    /**
     * Executes requested context menu command.
     * @param event The UserContextInteractionEvent.
     */
    public abstract void executeCommand(UserContextInteractionEvent event);

}
