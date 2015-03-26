package com.microsoft.applicationinsights;

import android.content.Context;

import com.microsoft.applicationinsights.channel.InternalLogging;
import com.microsoft.applicationinsights.channel.TelemetryQueue;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.LinkedHashMap;

public class ExceptionTracking implements UncaughtExceptionHandler {
    private static final Object lock = new Object();
    private static String TAG = "ExceptionHandler";
    private boolean ignoreDefaultHandler;
    protected TelemetryClient telemetryClient;
    protected UncaughtExceptionHandler preexistingExceptionHandler;

    public static void registerExceptionHandler(Context context) {
        ExceptionTracking.registerExceptionHandler(context, false);
    }

    public static void registerExceptionHandler(Context context, boolean ignoreDefaultHandler) {
        synchronized (ExceptionTracking.lock) {
            UncaughtExceptionHandler preexistingExceptionHandler =
                  Thread.getDefaultUncaughtExceptionHandler();

            if (preexistingExceptionHandler instanceof ExceptionTracking) {
                InternalLogging._error(TAG,
                        "ExceptionHandler was already registered for this thread");
            } else {
                ExceptionTracking handler = new ExceptionTracking(
                        context,
                        preexistingExceptionHandler,
                        ignoreDefaultHandler);

                Thread.setDefaultUncaughtExceptionHandler(handler);
            }
        }
    }

    protected ExceptionTracking(Context context,
                              UncaughtExceptionHandler preexistingExceptionHandler,
                              boolean ignoreDefaultHandler) {
        this.preexistingExceptionHandler = preexistingExceptionHandler;
        if (context != null) {
            this.telemetryClient = TelemetryClient.getInstance(context);
            this.ignoreDefaultHandler = ignoreDefaultHandler;
        } else {
            InternalLogging._error(TAG, "Failed to initialize ExceptionHandler with null Context");
        }
    }

    public void uncaughtException(Thread thread, Throwable exception) {
        LinkedHashMap<String, String> properties = null;
        if (thread != null) {
            properties = new LinkedHashMap<>();
            properties.put("threadName", thread.getName());
            properties.put("threadId", Long.toString(thread.getId()));
            properties.put("threadPriority", Integer.toString(thread.getPriority()));
        }

        // track the crash
        this.telemetryClient.trackException(exception, "uncaughtException", properties);

        // signal the queue that the app is crashing so future data should be persisted
        TelemetryQueue.instance.setIsCrashing(true);

        // flush the queue to disk
        this.telemetryClient.flush();

        // invoke the existing handler if requested and if it exists
        if (!this.ignoreDefaultHandler && this.preexistingExceptionHandler != null) {
            this.preexistingExceptionHandler.uncaughtException(thread, exception);
        } else {
            this.killProcess();
        }
    }

    /**
     * Test hook for killing the process
     */
    protected void killProcess() {
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(10);
    }
}
