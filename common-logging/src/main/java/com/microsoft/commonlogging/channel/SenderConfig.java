
package com.microsoft.commonlogging.channel;

public class SenderConfig {

    public static final String defaultEndpointUrl = "https://dc.services.visualstudio.com/v2/track";
    public static final boolean defaultDisableTelemetry = false;

    /**
     * Lock object to ensure thread safety of the configuration
     */
    private final Object lock;

    /**
     * The url to which payloads will be sent
     */
    private String endpointUrl;

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
        synchronized (this.lock) {
            this.endpointUrl = endpointUrl;
        }
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
        synchronized (this.lock) {
            this.telemetryDisabled = disableTelemetry;
        }
    }

    /**
     * Constructs a new instance of the sender config
     */
    public SenderConfig() {
        this.lock = new Object();
        this.endpointUrl = SenderConfig.defaultEndpointUrl;
        this.telemetryDisabled = SenderConfig.defaultDisableTelemetry;
    }
}