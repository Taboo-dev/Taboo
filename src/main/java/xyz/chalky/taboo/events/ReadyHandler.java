package xyz.chalky.taboo.events;

import lavalink.client.io.jda.JdaLavalink;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import xyz.chalky.taboo.central.Taboo;
import xyz.chalky.taboo.central.TabooConfig;
import xyz.chalky.taboo.util.ResponseHelper;

import java.net.URI;

public class ReadyHandler extends ListenerAdapter {

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        Taboo.getInstance().getExecutor().submit(() -> {
            TabooConfig config = Taboo.getInstance().getConfig();
            Taboo.getLogger().info("Successfully started " + Taboo.getInstance().getShardManager().getShards().size() + " shards.");
            Taboo.getInstance().getInteractionCommandHandler().initialize();
            if (Taboo.getInstance().isDebug()) Taboo.getLogger().warn("Debug mode is enabled.");
            Taboo.getInstance().initCommandCheck();
            JdaLavalink lavalink = Taboo.getInstance().getLavalink();
            lavalink.setJdaProvider(id -> Taboo.getInstance().getShardManager().getShardById(id));
            lavalink.setUserId(event.getJDA().getSelfUser().getId());
            lavalink.addNode("node-1", URI.create(config.getLavalink().getHostUrl()), config.getLavalink().getPassword());
            MessageEmbed embed = ResponseHelper.createEmbed("Taboo is ready!", null, "0x9F90CF", null).build();
            Taboo.getInstance().getWebhookClient().send(embed);
        });
    }

}
