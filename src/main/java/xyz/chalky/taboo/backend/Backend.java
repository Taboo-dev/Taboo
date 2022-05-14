package xyz.chalky.taboo.backend;

import io.javalin.Javalin;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.chalky.taboo.Taboo;
import xyz.chalky.taboo.database.ConfigEntry;
import xyz.chalky.taboo.database.DatabaseHelperKt;
import xyz.chalky.taboo.database.ResponseEntry;

public class Backend {

    private static final Logger LOGGER = LoggerFactory.getLogger(Backend.class);

    public void init() {
        Javalin javalin = Javalin.create().start(7000);
        javalin.get("/api/get-config/{guildId}", ctx -> {
            String guildId = ctx.pathParam("guildId");
            Guild guild = Taboo.getInstance().getShardManager().getGuildById(guildId);
            if (guild == null) {
                ctx.status(404);
                ctx.result("Guild not found");
                return;
            }
            long logChannelId = DatabaseHelperKt.getLogChannelId(guild);
            long musicChannelId = DatabaseHelperKt.getMusicChannelId(guild);
            ctx.status(200);
            ctx.json(new ConfigEntry(logChannelId, musicChannelId));
        });
        javalin.post("/api/set-config/{guildId}", ctx -> {
            String guildId = ctx.pathParam("guildId");
            Guild guild = Taboo.getInstance().getShardManager().getGuildById(guildId);
            if (guild == null) {
                ctx.status(404);
                ctx.result("Guild not found");
                return;
            }
            String logInput = ctx.queryParam("logChannelId");
            if (logInput == null) return;
            long logChannelId = Long.parseLong(logInput);
            TextChannel logChannel = Taboo.getInstance().getShardManager().getTextChannelById(logChannelId);
            if (logChannel == null) {
                ctx.status(404);
                ctx.json(new ResponseEntry("Channel not found"));
                return;
            }
            String musicInput = ctx.queryParam("musicChannelId");
            if (musicInput == null) return;
            long musicChannelId = Long.parseLong(musicInput);
            TextChannel musicChannel = Taboo.getInstance().getShardManager().getTextChannelById(musicChannelId);
            if (musicChannel == null) {
                ctx.status(404);
                ctx.json(new ResponseEntry("Channel not found"));
                return;
            }
            DatabaseHelperKt.insertConfig(guild, logChannelId, musicChannelId);
            ctx.status(200);
            ctx.json(new ConfigEntry(logChannelId, musicChannelId));
        });
        javalin.post("/api/clear-config/{guildId}", ctx -> {
            String guildId = ctx.pathParam("guildId");
            Guild guild = Taboo.getInstance().getShardManager().getGuildById(guildId);
            if (guild == null) {
                ctx.status(404);
                ctx.json(new ResponseEntry("Guild not found"));
                return;
            }
            try {
                DatabaseHelperKt.clearConfig(guild);
                ctx.status(200);
                ctx.json(new ResponseEntry(String.format("Cleared config for guild %s", guild.getId())));
            } catch (Exception e) {
                ctx.status(500);
                ctx.json(new ResponseEntry(String.format("Failed to clear config for guild %s", guild.getId())));
                LOGGER.debug(e.getMessage());
            }
        });
    }

}
