package xyz.chalky.taboo.commands;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import xyz.chalky.taboo.backend.MessageContextCommand;

public class BookmarkContextMenuMessageCommand extends MessageContextCommand {

    public BookmarkContextMenuMessageCommand() {
        setCommandData(Commands.message("Bookmark"));
    }

    @Override
    public void executeCommand(MessageContextInteractionEvent event) {
        Message target = event.getTarget();
        event.reply("Bookmarking message: " + target.getContentDisplay()).queue();
    }

}
