package com.microsoft.mocks;

import android.content.Context;

import com.microsoft.applicationinsights.channel.TelemetryContext;

public class MockTelemetryContext extends TelemetryContext {

    public MockTelemetryContext(Context context) {
        super(context);
        TelemetryContext.setDeviceContext(context);
        TelemetryContext.setSessionContext();
        TelemetryContext.setUserContext();
        TelemetryContext.setAppContext(context);
        TelemetryContext.setInternalContext();
    }
}
