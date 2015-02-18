package com.microsoft.mocks;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class MockApplication extends Application {
    Context context;

    public MockApplication(Context context) {
        this.context = context;
    }

    @Override
    public Context getApplicationContext() {
        return this.context;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            registerActivityLifecycleCallbacks(MockLifeCycleTracking.getInstance(this.context));
        }
    }

    public void unregister() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            unregisterActivityLifecycleCallbacks(MockLifeCycleTracking.getInstance(this.context));
        }
    }
}