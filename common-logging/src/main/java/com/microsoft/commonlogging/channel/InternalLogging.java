package com.microsoft.commonlogging.channel;

import android.util.Log;

public class InternalLogging {
    // todo: implement 'development mode' in config/common
    public static boolean enableDebugMode = true;
    public static boolean enableVerboseMode = true;
    private static final String prefix = InternalLogging.class.getPackage().getName();

    /**
     * Inform SDK users about SDK activities. This has 3 parameters to avoid the string
     * concatenation then verbose mode is disabled.
     * @param tag the log context
     * @param message the log message
     * @param payload the payload for the message
     */
    public static void _info(String tag, String message, String payload) {
        if(enableVerboseMode) {
            Log.i(prefix + tag, message + ":\n" + payload);
        }
    }

    /**
     * Warn SDK users about non-critical SDK misuse
     * @param tag the log context
     * @param message the log message
     */
    public static void _warn(String tag, String message) {
        if(enableDebugMode) {
            Log.w(prefix + tag, message);
        }
    }

    /**
     * Log critical SDK misuse, throw if developer mode is enabled
     * @param tag the log context
     * @param message the log message
     * @throws Exception
     */
    public static void _throw(String tag, String message) throws Exception {

        // todo: track SDK misuse as an event
        Log.e(prefix + tag, message);
        if(enableDebugMode) {
            throw new Exception(tag + ": " + message);
        }
    }
}
