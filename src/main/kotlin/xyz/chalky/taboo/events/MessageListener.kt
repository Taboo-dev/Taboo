package xyz.chalky.taboo.events

import dev.minn.jda.ktx.Embed
import net.dv8tion.jda.api.events.message.MessageDeleteEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.message.MessageUpdateEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import xyz.chalky.taboo.database.Config
import java.sql.SQLException
import java.time.Instant

class MessageListener : ListenerAdapter() {

    private val messages = HashMap<Long, String>()

    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (!event.isFromGuild) return
        val message = event.message
        val author = message.author
        if (author.isBot) return
        if (author.idLong == event.jda.selfUser.idLong) return
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
                }.firstOrNull()?.getOrNull(Config.actionLog)
            }
            val msgId = message.idLong
            val msgContent = message.contentRaw
            val originalContent = messages[msgId]
            if (channelId != null) {
                val channel = guild.getTextChannelById(channelId!!)
                channel!!.sendMessageEmbeds(
                    Embed {
                        title = "Message Edited"
                        field {
                            name = "Original Message"
                            value = originalContent!!
                            inline = false
                        }
                        field {
                            name = "New Message"
                            value = msgContent
                            inline = false
                        }
                        author {
                            name = message.author.asTag
                            iconUrl = message.author.effectiveAvatarUrl
                        }
                        color = 0x9F90CF
                        footer {
                            name = "Message ID: $msgId"
                        }
                        timestamp = Instant.now()
                    }
                ).queue()
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    override fun onMessageDelete(event: MessageDeleteEvent) {
        if (!event.isFromGuild) return
        val msgId = event.messageIdLong
        val guild = event.guild
        val msgContent = messages[msgId] ?: return
        var channelId: Long? = null
        transaction {
            channelId = Config.select {
                Config.guildId eq guild.idLong
            }.firstOrNull()?.getOrNull(Config.actionLog)
        }
        if (channelId != null) {
            val channel = guild.getTextChannelById(channelId!!)
            channel!!.sendMessageEmbeds(
                Embed {
                    title = "Message Deleted"
                    field {
                        name = "Message"
                        value = msgContent
                        inline = false
                    }
                    color = 0x9F90CF
                    footer {
                        name = "Message ID: $msgId"
                    }
                    timestamp = Instant.now()
                }
            ).queue()
        }
    }
}