package com.microsoft.applicationinsights;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.microsoft.applicationinsights.channel.Persistence;

import java.util.Date;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;


/**
 * The public API for auto collecting application insights telemetry.
 */

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class ApplicationLifeCycleEventTracking implements Application.ActivityLifecycleCallbacks {

    /**
     * The interval at which sessions are renewed
     */
    private static final int SessionInterval = 20 * 1000; // 20 seconds

    /**
     * Singleton instance of this class
     */
    public static final ApplicationLifeCycleEventTracking instance =
            new ApplicationLifeCycleEventTracking();

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
    protected ApplicationLifeCycleEventTracking() {
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
            synchronized (ApplicationLifeCycleEventTracking.lock) {
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
        this.getTelemetryClient(activity).trackPageView(activity.getApplicationInfo().className);
    }

    @Override
    public void onActivityResumed(Activity activity) {
        long now = new Date().getTime();
        long then = this.lastBackground.get();

        boolean shouldRenew = now - then > ApplicationLifeCycleEventTracking.SessionInterval;
        if(shouldRenew) {
            TelemetryClient tc = this.getTelemetryClient(activity);
            tc.getContext().renewSessionId();
            tc.trackEvent("Session Start Event");
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {
        long now = new Date().getTime();
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
}
