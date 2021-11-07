package dev.taboo.taboo.events

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.slf4j.LoggerFactory

class MessageLog: ListenerAdapter() {

    override fun onGuildMessageReceived(event: GuildMessageReceivedEvent) {
        val contentDisplay = event.message.contentDisplay
        LoggerFactory.getLogger("Message Log").info(contentDisplay)
    }

}