package com.microsoft.applicationinsights.channel;

public class SenderConfig {

    public static final String defaultEndpointUrl = "https://dc.services.visualstudio.com/v2/track";
    public static final int defaultMaxBatchCount = 100;
    public static final int defaultMaxBatchIntervalMs = 15 * 1000; // 15 seconds
    public static final boolean defaultDisableTelemetry = false;

    // todo: make this config thread-safe

    /**
     * The url to which payloads will be sent
     */
    private String endpointUrl;

    /**
     * The maximum size of a batch in bytes
     */
    private int maxBatchCount;

    /**
     * The maximum interval allowed between calls to batchInvoke
     */
    private int maxBatchIntervalMs;

    /**
     * The master off switch.  Do not send any data if set to TRUE
     */
    private boolean telemetryDisabled;

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
        this.endpointUrl = endpointUrl;
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
        this.maxBatchCount = maxBatchCount;
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
        this.maxBatchIntervalMs = maxBatchIntervalMs;
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
        this.telemetryDisabled = disableTelemetry;
    }

    /**
     * Constructs a new instance of the sender config
     */
    public SenderConfig() {
        this.endpointUrl = SenderConfig.defaultEndpointUrl;
        this.maxBatchCount = SenderConfig.defaultMaxBatchCount;
        this.maxBatchIntervalMs = SenderConfig.defaultMaxBatchIntervalMs;
        this.telemetryDisabled = SenderConfig.defaultDisableTelemetry;
    }
}