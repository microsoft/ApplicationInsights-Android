package com.microsoft.mocks;

import com.microsoft.applicationinsights.TelemetryClientConfig;
import com.microsoft.applicationinsights.channel.TelemetryContext;
import com.microsoft.applicationinsights.channel.CommonContext;

public class MockTelemetryContext extends TelemetryContext {

    public MockTelemetryContext(TelemetryClientConfig config) {
        super(config);
        TelemetryContext.commonContext = new CommonContext(config.getAppContext());
        TelemetryContext.setDeviceContext();
        TelemetryContext.setSessionContext();
        TelemetryContext.setUserContext();
        TelemetryContext.setAppContext();
        TelemetryContext.setInternalContext();
    }
}
