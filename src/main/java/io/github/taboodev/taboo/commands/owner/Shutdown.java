package io.github.taboodev.taboo.commands.owner;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import io.github.taboodev.taboo.Taboo;
import io.github.taboodev.taboo.util.PropertiesManager;
import io.github.taboodev.taboo.util.ResponseHelper;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
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
        event.replyEmbeds(initialShutdownEmbed(user)).addActionRow(
                Button.of(ButtonStyle.SECONDARY, "shutdown:yes", "Yes"),
                Button.of(ButtonStyle.SECONDARY, "shutdown:no", "No")
        ).mentionRepliedUser(false).setEphemeral(false).queue(interactionHook -> eventWaiter(user, jda, actionLog));
    }

    @Override
    protected void execute(CommandEvent event) {
        Message message = event.getMessage();
        User author = message.getAuthor();
        JDA jda = event.getJDA();
        TextChannel actionLog = jda.getTextChannelById(PropertiesManager.getActionLog());
        message.replyEmbeds(initialShutdownEmbed(author)).setActionRow(
                Button.of(ButtonStyle.SECONDARY, "shutdown:yes", "Yes"),
                Button.of(ButtonStyle.SECONDARY, "shutdown:no", "No")
        ).mentionRepliedUser(false).queue(message1 -> eventWaiter(author, jda, actionLog));
    }

    private void eventWaiter(User user, JDA jda, TextChannel actionLog) {
        Taboo.INSTANCE.waiter.waitForEvent(ButtonClickEvent.class, buttonClickEvent -> {
            if (!buttonClickEvent.getUser().equals(user)) return false;
            if (!equalsAny(buttonClickEvent.getComponentId())) return false;
            return !buttonClickEvent.isAcknowledged();
        }, buttonClickEvent -> {
            User buttonClickEventUser = buttonClickEvent.getUser();
            String id = buttonClickEvent.getComponentId().split(":")[1];
            InteractionHook hook = buttonClickEvent.getHook();
            buttonClickEvent.deferEdit().queue();
            switch (id) {
                case "yes" -> {
                    hook.editOriginalEmbeds(finalShutdownEmbed(buttonClickEventUser)).setActionRows(Collections.emptyList()).queue(message -> {
                        actionLog.sendMessageEmbeds(finalShutdownEmbed(buttonClickEventUser)).queue();
                        try { TimeUnit.SECONDS.sleep(10); } catch (InterruptedException e) { e.printStackTrace(); }
                        Taboo.INSTANCE.jda.setStatus(OnlineStatus.INVISIBLE);
                        jda.shutdownNow();
                        try { TimeUnit.SECONDS.sleep(3); } catch (InterruptedException e) { e.printStackTrace(); }
                        System.exit(0);
                    });
                } case "no" -> {
                    hook.editOriginalEmbeds(noShutdownEmbed(buttonClickEventUser)).setActionRows(Collections.emptyList()).queue();
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

    private boolean equalsAny(String id) {
        return id.equals("shutdown:yes") ||
                id.equals("shutdown:no");
    }

}
