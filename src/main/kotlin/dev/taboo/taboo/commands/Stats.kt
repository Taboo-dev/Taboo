package dev.taboo.taboo.commands

import com.jagrosh.jdautilities.command.CommandEvent
import com.jagrosh.jdautilities.command.SlashCommand
import dev.minn.jda.ktx.EmbedBuilder
import dev.taboo.taboo.util.DateDifference
import dev.taboo.taboo.util.ParseBytes
import dev.taboo.taboo.util.PropertiesManager
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDAInfo
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.commands.SlashCommandEvent
import oshi.SystemInfo
import java.time.Instant

class Stats: SlashCommand() {

    private val startTime = Instant.now()

    init {
        name = "stats"
        help = "Shows some bot statistics."
        aliases = arrayOf("statistics", "botstats")
        defaultEnabled = true
        guildOnly = true
    }

    override fun execute(event: SlashCommandEvent) {
        val jda = event.jda
        val user = event.user
        val hook = event.hook
        event.deferReply(true).queue()
        hook.sendMessageEmbeds(statsEmbed(jda, user)).mentionRepliedUser(false).queue()
    }

    override fun execute(event: CommandEvent) {
        val jda = event.jda
        val author = event.author
        val message = event.message
        message.replyEmbeds(statsEmbed(jda, author)).mentionRepliedUser(false).queue()
    }

    private fun statsEmbed(jda: JDA, user: User): MessageEmbed {
        val systemInfo = SystemInfo()
        val hardware = systemInfo.hardware
        val processor = hardware.processor
        val cpuName = processor.processorIdentifier.name
        val cores = processor.physicalProcessorCount
        val threads = processor.logicalProcessorCount
        val memory = hardware.memory
        val available = memory.available.toDouble()
        val total = memory.total.toDouble()
        val used = total - available
        val operatingSystem = systemInfo.operatingSystem
        // not async because it takes time to retrieve owner
        val botOwner = jda.retrieveUserById(PropertiesManager.ownerId).complete()
        val embed = EmbedBuilder {
            name = "Taboo Stats"
            field {
                name = "Author"
                value = botOwner.asTag
                inline = true
            }
            field {
                name = "Source"
                value = "[View source on GitHub](https://github.com/Taboo-dev-Taboo)"
                inline = true
            }
            field {
                name = "Library"
                value = "[JDA ${JDAInfo.VERSION}](${JDAInfo.GITHUB})"
                inline = true
            }
            field {
                name = "Uptime"
                value = DateDifference.timeSince(Instant.now().toEpochMilli() - startTime.toEpochMilli())
                inline = true
            }
            field {
                name = "Servers"
                value = jda.guildCache.size().toString()
                inline = true
            }
            field {
                name = "CPU"
                value = cpuName
                inline = true
            }
            field {
                name = "Cores"
                value = cores.toString()
                inline = true
            }
            field {
                name = "Threads"
                value = threads.toString()
                inline = true
            }
            field {
                name = "Total RAM"
                value = ParseBytes.parseBytes(total)
                inline = true
            }
            field {
                name = "Available RAM"
                value = ParseBytes.parseBytes(available)
                inline = true
            }
            field {
                name = "Used RAM"
                value = ParseBytes.parseBytes(used)
                inline = true
            }
            field {
                name = "OS"
                value = operatingSystem.toString()
                inline = true
            }
            color = 0x9F90CF
            footer {
                name = "Requested by ${user.asTag}"
                iconUrl = user.effectiveAvatarUrl
            }
            timestamp = Instant.now()
        }
        return embed.build()
    }

}