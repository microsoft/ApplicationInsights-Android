package com.microsoft.applicationinsights.channel;

import android.util.Log;

public class Logging {
    public static boolean enableDebugMode = true;
    private static final String prefix = "com.microsoft.applicationinsights";

    public static void Warn(String tag, String message) {
        if(enableDebugMode) {
            Log.w(prefix + tag, message);
        }
    }

    public static void Throw(String tag, String message) throws Exception {
        Log.e(prefix + tag, message);
        if(enableDebugMode) {
            throw new Exception(tag + ": " + message);
        }
    }
}
