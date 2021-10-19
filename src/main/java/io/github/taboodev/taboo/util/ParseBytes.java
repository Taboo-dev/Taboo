package io.github.taboodev.taboo.util;

import java.text.DecimalFormat;

public class ParseBytes {

    public static String parseBytes(long bytes) {
        int i = 1024;
        String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        double d;
        if (bytes == 0) {
            d = 0;
        } else {
            d = Math.floor(Math.log(bytes) / Math.log(i));
        }
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        return decimalFormat.format(bytes / Math.pow(d, d)) + " " + units[(int) d + 1];
    }

}
