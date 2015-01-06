package com.microsoft.applicationinsights.channel.contracts.shared;

import java.util.LinkedHashMap;

public interface ITelemetry extends ITelemetryData {
    /**
     * Gets the properties.
     */
    public LinkedHashMap<String, String> getProperties();

	/**
     * Sets the properties.
     */
    public void setProperties(LinkedHashMap<String, String> value);

    /**
     * Sets the version
     */
    public void setVer(int ver);
}
