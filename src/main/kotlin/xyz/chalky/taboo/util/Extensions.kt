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

fun isUrl(input: String) : Boolean {
    return try {
        URL(input)
        true
    } catch (e: Exception) {
        false
    }
}
