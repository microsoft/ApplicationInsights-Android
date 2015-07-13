package com.microsoft.applicationinsights.library.config;

public interface IQueueConfig {

    /**
     * Gets the maximum size of a batch in bytes
     * @return the max batch count until we send a bundle of data to the server
     */
    int getMaxBatchCount();

    /**
     * Sets the maximum size of a batch in bytes
     * @param maxBatchCount the batchsize of data that will be queued until we send/writeToDisk it
     */
    void setMaxBatchCount(int maxBatchCount);

    /**
     * Gets the maximum interval allowed between calls to batchInvoke
     * @return the interval until we send/writeToDisk queued up data
     */
    int getMaxBatchIntervalMs();

    /**
     * Sets the maximum interval allowed between calls to batchInvoke
     * @param maxBatchIntervalMs the amount of MS until we want to send out a batch of data
     */
    void setMaxBatchIntervalMs(int maxBatchIntervalMs);
}
