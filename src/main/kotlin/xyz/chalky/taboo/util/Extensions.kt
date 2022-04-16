@file:Suppress("FunctionName")

package xyz.chalky.taboo.util

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.requests.restaction.MessageAction
import net.dv8tion.jda.api.requests.restaction.WebhookMessageAction
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import xyz.chalky.taboo.database.Config
import java.net.URL

fun SlashCommandInteractionEvent.onSubCommand(name: String, function: (SlashCommandInteractionEvent) -> Unit) {
    if (this.subcommandName == name) {
        function(this)
    }
}

fun SlashCommandInteractionEvent._reply(message: String) : WebhookMessageAction<Message> {
    return this.hook.sendMessage(message)
}

fun SlashCommandInteractionEvent._reply(embed: MessageEmbed, vararg embeds: MessageEmbed) : WebhookMessageAction<Message> {
    return this.hook.sendMessageEmbeds(embed, *embeds)
}

fun Message._edit(content: String) : MessageAction {
    return this.editMessage(content)
}

fun Message._edit(embed: MessageEmbed, vararg embeds: MessageEmbed) : MessageAction {
    return this.editMessageEmbeds(embed, *embeds)
}

fun isUrl(input: String) : Boolean {
    return try {
        URL(input)
        true
    } catch (e: Exception) {
        false
    }
}

fun getActionLogId(guild: Guild) : Long? {
    var channelId: Long? = null
    transaction {
        channelId = Config.select {
            Config.guildId eq guild.idLong
        }.firstOrNull()?.getOrNull(Config.actionLog) ?: return@transaction null
    }
    return channelId
}

fun getJoinLeaveLogId(guild: Guild) : Long? {
    var channelId: Long? = null
    transaction {
        channelId = Config.select {
            Config.guildId eq guild.idLong
        }.firstOrNull()?.getOrNull(Config.joinLeaveLog) ?: return@transaction null
    }
    return channelId
}

fun parseLength(length: Long) : Pair<Long, Long> {
    val minutes = length / 60000
    val seconds = length % 60
    return Pair(minutes, seconds)
}