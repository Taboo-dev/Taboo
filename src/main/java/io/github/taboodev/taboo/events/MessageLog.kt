package io.github.taboodev.taboo.events

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.slf4j.LoggerFactory

class MessageLog: ListenerAdapter() {

    override fun onGuildMessageReceived(event: GuildMessageReceivedEvent) {
        LoggerFactory.getLogger("Message Log").info(event.message.contentDisplay)
    }

}