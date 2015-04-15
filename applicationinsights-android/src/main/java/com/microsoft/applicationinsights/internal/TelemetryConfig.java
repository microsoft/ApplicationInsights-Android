package com.microsoft.applicationinsights.internal;

import com.microsoft.applicationinsights.ApplicationInsights;

public class TelemetryConfig {

    protected static final int DEBUG_MAX_BATCH_COUNT = 5;
    protected static final int DEBUG_MAX_BATCH_INTERVAL_MS = 3 * 1000;
    public static final int DEFAULT_MAX_BATCH_COUNT = 100;
    public static final int DEFAULT_MAX_BATCH_INTERVAL_MS = 3 * 1000;
    public static final String DEFAULT_ENDPOINT_URL = "https://dc.services.visualstudio.com/v2/track";
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
    private boolean developerMode; //TODO make logging more elegant?

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
    public TelemetryConfig() {
        // TODO: Create several configs for queue and sender
        this.lock = new Object();
        this.maxBatchCount = (ApplicationInsights.isDeveloperMode()) ? TelemetryConfig.DEBUG_MAX_BATCH_COUNT : TelemetryConfig.DEFAULT_MAX_BATCH_COUNT;
        this.maxBatchIntervalMs = (ApplicationInsights.isDeveloperMode()) ? TelemetryConfig.DEBUG_MAX_BATCH_INTERVAL_MS : TelemetryConfig.DEFAULT_MAX_BATCH_INTERVAL_MS;
        this.endpointUrl = TelemetryConfig.DEFAULT_ENDPOINT_URL;
        this.senderReadTimeoutMs = TelemetryConfig.DEFAULT_SENDER_READ_TIMEOUT;
        this.senderConnectTimeoutMs = TelemetryConfig.DEFAULTSENDER_CONNECT_TIMEOUT;
    }

    /**
     * Gets the maximum size of a batch in bytes
     */
    public int getMaxBatchCount() {
        return this.maxBatchCount;
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
     * Gets the maximum interval allowed between calls to batchInvoke
     */
    public int getMaxBatchIntervalMs() {
        return this.maxBatchIntervalMs;
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
        return this.endpointUrl;
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