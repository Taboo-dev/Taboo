package io.github.taboodev.taboo.events

import io.github.taboodev.taboo.commands.Prefix
import io.github.taboodev.taboo.util.PropertiesManager
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant

class GuildJoinHandler: ListenerAdapter() {

    override fun onGuildJoin(event: GuildJoinEvent) {
        val guild = event.guild
        val id = guild.id
        val defaultChannel = guild.defaultChannel
        val owner = guild.owner
        val jda = event.jda
        val botOwner = jda.getUserById(PropertiesManager.ownerId)
        if (guild.roles.stream().noneMatch { it.name == "Taboo Manager" }) {
            guild.createRole()
                .setName("Taboo Manager")
                .setHoisted(false)
                .setMentionable(false)
                .flatMap { guild.addRoleToMember(owner!!, it) }
                .queue()
        }
        val joinEmbed = EmbedBuilder()
            .setTitle("Hi! I'm Taboo!")
            .setDescription(
                """
                    I'm a bot that deletes harmful files or uploads them to Hastebin when they are uploaded by server members. I can do both at the same time too!
                    My prefix is simply mentioning me (or **t!**), or using Slash Commands, but it can be changed by running **@Taboo prefix <newPrefix>**.
                    To configure my settings, you need to have the **Taboo Manager** role, and you will need to run the **Settings** command.
                    By default, I have already given this role to the Server Owner, ${guild.owner!!.user.asMention}
                """.trimIndent()
            ).setColor(0x9F90CF)
            .setFooter("Made with <3 by ${botOwner!!.asTag}")
            .setTimestamp(Instant.now())
            .build()
        defaultChannel!!.sendMessageEmbeds(joinEmbed).queue()
        transaction {
            Prefix.Prefix.insertIgnore {
                it[guildId] = id
                it[prefix] = "t!"
            }
        }

    }

}