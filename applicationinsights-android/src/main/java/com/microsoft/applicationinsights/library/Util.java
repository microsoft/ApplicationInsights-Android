package com.microsoft.applicationinsights.library;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Debug;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

class Util {

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    private static final DateFormat DATE_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ROOT);

    static {
        TimeZone timeZone = TimeZone.getTimeZone("UTC");
        DATE_FORMAT.setTimeZone(timeZone);
    }

    private Util() {
        // hide default constructor
    }

    /**
     * Convert a date object to an ISO 8601 formatted string
     *
     * @param date the date object to be formatted
     * @return an ISO 8601 string representation of the date
     */
    protected static String dateToISO8601(Date date) {
        Date localDate = date;
        if (localDate == null) {
            localDate = new Date();
        }

        return DATE_FORMAT.format(localDate);
    }

    /**
     * Convert a duration in milliseconds to the Application Insights serialized duration format
     *
     * @param durationMs the duration in milliseconds
     * @return a string representation of the time span
     */
    protected static String msToTimeSpan(long durationMs) {
        long localDurationMs = durationMs;
        if (localDurationMs <= 0) {
            localDurationMs = 0;
        }

        long ms = localDurationMs % 1000;
        long sec = (localDurationMs / 1000) % 60;
        long min = (localDurationMs / (1000 * 60)) % 60;
        long hour = (localDurationMs / (1000 * 60 * 60)) % 24;
        long days = localDurationMs / (1000 * 60 * 60 * 24);

        String result;
        if (days == 0) {
            result = String.format(Locale.ROOT, "%02d:%02d:%02d.%03d", hour, min, sec, ms);
        } else {
            result = String.format(Locale.ROOT, "%d.%02d:%02d:%02d.%03d", days, hour, min, sec, ms);
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
    protected static String tryHashStringSha256(String input) {
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
                hexChars[j * 2] = HEX_ARRAY[v >>> 4];
                hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
            }

            return new String(hexChars);
        } catch (NoSuchAlgorithmException e) {
            // All android devices should support SHA256, but if unavailable return ""
            return "";
        }
    }

    /**
     * Determines whether the app is running on aan emulator or on a real device.
     *
     * @return YES if the app is running on an emulator, NO if it is running on a real device
     */
    protected static boolean isEmulator() {
        return Build.BRAND.equalsIgnoreCase("generic");
    }

    /**
     * Determines whether a debugger is attached while running the app.
     *
     * @return YES the debugger is attached, otherwise NO
     */
    protected static boolean isDebuggerAttached() {
        return Debug.isDebuggerConnected();
    }

    /**
     * Determines if Lifecycle Tracking is available for the current user or not.
     *
     * @return YES if app runs on at least OS 4.0
     */
    protected static boolean isLifecycleTrackingAvailable() {
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH);
    }

    /**
     * Executes an async task depending on the os version the app runs on
     */
    public static void executeTask(AsyncTask<Void, ?, ?> asyncTask) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.HONEYCOMB_MR1) {
            asyncTask.execute();
        } else {
            asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }
}
