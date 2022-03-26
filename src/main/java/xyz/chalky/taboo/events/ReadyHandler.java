package xyz.chalky.taboo.events;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import xyz.chalky.taboo.Taboo;
import xyz.chalky.taboo.util.PropertiesManager;
import xyz.chalky.taboo.util.ResponseHelper;

public class ReadyHandler extends ListenerAdapter {

    private final PropertiesManager propertiesManager;

    public ReadyHandler(PropertiesManager propertiesManager) {
        this.propertiesManager = propertiesManager;
    }

    @Override
    public void onReady(ReadyEvent event) {
        Taboo.getInstance().getExecutor().submit(() -> {
            Taboo.getLogger().info("Successfully started " + Taboo.getInstance().getShardManager().getShards().size() + " shards.");
            Taboo.getInstance().getInteractionCommandHandler().initialize();
            if (Taboo.getInstance().isDebug()) Taboo.getLogger().warn("Debug mode is enabled.");
            Taboo.getInstance().initCommandCheck(propertiesManager);
            Guild guild = Taboo.getInstance().getShardManager().getGuildById(propertiesManager.getGuildId());
            TextChannel channel = guild.getTextChannelById(propertiesManager.getActionLogId());
            channel.sendMessageEmbeds(ResponseHelper.createEmbed("Taboo is ready!", null, "0x9F90CF", null).build()).queue();
        });
    }

}
