package com.microsoft.applicationinsights.channel;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class Util {

    private static final DateFormat dateFormat =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    static {
        TimeZone timeZone = TimeZone.getTimeZone("UTC");
        dateFormat.setTimeZone(timeZone);
    }

    /**
     * Convert a date object to an ISO 8601 formatted string
     * @param date the date object to be formatted
     * @return an ISO 8601 string representation of the date
     */
    public static String dateToISO8601(Date date) {
        if(date == null) {
            date = new Date();
        }

        return dateFormat.format(date);
    }

    /**
     * Convert a duration in milliseconds to the Application Insights serialized duration format
     * @param durationMs the duration in milliseconds
     * @return a string representation of the time span
     */
    public static String msToTimeSpan(long durationMs) {
        if (durationMs <= 0) {
            durationMs = 0;
        }

        long ms = durationMs % 1000;
        long sec = (durationMs / 1000) % 60;
        long min = (durationMs / (1000 * 60)) % 60;
        long hour = (durationMs / (1000 * 60 * 60)) % 24;
        long days = durationMs / (1000 * 60 * 60 * 24);

        String result;
        if (days == 0) {
            result = String.format("%02d:%02d:%02d.%03d", hour, min, sec, ms);
        } else {
            result = String.format("%d.%02d:%02d:%02d.%03d", days, hour, min, sec, ms);
        }

        return result;
    }
}
