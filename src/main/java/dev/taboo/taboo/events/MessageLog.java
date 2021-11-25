package dev.taboo.taboo.events;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.LoggerFactory;

public class MessageLog extends ListenerAdapter {

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        String contentDisplay = event.getMessage().getContentDisplay();
        LoggerFactory.getLogger("Message Log").info(contentDisplay);
    }

}
