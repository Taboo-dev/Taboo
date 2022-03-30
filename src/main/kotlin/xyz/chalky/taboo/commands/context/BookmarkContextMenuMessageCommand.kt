package xyz.chalky.taboo.commands.context

import xyz.chalky.taboo.backend.MessageContextCommand
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.Commands

class BookmarkContextMenuMessageCommand : MessageContextCommand() {

    init {
        setCommandData(Commands.message("Bookmark"))
    }

    override fun executeCommand(event: MessageContextInteractionEvent) {
        val target = event.target
        event.reply("Bookmarking message: " + target.contentDisplay).queue()
    }

}