package com.microsoft.applicationinsights.library.config;

public class SenderConfig extends Config{

    private static final String DEFAULT_ENDPOINT_URL = "https://dc.services.visualstudio.com/v2/track";
    private static final int DEFAULT_SENDER_READ_TIMEOUT = 10 * 1000;
    private static final int DEFAULTSENDER_CONNECT_TIMEOUT = 15 * 1000;

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
     * Constructs a new INSTANCE of the sender config
     */
    public SenderConfig() {
        super();
        this.endpointUrl = DEFAULT_ENDPOINT_URL;
        this.senderReadTimeoutMs = DEFAULT_SENDER_READ_TIMEOUT;
        this.senderConnectTimeoutMs = DEFAULTSENDER_CONNECT_TIMEOUT;
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
}
