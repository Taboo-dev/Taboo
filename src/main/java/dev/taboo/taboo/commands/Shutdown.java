package dev.taboo.taboo.commands;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import dev.taboo.taboo.Taboo;
import dev.taboo.taboo.util.PropertiesManager;
import dev.taboo.taboo.util.ResponseHelper;
import io.sentry.Sentry;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.commands.SlashCommandEvent;
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
        this.enabledUsers = new String[] { String.valueOf(PropertiesManager.getOwnerId()) };
        this.ownerCommand = true;
        this.guildOnly = true;
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        User user = event.getUser();
        JDA jda = event.getJDA();
        InteractionHook hook = event.getHook();
        TextChannel actionLog = jda.getTextChannelById(PropertiesManager.getActionLog());
        event.deferReply(true).queue();
        hook.sendMessageEmbeds(initialShutdownEmbed(user))
                .addActionRow(
                        Button.of(ButtonStyle.SECONDARY, "shutdown:yes", "Yes"),
                        Button.of(ButtonStyle.SECONDARY, "shutdown:no", "No")
                ).mentionRepliedUser(false)
                .queue(m -> waitForButtonClick(user, actionLog), Sentry::captureException);
    }

    @Override
    protected void execute(CommandEvent event) {
        User author = event.getAuthor();
        JDA jda = event.getJDA();
        Message message = event.getMessage();
        TextChannel actionLog = jda.getTextChannelById(PropertiesManager.getActionLog());
        message.replyEmbeds(initialShutdownEmbed(author))
                .setActionRow(
                        Button.of(ButtonStyle.SECONDARY, "shutdown:yes", "Yes"),
                        Button.of(ButtonStyle.SECONDARY, "shutdown:no", "No")
                ).mentionRepliedUser(false)
                .queue(m -> waitForButtonClick(author, actionLog), Sentry::captureException);
    }

    private void waitForButtonClick(User user, TextChannel actionLog) throws RuntimeException {
        Taboo taboo = Taboo.INSTANCE;
        if (taboo == null) {
            throw new RuntimeException("Taboo is null");
        }
        taboo.waiter.waitForEvent(ButtonClickEvent.class, clickEvent -> {
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
                    hook.editOriginalEmbeds(finalShutdownEmbed(clickUser))
                            .setActionRows(Collections.emptyList())
                            .submit()
                            .thenAcceptAsync(m -> {
                                actionLog.sendMessageEmbeds(finalShutdownEmbed(clickUser)).queue();
                                try {
                                    TimeUnit.SECONDS.sleep(10L);
                                } catch (InterruptedException e) {
                                    Sentry.captureException(e);
                                }
                                taboo.jda.setStatus(OnlineStatus.OFFLINE);
                                taboo.jda.shutdown();
                                System.exit(0);
                            });
                } case "no" -> {
                    hook.editOriginalEmbeds(noShutdownEmbed(clickUser))
                            .setActionRows(Collections.emptyList())
                            .queue();
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
                "", Color.RED
        );
    }

    private boolean equalsAny(String id) {
        return id.equals("shutdown:yes") ||
                id.equals("shutdown:no");
    }

}
