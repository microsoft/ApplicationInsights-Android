package com.microsoft.applicationinsights.channel;

public class TelemetryQueueConfig {

    public static final int DEFAULT_MAX_BATCH_COUNT = 100;
    public static final int DEFAULT_MAX_BATCH_INTERVAL_MS = 3 * 1000;
    public static final String DEFAULT_ENDPOINT_URL = "https://dc.services.visualstudio.com/v2/track";
    public static final boolean DEFAULT_DISABLE_TELEMETRY = false;
    public static final boolean DEFAULT_DEVELOPER_MODE = false;
    public static final int DEFAULT_SENDER_READ_TIMEOUT = 10 * 1000;
    public static final int DEFAULTSENDER_CONNECT_TIMEOUT = 15 * 1000;

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
     * The master off switch.  Do not enqueue any data if set to TRUE
     */
    private boolean telemetryDisabled;

    /**
     * The flag to enable developer mode logging
     */
    private boolean developerMode;

    /**
     * The timeout for reading the response from the data collector endpoint
     */

    private int senderReadTimeoutMs;

    /**
     * The timeout for connecting to the data collector endpoint
     */
    private int senderConnectTimeoutMs;

    /**
     * Constructs a new INSTANCE of the sender config
     */
    public TelemetryQueueConfig() {
        // TODO: Create several configs for queue and sender
        this.lock = new Object();
        this.maxBatchCount = TelemetryQueueConfig.DEFAULT_MAX_BATCH_COUNT;
        this.maxBatchIntervalMs = TelemetryQueueConfig.DEFAULT_MAX_BATCH_INTERVAL_MS;
        this.endpointUrl = TelemetryQueueConfig.DEFAULT_ENDPOINT_URL;
        this.telemetryDisabled = TelemetryQueueConfig.DEFAULT_DISABLE_TELEMETRY;
        this.developerMode = TelemetryQueueConfig.DEFAULT_DEVELOPER_MODE;
        this.senderReadTimeoutMs = TelemetryQueueConfig.DEFAULT_SENDER_READ_TIMEOUT;
        this.senderConnectTimeoutMs = TelemetryQueueConfig.DEFAULTSENDER_CONNECT_TIMEOUT;
    }

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
     * Sets the master off switch.  Do not enqueue any data if set to TRUE
     */
    public void setTelemetryDisabled(boolean disableTelemetry) {
        synchronized (this.lock) {
            this.telemetryDisabled = disableTelemetry;
        }
    }

    /**
     * Gets the timeout for reading the response from the data collector endpoint
     */
    public int getSenderReadTimeout() {
        return this.senderReadTimeoutMs;
    }

    /**
     * Gets the timeout for connecting to the data collector endpoint
     */
    public int getSenderConnectTimeout() {
        return this.senderConnectTimeoutMs;
    }
}