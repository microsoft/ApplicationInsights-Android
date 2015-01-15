package com.microsoft.applicationinsights;

import android.content.Context;

import com.microsoft.applicationinsights.channel.TelemetryContext;
import com.microsoft.applicationinsights.channel.TelemetryChannel;
import com.microsoft.applicationinsights.common.AbstractTelemetryClient;

import java.util.LinkedHashMap;

/**
 * The public API for recording application insights telemetry.
 * Users would call TelemetryClient.track*
 */
public class TelemetryClient extends
        AbstractTelemetryClient<TelemetryClientConfig, TelemetryContext, TelemetryChannel> {

    // todo: write queued telemetry to persistent storage on app exit

    /**
     * Constructor of the class TelemetryClient.
     * 
     * @param iKey the instrumentation key
     * @param context application telemetryContext from the caller
     */
    public TelemetryClient(String iKey, Context context) {
        this(new TelemetryClientConfig(iKey, context));
    }

    /**
     * Constructor of the class TelemetryClient.
     *
     * @param config the configuration for this client
     */
    public TelemetryClient(TelemetryClientConfig config) {
        this(config, new TelemetryContext(config), new TelemetryChannel(config));
    }

    /**
     * Constructor of the class TelemetryClient.
     *
     * @param config the configuration for this client
     * @param context application telemetryContext from the caller
     * @param channel the channel for this client
     */
    private TelemetryClient(
            TelemetryClientConfig config,
            TelemetryContext context,
            TelemetryChannel channel) {
        super(config, context, channel);
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
