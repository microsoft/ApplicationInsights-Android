package com.microsoft.commonlogging.channel;

public class TelemetryQueueConfig {

    public static final int defaultMaxBatchCount = 100;
    public static final int defaultMaxBatchIntervalMs = 15 * 1000; // 15 seconds
    public static final String defaultEndpointUrl = "https://dc.services.visualstudio.com/v2/track";
    public static final boolean defaultDisableTelemetry = false;
    public static final boolean defaultDeveloperMode = false;

    /**
     * Lock object to ensure thread safety of the configuration
     */
    private final Object lock;

    /**
     * The maximum size of a batch in bytes
     */
    private int maxBatchCount;

    /**
     * The maximum interval allowed between calls to batchInvoke
     */
    private int maxBatchIntervalMs;

    /**
     * The url to which payloads will be sent
     */
    private String endpointUrl;

    /**
     * The master off switch.  Do not send any data if set to TRUE
     */
    private boolean telemetryDisabled;

    /**
     * The flag to enable developer mode logging
     */
    private boolean developerMode;

    /**
     * Gets the maximum size of a batch in bytes
     */
    public int getMaxBatchCount() {
        return maxBatchCount;
    }

    /**
     * Sets the maximum size of a batch in bytes
     */
    public void setMaxBatchCount(int maxBatchCount) {
        synchronized (this.lock) {
            this.maxBatchCount = maxBatchCount;
        }
    }

    /**
     * Get the flag to enable developer mode logging
     */
    public boolean isDeveloperMode() {
        return this.developerMode;
    }

    /**
     * Set the flag to enable developer mode logging
     */
    public void setDeveloperMode(boolean enableDeveloperMode) {
        this.developerMode = enableDeveloperMode;
    }

    /**
     * Gets the maximum interval allowed between calls to batchInvoke
     */
    public int getMaxBatchIntervalMs() {
        return maxBatchIntervalMs;
    }

    /**
     * Sets the maximum interval allowed between calls to batchInvoke
     */
    public void setMaxBatchIntervalMs(int maxBatchIntervalMs) {
        synchronized (this.lock) {
            this.maxBatchIntervalMs = maxBatchIntervalMs;
        }
    }

    /**
     * Gets the url to which payloads will be sent
     */
    public String getEndpointUrl() {
        return endpointUrl;
    }

    /**
     * Sets the url to which payloads will be sent
     */
    public void setEndpointUrl(String endpointUrl) {
        synchronized (this.lock) {
            this.endpointUrl = endpointUrl;
        }
    }

    /**
     * Gets the value of the master off switch. No data is queued when TRUE
     */
    public boolean isTelemetryDisabled() {
        return telemetryDisabled;
    }

    /**
     * Sets the master off switch.  Do not send any data if set to TRUE
     */
    public void setTelemetryDisabled(boolean disableTelemetry) {
        synchronized (this.lock) {
            this.telemetryDisabled = disableTelemetry;
        }
    }

    /**
     * Constructs a new instance of the sender config
     */
    public TelemetryQueueConfig() {
        this.lock = new Object();
        this.maxBatchCount = TelemetryQueueConfig.defaultMaxBatchCount;
        this.maxBatchIntervalMs = TelemetryQueueConfig.defaultMaxBatchIntervalMs;
        this.endpointUrl = TelemetryQueueConfig.defaultEndpointUrl;
        this.telemetryDisabled = TelemetryQueueConfig.defaultDisableTelemetry;
        this.developerMode = TelemetryQueueConfig.defaultDeveloperMode;
    }
}