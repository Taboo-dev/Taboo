package xyz.chalky.taboo.events

import dev.minn.jda.ktx.Embed
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent
import net.dv8tion.jda.api.events.channel.update.GenericChannelUpdateEvent
import net.dv8tion.jda.api.events.role.RoleCreateEvent
import net.dv8tion.jda.api.events.role.RoleDeleteEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import xyz.chalky.taboo.util.getActionLogId
import java.awt.Color
import java.time.Instant

class GuildEvents : ListenerAdapter() {

    // Role Events

    override fun onRoleCreate(event: RoleCreateEvent) {
        val role = event.role
        val guild = event.guild
        val actionLogId = getActionLogId(guild) ?: return
        val actionLog = guild.getTextChannelById(actionLogId) ?: return
        actionLog.sendMessageEmbeds(
            Embed {
                title = "Role Created"
                description = "Role: ${role.asMention}"
                color = role.color.hashCode()
                footer {
                    name = "Role ID: ${role.id}"
                }
                timestamp = Instant.now()
            }
        ).queue()
    }

    override fun onRoleDelete(event: RoleDeleteEvent) {
        val role = event.role
        val guild = event.guild
        val actionLogId = getActionLogId(guild) ?: return
        val actionLog = guild.getTextChannelById(actionLogId) ?: return
        actionLog.sendMessageEmbeds(
            Embed {
                title = "Role Deleted"
                description = "Role: ${role.asMention}"
                color = role.color.hashCode()
                footer {
                    name = "Role ID: ${role.id}"
                }
                timestamp = Instant.now()
            }
        ).queue()
    }

    // Channel Events

    override fun onChannelCreate(event: ChannelCreateEvent) {
        val channel = event.channel
        val guild = event.guild
        val actionLogId = getActionLogId(guild) ?: return
        val actionLog = guild.getTextChannelById(actionLogId) ?: return
        actionLog.sendMessageEmbeds(
            Embed {
                title = "Channel Created"
                description = "Channel: ${channel.asMention}"
                color = Color.CYAN.hashCode()
                footer {
                    name = "Channel ID: ${channel.id}"
                }
                timestamp = Instant.now()
            }
        ).queue()
    }

    override fun onChannelDelete(event: ChannelDeleteEvent) {
        val channel = event.channel
        val guild = event.guild
        val actionLogId = getActionLogId(guild) ?: return
        val actionLog = guild.getTextChannelById(actionLogId) ?: return
        actionLog.sendMessageEmbeds(
            Embed {
                title = "Channel Deleted"
                description = "Channel: ${channel.asMention}"
                color = Color.CYAN.hashCode()
                footer {
                    name = "Channel ID: ${channel.id}"
                }
                timestamp = Instant.now()
            }
        ).queue()
    }

    override fun onGenericChannelUpdate(event: GenericChannelUpdateEvent<*>) {
        val channel = event.channel
        val guild = event.guild
        val actionLogId = getActionLogId(guild) ?: return
        val actionLog = guild.getTextChannelById(actionLogId) ?: return
        actionLog.sendMessageEmbeds(
            Embed {
                title = "Channel Updated"
                description = "Channel: ${channel.asMention}"
                color = Color.CYAN.hashCode()
                footer {
                    name = "Channel ID: ${channel.id}"
                }
                timestamp = Instant.now()
            }
        ).queue()
    }

}