package com.microsoft.applicationinsights.channel;

import android.util.Log;

import com.microsoft.applicationinsights.channel.ILoggingInternal;

public class LoggingInternal implements ILoggingInternal {
    // todo: implement 'development mode' in config/common
    public static boolean enableDebugMode = true;
    private static final String prefix = "com.microsoft.applicationinsights";

    /**
     * Warn SDK users about non-critical SDK misuse
     * @param tag the log context
     * @param message the log message
     */
    public void warn(String tag, String message) {
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
    public void throwInternal(String tag, String message) throws Exception {

        // todo: track SDK misuse as an event
        Log.e(prefix + tag, message);
        if(enableDebugMode) {
            throw new Exception(tag + ": " + message);
        }
    }
}
