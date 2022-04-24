package xyz.chalky.taboo.music;

import com.dunctebot.sourcemanagers.DuncteBotSources;
import com.github.topislavalinkplugins.topissourcemanagers.applemusic.AppleMusicSourceManager;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import lavalink.client.LavalinkUtil;
import org.jetbrains.annotations.NotNull;
import xyz.chalky.taboo.music.spotify.SpotifyAudioSource;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class AudioManager {

    private final Map<Long, GuildAudioPlayer> audioPlayers;

    public AudioManager() {
        this.audioPlayers = new ConcurrentHashMap<>();
        AudioPlayerManager playerManager = LavalinkUtil.getPlayerManager();
        playerManager.registerSourceManager(new SpotifyAudioSource());
        playerManager.registerSourceManager(new AppleMusicSourceManager(null, "us", playerManager));
        DuncteBotSources.registerAll(playerManager, "en-US");
    }

    public synchronized GuildAudioPlayer getAudioPlayer(long guildId) {
        if (audioPlayers.containsKey(guildId)) {
            return audioPlayers.get(guildId);
        }
        GuildAudioPlayer player = new GuildAudioPlayer(guildId);
        audioPlayers.put(guildId, player);
        return player;
    }

    public Set<GuildAudioPlayer> getAudioPlayers() {
        return Set.copyOf(audioPlayers.values());
    }

    public void removePlayer(@NotNull GuildAudioPlayer player) {
        audioPlayers.remove(player.getGuildId());
    }

}
