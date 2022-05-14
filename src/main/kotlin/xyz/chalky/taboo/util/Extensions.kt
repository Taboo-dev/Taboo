@file:Suppress("FunctionName")

package xyz.chalky.taboo.util

import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.requests.restaction.MessageAction
import net.dv8tion.jda.api.requests.restaction.WebhookMessageAction
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

fun String.isUrl() : Boolean {
    return try {
        URL(this)
        true
    } catch (e: Exception) {
        false
    }
}

fun Long.toMinutesAndSeconds() : String {
    val minutes = this / 60000
    val seconds = (this % 60000) / 1000
    return String.format("%02d:%02d", minutes, seconds)
}