package xyz.chalky.taboo.events;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import xyz.chalky.taboo.database.util.DatabaseHelper;

import java.awt.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class JoinLeaveListener extends ListenerAdapter {

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        Guild guild = event.getGuild();
        Member member = event.getMember();
        User user = event.getUser();
        Long logId = DatabaseHelper.getInstance().getLogChannelById(guild.getIdLong());
        if (logId == null) return;
        TextChannel log = guild.getTextChannelById(logId);
        if (log == null) return;
        MessageEmbed embed = new EmbedBuilder()
                .setTitle("Member Joined")
                .setDescription(String.format("%s %s", member.getAsMention(), user.getAsTag()))
                .setColor(Color.GREEN)
                .addField(
                        "Account Age",
                        user.getTimeCreated().toInstant().until(Instant.now(), ChronoUnit.DAYS) + " days",
                        false
                ).setFooter(String.format("Member ID: %s", member.getIdLong()), member.getEffectiveAvatarUrl())
                .setTimestamp(Instant.now())
                .build();
        log.sendMessageEmbeds(embed).queue();
    }

    @Override
    public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
        Guild guild = event.getGuild();
        Member member = event.getMember();
        User user = event.getUser();
        Long logId = DatabaseHelper.getInstance().getLogChannelById(guild.getIdLong());
        if (logId == null) return;
        TextChannel log = guild.getTextChannelById(logId);
        if (log == null) return;
        MessageEmbed embed = new EmbedBuilder()
                .setTitle("Member Left")
                .setDescription(String.format("%s %s", member.getAsMention(), user.getAsTag()))
                .setColor(Color.RED)
                .setFooter(String.format("Member ID: %s", member.getId()), member.getEffectiveAvatarUrl())
                .setTimestamp(Instant.now())
                .build();
        log.sendMessageEmbeds(embed).queue();
    }

}
