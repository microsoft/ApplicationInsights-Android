package com.microsoft.applicationinsights;

import android.content.Context;

import com.microsoft.applicationinsights.internal.CreateDataTask;
import com.microsoft.applicationinsights.internal.logging.InternalLogging;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.LinkedHashMap;
import java.util.Map;

public class ExceptionTracking implements UncaughtExceptionHandler {
    private static final Object LOCK = new Object();
    private static String TAG = "ExceptionHandler";

    protected UncaughtExceptionHandler preexistingExceptionHandler;

    private boolean ignoreDefaultHandler;

    /**
     * Constructs a new instance of the ExceptionTracking class
     *
     * @param context                     The context associated with this tracker
     * @param preexistingExceptionHandler the pre-existing exception handler
     * @param ignoreDefaultHandler        indicates that the pre-existing handler should be ignored
     */
    protected ExceptionTracking(Context context,
                                UncaughtExceptionHandler preexistingExceptionHandler,
                                boolean ignoreDefaultHandler) {
        this.preexistingExceptionHandler = preexistingExceptionHandler;
        if (context != null) {
            this.ignoreDefaultHandler = ignoreDefaultHandler;
        } else {
            InternalLogging.error(TAG, "Failed to initialize ExceptionHandler with null Context");
        }
    }

    /**
     * Registers the application insights exception handler to track uncaught exceptions
     * {@code ignoreDefaulthandler} defaults to {@literal false}
     *
     * @param context the context associated with uncaught exceptions
     */
    public static void registerExceptionHandler(Context context) {
        ExceptionTracking.registerExceptionHandler(context, false);
    }

    /**
     * Registers the application insights exception handler to track uncaught exceptions
     *
     * @param context              the context associated with uncaught exceptions
     * @param ignoreDefaultHandler if true the default exception handler will be ignored
     */
    public static void registerExceptionHandler(Context context, boolean ignoreDefaultHandler) {
        synchronized (ExceptionTracking.LOCK) {
            UncaughtExceptionHandler preexistingExceptionHandler =
                  Thread.getDefaultUncaughtExceptionHandler();

            if (preexistingExceptionHandler instanceof ExceptionTracking) {
                InternalLogging.error(TAG,
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

    /**
     * The thread is being terminated by an uncaught exception. The exception will be stored
     * to disk and sent to application insights on the next app start. The default exception
     * handler will be invoked if ignoreDefaultHandler is false, otherwise the process will
     * be terminated and System.Exit(10) will be called.
     *
     * @param thread    the thread that has an uncaught exception
     * @param exception the exception that was thrown
     */
    @Override
    public void uncaughtException(Thread thread, Throwable exception) {
        Map<String, String> properties = null;
        if (thread != null) {
            properties = new LinkedHashMap<>();
            properties.put("threadName", thread.getName());
            properties.put("threadId", Long.toString(thread.getId()));
            properties.put("threadPriority", Integer.toString(thread.getPriority()));
        }

        // track the crash
        new CreateDataTask(CreateDataTask.DataType.UNHANDLED_EXCEPTION, exception, properties).execute();

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
