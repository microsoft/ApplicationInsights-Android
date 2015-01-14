package com.microsoft.applicationinsights;

import com.microsoft.applicationinsights.channel.TelemetryContext;
import com.microsoft.applicationinsights.common.AbstractTelemetryClient;

import java.util.Date;
import java.util.LinkedHashMap;

/**
 * A basic implementation of the SDK for Java 1.6 (no android dependencies).
 */
public class TelemetryClient extends
        AbstractTelemetryClient<TelemetryClientConfig, TelemetryContext> {

    /**
     * Construct a new instance of the telemetry client
     */
    public TelemetryClient(String instrumentationKey) {
        this(new TelemetryClientConfig(instrumentationKey));
    }

    /**
     * Construct a new instance of the telemetry client
     */
    private TelemetryClient(TelemetryClientConfig config) {
        super(config, new TelemetryContext(config));
    }

    /**
     * Sends information about a page view to Application Insights.
     *
     * @param pageName the name of the page
     * @param url the url of the page
     * @param pageLoadDurationMs the duration of the page load
     * @param properties custom properties
     * @param measurements    custom metrics
     */
    public void trackPageView(
            String pageName,
            String url,
            long pageLoadDurationMs,
            LinkedHashMap<String, String> properties,
            LinkedHashMap<String, Double> measurements) {
        super.trackPageView(pageName, url, pageLoadDurationMs, properties, measurements);
    }

    /**
     * Sends information about a request to Application Insights.
     *
     * @param name the name of this request
     * @param url the url for this request
     * @param httpMethod the http method for this request
     * @param startTime the start time of this request
     * @param durationMs the duration of this request in milliseconds
     * @param responseCode the response code for this request
     * @param isSuccess the success status of this request
     * @param properties custom properties
     * @param measurements    custom metrics
     */
    public void trackRequest(
            String name,
            String url,
            String httpMethod,
            Date startTime,
            long durationMs,
            int responseCode,
            boolean isSuccess,
            LinkedHashMap<String, String> properties,
            LinkedHashMap<String, Double> measurements) {
        super.trackRequest(name, url, httpMethod, startTime, durationMs,
                responseCode, isSuccess, properties, measurements);
    }
}
