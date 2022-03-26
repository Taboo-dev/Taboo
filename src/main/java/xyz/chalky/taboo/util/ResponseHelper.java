package xyz.chalky.taboo.util;

import java.awt.Color;
import java.time.Instant;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;

public class ResponseHelper {
    
    public static EmbedBuilder createEmbed(String title, String description, String color, User user) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        if (title != null) embedBuilder.setTitle(title);
        if (description != null) embedBuilder.setDescription(description);
        embedBuilder.setColor(Color.decode(color));
        embedBuilder.setTimestamp(Instant.now());
        if (user != null) embedBuilder.setFooter("Requested by " + user.getAsTag(), user.getEffectiveAvatarUrl());
        return embedBuilder;
    }

    public static EmbedBuilder createEmbed(String title, String description, Color color, User user) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        if (title != null) embedBuilder.setTitle(title);
        if (description != null) embedBuilder.setDescription(description);
        embedBuilder.setColor(color);
        embedBuilder.setTimestamp(Instant.now());
        if (user != null) embedBuilder.setFooter("Requested by " + user.getAsTag(), user.getEffectiveAvatarUrl());
        return embedBuilder;
    }

}
