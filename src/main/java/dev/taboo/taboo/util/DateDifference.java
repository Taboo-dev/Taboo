package dev.taboo.taboo.util;

import java.util.ArrayList;

public class DateDifference {

    public static String timeSince(long diff, boolean useSeconds) {
        long secondsInMillis = 1000;
        long minutesInMillis = secondsInMillis * 60;
        long hoursInMillis = minutesInMillis * 60;
        long daysInMillis = hoursInMillis * 24;
        long yearsInMillis = daysInMillis * 365;

        long elapsedYears = diff / yearsInMillis;
        diff = diff % yearsInMillis;

        long elapsedDays = diff / daysInMillis;
        diff = diff % daysInMillis;

        long elapsedHours = diff / hoursInMillis;
        diff = diff % hoursInMillis;

        long elapsedMinutes = diff / minutesInMillis;
        diff = diff % minutesInMillis;

        long elapsedSeconds = diff / secondsInMillis;

        ArrayList<String> output = new ArrayList<>();
        if (elapsedYears > 0)
            output.add(elapsedYears + " year" + (elapsedYears > 1 ? "s" : ""));
        if (elapsedDays > 0)
            output.add(elapsedDays + " day" + (elapsedDays > 1 ? "s" : ""));
        if (elapsedHours > 0)
            output.add(elapsedHours + " hour" + (elapsedHours > 1 ? "s" : ""));
        if (elapsedMinutes > 0)
            output.add(elapsedMinutes + " minute" + (elapsedMinutes > 1 ? "s" : ""));
        if (elapsedSeconds > 0 && useSeconds)
            output.add(elapsedSeconds + " second" + (elapsedSeconds > 1 ? "s" : ""));

        StringBuilder response = new StringBuilder();
        for (var module : output) {
            if (response.toString().equals("")) {
                response = new StringBuilder(module);
            } else if (module.equals(output.get(output.size() - 1))) {
                response.append(", and ").append(module);
            } else {
                response.append(", ").append(module);
            }
        }
        return response.toString();
    }

    public static String timeSince(long diff) {
        return timeSince(diff, true);
    }

}
