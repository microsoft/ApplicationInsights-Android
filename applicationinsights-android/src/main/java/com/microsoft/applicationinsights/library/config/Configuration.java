package com.microsoft.applicationinsights.library.config;

import com.microsoft.applicationinsights.library.ApplicationInsights;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class Configuration implements ISenderConfig, ISessionConfig, IQueueConfig {

    // Default values for queue config
    static final int DEBUG_MAX_BATCH_COUNT = 5;
    static final int DEBUG_MAX_BATCH_INTERVAL_MS = 3 * 1000;
    static final int DEFAULT_MAX_BATCH_COUNT = 100;
    static final int DEFAULT_MAX_BATCH_INTERVAL_MS = 15 * 1000;

    // Default values for sender config
    static final String DEFAULT_ENDPOINT_URL = "https://dc.services.visualstudio.com/v2/track";
    static final int DEFAULT_SENDER_READ_TIMEOUT = 10 * 1000;
    static final int DEFAULT_SENDER_CONNECT_TIMEOUT = 15 * 1000;

    // Default values for session config
    protected static final int DEFAULT_SESSION_INTERVAL = 20 * 1000; // 20 seconds

    /**
     * The maximum size of a batch in bytes
     */
    private AtomicInteger maxBatchCount;

    /**
     * The maximum interval allowed between calls to batchInvoke
     */
    private AtomicInteger maxBatchIntervalMs;

    /**
     * The url to which payloads will be sent
     */
    private String endpointUrl;

    /**
     * The timeout for reading the response from the data collector endpoint
     */

    private AtomicInteger senderReadTimeoutMs;

    /**
     * The timeout for connecting to the data collector endpoint
     */
    private AtomicInteger senderConnectTimeoutMs;

    /**
     * The interval at which sessions are renewed
     */
    private AtomicLong sessionIntervalMs;

    /**
     * Constructs a new INSTANCE of a config
     */
    public Configuration() {


        // Initialize default values for queue config
        //TODO: If running on a device with developer mode enabled, the default values will be set (move to getter)
        this.maxBatchCount = new AtomicInteger(DEFAULT_MAX_BATCH_COUNT);
        this.maxBatchIntervalMs = new AtomicInteger(DEFAULT_MAX_BATCH_INTERVAL_MS);

        // Initialize default values for sender config
        this.endpointUrl = DEFAULT_ENDPOINT_URL;
        this.senderReadTimeoutMs = new AtomicInteger(DEFAULT_SENDER_READ_TIMEOUT);
        this.senderConnectTimeoutMs = new AtomicInteger(DEFAULT_SENDER_CONNECT_TIMEOUT);

        // Initialize default values for session config
        this.sessionIntervalMs = new AtomicLong(DEFAULT_SESSION_INTERVAL);
    }

    /**
     * Get the maximum size of a batch in bytes.
     *
     * @return the max batch count until we send a bundle of data to the server
     */
    public int getMaxBatchCount() {
        if(ApplicationInsights.isDeveloperMode()){
            return DEBUG_MAX_BATCH_COUNT;
        }else{
            return this.maxBatchCount.get();
        }
    }

    /**
     * Set the maximum size of a batch in bytes.
     *
     * @param maxBatchCount the batchsize of data that will be queued until we send/persist it
     */
    public void setMaxBatchCount(int maxBatchCount) {
        this.maxBatchCount.set(maxBatchCount);
    }

    /**
     * Get the maximum interval allowed between calls to batchInvoke.
     *
     * @return the interval until we send/persist queued up data
     */
    public int getMaxBatchIntervalMs() {
        if(ApplicationInsights.isDeveloperMode()){
            return DEBUG_MAX_BATCH_INTERVAL_MS;
        }else{
            return this.maxBatchIntervalMs.get();
        }
    }

    /**
     * Set the maximum interval allowed between calls to batchInvoke.
     *
     * @param maxBatchIntervalMs the amount of MS until we want to send out a batch of data
     */
    public void setMaxBatchIntervalMs(int maxBatchIntervalMs) {
        this.maxBatchIntervalMs.set(maxBatchIntervalMs);
    }

    /**
     * Get the url to which payloads will be sent.
     *
     * @return the server's endpoint URL
     */
    public synchronized String getEndpointUrl() {
        return this.endpointUrl;
    }

    /**
     * Set the url to which payloads will be sent.
     *
     * @param endpointUrl url of the server that receives our data
     */
    public synchronized void setEndpointUrl(String endpointUrl) {
        this.endpointUrl = endpointUrl;
    }

    /**
     * Get the timeout for reading the response from the data collector endpoint.
     *
     * @return configured timeout in ms for reading
     */
    public int getSenderReadTimeout() {
        return this.senderReadTimeoutMs.get();
    }

    /**
     * Set the timeout for reading the response from the data collector endpoint.
     *
     * @param senderReadTimeout the timeout for reading the response from the endpoint
     */
    public void setSenderReadTimeout(int senderReadTimeout) {
        this.senderReadTimeoutMs.set(senderReadTimeout);
    }

    /**
     * Get the timeout for connecting to the data collector endpoint.
     *
     * @return configured timeout in ms for sending
     */
    public int getSenderConnectTimeout() {
        return this.senderConnectTimeoutMs.get();
    }

    /**
     * Set the timeout for connecting to the data collector endpoint.
     *
     * @param senderConnectTimeout the timeout for connecting to the data collector endpoint in Ms
     */
    public void setSenderConnectTimeout(int senderConnectTimeout) {
        this.senderConnectTimeoutMs.set(senderConnectTimeout);
    }

    /**
     * Get the interval at which sessions are renewed.
     */
    public long getSessionIntervalMs() {
        return sessionIntervalMs.get();
    }

    /**
     * Set the interval at which sessions are renewed.
     *
     * @param sessionIntervalMs  the interval at which sessions are renewed in Ms
     */
    public void setSessionIntervalMs(long sessionIntervalMs) {
        this.sessionIntervalMs.set(sessionIntervalMs);
    }
}
