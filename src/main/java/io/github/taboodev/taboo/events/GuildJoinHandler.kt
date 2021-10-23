package io.github.taboodev.taboo.events

import io.github.taboodev.taboo.commands.Prefix
import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.transactions.transaction

class GuildJoinHandler: ListenerAdapter() {

    override fun onGuildJoin(event: GuildJoinEvent) {
        val guild = event.guild
        val id = guild.id
        transaction {
            Prefix.Prefix.insertIgnore {
                it[guildId] = id
                it[prefix] = ""
            }
        }
        TODO("send embed on guild join")
    }

}