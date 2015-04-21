package com.microsoft.applicationinsights.library.config;

public interface ISenderConfig{

    /**
     * Gets the url to which payloads will be sent
     *
     * @return the server's endpoint URL
     */
    public String getEndpointUrl();

    /**
     * Sets the url to which payloads will be sent
     *
     * @param endpointUrl url of the server that receives our data
     */
    public void setEndpointUrl(String endpointUrl);

    /**
     * Gets the timeout for reading the response from the data collector endpoint
     *
     * @return configured timeout in ms for reading
     */
    public int getSenderReadTimeout();

    /**
     * Gets the timeout for connecting to the data collector endpoint
     *
     * @return configured timeout in ms for sending
     */
    public int getSenderConnectTimeout();
}
