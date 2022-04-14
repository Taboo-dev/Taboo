package xyz.chalky.taboo.events

import dev.minn.jda.ktx.Embed
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.jetbrains.exposed.sql.replace
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import xyz.chalky.taboo.Taboo
import xyz.chalky.taboo.database.AutoLeave
import java.time.Instant
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class VoiceListener : ListenerAdapter() {

    private val ses = Executors.newScheduledThreadPool(1)

    override fun onGuildVoiceJoin(event: GuildVoiceJoinEvent) {
        val jda = event.jda
        val voiceChannel = event.channelJoined
        val members = voiceChannel.members
        var autoLeave: Boolean? = null
        if (members.size > 1 && members.stream().anyMatch { member: Member -> member.idLong == jda.selfUser.idLong }) {
            transaction {
                AutoLeave.replace {
                    it[channelId] = voiceChannel.idLong
                    it[leave] = false
                }
            }
            transaction {
                autoLeave = AutoLeave.select {
                    AutoLeave.channelId eq voiceChannel.idLong
                }.firstOrNull()?.getOrNull(AutoLeave.leave) ?: false
            }
            if (autoLeave!!) {
                ses.shutdownNow()
            }
            println("ses cancelled")
        }
    }

    override fun onGuildVoiceLeave(event: GuildVoiceLeaveEvent) {
        val jda = event.jda
        val guild = event.guild
        val voiceChannel = event.channelLeft
        val members = voiceChannel.members
        val guildAudioPlayer = Taboo.getInstance().audioManager.getAudioPlayer(guild.idLong)
        val scheduler = guildAudioPlayer.scheduler
        var autoLeave: Boolean? = null
        val embed = Embed {
            title = "Voice Channel Left"
            description = "I have left the voice channel because I was alone."
            color = 0x9F90CF
            timestamp = Instant.now()
        }
        if (members.size == 1 && members[0].idLong == jda.selfUser.idLong) {
            transaction {
                AutoLeave.replace {
                    it[channelId] = voiceChannel.idLong
                    it[leave] = true
                }
            }
            transaction {
                autoLeave = AutoLeave.select {
                    AutoLeave.channelId eq voiceChannel.idLong
                }.firstOrNull()?.getOrNull(AutoLeave.leave) ?: false
            }
            if (autoLeave!!) {
                println("ses started")
                ses.schedule({
                    val channelId = scheduler.channelId
                    val channel = guild.getTextChannelById(channelId)
                    scheduler.destroy()
                    jda.directAudioController.disconnect(guild)
                    channel!!.sendMessageEmbeds(embed).queue()
                    transaction {
                        AutoLeave.replace {
                            it[AutoLeave.channelId] = voiceChannel.idLong
                            it[leave] = false
                        }
                    }
                }, 2, TimeUnit.MINUTES)
            }
        }
    }

}