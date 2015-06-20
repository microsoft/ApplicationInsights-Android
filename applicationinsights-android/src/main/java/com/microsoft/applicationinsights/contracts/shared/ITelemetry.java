package com.microsoft.applicationinsights.contracts.shared;

import java.util.Map;

public interface ITelemetry extends ITelemetryData {
    /**
     * Gets the properties.
     */
    Map<String, String> getProperties();

    /**
     * Sets the properties.
     */
    void setProperties(Map<String, String> value);

    /**
     * Sets the version
     */
    void setVer(int ver);

    int getVer();
    /**
     * Gets the envelope name for this telemetry object.
     */
    String getEnvelopeName();

    /**
     * Gets the base type for this telemetry object.
     */
    String getBaseType();
}
