package com.microsoft.mocks;

import com.microsoft.applicationinsights.TelemetryClientConfig;
import com.microsoft.applicationinsights.channel.TelemetryContext;

public class MockTelemetryContext extends TelemetryContext {

    public MockTelemetryContext(TelemetryClientConfig config) {
        super(config);
        TelemetryContext.setDeviceContext();
        TelemetryContext.setSessionContext();
        TelemetryContext.setUserContext();
        TelemetryContext.setAppContext();
        TelemetryContext.setInternalContext();
    }
}
