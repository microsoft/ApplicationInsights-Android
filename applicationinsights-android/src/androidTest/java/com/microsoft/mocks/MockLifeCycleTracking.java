package com.microsoft.mocks;

import android.app.Activity;

import com.microsoft.applicationinsights.LifeCycleTracking;
import com.microsoft.applicationinsights.TelemetryClient;

public class MockLifeCycleTracking extends LifeCycleTracking {

    public MockTelemetryClient tc;

    public long currentTime;

    public static final MockLifeCycleTracking instance = new MockLifeCycleTracking();

    private static final Object lock = new Object();

    private MockLifeCycleTracking() {
        super();
        currentTime = 0;
    }

    @Override
    protected TelemetryClient getTelemetryClient(Activity activity) {
        if(this.tc == null) {
            synchronized (MockLifeCycleTracking.lock) {
                this.tc = new MockTelemetryClient(activity);
            }
        }

        return this.tc;
    }

    @Override
    protected long getTime() {
        return currentTime;
    }

    public long getSessionInterval() {
        return LifeCycleTracking.SessionInterval;
    }
}
