package com.microsoft.applicationinsights.library.config;

import com.microsoft.applicationinsights.library.ApplicationInsights;

public class ApplicationInsightsConfig implements ISenderConfig, ISessionConfig, IQueueConfig {

    // Default values for queue config
    private static final int DEBUG_MAX_BATCH_COUNT = 5;
    private static final int DEBUG_MAX_BATCH_INTERVAL_MS = 3 * 1000;
    private static final int DEFAULT_MAX_BATCH_COUNT = 100;
    private static final int DEFAULT_MAX_BATCH_INTERVAL_MS = 15 * 1000;

    // Default values for sender config
    private static final String DEFAULT_ENDPOINT_URL = "https://dc.services.visualstudio.com/v2/track";
    private static final int DEFAULT_SENDER_READ_TIMEOUT = 10 * 1000;
    private static final int DEFAULTSENDER_CONNECT_TIMEOUT = 15 * 1000;

    // Default values for session config
    protected static final int DEFAULT_SESSION_INTERVAL = 20 * 1000; // 20 seconds
    
    /**
     * Lock object to ensure thread safety of the configuration
     */
    protected final Object lock;

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
     * The timeout for reading the response from the data collector endpoint
     */

    private int senderReadTimeoutMs;

    /**
     * The timeout for connecting to the data collector endpoint
     */
    private int senderConnectTimeoutMs;

    /**
     * The interval at which sessions are renewed
     */
    private long sessionIntervalMs;

    /**
     * Constructs a new INSTANCE of a config
     */
    public ApplicationInsightsConfig() {
        this.lock = new Object();

        // Initialize default values for queue config
        this.maxBatchCount = (ApplicationInsights.isDeveloperMode()) ? DEBUG_MAX_BATCH_COUNT : DEFAULT_MAX_BATCH_COUNT;
        this.maxBatchIntervalMs = (ApplicationInsights.isDeveloperMode()) ? DEBUG_MAX_BATCH_INTERVAL_MS : DEFAULT_MAX_BATCH_INTERVAL_MS;

        // Initialize default values for sender config
        this.endpointUrl = DEFAULT_ENDPOINT_URL;
        this.senderReadTimeoutMs = DEFAULT_SENDER_READ_TIMEOUT;
        this.senderConnectTimeoutMs = DEFAULTSENDER_CONNECT_TIMEOUT;

        // Initialize default values for session config
        this.sessionIntervalMs = DEFAULT_SESSION_INTERVAL;
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

    /**
     * Gets the url to which payloads will be sent
     *
     * @return the server's endpoint URL
     */
    public String getEndpointUrl() {
        return this.endpointUrl;
    }

    /**
     * Sets the url to which payloads will be sent
     *
     * @param endpointUrl url of the server that receives our data
     */
    public void setEndpointUrl(String endpointUrl) {
        synchronized (this.lock) {
            this.endpointUrl = endpointUrl;
        }
    }

    /**
     * Gets the timeout for reading the response from the data collector endpoint
     *
     * @return configured timeout in ms for reading
     */
    public int getSenderReadTimeout() {
        return this.senderReadTimeoutMs;
    }

    /**
     * Gets the timeout for connecting to the data collector endpoint
     *
     * @return configured timeout in ms for sending
     */
    public int getSenderConnectTimeout() {
        return this.senderConnectTimeoutMs;
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
