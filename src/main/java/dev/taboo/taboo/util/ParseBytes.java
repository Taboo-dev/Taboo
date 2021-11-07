package dev.taboo.taboo.util;

public class ParseBytes {

    public static String parseBytes(double bytes) {
        String converted;
        double kb = bytes / 1024;
        double mb = kb / 1024;
        double gb = mb / 1024;
        if (gb > 0) {
            converted = gb + " GB";
        } else if (mb > 0) {
            converted = mb + " MB";
        } else {
            converted = kb + " KB";
        }
        return converted;
    }

}
