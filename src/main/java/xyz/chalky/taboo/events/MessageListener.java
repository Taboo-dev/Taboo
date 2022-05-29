package xyz.chalky.taboo.events;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import xyz.chalky.taboo.database.util.DatabaseHelper;

import java.time.Instant;
import java.util.HashMap;

public class MessageListener extends ListenerAdapter {

    private final HashMap<Long, String> messageMap = new HashMap<>();

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (!event.isFromGuild()) return;
        if (!event.isWebhookMessage()) return;
        Message message = event.getMessage();
        User author = message.getAuthor();
        if (author.isBot()) return;
        if (author.getIdLong() == event.getJDA().getSelfUser().getIdLong()) return;
        messageMap.put(message.getIdLong(), message.getContentRaw());
    }

    @Override
    public void onMessageUpdate(@NotNull MessageUpdateEvent event) {
        if (!event.isFromGuild()) return;
        Message message = event.getMessage();
        User author = message.getAuthor();
        if (author.isBot()) return;
        Guild guild = event.getGuild();
        long msgId = message.getIdLong();
        String originalContent = messageMap.get(msgId);
        String msgContent = message.getContentRaw();
        Long logId = DatabaseHelper.getInstance().getLogChannelById(guild.getIdLong());
        if (logId == null) return;
        TextChannel log = guild.getTextChannelById(logId);
        if (log == null) return;
        MessageEmbed embed = new EmbedBuilder()
                .setTitle("Message Edited")
                .addField("Original Message", originalContent, false)
                .addField("New Message", msgContent, false)
                .setAuthor(message.getAuthor().getAsTag(), message.getAuthor().getEffectiveAvatarUrl())
                .setColor(0x9F90CF)
                .setFooter(String.format("Message ID: %s", msgId))
                .setTimestamp(Instant.now())
                .build();
        log.sendMessageEmbeds(embed).queue();
    }

    @Override
    public void onMessageDelete(@NotNull MessageDeleteEvent event) {
        if (!event.isFromGuild()) return;
        Guild guild = event.getGuild();
        long msgId = event.getMessageIdLong();
        String msgContent = messageMap.get(msgId);
        if (msgContent == null) return;
        Long logId = DatabaseHelper.getInstance().getLogChannelById(guild.getIdLong());
        if (logId == null) return;
        TextChannel log = guild.getTextChannelById(logId);
        if (log == null) return;
        MessageEmbed embed = new EmbedBuilder()
                .setTitle("Message Deleted")
                .addField("Message", msgContent, false)
                .setColor(0x9F90CF)
                .setFooter(String.format("Message ID: %s", msgId))
                .setTimestamp(Instant.now())
                .build();
        log.sendMessageEmbeds(embed).queue();
    }

}
