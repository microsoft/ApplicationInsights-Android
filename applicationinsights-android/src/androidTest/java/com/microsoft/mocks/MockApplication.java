package com.microsoft.mocks;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.Context;
import android.os.Build;

import com.microsoft.applicationinsights.ApplicationLifeCycleEventTracking;

public class MockApplication extends Application {
    Context context;

    public MockApplication(Context context) {
        this.context = context;
    }

    @Override
    public Context getApplicationContext() {
        return this.context;
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void onCreate() {
        super.onCreate();
        ApplicationLifeCycleEventTracking tracking = new ApplicationLifeCycleEventTracking();
        registerActivityLifecycleCallbacks(tracking.getApplicationLifeCycleEventTracking());
    }
}