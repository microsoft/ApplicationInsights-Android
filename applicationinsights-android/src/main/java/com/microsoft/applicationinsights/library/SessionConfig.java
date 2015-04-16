package com.microsoft.applicationinsights.library;

import android.content.Context;

/**
 * Configuration object when instantiating TelemetryClient
 */
public class SessionConfig extends ChannelConfig {

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
    protected SessionConfig(Context context) {
        super(context);
        this.sessionIntervalMs = SessionConfig.SESSION_INTERVAL;
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
