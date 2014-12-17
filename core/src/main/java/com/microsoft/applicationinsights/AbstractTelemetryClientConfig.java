package com.microsoft.applicationinsights;

import com.microsoft.applicationinsights.channel.IChannelConfig;
import com.microsoft.applicationinsights.channel.IContextConfig;
import com.microsoft.applicationinsights.channel.SenderConfig;

/**
 * Configuration object when instantiating TelemetryClient
 */
public abstract class AbstractTelemetryClientConfig extends SenderConfig implements IChannelConfig, IContextConfig {

    /**
     * The instrumentation key for this telemetryContext
     */
    protected final String instrumentationKey;

    public AbstractTelemetryClientConfig(String iKey){
        this.instrumentationKey = iKey;
    }
}
