package xyz.chalky.taboo.util;

import lavalink.client.player.track.AudioTrack;

import java.net.MalformedURLException;
import java.net.URL;

public class MiscUtil {

    public static boolean isUrl(String url) {
        try {
            new URL(url);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }

    public static String toMinutesAndSeconds(Long time) {
        long minutes = time / 60000;
        long seconds = (time % 60000) / 1000;
        return String.format("%02d:%02d", minutes, seconds);
    }

    public static String getArtworkUrl(AudioTrack track) {
        if (track.getInfo().getArtworkUrl() != null) {
            return track.getInfo().getArtworkUrl();
        } else {
            return String.format("https://img.youtube.com/vi/%s/mqdefault.jpg", track.getInfo().getIdentifier());
        }
    }

}
