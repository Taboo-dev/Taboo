package xyz.chalky.taboo.events;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.BaseGuildMessageChannel;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;

public class GuildJoinListener extends ListenerAdapter {

    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        Guild guild = event.getGuild();
        BaseGuildMessageChannel defaultChannel = guild.getDefaultChannel();
        MessageEmbed embed = new EmbedBuilder()
                .setTitle("Hi! I'm Taboo!")
                .setDescription(String.format(
                        """
                        Thanks for inviting me to your server! %s
                        I can play music, moderate your server, and more!
                        To get started, use the `/config set` command so I can set up your server.
                        """
                , "\uD83D\uDE0A"))
                .setColor(0x9F90CF)
                .setTimestamp(Instant.now())
                .build();
        String botInvUrl = "https://discord.com/api/oauth2/authorize?client_id=963732351937044480&permissions=8&scope=bot%20applications.commands";
        String supportInvUrl = "https://discord.gg/WnaDwug5tc";
        defaultChannel.sendMessageEmbeds(embed).setActionRow(
                Button.link(supportInvUrl, "Support Server"),
                Button.link(botInvUrl, "Invite Me")
        ).queue();
    }

}
