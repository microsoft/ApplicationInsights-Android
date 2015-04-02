package com.microsoft.mocks;

import android.content.Context;

import com.microsoft.applicationinsights.internal.TelemetryContext;

public class MockTelemetryContext extends TelemetryContext {

    public MockTelemetryContext(Context context, String instrumentationKey) {
        super(context, instrumentationKey);
        TelemetryContext.setDeviceContext(context);
        TelemetryContext.setSessionContext();
        TelemetryContext.setUserContext();
        TelemetryContext.setAppContext(context);
        TelemetryContext.setInternalContext();
    }
}
