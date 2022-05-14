package xyz.chalky.taboo.commands.misc;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;
import xyz.chalky.taboo.core.MessageContextCommand;

import java.awt.*;
import java.time.Instant;

public class BookmarkContextCommand extends MessageContextCommand {

    public BookmarkContextCommand() {
        setCommandData(Commands.message("Bookmark"));
        setEphemeral(true);
    }

    @Override
    public void executeCommand(@NotNull MessageContextInteractionEvent event) {
        Message message = event.getTarget();
        User user = event.getUser();
        User author = message.getAuthor();
        MessageEmbed embed = new EmbedBuilder()
                .setTitle("Bookmark")
                .setDescription(String.format(
                        """
                        ***Message Information:***
                        **Content:** %s
                        **Server:** %s
                        **Author:** %s (%s)
                        """
                , message.getContentDisplay(), message.getGuild().getName(), author.getAsMention(), author.getId()))
                .setColor(0x9F90CF)
                .setFooter(String.format("Message ID: %s", message.getId()), user.getEffectiveAvatarUrl())
                .build();
        user.openPrivateChannel()
                .flatMap(channel -> channel.sendMessageEmbeds(embed).setActionRow(Button.link(message.getJumpUrl(), "View")))
                .submit()
                .thenAccept(msg -> event.getHook().sendMessageEmbeds(embed).addActionRow(Button.link(message.getJumpUrl(), "View")).queue())
                .exceptionally(throwable -> {
                    MessageEmbed failEmbed = new EmbedBuilder()
                            .setDescription("Failed to send bookmark message. Are your DMs open?")
                            .setColor(Color.RED)
                            .setTimestamp(Instant.now())
                            .build();
                    event.getHook().sendMessageEmbeds(failEmbed).queue();
                    return null;
                });
    }

}
