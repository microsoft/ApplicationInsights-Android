package com.microsoft.applicationinsights;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.os.Build;
import android.os.Bundle;

import com.microsoft.applicationinsights.contracts.SessionState;
import com.microsoft.applicationinsights.contracts.SessionStateData;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * The public API for auto collecting application insights telemetry.
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class LifeCycleTracking implements Application.ActivityLifecycleCallbacks {

    /**
     * The activity counter
     */
    protected final AtomicInteger activityCount;

    /**
     * The timestamp of the last activity
     */
    protected final AtomicLong lastBackground;

    /**
     * Create a new INSTANCE of the lifecycle event tracking
     */
    protected LifeCycleTracking() {
        this.activityCount = new AtomicInteger(0);
        this.lastBackground = new AtomicLong(this.getTime());
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
     * Gets the singleton INSTANCE of LifeCycleTracking
     *
     * @return the singleton INSTANCE of LifeCycleTracking
     */
    protected static LifeCycleTracking getInstance() {
        return LazyInitialization.INSTANCE;
    }

    /**
     * Private class to facilitate lazy singleton initialization
     */
    private static class LazyInitialization {
        private static final LifeCycleTracking INSTANCE = new LifeCycleTracking();

        private LazyInitialization() {
            // hide default constructor
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
            TelemetryClient tc = this.getTelemetryClient(activity);
            tc.trackNewSession();
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
        TelemetryClient tc = this.getTelemetryClient(activity);

        // check if the session should be renewed
        long now = this.getTime();
        long then = this.lastBackground.getAndSet(this.getTime());
        boolean shouldRenew = now - then >= tc.getConfig().getSessionIntervalMs();
        if (shouldRenew) {
            tc.getContext().renewSessionId();
            tc.trackNewSession();
        }

        // track the page view
        tc.trackPageView(activity.getClass().getName());
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

    /**
     * Test hook for injecting a mock telemetry client
     *
     * @param activity the activity to get a telemetry client for
     * @return a telemetry client associated with the given activity
     */
    protected TelemetryClient getTelemetryClient(Activity activity) {
        return TelemetryClient.getInstance(activity);
    }
}
