package com.microsoft.applicationinsights;

import com.microsoft.applicationinsights.channel.TelemetryChannel;
import com.microsoft.applicationinsights.channel.TelemetryContext;
import com.microsoft.applicationinsights.channel.Util;
import com.microsoft.applicationinsights.channel.contracts.DataPoint;
import com.microsoft.applicationinsights.channel.contracts.DataPointType;
import com.microsoft.applicationinsights.channel.contracts.EventData;
import com.microsoft.applicationinsights.channel.contracts.ExceptionData;
import com.microsoft.applicationinsights.channel.contracts.MessageData;
import com.microsoft.applicationinsights.channel.contracts.MetricData;
import com.microsoft.applicationinsights.channel.contracts.PageViewData;
import com.microsoft.applicationinsights.channel.contracts.RequestData;
import com.microsoft.applicationinsights.channel.contracts.shared.ITelemetry;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;

/**
 * The public API for recording application insights telemetry.
 * Users would call TelemetryClient.track*
 */
public abstract class AbstractTelemetryClient<TConfig extends TelemetryClientConfig> {

    /**
     * The configuration for this telemetry client.
     */
    public final TConfig config;

    /**
     * The telemetry telemetryContext object.
     */
    protected TelemetryContext telemetryContext;

    /**
     * The telemetry channel for this client.
     */
    protected TelemetryChannel channel;

    /**
     * Force inheritance via a protected constructor
     */
    protected AbstractTelemetryClient(TConfig config) {
        this.config = config;
        this.telemetryContext = new TelemetryContext(this.config);
        this.channel = new TelemetryChannel(this.config);
    }

    /**
     * track the event by name.
     *
     * @param eventName
     */
    public void trackEvent(String eventName) {
        trackEvent(eventName, null, null);
    }

    /**
     * track the event by name.
     *
     * @param eventName
     */
    public void trackEvent(String eventName, LinkedHashMap<String, String> properties) {
        trackEvent(eventName, properties, null);
    }

    /**
     * Track the event by event name and customized properties and metrics.
     *
     * @param eventName  event name
     * @param properties custom properties
     * @param measurements    custom metrics
     */
    public void trackEvent(
            String eventName,
            LinkedHashMap<String, String> properties,
            LinkedHashMap<String, Double> measurements) {

        EventData telemetry = new EventData();

        telemetry.setName(eventName);
        telemetry.setProperties(properties);
        telemetry.setMeasurements(measurements);

        track(telemetry, EventData.EnvelopeName, EventData.BaseType);
    }

    /**
     * track with the message.
     *
     * @param message message for transmission to Application insight
     */
    public void trackTrace(String message) {
        trackTrace(message, null);
    }

    /**
     * track with the message and properties.
     *
     * @param message    message for transmission to Application insight
     * @param properties properties of the message
     */
    public void trackTrace(String message, LinkedHashMap<String, String> properties) {
        MessageData telemetry = new MessageData();

        telemetry.setMessage(message);
        telemetry.setProperties(properties);

        track(telemetry, MessageData.EnvelopeName, MessageData.BaseType);
    }

    /**
     * track the metric.
     *
     * @param name  name of the metric
     * @param value value of the metric
     */
    public void trackMetric(String name, Double value) {
        this.trackMetric(name, value, null);
    }

    /**
     * Track the metric with properties.
     *
     * @param name       metric name
     * @param value      metric value
     * @param properties metric properties
     */
    public void trackMetric(String name, double value, LinkedHashMap<String, String> properties) {
        MetricData telemetry = new MetricData();

        // todo: batch in client
        DataPoint data = new DataPoint();
        data.setCount(1);
        data.setKind(DataPointType.Measurement);
        data.setMax(value);
        data.setMax(value);
        data.setName(name);
        data.setValue(value);
        ArrayList<DataPoint> metricsList = new ArrayList<DataPoint>();
        metricsList.add(data);

        telemetry.setMetrics(metricsList);
        telemetry.setProperties(properties);

        track(telemetry, MetricData.EnvelopeName, MetricData.BaseType);
    }

    /**
     * Track exception with properties.
     *
     * @param exception exception data object
     */
    public void trackException(ExceptionData exception) {
        this.trackException(exception, null);
    }

    /**
     * Track exception with properties.
     *
     * @param exception  exception data object
     * @param properties exception properties
     */
    public void trackException(ExceptionData exception, LinkedHashMap<String, String> properties) {
        ExceptionData localException = exception;
        if (localException == null) {
            localException = new ExceptionData();
        }

        // todo: fill in data and accept a java Exception object as input
        exception.setProperties(properties);

        track(localException, ExceptionData.EnvelopeName, ExceptionData.BaseType);
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
    protected void trackPageView(
            String pageName,
            String url,
            long pageLoadDurationMs,
            LinkedHashMap<String, String> properties,
            LinkedHashMap<String, Double> measurements) {

        PageViewData telemetry = new PageViewData();

        telemetry.setName(pageName);
        telemetry.setUrl(url);
        telemetry.setDuration(Util.msToTimeSpan(pageLoadDurationMs));
        telemetry.setProperties(properties);
        telemetry.setMeasurements(measurements);

        track(telemetry, PageViewData.EnvelopeName, PageViewData.BaseType);
    }

    /**
     * Sends information about a request to Application Insights.
     *
     * @param name hte name of this request
     * @param url the url for this request
     * @param httpMethod the http method for this request
     * @param startTime the start time of this request
     * @param durationMs the duration of this request in milliseconds
     * @param responseCode the response code for this request
     * @param isSuccess the success status of this request
     * @param properties custom properties
     * @param measurements    custom metrics
     */
    protected void trackRequest(
            String name,
            String url,
            String httpMethod,
            Date startTime,
            long durationMs,
            int responseCode,
            boolean isSuccess,
            LinkedHashMap<String, String> properties,
            LinkedHashMap<String, Double> measurements) {

        RequestData telemetry = new RequestData();

        telemetry.setName(name);
        telemetry.setUrl(url);
        telemetry.setHttpMethod(httpMethod);
        telemetry.setStartTime(Util.dateToISO8601(startTime));
        telemetry.setDuration(Util.msToTimeSpan(durationMs));
        telemetry.setResponseCode(String.valueOf(responseCode));
        telemetry.setSuccess(isSuccess);
        telemetry.setProperties(properties);
        telemetry.setMeasurements(measurements);

        track(telemetry, RequestData.EnvelopeName, RequestData.BaseType);
    }

    /**
     * Send message to the channel.
     *
     * @param telemetry    telemetry object
     * @param itemDataType data type
     * @param itemType     item type
     */
    protected void track(ITelemetry telemetry, String itemDataType, String itemType) {
        this.channel.send(this.telemetryContext, telemetry, itemDataType, itemType);
    }
}
