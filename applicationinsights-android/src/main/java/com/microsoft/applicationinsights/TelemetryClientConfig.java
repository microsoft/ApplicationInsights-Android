package com.microsoft.applicationinsights;

import android.content.Context;

import com.microsoft.commonlogging.channel.TelemetryChannelConfig;

/**
 * Configuration object when instantiating TelemetryClient
 */
public class TelemetryClientConfig extends TelemetryChannelConfig {

    /**
     * The interval at which sessions are renewed
     */
    protected static final int SessionInterval = 20 * 1000; // 20 seconds

    /**
     * The interval at which sessions are renewed
     */
    private long sessionIntervalMS;

    /**
     * Gets the interval at which sessions are renewed
     */
    public long getSessionIntervalMS() {
        return sessionIntervalMS;
    }

    /**
     * Sets the interval at which sessions are renewed
     */
    public void setSessionIntervalMS(long sessionIntervalMS) {
        sessionIntervalMS = sessionIntervalMS;
    }

    /**
     * Constructs a new instance of TelemetryClientConfig
     *
     * @param context The android app context
     */
    public TelemetryClientConfig(Context context) {
        super(context);
        this.sessionIntervalMS = TelemetryClientConfig.SessionInterval;
    }
}
