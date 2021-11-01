package dev.taboo.taboo.commands.owner;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import dev.taboo.taboo.Taboo;
import dev.taboo.taboo.util.PropertiesManager;
import dev.taboo.taboo.util.ResponseHelper;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.ButtonStyle;

import java.awt.*;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class Shutdown extends SlashCommand {

    public Shutdown() {
        this.name = "shutdown";
        this.help = "Shuts down the bot.";
        this.defaultEnabled = false;
        this.ownerCommand = true;
        this.guildOnly = true;
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        User user = event.getUser();
        JDA jda = event.getJDA();
        TextChannel actionLog = jda.getTextChannelById(PropertiesManager.getActionLog());
        event.replyEmbeds(initialShutdownEmbed(user))
                .addActionRows(getButtons())
                .mentionRepliedUser(false)
                .setEphemeral(false)
                .queue(hook -> waitForEvent(user, actionLog));
    }

    @Override
    protected void execute(CommandEvent event) {
        Message message = event.getMessage();
        User author = message.getAuthor();
        JDA jda = event.getJDA();
        TextChannel actionLog = jda.getTextChannelById(PropertiesManager.getActionLog());
        message.replyEmbeds(initialShutdownEmbed(author))
                .setActionRows(getButtons())
                .mentionRepliedUser(false)
                .queue(m -> waitForEvent(author, actionLog));
    }

    private void waitForEvent(User user, TextChannel actionLog) {
        Taboo.INSTANCE.waiter.waitForEvent(ButtonClickEvent.class, clickEvent -> {
            if (!clickEvent.getUser().equals(user)) return false;
            if (!equalsAny(clickEvent.getComponentId())) return false;
            return !clickEvent.isAcknowledged();
        }, clickEvent -> {
            User clickUser = clickEvent.getUser();
            String id = clickEvent.getComponentId().split(":")[1];
            InteractionHook hook = clickEvent.getHook();
            clickEvent.deferEdit().queue();
            switch (id) {
                case "yes" -> {
                    hook.editOriginalEmbeds(finalShutdownEmbed(clickUser)).setActionRows(Collections.emptyList()).queue(m -> {
                        actionLog.sendMessageEmbeds(finalShutdownEmbed(clickUser)).queue();
                        try {
                            TimeUnit.SECONDS.sleep(10L);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        Taboo.INSTANCE.jda.setStatus(OnlineStatus.INVISIBLE);
                        Taboo.INSTANCE.jda.shutdown();
                        System.exit(0);
                    });
                } case "no" -> {
                    hook.editOriginalEmbeds(noShutdownEmbed(clickUser)).setActionRows(Collections.emptyList()).queue();
                }
            }
        });
    }

    private MessageEmbed initialShutdownEmbed(User user) {
        return ResponseHelper.generateSuccessEmbed(
                user, "Shut down",
                "Do you want to shut down the bot? Respond with the buttons below",
                Color.RED
        );
    }

    private MessageEmbed finalShutdownEmbed(User user) {
        return ResponseHelper.generateSuccessEmbed(
                user, "Shutting down...",
                "Note: It may take a few minutes for Discord to update my presence and say that I am offline.",
                Color.RED
        );
    }

    private MessageEmbed noShutdownEmbed(User user) {
        return ResponseHelper.generateSuccessEmbed(
                user, "Shutdown cancelled",
                null, Color.RED
        );
    }

    private ActionRow getButtons() {
        return ActionRow.of(
                Button.of(ButtonStyle.SECONDARY, "shutdown:yes", "Yes"),
                Button.of(ButtonStyle.SECONDARY, "shutdown:no", "No")
        );
    }

    private boolean equalsAny(String id) {
        return id.equals("shutdown:yes") ||
                id.equals("shutdown:no");
    }


}
