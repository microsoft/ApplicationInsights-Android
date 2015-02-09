package com.microsoft.mocks;

import android.app.Activity;
import android.content.Context;

import com.microsoft.applicationinsights.LifeCycleTracking;
import com.microsoft.applicationinsights.TelemetryClient;

public class MockLifeCycleTracking extends LifeCycleTracking {

    public static MockLifeCycleTracking instance;
    public MockTelemetryClient tc;
    public long currentTime;

    public MockLifeCycleTracking(Context context) {
        super();
        currentTime = 0;
        this.tc = new MockTelemetryClient(context);
        MockLifeCycleTracking.instance = this;
    }

    @Override
    protected TelemetryClient getTelemetryClient(Activity activity) {
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
