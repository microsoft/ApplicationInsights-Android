package com.microsoft.applicationinsights.library.config;

public interface ISenderConfig{

    /**
     * Gets the url to which payloads will be sent
     *
     * @return the server's endpoint URL
     */
    String getEndpointUrl();

    /**
     * Sets the url to which payloads will be sent
     *
     * @param endpointUrl url of the server that receives our data
     */
    void setEndpointUrl(String endpointUrl);

    /**
     * Gets the timeout for reading the response from the data collector endpoint
     *
     * @return configured timeout in ms for reading
     */
    int getSenderReadTimeout();

    /**
     * Set the timeout for reading the response from the data collector endpoint
     */
    void setSenderReadTimeout(int senderReadTimeout);

    /**
     * Gets the timeout for connecting to the data collector endpoint
     *
     * @return configured timeout in ms for sending
     */
    int getSenderConnectTimeout();

    /**
     * Set the timeout for connecting to the data collector endpoint
     */
    void setSenderConnectTimeout(int senderConnectTimeout);
}
