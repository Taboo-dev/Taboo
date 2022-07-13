package xyz.chalky.taboo.events;

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import xyz.chalky.taboo.central.Taboo;
import xyz.chalky.taboo.config.TabooConfigProperties;
import xyz.chalky.taboo.util.ResponseHelper;

import java.awt.*;

public class InteractionsListener extends ListenerAdapter {

    private final TabooConfigProperties config;

    public InteractionsListener() {
        this.config = Taboo.getInstance().getConfig();
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.isFromGuild()) return;
        if (Taboo.getInstance().isDebug() && !(config.getOwnerId() == (event.getUser().getIdLong()))) {
            event.replyEmbeds(ResponseHelper.createEmbed(null, "I am in debug mode! Only my owner can use commands!",
                    Color.RED, event.getUser()).build()).setEphemeral(true).queue();
        } else {
            Taboo.getInstance().getInteractionCommandHandler().handleSlashCommand(event, event.getMember());
        }
    }

    @Override
    public void onMessageContextInteraction(@NotNull MessageContextInteractionEvent event) {
        if (!event.isFromGuild()) return;
        if (Taboo.getInstance().isDebug() && !(config.getOwnerId() == (event.getUser().getIdLong()))) {
            event.replyEmbeds(ResponseHelper.createEmbed(null, "I am in debug mode! Only my owner can use commands!",
                    Color.RED, event.getUser()).build()).setEphemeral(true).queue();
        } else {
            Taboo.getInstance().getInteractionCommandHandler().handleMessageContextCommand(event);
        }
    }

    @Override
    public void onUserContextInteraction(@NotNull UserContextInteractionEvent event) {
        if (!event.isFromGuild()) return;
        if (Taboo.getInstance().isDebug() && !(config.getOwnerId() == (event.getUser().getIdLong()))) {
            event.replyEmbeds(ResponseHelper.createEmbed(null, "I am in debug mode! Only my owner can use commands!",
                    Color.RED, event.getUser()).build()).setEphemeral(true).queue();
        } else {
            Taboo.getInstance().getInteractionCommandHandler().handleUserContextCommand(event);
        }
    }

    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
        if (!event.isFromGuild()) return;
        Taboo.getInstance().getInteractionCommandHandler().handleAutoComplete(event);
    }

}
