package xyz.chalky.taboo.events;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import xyz.chalky.taboo.central.Taboo;
import xyz.chalky.taboo.config.TabooConfigProperties;

public class CommandsListener extends ListenerAdapter {

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (!event.isFromGuild()) return;
        if (event.isWebhookMessage() || event.getAuthor().isBot()) return;
        String content = event.getMessage().getContentRaw();
        TabooConfigProperties config = Taboo.getInstance().getConfig();
        String prefix = config.getPrefix();
        if (content.startsWith(prefix) || content.startsWith(String.format("<@%s> ", event.getJDA().getSelfUser().getId()))) {
            Taboo.getInstance().getCommandHandler().handleCommand(event);
        }
    }

}
