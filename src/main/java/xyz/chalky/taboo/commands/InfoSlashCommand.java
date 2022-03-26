package xyz.chalky.taboo.commands;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import xyz.chalky.taboo.backend.SlashCommand;
import xyz.chalky.taboo.backend.SlashCommandContext;

public class InfoSlashCommand extends SlashCommand {

    public InfoSlashCommand() {
        setCommandData(Commands.slash("info", "Displays information about me!"));
    }

    @Override
    public void executeCommand(SlashCommandInteractionEvent event, Member sender, SlashCommandContext ctx) {
        event.getHook().sendMessage("Hi! I'm Taboo!").queue();
    }

}
