package com.microsoft.applicationinsights;

import android.content.Context;

import com.microsoft.applicationinsights.channel.AndroidTelemetryContext;
import com.microsoft.applicationinsights.channel.TelemetryChannel;

import java.util.LinkedHashMap;

/**
 * The public API for recording application insights telemetry.
 * Users would call TelemetryClient.track*
 */
public class TelemetryClient extends
        com.microsoft.applicationinsights.core.TelemetryClient{

    /**
     * The telemetry client configuration.
     */
    public TelemetryClientConfig config;

    /**
     * Constructor of the class TelemetryClient.
     * 
     * @param iKey the instrumentation key
     * @param context application telemetryContext from the caller
     */
    public TelemetryClient(String iKey, Context context) {
        super(new TelemetryClientConfig(iKey, context));
        this.config = (TelemetryClientConfig)super.config;
        this.telemetryContext = new AndroidTelemetryContext(this.config);
        this.channel = new TelemetryChannel(this.config);
    }

    /**
     * Sends information about a page view to Application Insights.
     *
     * @param pageName the name of the page
     */
    public void trackPageView(String pageName) {
        this.trackPageView(pageName, 0, null);
    }

    /**
     * Sends information about a page view to Application Insights.
     *
     * @param pageName the name of the page
     * @param pageLoadDurationMs the duration of the page load
     */
    public void trackPageView(String pageName, long pageLoadDurationMs) {
        this.trackPageView(pageName, pageLoadDurationMs, null);
    }

    /**
     * Sends information about a page view to Application Insights.
     *
     * @param pageName the name of the page
     * @param pageLoadDurationMs the duration of the page load
     * @param properties custom properties
     */
    public void trackPageView(
            String pageName,
            long pageLoadDurationMs,
            LinkedHashMap<String, String> properties) {
        this.trackPageView(pageName, null, pageLoadDurationMs, properties, null);
    }

    /**
     * Sends information about a page view to Application Insights.
     *
     * @param pageName the name of the page
     * @param pageLoadDurationMs the duration of the page load
     * @param properties custom properties
     * @param measurements    custom metrics
     */
    public void trackPageView(
            String pageName,
            long pageLoadDurationMs,
            LinkedHashMap<String, String> properties,
            LinkedHashMap<String, Double> measurements) {
        super.trackPageView(pageName, null, pageLoadDurationMs, properties, measurements);
    }
}
