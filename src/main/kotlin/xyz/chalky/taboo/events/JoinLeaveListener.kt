package xyz.chalky.taboo.events

import dev.minn.jda.ktx.Embed
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import xyz.chalky.taboo.database.Config
import java.awt.Color
import java.time.Instant
import java.time.temporal.ChronoUnit

class JoinLeaveListener : ListenerAdapter() {

    override fun onGuildMemberJoin(event: GuildMemberJoinEvent) {
        val guild = event.guild
        val member = event.member
        val user = event.user
        var channelId: Long? = null
        transaction {
            channelId = Config.select {
                Config.guildId eq guild.idLong
            }.firstOrNull()?.getOrNull(Config.joinLeaveLog)
        }
        if (channelId == null) return
        val channel = guild.getTextChannelById(channelId!!)
        channel!!.sendMessageEmbeds(
            Embed {
                title = "Member Joined"
                description = "${member.asMention} ${user.asTag}"
                color = Color.GREEN.hashCode()
                field {
                    name = "Account Age"
                    value = user.timeCreated.toInstant().until(Instant.now(), ChronoUnit.DAYS).toString() + " days"
                    inline = false
                }
                footer {
                    name = "Member ID: ${member.id}"
                    iconUrl = member.effectiveAvatarUrl
                }
                timestamp = Instant.now()
            }
        ).queue()
    }

    override fun onGuildMemberRemove(event: GuildMemberRemoveEvent) {
        val guild = event.guild
        val member = event.member
        val user = event.user
        var channelId: Long? = null
        transaction {
            channelId = Config.select {
                Config.guildId eq guild.idLong
            }.firstOrNull()?.getOrNull(Config.joinLeaveLog)
        }
        if (channelId == null) return
        val channel = guild.getTextChannelById(channelId!!)
        channel!!.sendMessageEmbeds(
            Embed {
                title = "Member Left"
                description = "${member!!.asMention} ${user.asTag}"
                color = Color.RED.hashCode()
                footer {
                    name = "Member ID: ${member.id}"
                    iconUrl = member.effectiveAvatarUrl
                }
                timestamp = Instant.now()
            }
        ).queue()
    }

}