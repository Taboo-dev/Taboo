package xyz.chalky.taboo.commands.misc;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import xyz.chalky.taboo.backend.SlashCommand;

public class InfoSlashCommand extends SlashCommand {

    public InfoSlashCommand() {
        setCommandData(Commands.slash("info", "Displays information about me!"));
    }

    @Override
    public void executeCommand(SlashCommandInteractionEvent event) {
        event.getHook().sendMessage("Hi! I'm Taboo!").queue();
    }

}
