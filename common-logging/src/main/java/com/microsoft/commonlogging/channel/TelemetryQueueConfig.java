package com.microsoft.commonlogging.channel;

public class TelemetryQueueConfig {

    public static final int defaultMaxBatchCount = 100;
    public static final int defaultMaxBatchIntervalMs = 15 * 1000; // 15 seconds

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
     * Constructs a new instance of the sender config
     */
    public TelemetryQueueConfig() {
        this.lock = new Object();
        this.maxBatchCount = TelemetryQueueConfig.defaultMaxBatchCount;
        this.maxBatchIntervalMs = TelemetryQueueConfig.defaultMaxBatchIntervalMs;
    }
}