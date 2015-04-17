package com.microsoft.applicationinsights.library;

import android.content.Context;

import com.microsoft.applicationinsights.library.TelemetryContext;

public class MockTelemetryContext extends TelemetryContext {

    public MockTelemetryContext(Context context, String instrumentationKey) {
        super(context, instrumentationKey);
        TelemetryContext.setDeviceContext(context);
        TelemetryContext.setSessionContext();
        TelemetryContext.setUserContext();
        TelemetryContext.setAppContext(context);
        TelemetryContext.setInternalContext(context);
    }
}
