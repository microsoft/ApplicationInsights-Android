package com.microsoft.applicationinsights.channel;

import java.util.LinkedHashMap;

/**
 * This interface will provide context for the telemetry channel
 */
public interface ITelemetryContext {
    public LinkedHashMap<String, String> toHashMap();
    public LinkedHashMap<String, String> getProperties();
}
