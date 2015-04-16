package com.microsoft.applicationinsights.library;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.microsoft.applicationinsights.logging.InternalLogging;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class ChannelConfig {

    private static final String TAG = "TelemetryChannelConfig";

    /**
     * Synchronization LOCK for setting the iKey
     */
    private static final Object LOCK = new Object();

    /**
     * The instrumentationKey from AndroidManifest.xml
     */
    private static String iKeyFromManifest = null;

    /**
     * The instrumentation key for this telemetry channel
     */
    private String instrumentationKey;

    /**
     * Constructs a new INSTANCE of TelemetryChannelConfig
     *
     * @param context The android activity context
     */
    public ChannelConfig(Context context) {
        this.instrumentationKey = ChannelConfig.readInstrumentationKey(context);
    }

    /**
     * Gets the instrumentation key for this telemetry channel
     * @return the instrumentation key
     */
    public String getInstrumentationKey() {
        return instrumentationKey;
    }

    /**
     * Sets the instrumentation key for this telemetry channel
     *
     * @param instrumentationKey
     */
    public void setInstrumentationKey(String instrumentationKey) {
        this.instrumentationKey = instrumentationKey;
    }

    /**
     * Gets the sender config INSTANCE for this channel.
     *
     * @return The TelemetryConfig for Application Insights
     */
    public static TelemetryConfig getStaticConfig() {
        return ChannelQueue.INSTANCE.getConfig();
    }

    /**
     * Gets the static instrumentation key from AndroidManifest.xml if it is available
     *
     * @param context the application context to check the manifest from
     * @return the instrumentation key for the application or empty string if not available
     */
    private static String getInstrumentationKey(Context context) {
        synchronized (ChannelConfig.LOCK) {
            if (ChannelConfig.iKeyFromManifest == null) {
                String iKey = ChannelConfig.readInstrumentationKey(context);
                ChannelConfig.iKeyFromManifest = iKey;
            }
        }

        return ChannelConfig.iKeyFromManifest;
    }

    /**
     * Reads the instrumentation key from AndroidManifest.xml if it is available
     *
     * @param context the application context to check the manifest from
     * @return the instrumentation key configured for the application
     */
    private static String readInstrumentationKey(Context context) {
        String iKey = "";
        if (context != null) {
            try {
                Bundle bundle = context
                        .getPackageManager()
                        .getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA)
                        .metaData;
                if (bundle != null) {
                    iKey = bundle.getString("com.microsoft.applicationinsights.instrumentationKey");
                } else {
                    logInstrumentationInstructions();
                }
            } catch (PackageManager.NameNotFoundException exception) {
                logInstrumentationInstructions();
                Log.v(TAG, exception.toString());
            }
        }

        return iKey;
    }

    /**
     * Writes instructions on how to configure the instrumentation key.
     */
    private static void logInstrumentationInstructions() {
        String instructions = "No instrumentation key found.\n" +
                "Set the instrumentation key in AndroidManifest.xml";
        String manifestSnippet = "<meta-data\n" +
                "android:name=\"com.microsoft.applicationinsights.instrumentationKey\"\n" +
                "android:value=\"${AI_INSTRUMENTATION_KEY}\" />";
        InternalLogging.error("MissingInstrumentationkey", instructions + "\n" + manifestSnippet);
    }

    public static class ExceptionTracking implements Thread.UncaughtExceptionHandler {
        private static final Object LOCK = new Object();
        private static String TAG = "ExceptionHandler";

        protected Thread.UncaughtExceptionHandler preexistingExceptionHandler;

        private boolean ignoreDefaultHandler;

        /**
         * Constructs a new instance of the ExceptionTracking class
         *
         * @param context                     The context associated with this tracker
         * @param preexistingExceptionHandler the pre-existing exception handler
         * @param ignoreDefaultHandler        indicates that the pre-existing handler should be ignored
         */
        protected ExceptionTracking(Context context,
                                    Thread.UncaughtExceptionHandler preexistingExceptionHandler,
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
                Thread.UncaughtExceptionHandler preexistingExceptionHandler =
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
                properties = new LinkedHashMap<String, String>();
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

    /**
     * The public API for auto collecting application insights telemetry.
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public static class LifeCycleTracking implements Application.ActivityLifecycleCallbacks {

        /**
         * The activity counter
         */
        protected final AtomicInteger activityCount;

        /**
         * The configuration for tracking sessions
         */
        protected SessionConfig config;

        /**
         * The timestamp of the last activity
         */
        protected final AtomicLong lastBackground;

        /**
         * The telemetryContext which is needed to renew a session
         */
        protected TelemetryContext telemetryContext;

        /**
         * Volatile boolean for double checked synchronize block
         */
        private static volatile boolean isLoaded = false;

        /**
         * Synchronization LOCK for setting static context
         */
        private static final Object LOCK = new Object();

        /**
         * The singleton INSTANCE of this class
         */
        private static LifeCycleTracking instance;

        /**
         * The tag for logging
         */
        private static final String TAG = "LifeCycleTracking";

        /**
         * Create a new INSTANCE of the lifecycle event tracking
         *
         * @param config the session configuration for session tracking
         * @param telemetryContext the context, which is needed to renew sessions
         */
        protected LifeCycleTracking(SessionConfig config, TelemetryContext telemetryContext) {
            this.activityCount = new AtomicInteger(0);
            this.lastBackground = new AtomicLong(this.getTime());
            this.config = config;
            this.telemetryContext = telemetryContext;
        }

        /**
         * Initialize the INSTANCE of lifecycle event tracking
         *
         * @param config the session configuration for session tracking
         * @param telemetryContext the context, which is needed to renew sessions
         */
        public static void initialize(SessionConfig config, TelemetryContext telemetryContext) {
            // note: isPersistenceLoaded must be volatile for the double-checked LOCK to work
            if (!LifeCycleTracking.isLoaded) {
                synchronized (LifeCycleTracking.LOCK) {
                    if (!LifeCycleTracking.isLoaded) {
                        LifeCycleTracking.isLoaded = true;
                        LifeCycleTracking.instance = new LifeCycleTracking(config, telemetryContext);
                    }
                }
            }
        }

        /**
         * @return the INSTANCE of lifecycle event tracking or null if not yet initialized
         */
        protected static LifeCycleTracking getInstance() {
            if (LifeCycleTracking.instance == null) {
                InternalLogging.error(TAG, "getInstance was called before initialization");
            }

            return LifeCycleTracking.instance;
        }

        /**
         * Enables lifecycle event tracking for the provided application
         *
         * @param application
         */
        @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
        public static void registerActivityLifecycleCallbacks(Application application) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                application.registerActivityLifecycleCallbacks(LifeCycleTracking.getInstance());
            }
        }

        /**
         * This is called each time an activity is created.
         *
         * @param activity
         * @param savedInstanceState
         */
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            int count = this.activityCount.getAndIncrement();
            if (count == 0) {
                new CreateDataTask(CreateDataTask.DataType.NEW_SESSION).execute();
            }
        }

        /**
         * This is called each time an activity becomes visible
         *
         * @param activity the activity which entered the foreground
         */
        public void onActivityStarted(Activity activity) {
            // unused but required to implement ActivityLifecycleCallbacks
        }

        /**
         * This is called each time an activity leaves the foreground
         *
         * @param activity the activity which left the foreground
         */
        public void onActivityResumed(Activity activity) {

            // check if the session should be renewed
            long now = this.getTime();
            long then = this.lastBackground.getAndSet(this.getTime());
            boolean shouldRenew = now - then >= this.config.getSessionIntervalMs();
            if (shouldRenew) {
                this.telemetryContext.renewSessionId();
                new CreateDataTask(CreateDataTask.DataType.NEW_SESSION).execute();
            }

            // track the page view
            new CreateDataTask(CreateDataTask.DataType.PAGE_VIEW, activity.getClass().getName(), null, null).execute();
        }

        /**
         * This is called each time an activity leaves the foreground
         *
         * @param activity the activity which was paused
         */
        public void onActivityPaused(Activity activity) {
            this.lastBackground.set(this.getTime());
        }

        public void onActivityStopped(Activity activity) {
            // unused but required to implement ActivityLifecycleCallbacks
        }

        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
            // unused but required to implement ActivityLifecycleCallbacks
        }

        public void onActivityDestroyed(Activity activity) {
            // unused but required to implement ActivityLifecycleCallbacks
        }

        /**
         * Test hook to get the current time
         *
         * @return the current time in milliseconds
         */
        protected long getTime() {
            return new Date().getTime();
        }
    }

    /**
     * Configuration object when instantiating TelemetryClient
     */
    public static class SessionConfig extends ChannelConfig {

        /**
         * The interval at which sessions are renewed
         */
        protected static final int SESSION_INTERVAL = 20 * 1000; // 20 seconds

        /**
         * The interval at which sessions are renewed
         */
        private long sessionIntervalMs;

        /**
         * Constructs a new INSTANCE of TelemetryClientConfig
         *
         * @param context The android app context
         */
        public SessionConfig(Context context) {
            super(context);
            this.sessionIntervalMs = SessionConfig.SESSION_INTERVAL;
        }

        /**
         * Gets the interval at which sessions are renewed
         */
        public long getSessionIntervalMs() {
            return sessionIntervalMs;
        }

        /**
         * Sets the interval at which sessions are renewed
         */
        public void setSessionIntervalMs(long sessionIntervalMs) {
            this.sessionIntervalMs = sessionIntervalMs;
        }
    }
}
