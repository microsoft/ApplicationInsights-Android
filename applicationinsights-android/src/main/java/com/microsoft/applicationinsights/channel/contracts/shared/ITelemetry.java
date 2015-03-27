package com.microsoft.applicationinsights.channel.contracts.shared;

import java.util.Map;

public interface ITelemetry extends ITelemetryData {
    /**
     * Gets the properties.
     */
    public Map<String, String> getProperties();

    /**
     * Sets the properties.
     */
    public void setProperties(Map<String, String> value);

    /**
     * Sets the version
     */
    public void setVer(int ver);

    /**
     * Gets the envelope name for this telemetry object.
     */
    public String getEnvelopeName();

    /**
     * Gets the base type for this telemetry object.
     */
    public String getBaseType();
}
