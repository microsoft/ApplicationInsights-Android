package com.microsoft.applicationinsights.channel;

import android.util.Log;

import com.microsoft.applicationinsights.channel.ILoggingInternal;

public class LoggingInternal implements ILoggingInternal {
    public static boolean enableDebugMode = true;
    private static final String prefix = "com.microsoft.applicationinsights";

    public void warn(String tag, String message) {
        if(enableDebugMode) {
            Log.w(prefix + tag, message);
        }
    }

    public void throwInternal(String tag, String message) throws Exception {
        Log.e(prefix + tag, message);
        if(enableDebugMode) {
            throw new Exception(tag + ": " + message);
        }
    }
}
