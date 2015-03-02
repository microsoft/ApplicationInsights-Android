package com.microsoft.applicationinsights.appsample;

import android.annotation.TargetApi;
import android.app.Application;
import android.os.Build;

import com.microsoft.applicationinsights.LifeCycleTracking;

/**
 * Created by scsouthw on 2/27/15.
 */
public class MyApplication extends Application {

    @Override
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public void onCreate() {
        super.onCreate();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            registerActivityLifecycleCallbacks(LifeCycleTracking.getInstance());
        }
    }
}
