package dev.taboo.taboo.util;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;

import java.awt.*;
import java.time.Instant;

public class ResponseHelper {

    /**
     * This method generates a Failure embed for ease of use.
     * @param user The user of the event.
     * @param title The title of the embed.
     * @param description The description of the embed.
     * @return the built embed
     */
    public static MessageEmbed generateFailureEmbed(User user, String title, String description) {
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Something went wrong!")
                .setDescription("Please try again!")
                .setColor(Color.RED)
                .setFooter("Requested by " + user.getAsTag(), user.getEffectiveAvatarUrl())
                .setTimestamp(Instant.now());
        if (title != null) embed.setTitle(title);
        if (description != null) embed.setDescription(description);
        return embed.build();
    }

    /**
     * This method generates a Success embed for ease of use.
     * @param user The user of the event.
     * @param title The title of the embed.
     * @param description The description of the embed.
     * @param color The color of the embed.
     * @return the built embed.
     */
    public static MessageEmbed generateSuccessEmbed(User user, String title, String description, Color color) {
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Success!")
                .setDescription("The command worked!")
                .setColor(color)
                .setFooter("Requested by " + user.getAsTag(), user.getEffectiveAvatarUrl())
                .setTimestamp(Instant.now());
        if (title != null) embed.setTitle(title);
        if (description != null) embed.setDescription(description);
        return embed.build();
    }

}
