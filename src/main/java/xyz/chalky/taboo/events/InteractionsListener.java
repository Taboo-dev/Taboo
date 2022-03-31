package xyz.chalky.taboo.events;

import java.awt.Color;

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import xyz.chalky.taboo.Taboo;
import xyz.chalky.taboo.util.PropertiesManager;
import xyz.chalky.taboo.util.ResponseHelper;

public class InteractionsListener extends ListenerAdapter {

    private final PropertiesManager propertiesManager;

    public InteractionsListener(PropertiesManager propertiesManager) {
        this.propertiesManager = propertiesManager;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.isFromGuild()) return;
        if (Taboo.getInstance().isDebug() && !(propertiesManager.getOwnerId() == (event.getUser().getIdLong()))) {
            event.replyEmbeds(ResponseHelper.createEmbed(null, "I am in debug mode! Only my owner can use commands!", Color.RED, event.getUser()).build()).queue();
        } else {
            Taboo.getInstance().getInteractionCommandHandler().handleSlashCommand(event, event.getMember());
        }
    }

    @Override
    public void onMessageContextInteraction(MessageContextInteractionEvent event) {
        if (!event.isFromGuild()) return;
        if (Taboo.getInstance().isDebug() && !(propertiesManager.getOwnerId() == (event.getUser().getIdLong()))) {
            event.replyEmbeds(ResponseHelper.createEmbed(null, "I am in debug mode! Only my owner can use commands!", Color.RED, event.getUser()).build()).queue();
        } else {
            Taboo.getInstance().getInteractionCommandHandler().handleMessageContextCommand(event);
        }
    }

    @Override
    public void onUserContextInteraction(UserContextInteractionEvent event) {
        if (!event.isFromGuild()) return;
        if (Taboo.getInstance().isDebug() && !(propertiesManager.getOwnerId() == (event.getUser().getIdLong()))) {
            event.replyEmbeds(ResponseHelper.createEmbed(null, "I am in debug mode! Only my owner can use commands!", Color.RED, event.getUser()).build()).queue();
        } else {
            Taboo.getInstance().getInteractionCommandHandler().handleUserContextCommand(event);
        }
    }

    @Override
    public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {
        if (!event.isFromGuild()) return;
        Taboo.getInstance().getInteractionCommandHandler().handleAutoComplete(event);
    }

}