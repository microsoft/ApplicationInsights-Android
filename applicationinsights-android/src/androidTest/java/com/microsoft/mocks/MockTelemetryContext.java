package com.microsoft.mocks;

import android.content.Context;

import com.microsoft.applicationinsights.TelemetryClientConfig;
import com.microsoft.applicationinsights.channel.TelemetryContext;
import com.microsoft.commonlogging.channel.CommonContext;

public class MockTelemetryContext extends TelemetryContext {

    public MockTelemetryContext(Context context) {
        super(context);
        TelemetryContext.commonContext = new CommonContext(context);
        TelemetryContext.setDeviceContext(context);
        TelemetryContext.setSessionContext();
        TelemetryContext.setUserContext();
        TelemetryContext.setAppContext();
        TelemetryContext.setInternalContext();
    }
}
