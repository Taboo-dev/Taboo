package xyz.chalky.taboo.events

import dev.minn.jda.ktx.Embed
import net.dv8tion.jda.api.events.message.MessageDeleteEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.message.MessageUpdateEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import xyz.chalky.taboo.Taboo
import xyz.chalky.taboo.database.Config
import java.sql.SQLException
import java.time.Instant

class MessageListener : ListenerAdapter() {

    private val messages = HashMap<Long, String>()

    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (!event.isFromGuild) return
        val message = event.message
        if (message.author.isBot) return
        val msgId = message.idLong
        val msgContent = message.contentRaw
        messages[msgId] = msgContent
    }

    override fun onMessageUpdate(event: MessageUpdateEvent) {
        if (!event.isFromGuild) return
        val message = event.message
        if (message.author.isBot) return
        val guild = event.guild
        try {
            var channelId: Long? = null
            transaction {
                channelId = Config.select {
                    Config.guildId eq guild.idLong
                }.first()[Config.actionLog]
            }
            val msgId = message.idLong
            val msgContent = message.contentRaw
            val originalContent = messages[msgId]
            if (channelId != null) {
                val channel = Taboo.getInstance().shardManager!!.getTextChannelById(channelId!!)
                val embed = Embed {
                    title = "Message Edited"
                    description = """
                        Original Message: $originalContent
                        New Message: $msgContent
                    """.trimIndent()
                    color = 0x9F90CF
                    timestamp = Instant.now()
                }
                channel!!.sendMessageEmbeds(embed).queue()
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    override fun onMessageDelete(event: MessageDeleteEvent) {
        if (!event.isFromGuild) return
        val msgId = event.messageIdLong
        val channel = event.channel
        val msgContent = messages[msgId]
        val embed = Embed {
            title = "Message Deleted"
            description = "Message: $msgContent"
            color = 0x9F90CF
            timestamp = Instant.now()
        }
        channel.sendMessageEmbeds(embed).queue()
    }
}