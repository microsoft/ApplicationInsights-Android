package com.microsoft.applicationinsights;

import android.content.Context;

import com.microsoft.applicationinsights.channel.TelemetryChannelConfig;

/**
 * Configuration object when instantiating TelemetryClient
 */
// TODO: Rename class to something like SessionConfig
public class TelemetryClientConfig extends TelemetryChannelConfig {

    /**
     * The interval at which sessions are renewed
     */
    protected static final int SESSION_INTERVAL = 20 * 1000; // 20 seconds

    /**
     * The interval at which sessions are renewed
     */
    private long sessionIntervalMs;

    /**
     * Constructs a new INSTANCE of TelemetryClientConfig
     *
     * @param context The android app context
     */
    public TelemetryClientConfig(Context context) {
        super(context);
        this.sessionIntervalMs = TelemetryClientConfig.SESSION_INTERVAL;
    }

    /**
     * Gets the interval at which sessions are renewed
     */
    public long getSessionIntervalMs() {
        return sessionIntervalMs;
    }

    /**
     * Sets the interval at which sessions are renewed
     */
    public void setSessionIntervalMs(long sessionIntervalMs) {
        this.sessionIntervalMs = sessionIntervalMs;
    }
}
