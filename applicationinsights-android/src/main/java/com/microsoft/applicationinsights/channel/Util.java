package com.microsoft.applicationinsights.channel;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class Util {

    private static final char[] hexArray = "0123456789ABCDEF".toCharArray();
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

    /**
     * Get a SHA-256 hash of the input string if the algorithm is available. If the algorithm is
     * unavailable, return empty string.
     *
     * @param input the string to hash.
     * @return a SHA-256 hash of the input or the empty string.
     */
    public static String tryHashStringSha256(String input) {
        String salt = "oRq=MAHHHC~6CCe|JfEqRZ+gc0ESI||g2Jlb^PYjc5UYN2P 27z_+21xxd2n";
        try {
            // Get a Sha256 digest
            MessageDigest hash = MessageDigest.getInstance("SHA-256");
            hash.reset();
            hash.update(input.getBytes());
            hash.update(salt.getBytes());
            byte[] hashedBytes = hash.digest();

            char[] hexChars = new char[hashedBytes.length * 2];
            for (int j = 0; j < hashedBytes.length; j++) {
                int v = hashedBytes[j] & 0xFF;
                hexChars[j * 2] = hexArray[v >>> 4];
                hexChars[j * 2 + 1] = hexArray[v & 0x0F];
            }

            return new String(hexChars);
        } catch (NoSuchAlgorithmException e) {
            // All android devices should support SHA256, but if unavailable return ""
            return "";
        }
    }
}
