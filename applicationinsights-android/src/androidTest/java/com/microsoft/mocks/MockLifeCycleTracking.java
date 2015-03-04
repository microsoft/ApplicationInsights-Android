package com.microsoft.mocks;

import android.app.Activity;
import android.content.Context;

import com.microsoft.applicationinsights.LifeCycleTracking;
import com.microsoft.applicationinsights.TelemetryClient;

public class MockLifeCycleTracking extends LifeCycleTracking {

    private static final Object lock = new Object();
    private static MockLifeCycleTracking instance;

    public final MockTelemetryClient tc;
    public long currentTime;

    public static MockLifeCycleTracking getInstance(Context context) {
        synchronized (MockLifeCycleTracking.lock) {
            if(MockLifeCycleTracking.instance == null) {
                MockLifeCycleTracking.instance = new MockLifeCycleTracking(context);
            }
        }

        return MockLifeCycleTracking.instance;
    }

    protected MockLifeCycleTracking(Context context) {
        super();
        currentTime = 0;
        this.tc = new MockTelemetryClient(context);
    }

    @Override
    protected TelemetryClient getTelemetryClient(Activity activity) {
        return this.tc;
    }

    @Override
    protected long getTime() {
        return currentTime;
    }

    public void reset() {
        this.currentTime = 0;
        this.tc.clearMessages();
        super.activityCount.set(0);
        super.lastBackground.set(0);
    }
}
