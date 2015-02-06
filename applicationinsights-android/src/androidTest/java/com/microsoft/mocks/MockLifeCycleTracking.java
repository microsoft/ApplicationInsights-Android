package com.microsoft.mocks;

import android.app.Activity;

import com.microsoft.applicationinsights.ApplicationLifeCycleEventTracking;
import com.microsoft.applicationinsights.TelemetryClient;

public class MockLifeCycleTracking extends ApplicationLifeCycleEventTracking {

    public MockTelemetryClient tc;

    public static final MockLifeCycleTracking instance = new MockLifeCycleTracking();

    private static final Object lock = new Object();

    private MockLifeCycleTracking() {
        super();
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
}
