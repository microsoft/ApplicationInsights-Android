package com.microsoft.applicationinsights.channel;

import android.util.Log;

public class InternalLogging {
    private static final String prefix = InternalLogging.class.getPackage().getName();

    /**
     * Inform SDK users about SDK activities. This has 3 parameters to avoid the string
     * concatenation then verbose mode is disabled.
     * @param tag the log context
     * @param message the log message
     * @param payload the payload for the message
     */
    public static void _info(String tag, String message, String payload) {
        if(TelemetryQueue.instance.getConfig().isDeveloperMode()) {
            Log.i(prefix + tag, message + ":" + payload);
        }
    }

    /**
     * Warn SDK users about non-critical SDK misuse
     * @param tag the log context
     * @param message the log message
     */
    public static void _warn(String tag, String message) {
        if(TelemetryQueue.instance.getConfig().isDeveloperMode()) {
            Log.w(prefix + tag, message);
        }
    }

    /**
     * Log critical SDK misuse, throw if developer mode is enabled
     * @param tag the log context
     * @param message the log message
     */
    public static void _error(String tag, String message) {
        if(TelemetryQueue.instance.getConfig().isDeveloperMode()) {
            Log.e(prefix + tag, message);
            throw new RuntimeException(prefix + tag + "\n" + message);
        } else {
            // todo: track SDK misuse as an event to the user's channel
        }
    }
}
