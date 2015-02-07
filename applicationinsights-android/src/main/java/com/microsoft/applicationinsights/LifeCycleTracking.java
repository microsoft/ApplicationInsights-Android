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
     * Singleton instance of this class
     */
    public static final LifeCycleTracking instance =
            new LifeCycleTracking();

    /**
     * The lock for initializing the telemetry client
     */
    private static final Object lock = new Object();

    /**
     * The telemetry client for this instance
     */
    private TelemetryClient _telemetryClient;

    /**
     * The activity counter
     */
    private final AtomicInteger activityCount;

    /**
     * The timestamp of the last activity
     */
    private final AtomicLong lastBackground;

    /**
     * Hide the constructor to ensure singleton use
     */
    protected LifeCycleTracking() {
        this.activityCount = new AtomicInteger(0);
        this.lastBackground = new AtomicLong(0);
    }

    /**
     * Gets the instance of telemetry client for this class or creates it
     * @param activity the activity to use when creating the telemetry client
     * @return a telemetry client
     */
    protected TelemetryClient getTelemetryClient(Activity activity) {
        if (this._telemetryClient == null) {
            synchronized (LifeCycleTracking.lock) {
                this._telemetryClient = TelemetryClient.getInstance(activity);
            }
        }

        return this._telemetryClient;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        activityCount.incrementAndGet();
        this.getTelemetryClient(activity).trackEvent("Session Start Event");
    }

    @Override
    public void onActivityStarted(Activity activity) {
        this.getTelemetryClient(activity).trackPageView(activity.getClass().getName());
    }

    @Override
    public void onActivityResumed(Activity activity) {
        long now = this.getTime();
        long then = this.lastBackground.get();

        boolean shouldRenew = now - then > LifeCycleTracking.SessionInterval;
        if(shouldRenew) {
            TelemetryClient tc = this.getTelemetryClient(activity);
            tc.getContext().renewSessionId();
            tc.trackEvent("Session Start Event");
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {
        long now = this.getTime();
        this.lastBackground.set(now);
    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        int count = this.activityCount.decrementAndGet();

        if(count == 0) {
            TelemetryClient tc = this.getTelemetryClient(activity);
            tc.trackEvent("Session Stop Event");

            // Try to send the data if we can
            tc.flush();

            // reset date timer
            this.activityCount.set(0);
        }
    }

    /**
     * Test hook to get the current time
     * @return the current time in milliseconds
     */
    protected long getTime() {
        return new Date().getTime();
    }
}
