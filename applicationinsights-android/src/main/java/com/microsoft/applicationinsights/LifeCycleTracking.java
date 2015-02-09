package com.microsoft.applicationinsights;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.os.Build;
import android.os.Bundle;

import java.util.Date;
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
     * The activity counter
     */
    private final AtomicInteger activityCount;

    /**
     * The timestamp of the last activity
     */
    private final AtomicLong lastBackground;

    /**
     * Create a new instance of the lifecycle event tracking
     */
    public LifeCycleTracking() {
        this.activityCount = new AtomicInteger(0);
        this.lastBackground = new AtomicLong(0);
    }

    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
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
        TelemetryClient tc = this.getTelemetryClient(activity);

        int count = this.activityCount.getAndIncrement();
        long now = this.getTime();
        long then = this.lastBackground.getAndSet(this.getTime());
        boolean shouldRenew = now - then >= LifeCycleTracking.SessionInterval;
        boolean isFirst = count == 0; // todo: switch this to this.isTaskRoot()?
        if(shouldRenew || isFirst) {
            tc.getContext().renewSessionId();
            tc.trackEvent("Session Start Event");
        }

        // track a page view for this activity
        this.getTelemetryClient(activity).trackPageView(activity.getClass().getName());
    }

    /**
     * This is called each time an activity leaves the foreground
     * @param activity the activity which was paused
     */
    public void onActivityPaused(Activity activity) {
        int count = this.activityCount.decrementAndGet();
        if(count == 0) {
            TelemetryClient tc = this.getTelemetryClient(activity);
            tc.trackEvent("Session Stop Event");

            // Try to send the data (will be written to disk if the send fails)
            tc.flush();
        }

        // keep track of the last time the app was active
        long now = this.getTime();
        this.lastBackground.set(now);
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
