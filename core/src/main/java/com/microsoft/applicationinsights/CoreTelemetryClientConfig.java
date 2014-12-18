package com.microsoft.applicationinsights;

import com.microsoft.applicationinsights.channel.IChannelConfig;
import com.microsoft.applicationinsights.channel.IContextConfig;
import com.microsoft.applicationinsights.channel.SenderConfig;

/**
 * Configuration object when instantiating TelemetryClient
 */
public class CoreTelemetryClientConfig extends SenderConfig implements IChannelConfig, IContextConfig {

    /**
     * The instrumentation key for this telemetryContext
     */
    protected final String instrumentationKey;

    /**
     * The account id for this telemetryContext
     */
    private String accountId;

    /**
     * The number of milliseconds which must expire before a session is renewed.
     */
    private int sessionRenewalMs;

    /**
     * The number of milliseconds until a session expires.
     */
    private int sessionExpirationMs;

    /**
     * The instrumentation key for this telemetryContext
     */
    public String getInstrumentationKey() {
        return this.instrumentationKey;
    }

    /**
     * The account id for this telemetryContext
     */
    public String getAccountId() {
        return accountId;
    }

    /**
     * The number of milliseconds which must expire before a session is renewed.
     */
    public int getSessionRenewalMs() {
        return sessionRenewalMs;
    }

    /**
     * The number of milliseconds until a session expires.
     */
    public int getSessionExpirationMs() {
        return sessionExpirationMs;
    }

    public CoreTelemetryClientConfig(String iKey){
        this.instrumentationKey = iKey;
        this.sessionExpirationMs = 24 * 60 * 60 * 1000; // 24 hours
        this.sessionRenewalMs = 30 * 60 * 1000; // 30 minutes
    }
}
