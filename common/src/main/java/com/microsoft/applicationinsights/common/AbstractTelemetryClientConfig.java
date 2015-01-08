package com.microsoft.applicationinsights.common;

import com.microsoft.applicationinsights.channel.IChannelConfig;
import com.microsoft.applicationinsights.channel.IContextConfig;
import com.microsoft.applicationinsights.channel.Sender;
import com.microsoft.applicationinsights.channel.SenderConfig;

/**
 * Configuration object when instantiating TelemetryClient
 */
public abstract class AbstractTelemetryClientConfig implements IChannelConfig, IContextConfig {

    /**
     * The instrumentation key for this telemetryContext
     */
    private String instrumentationKey;

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
     * The sender instance configuration for this channel.
     */
    private SenderConfig senderConfig;

    /**
     * Gets the instrumentation key for this telemetryContext
     */
    public String getInstrumentationKey() {
        return instrumentationKey;
    }

    /**
     * Sets the instrumentation key for this telemetryContext
     */
    public void setInstrumentationKey(String instrumentationKey) {
        this.instrumentationKey = instrumentationKey;
    }

    /**
     * Gets the account id for this telemetryContext
     */
    public String getAccountId() {
        return accountId;
    }

    /**
     * Sets the account id for this telemetryContext
     */
    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    /**
     * Gets the number of milliseconds which must expire before a session is renewed.
     */
    public int getSessionRenewalMs() {
        return sessionRenewalMs;
    }

    /**
     * Sets the number of milliseconds which must expire before a session is renewed.
     */
    public void setSessionRenewalMs(int sessionRenewalMs) {
        this.sessionRenewalMs = sessionRenewalMs;
    }

    /**
     * Gets the number of milliseconds until a session expires.
     */
    public int getSessionExpirationMs() {
        return sessionExpirationMs;
    }

    /**
     * Sets thenumber of milliseconds until a session expires.
     */
    public void setSessionExpirationMs(int sessionExpirationMs) {
        this.sessionExpirationMs = sessionExpirationMs;
    }

    /**
     * Gets the sender instance configuration for this channel.
     */
    public SenderConfig getSenderConfig() {
        return senderConfig;
    }

    /**
     * Constructs a new instance of the TelemetryClientConfig
     * @param iKey The instrumentation key
     */
    protected AbstractTelemetryClientConfig(String iKey){
        this.instrumentationKey = iKey;
        this.accountId = null;
        this.sessionExpirationMs = IContextConfig.defaultSessionExpirationMs;
        this.sessionRenewalMs = IContextConfig.defaultSessionRenewalMs;
        this.senderConfig = Sender.instance.getConfig();
    }
}
