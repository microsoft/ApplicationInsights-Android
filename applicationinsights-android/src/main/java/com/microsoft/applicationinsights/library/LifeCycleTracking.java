package com.microsoft.applicationinsights.library;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.os.Build;
import android.os.Bundle;

import com.microsoft.applicationinsights.library.config.SessionConfig;
import com.microsoft.applicationinsights.logging.InternalLogging;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * The public API for auto collecting application insights telemetry.
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
class LifeCycleTracking implements Application.ActivityLifecycleCallbacks {

    /**
     * The activity counter
     */
    protected final AtomicInteger activityCount;

    /**
     * The configuration for tracking sessions
     */
    protected final SessionConfig config;

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
     * @param telemetryContext the context, which is needed to renew sessions
     */
    protected static void initialize(TelemetryContext telemetryContext, SessionConfig config) {
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
     * @param application the application object
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
     * @param activity the Android Activity that's created
     * @param savedInstanceState the bundle
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
