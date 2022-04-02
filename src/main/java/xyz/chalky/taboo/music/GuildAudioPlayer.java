package xyz.chalky.taboo.music;

import lavalink.client.io.jda.JdaLink;
import lavalink.client.player.LavalinkPlayer;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import xyz.chalky.taboo.Taboo;

public class GuildAudioPlayer {

    private final LavalinkPlayer player;
    private final AudioScheduler scheduler;
    private final long guildId;
    private final JdaLink link;

    public GuildAudioPlayer(SlashCommandInteractionEvent event) {
        this.guildId = event.getGuild().getIdLong();
        this.link = Taboo.getInstance().getLavalink().getLink(String.valueOf(guildId));
        this.player = link.getPlayer();
        this.scheduler = new AudioScheduler(event, player, this, guildId);
        player.addListener(scheduler);
    }

    public AudioScheduler getScheduler() {
        return scheduler;
    }

    public LavalinkPlayer getPlayer() {
        return player;
    }

    public JdaLink getLink() {
        return link;
    }

    public long getGuildId() {
        return guildId;
    }

    public void destroy() {
        link.destroy();
        scheduler.destroy();
    }

}
