package dev.taboo.taboo.events;

import dev.taboo.taboo.Taboo;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.commands.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class RemoveSlashCommand extends ListenerAdapter {

    @Override
    public void onSlashCommand(SlashCommandEvent event) {
        String name = event.getName();
        String commandId = event.getCommandId();
        Guild guild = event.getGuild();
        if (guild == null) return;
        guild.deleteCommandById(commandId).queue();
        Taboo.LOGGER.info("Removed command " + name + " from " + guild.getName());
    }

}
