package com.microsoft.applicationinsights.library.config;

import com.microsoft.applicationinsights.library.ApplicationInsights;

public class QueueConfig extends Config {

    private static final int DEBUG_MAX_BATCH_COUNT = 5;
    private static final int DEBUG_MAX_BATCH_INTERVAL_MS = 3 * 1000;
    private static final int DEFAULT_MAX_BATCH_COUNT = 100;
    private static final int DEFAULT_MAX_BATCH_INTERVAL_MS = 3 * 1000;

    /**
     * The maximum size of a batch in bytes
     */
    private int maxBatchCount;

    /**
     * The maximum interval allowed between calls to batchInvoke
     */
    private int maxBatchIntervalMs;

    /**
     * Constructs a new INSTANCE of the sender config
     */
    public QueueConfig() {
        super();
        this.maxBatchCount = (ApplicationInsights.isDeveloperMode()) ? DEBUG_MAX_BATCH_COUNT : DEFAULT_MAX_BATCH_COUNT;
        this.maxBatchIntervalMs = (ApplicationInsights.isDeveloperMode()) ? DEBUG_MAX_BATCH_INTERVAL_MS : DEFAULT_MAX_BATCH_INTERVAL_MS;
    }

    /**
     * Gets the maximum size of a batch in bytes
     * @return the max batch count until we send a bundle of data to the server
     */
    public int getMaxBatchCount() {
        return this.maxBatchCount;
    }

    /**
     * Sets the maximum size of a batch in bytes
     * @param maxBatchCount the batchsize of data that will be queued until we send/persist it
     */
    public void setMaxBatchCount(int maxBatchCount) {
        synchronized (this.lock) {
            this.maxBatchCount = maxBatchCount;
        }
    }

    /**
     * Gets the maximum interval allowed between calls to batchInvoke
     * @return the interval until we send/persist queued up data
     */
    public int getMaxBatchIntervalMs() {
        return this.maxBatchIntervalMs;
    }

    /**
     * Sets the maximum interval allowed between calls to batchInvoke
     * @param maxBatchIntervalMs the amount of MS until we want to send out a batch of data
     */
    public void setMaxBatchIntervalMs(int maxBatchIntervalMs) {
        synchronized (this.lock) {
            this.maxBatchIntervalMs = maxBatchIntervalMs;
        }
    }
}