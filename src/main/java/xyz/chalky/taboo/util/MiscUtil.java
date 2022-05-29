package xyz.chalky.taboo.util;

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

}
