package dev.taboo.taboo.events

import dev.taboo.taboo.util.PropertiesManager
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.CommandType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import org.slf4j.LoggerFactory
import java.time.Instant

class ReadyHandler: ListenerAdapter() {

    override fun onReady(event: ReadyEvent) {
        val jda = event.jda
        val actionLog = jda.getTextChannelById(PropertiesManager.actionLog)
        val guild = jda.getGuildById(PropertiesManager.guildId)
        LoggerFactory.getLogger(ReadyHandler::class.java).info("${jda.selfUser.asTag} is ready!")
        val readyEmbed = EmbedBuilder()
            .setTitle("Taboo is now online!")
            .setColor(0x9F90CF)
            .setTimestamp(Instant.now())
            .build()
        actionLog!!.sendMessageEmbeds(readyEmbed).queue {
            guild!!.upsertCommand(CommandData(CommandType.MESSAGE_CONTEXT, "Bookmark")).queue()
        }
    }

}