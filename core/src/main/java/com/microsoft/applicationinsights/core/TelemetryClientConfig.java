package com.microsoft.applicationinsights.core;

import com.microsoft.applicationinsights.channel.Sender;
import com.microsoft.applicationinsights.channel.SenderConfig;

/**
 * Configuration object when instantiating TelemetryClient
 */
public class TelemetryClientConfig {

    public static final int defaultSessionRenewalMs = 30 * 60 * 1000; // 30 minutes
    public static final int defaultSessionExpirationMs = 24 * 60 * 60 * 1000; // 24 hours

    /**
     * The instrumentation key for this telemetryContext
     */
    public String instrumentationKey;

    /**
     * The account id for this telemetryContext
     */
    public String accountId;

    /**
     * The number of milliseconds which must expire before a session is renewed.
     */
    public int sessionRenewalMs;

    /**
     * The number of milliseconds until a session expires.
     */
    public int sessionExpirationMs;

    /**
     * @return The sender instance for this channel
     */
    public SenderConfig getSenderConfig() {
        return Sender.instance.getConfig();
    }

    /**
     * Constructs a new instance of the TelemetryClientConfig
     * @param iKey The instrumentation key
     */
    public TelemetryClientConfig(String iKey){
        this.instrumentationKey = iKey;
        this.accountId = null;
        this.sessionExpirationMs = TelemetryClientConfig.defaultSessionExpirationMs;
        this.sessionRenewalMs = TelemetryClientConfig.defaultSessionRenewalMs;
    }
}
