package com.microsoft.applicationinsights.library;

import android.app.Activity;

import com.microsoft.applicationinsights.library.config.ISessionConfig;

public class MockLifeCycleTracking extends LifeCycleTracking {

    //TODO check this implementation

    public final MockTelemetryClient tc;
    public long currentTime;

    protected MockLifeCycleTracking(ISessionConfig config, TelemetryContext telemetryContext) {
        super(config, telemetryContext);
        currentTime = 0;
        this.tc = MockTelemetryClient.getInstance();
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
