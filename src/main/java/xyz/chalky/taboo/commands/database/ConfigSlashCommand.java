package xyz.chalky.taboo.commands.database;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import xyz.chalky.taboo.core.SlashCommand;
import xyz.chalky.taboo.database.model.Config;
import xyz.chalky.taboo.database.repository.ConfigRepository;

import java.time.Instant;
import java.util.Optional;

@Component
public class ConfigSlashCommand extends SlashCommand {

    private final ConfigRepository configRepository;

    public ConfigSlashCommand(ConfigRepository configRepository) {
        setCommandData(
                Commands.slash("config", "Server config")
                        .addSubcommands(
                                new SubcommandData("set", "Sets this server's config.").addOptions(
                                        new OptionData(OptionType.CHANNEL, "log", "Set the channel to log actions to.", true)
                                ), new SubcommandData("clear", "Clear this server's config."),
                                new SubcommandData("view", "View this server's config.")
                        ).setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_SERVER))
        );
        setEphemeral(true);
        this.configRepository = configRepository;
    }

    @Override
    public void executeCommand(@NotNull SlashCommandInteractionEvent event) {
        String subcommandName = event.getSubcommandName();
        long guildId = event.getGuild().getIdLong();
        switch (subcommandName) {
            case "set" -> {
                TextChannel logChannel = event.getOption("log").getAsChannel().asTextChannel();
                configRepository.save(new Config(guildId, logChannel.getIdLong()));
                MessageEmbed embed = new EmbedBuilder()
                        .setTitle("Config set!")
                        .setDescription(String.format("Log Channel set to %s", logChannel.getAsMention())
                        ).setColor(0x9F90CF)
                        .setTimestamp(Instant.now())
                        .build();
                event.getHook().sendMessageEmbeds(embed).queue();
            }
            case "clear" -> {
                configRepository.deleteById(guildId);
                MessageEmbed embed = new EmbedBuilder()
                        .setTitle("Config cleared!")
                        .setColor(0x9F90CF)
                        .setTimestamp(Instant.now())
                        .build();
                event.getHook().sendMessageEmbeds(embed).queue();
            }
            case "view" -> {
                Optional<Config> config = configRepository.findById(guildId);
                Long log = config.map(Config::getLogChannelId).orElse(null);
                if (log == null) {
                    MessageEmbed embed = new EmbedBuilder()
                            .setTitle(String.format("Config for %s", event.getGuild().getName()))
                            .setDescription("You have no config set for this server.")
                            .setColor(0x9F90CF)
                            .setTimestamp(Instant.now())
                            .build();
                    event.getHook().sendMessageEmbeds(embed).queue();
                    return;
                }
                MessageEmbed embed = new EmbedBuilder()
                        .setTitle(String.format("Config for %s", event.getGuild().getName()))
                        .setDescription(String.format("Log Channel: %s", event.getGuild().getTextChannelById(log).getAsMention()))
                        .setColor(0x9F90CF)
                        .setTimestamp(Instant.now())
                        .build();
                event.getHook().sendMessageEmbeds(embed).queue();
            }
        }
    }

}
