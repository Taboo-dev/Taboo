package xyz.chalky.taboo.events;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import net.dv8tion.jda.api.events.channel.update.GenericChannelUpdateEvent;
import net.dv8tion.jda.api.events.role.RoleCreateEvent;
import net.dv8tion.jda.api.events.role.RoleDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import xyz.chalky.taboo.database.model.Config;
import xyz.chalky.taboo.database.repository.ConfigRepository;

import java.awt.*;
import java.time.Instant;

@Component
public class GuildEvents extends ListenerAdapter {

    private final ConfigRepository configRepository;

    public GuildEvents(ConfigRepository configRepository) {
        this.configRepository = configRepository;
    }

    // Role Events

    @Override
    public void onRoleCreate(@NotNull RoleCreateEvent event) {
        Role role = event.getRole();
        Guild guild = event.getGuild();
        Long logId = configRepository.findById(guild.getIdLong()).map(Config::getLogChannelId).orElse(null);
        if (logId == null) return;
        TextChannel log = guild.getTextChannelById(logId);
        if (log == null) return;
        MessageEmbed embed = new EmbedBuilder()
                .setTitle("Role Created")
                .setDescription(String.format("Role: %s", role.getAsMention()))
                .setColor(role.getColorRaw())
                .setFooter(String.format("Role ID: %s", role.getId()))
                .setTimestamp(Instant.now())
                .build();
        log.sendMessageEmbeds(embed).queue();
    }

    @Override
    public void onRoleDelete(@NotNull RoleDeleteEvent event) {
        Role role = event.getRole();
        Guild guild = event.getGuild();
        Long logId = configRepository.findById(guild.getIdLong()).map(Config::getLogChannelId).orElse(null);
        if (logId == null) return;
        TextChannel log = guild.getTextChannelById(logId);
        if (log == null) return;
        MessageEmbed embed = new EmbedBuilder()
                .setTitle("Role Deleted")
                .setDescription(String.format("Role: %s", role.getAsMention()))
                .setColor(role.getColorRaw())
                .setFooter(String.format("Role ID: %s", role.getId()))
                .setTimestamp(Instant.now())
                .build();
        log.sendMessageEmbeds(embed).queue();
    }

    // Channel Events


    @Override
    public void onChannelCreate(@NotNull ChannelCreateEvent event) {
        Channel channel = event.getChannel();
        Guild guild = event.getGuild();
        Long logId = configRepository.findById(guild.getIdLong()).map(Config::getLogChannelId).orElse(null);
        if (logId == null) return;
        TextChannel log = guild.getTextChannelById(logId);
        if (log == null) return;
        MessageEmbed embed = new EmbedBuilder()
                .setTitle("Channel Created")
                .setDescription(String.format("Channel: %s", channel.getAsMention()))
                .setColor(Color.CYAN)
                .setFooter(String.format("Channel ID: %s", channel.getId()))
                .setTimestamp(Instant.now())
                .build();
        log.sendMessageEmbeds(embed).queue();
    }

    @Override
    public void onChannelDelete(@NotNull ChannelDeleteEvent event) {
        Channel channel = event.getChannel();
        Guild guild = event.getGuild();
        Long logId = configRepository.findById(guild.getIdLong()).map(Config::getLogChannelId).orElse(null);
        if (logId == null) return;
        TextChannel log = guild.getTextChannelById(logId);
        if (log == null) return;
        MessageEmbed embed = new EmbedBuilder()
                .setTitle("Channel Deleted")
                .setDescription(String.format("Channel: %s", channel.getAsMention()))
                .setColor(Color.CYAN)
                .setFooter(String.format("Channel ID: %s", channel.getId()))
                .setTimestamp(Instant.now())
                .build();
        log.sendMessageEmbeds(embed).queue();
    }

    @Override
    public void onGenericChannelUpdate(@NotNull GenericChannelUpdateEvent<?> event) {
        Channel channel = event.getChannel();
        Guild guild = event.getGuild();
        Long logId = configRepository.findById(guild.getIdLong()).map(Config::getLogChannelId).orElse(null);
        if (logId == null) return;
        TextChannel log = guild.getTextChannelById(logId);
        if (log == null) return;
        MessageEmbed embed = new EmbedBuilder()
                .setTitle("Channel Updated")
                .setDescription(String.format("Channel: %s", channel.getAsMention()))
                .setColor(Color.CYAN)
                .setFooter(String.format("Channel ID: %s", channel.getId()))
                .setTimestamp(Instant.now())
                .build();
        log.sendMessageEmbeds(embed).queue();
    }

}
