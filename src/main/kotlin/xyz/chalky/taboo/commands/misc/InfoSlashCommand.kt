package xyz.chalky.taboo.commands.misc

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.Commands
import xyz.chalky.taboo.backend.SlashCommand
import xyz.chalky.taboo.util._reply

class InfoSlashCommand : SlashCommand() {

    init {
        setCommandData(Commands.slash("info", "Displays information about me!"))
    }

    override fun executeCommand(event: SlashCommandInteractionEvent) {
        event._reply("Hi! I'm Taboo!").queue()
    }

}