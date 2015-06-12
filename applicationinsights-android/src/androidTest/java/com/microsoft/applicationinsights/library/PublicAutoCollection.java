package com.microsoft.applicationinsights.library;

import com.microsoft.applicationinsights.library.config.ISessionConfig;


public class PublicAutoCollection extends AutoCollection {
    protected PublicAutoCollection(ISessionConfig config, TelemetryContext telemetryContext) {
        super(config, telemetryContext);
    }

    protected static AutoCollection getInstance() {
        return AutoCollection.getInstance();
    }
}
