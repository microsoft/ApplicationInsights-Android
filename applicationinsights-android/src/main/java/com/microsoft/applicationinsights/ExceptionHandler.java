package com.microsoft.applicationinsights;

import android.content.Context;

import com.microsoft.applicationinsights.channel.InternalLogging;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.LinkedHashMap;

public class ExceptionHandler implements UncaughtExceptionHandler {
    private static final Object lock = new Object();
    private static String TAG = "ExceptionHandler";
    private boolean ignoreDefaultHandler;
    private TelemetryClient telemetryClient;
    private UncaughtExceptionHandler preexistingExceptionHandler;

    public static void registerExceptionHandler(Context context) {
        ExceptionHandler.registerExceptionHandler(context, false);
    }

    public static void registerExceptionHandler(Context context, boolean ignoreDefaultHandler) {
        synchronized (ExceptionHandler.lock) {
            UncaughtExceptionHandler preexistingExceptionHandler =
                    Thread.getDefaultUncaughtExceptionHandler();

            if (preexistingExceptionHandler instanceof ExceptionHandler) {
                InternalLogging._error(TAG,
                      "ExceptionHandler was already registered for this thread");
            } else {
                ExceptionHandler handler = new ExceptionHandler(
                        context,
                        preexistingExceptionHandler,
                        ignoreDefaultHandler);

                Thread.setDefaultUncaughtExceptionHandler(handler);
            }
        }
    }

    private ExceptionHandler(Context context,
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

      this.telemetryClient.sendCrash(exception, properties);
        this.telemetryClient.flush();

        if (!this.ignoreDefaultHandler) {
            this.preexistingExceptionHandler.uncaughtException(thread, exception);
        } else {
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(10);
        }
    }
}
