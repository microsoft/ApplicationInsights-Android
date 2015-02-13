package com.microsoft.applicationinsights;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.os.Build;
import android.os.Bundle;

import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * The public API for auto collecting application insights telemetry.
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class LifeCycleTracking implements Application.ActivityLifecycleCallbacks {

    /**
     * The interval at which sessions are renewed; todo: move this to TelemetryClientConfig
     */
    protected static final int SessionInterval = 20 * 1000; // 20 seconds

    /**
     * Private class to facilitate lazy singleton initialization
     */
    private static class LazyInitialization {
        private static final LifeCycleTracking INSTANCE = new LifeCycleTracking();
    }

    /**
     * Gets the singleton instance of LifeCycleTracking
     * @return the singleton instance of LifeCycleTracking
     */
    public static LifeCycleTracking getInstance() {
        return LazyInitialization.INSTANCE;
    }

    /**
     * The activity counter
     */
    protected final AtomicInteger activityCount;

    /**
     * The timestamp of the last activity
     */
    protected final AtomicLong lastBackground;

    /**
     * Create a new instance of the lifecycle event tracking
     */
    protected LifeCycleTracking() {
        this.activityCount = new AtomicInteger(0);
        this.lastBackground = new AtomicLong(0);
    }

    /**
     * This is called each time an activity is created.
     * @param activity
     * @param savedInstanceState
     */
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        int count = this.activityCount.getAndIncrement();
        if(count == 0) {
            TelemetryClient tc = this.getTelemetryClient(activity);
            tc.getContext().renewSessionId();
            tc.trackEvent("Session Start Event");
        }
    }

    /**
     * This is called each time an activity becomes visible
     * @param activity the activity which entered the foreground
     */
    public void onActivityStarted(Activity activity) {
    }

    /**
     * This is called each time an activity leaves the foreground
     * @param activity the activity which left the foreground
     */
    public void onActivityResumed(Activity activity) {
        // track the page view
        TelemetryClient tc = this.getTelemetryClient(activity);
        tc.trackPageView(activity.getClass().getName());

        // check if the session should be renewed
        long now = this.getTime();
        long then = this.lastBackground.getAndSet(this.getTime());
        boolean shouldRenew = now - then >= LifeCycleTracking.SessionInterval;
        if(shouldRenew) {
            tc.getContext().renewSessionId();
            tc.trackEvent("Session Start Event");
        }
    }

    /**
     * This is called each time an activity leaves the foreground
     * @param activity the activity which was paused
     */
    public void onActivityPaused(Activity activity) {
        this.lastBackground.set(this.getTime());
    }

    public void onActivityStopped(Activity activity) {
    }

    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    public void onActivityDestroyed(Activity activity) {
    }

    /**
     * Test hook to get the current time
     * @return the current time in milliseconds
     */
    protected long getTime() {
        return new Date().getTime();
    }

    /**
     * Test hook for injecting a mock telemetry client
     * @param activity the activity to get a telemetry client for
     * @return a telemetry client associated with the given activity
     */
    protected TelemetryClient getTelemetryClient(Activity activity) {
        return TelemetryClient.getInstance(activity);
    }
}
