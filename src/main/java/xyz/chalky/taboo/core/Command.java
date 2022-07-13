package xyz.chalky.taboo.core;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public abstract class Command {

    private final String name;
    private final String description;
    private final String usage;
    private final List<String> aliases = new ArrayList<>();
    private final List<Permission> requiredPermissions = new ArrayList<>();
    private final List<Permission> requiredBotPermissions = new ArrayList<>();
    private final List<Long> allowedGuilds = new ArrayList<>();
    private final EnumSet<CommandFlag> commandFlags;

    protected Command(String name, String description, String usage) {
        this.name = name;
        this.description = description;
        this.usage = usage;
        commandFlags = EnumSet.noneOf(CommandFlag.class);
    }

    public void addAliases(@NotNull String... aliases) {
        this.aliases.addAll(Arrays.asList(aliases));
    }

    public void addRequiredPermissions(@NotNull Permission... permissions) {
        this.requiredPermissions.addAll(Arrays.asList(permissions));
    }

    public void addRequiredBotPermissions(@NotNull Permission... permissions) {
        this.requiredBotPermissions.addAll(Arrays.asList(permissions));
    }

    public void addAllowedGuilds(@NotNull Long... guildIds) {
        this.allowedGuilds.addAll(Arrays.asList(guildIds));
    }

    public void addCommandFlags(@NotNull CommandFlag... commandFlags) {
        this.commandFlags.addAll(Arrays.asList(commandFlags));
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getUsage() {
        return usage;
    }

    public List<String> getAliases() {
        return aliases;
    }

    public List<Permission> getRequiredPermissions() {
        return Collections.unmodifiableList(requiredPermissions);
    }

    public List<Permission> getRequiredBotPermissions() {
        return Collections.unmodifiableList(requiredBotPermissions);
    }

    public List<Long> getAllowedGuilds() {
        return Collections.unmodifiableList(allowedGuilds);
    }

    public Set<CommandFlag> getCommandFlags() {
        return Collections.unmodifiableSet(commandFlags);
    }

    public boolean hasCommandFlag(CommandFlag flag) {
        return commandFlags.contains(flag);
    }

    public boolean isAvailableIn(long guildId) {
        if (hasCommandFlag(CommandFlag.PRIVATE)) {
            return getAllowedGuilds().contains(guildId);
        } else {
            return true;
        }
    }

    public abstract void executeCommand(MessageReceivedEvent event, CommandContext ctx);

}
