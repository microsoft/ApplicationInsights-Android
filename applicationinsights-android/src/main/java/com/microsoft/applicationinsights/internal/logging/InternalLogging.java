package com.microsoft.applicationinsights.internal.logging;

import android.util.Log;

import com.microsoft.applicationinsights.internal.TelemetryQueue;

public class InternalLogging {
    private static final String PREFIX = InternalLogging.class.getPackage().getName();

    private InternalLogging() {
        // hide default constructor
    }

    /**
     * Inform SDK users about SDK activities. This has 3 parameters to avoid the string
     * concatenation then verbose mode is disabled.
     *
     * @param tag     the log context
     * @param message the log message
     * @param payload the payload for the message
     */
    public static void info(String tag, String message, String payload) {
        if (TelemetryQueue.INSTANCE.getConfig().isDeveloperMode()) {
            Log.i(PREFIX + tag, message + ":" + payload);
        }
    }

    /**
     * Warn SDK users about non-critical SDK misuse
     *
     * @param tag     the log context
     * @param message the log message
     */
    public static void warn(String tag, String message) {
        if (TelemetryQueue.INSTANCE.getConfig().isDeveloperMode()) {
            Log.w(PREFIX + tag, message);
        }
    }

    /**
     * Log critical SDK misuse, throw if developer mode is enabled
     *
     * @param tag     the log context
     * @param message the log message
     */
    public static void error(String tag, String message) {
        if (TelemetryQueue.INSTANCE.getConfig().isDeveloperMode()) {
            Log.e(PREFIX + tag, message);
            throw new UserActionableSDKException(PREFIX + tag + "\n" + message);
        } else {
            // todo: track SDK misuse as an event to the user's channel
        }
    }
}

