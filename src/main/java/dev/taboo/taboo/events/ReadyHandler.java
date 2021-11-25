package dev.taboo.taboo.events;

import dev.taboo.taboo.util.PropertiesManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.CommandType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.slf4j.LoggerFactory;

import java.time.Instant;

public class ReadyHandler extends ListenerAdapter {

    @Override
    public void onReady(ReadyEvent event) {
        JDA jda = event.getJDA();
        TextChannel actionLog = jda.getTextChannelById(PropertiesManager.getActionLog());
        Guild guild = jda.getGuildById(PropertiesManager.getGuildId());
        LoggerFactory.getLogger(ReadyHandler.class).info(String.format("%s is ready!", jda.getSelfUser().getAsTag()));
        MessageEmbed readyEmbed = new EmbedBuilder()
                .setTitle("Taboo is now online!")
                .setColor(0x9F90CF)
                .setTimestamp(Instant.now())
                .build();
        actionLog.sendMessageEmbeds(readyEmbed).queue(m -> {
            guild.upsertCommand(new CommandData(CommandType.MESSAGE_CONTEXT, "Bookmark")).queue();
        });
    }

}
