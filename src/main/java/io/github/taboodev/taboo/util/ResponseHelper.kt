package io.github.taboodev.taboo.util

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.User
import java.awt.Color
import java.time.Instant

object ResponseHelper {

    /**
     * This method generates a Failure embed for ease of use.
     * @param user The user of the event.
     * @param title The title of the embed.
     * @param description The description of the embed.
     * @return the built embed
     */
    @JvmStatic
    fun generateFailureEmbed(user: User, title: String?, description: String?): MessageEmbed {
        val embed = EmbedBuilder()
            .setTitle("Something went wrong!")
            .setDescription("Please try again!")
            .setColor(Color.RED)
            .setFooter("Requested by " + user.asTag, user.effectiveAvatarUrl)
            .setTimestamp(Instant.now())
        if (title != null) embed.setTitle(title)
        if (description != null) embed.setDescription(description)
        return embed.build()
    }

    /**
     * This method generates a Success embed for ease of use.
     * @param user The user of the event.
     * @param title The title of the embed.
     * @param description The description of the embed.
     * @param color The color of the embed.
     * @return the built embed.
     */
    @JvmStatic
    fun generateSuccessEmbed(user: User, title: String?, description: String?, color: Color?): MessageEmbed {
        val embed = EmbedBuilder()
            .setTitle("Success!")
            .setDescription("The command worked!")
            .setColor(color)
            .setFooter("Requested by " + user.asTag, user.effectiveAvatarUrl)
            .setTimestamp(Instant.now())
        if (title != null) embed.setTitle(title)
        if (description != null) embed.setDescription(description)
        return embed.build()
    }

}