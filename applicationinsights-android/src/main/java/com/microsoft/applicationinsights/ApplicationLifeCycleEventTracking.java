package com.microsoft.applicationinsights;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;

import java.util.Date;


/**
 * The public API for auto collecting application insights telemetry.
 */

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class ApplicationLifeCycleEventTracking implements Application.ActivityLifecycleCallbacks {
    static int activityCount = 0;
    static Date lastBackground;

    public int getCount() {
        return activityCount;
    }

    private int BackgroundInterval = 20 * 1000;  //20 seconds
    private TelemetryClient tc;

    public ApplicationLifeCycleEventTracking getApplicationLifeCycleEventTracking() {
        return this;
    }

    public void setIKey(Activity activity, String iKey) {
        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
        SharedPreferences.Editor editor = preference.edit();
        editor.putString("iKey", iKey);
        editor.commit();
    }

    public String getIKey(Activity activity) {
        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
        String iKey = preference.getString("iKey", "00000000-0000-0000-0000-000000000000");
        return iKey;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        activityCount ++;
        String iKey = getIKey(activity);
        if(activityCount == 1 && iKey != "00000000-0000-0000-0000-000000000000") {
            if ( tc == null ) {
                tc = new TelemetryClient(activity, iKey);
            }
            tc.trackEvent("Session Start Event");
        }
    }

    @Override
    public void onActivityStarted(Activity activity) {
        tc.trackPageView(activity.getApplicationInfo().className);
    }

    @Override
    public void onActivityResumed(Activity activity) {
        Date now = new Date();
        if (lastBackground != null && tc != null) {
            if ((now.getTime() - lastBackground.getTime()) > BackgroundInterval) {
                tc.getContext().renewSessionContext(true);
                tc.trackEvent("Session Start Event");
            }
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {
        Date date = new Date();
        lastBackground = new Date();
    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        activityCount --;
        if(tc != null && activityCount == 0) {
            tc.trackEvent("Session Stop Event");
            tc = null;
            lastBackground = null;
        }
    }
}
