package xyz.chalky.taboo.backend;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class SlashCommandContext {

    private final SlashCommandInteractionEvent event;

    public SlashCommandContext(SlashCommandInteractionEvent event) {
        this.event = event;
    }
}
