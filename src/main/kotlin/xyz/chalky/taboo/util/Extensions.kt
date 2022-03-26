package xyz.chalky.taboo.util

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

fun SlashCommandInteractionEvent.onSubCommand(name: String, function: (SlashCommandInteractionEvent) -> Unit) {
    if (this.subcommandName == name) {
        function(this)
    }
}
