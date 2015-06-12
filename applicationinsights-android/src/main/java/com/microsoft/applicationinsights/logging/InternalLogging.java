package com.microsoft.applicationinsights.logging;

import android.util.Log;

import com.microsoft.applicationinsights.library.ApplicationInsights;

public class InternalLogging {
    private static final String PREFIX = InternalLogging.class.getPackage().getName();

    private InternalLogging() {
        // hide default constructor
    }

    /**
     * Inform SDK users about SDK activities. This has 3 parameters to avoid the string
     * concatenation when verbose mode is disabled.
     *
     * @param tag     the log context
     * @param message the log message
     * @param payload the payload for the message
     */
    public static void info(String tag, String message, String payload) {
        if (ApplicationInsights.isDeveloperMode()) {
            Log.i(PREFIX + " " + tag, message + ":" + payload);
        }
    }

    /**
     * Inform SDK users about SDK activities.
     *
     * @param tag     the log context
     * @param message the log message
     */
    public static void info(String tag, String message) {
        if (ApplicationInsights.isDeveloperMode()) {
            Log.i(PREFIX + " " + tag, message);
        }
    }


        /**
         * Warn SDK users about non-critical SDK misuse
         *
         * @param tag     the log context
         * @param message the log message
         */
    public static void warn(String tag, String message) {
        if (ApplicationInsights.isDeveloperMode()) {
            Log.w(PREFIX + " " + tag, message);
        }
    }

    /**
     * Log critical SDK error
     *
     * @param tag     the log context
     * @param message the log message
     */
    public static void error(String tag, String message) {
        Log.e(PREFIX + " " + tag, message);
    }
}

