package dev.taboo.taboo.commands;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

public class Suggest extends SlashCommand {

    public Suggest() {
        this.name = "suggest";
        this.aliases = new String[] { "suggestion" };
        this.help = "Suggest a new feature for Taboo.";
        this.guildOnly = true;
    }

    @Override
    protected void execute(SlashCommandEvent event) {

    }

    @Override
    protected void execute(CommandEvent event) {

    }

}
