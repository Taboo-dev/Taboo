package xyz.chalky.taboo.util;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.time.Instant;

public class ResponseHelper {
    
    public static @NotNull EmbedBuilder createEmbed(String title, String description, String color, User user) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        if (title != null) embedBuilder.setTitle(title);
        if (description != null) embedBuilder.setDescription(description);
        embedBuilder.setColor(Color.decode(color));
        embedBuilder.setTimestamp(Instant.now());
        if (user != null) embedBuilder.setFooter("Requested by " + user.getAsTag(), user.getEffectiveAvatarUrl());
        return embedBuilder;
    }

    public static @NotNull EmbedBuilder createEmbed(String title, String description, Color color, User user) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        if (title != null) embedBuilder.setTitle(title);
        if (description != null) embedBuilder.setDescription(description);
        embedBuilder.setColor(color);
        embedBuilder.setTimestamp(Instant.now());
        if (user != null) embedBuilder.setFooter("Requested by " + user.getAsTag(), user.getEffectiveAvatarUrl());
        return embedBuilder;
    }

}
