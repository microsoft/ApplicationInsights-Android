package com.microsoft.applicationinsights.library;

import android.app.Activity;

import com.microsoft.applicationinsights.library.config.SessionConfig;

public class MockLifeCycleTracking extends LifeCycleTracking {

    //TODO check this implementation

    public final MockTelemetryClient tc;
    public long currentTime;

    public static MockLifeCycleTracking getInstance() {
        return (MockLifeCycleTracking)LifeCycleTracking.getInstance();
    }

    protected MockLifeCycleTracking(SessionConfig config, TelemetryContext telemetryContext) {
        super(config, telemetryContext);
        currentTime = 0;
        this.tc = new MockTelemetryClient(true);
    }

    //@Override
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
