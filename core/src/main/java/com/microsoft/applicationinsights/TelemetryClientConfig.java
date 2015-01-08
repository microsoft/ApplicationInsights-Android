package com.microsoft.applicationinsights;

import com.microsoft.applicationinsights.common.AbstractTelemetryClientConfig;

/**
 * Configuration object when instantiating TelemetryClient
 */
public class TelemetryClientConfig extends AbstractTelemetryClientConfig {

    /**
     * Constructs a new instance of the TelemetryClientConfig
     * @param iKey The instrumentation key
     */
    public TelemetryClientConfig(String iKey){
        super(iKey);
    }
}
