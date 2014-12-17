package com.microsoft.applicationinsights;

import android.text.TextUtils;

import com.microsoft.applicationinsights.channel.TelemetryChannel;
import com.microsoft.applicationinsights.channel.TelemetryContext;
import com.microsoft.applicationinsights.channel.contracts.EventData;
import com.microsoft.applicationinsights.channel.contracts.ExceptionData;
import com.microsoft.applicationinsights.channel.contracts.MessageData;
import com.microsoft.applicationinsights.channel.contracts.MetricData;
import com.microsoft.applicationinsights.channel.contracts.PageViewData;
import com.microsoft.applicationinsights.channel.contracts.shared.ITelemetry;

import java.util.HashMap;

/**
 * The public API for recording application insights telemetry.
 * Users would call TelemetryClient.track*
 */
public class TelemetryClient extends AbstractTelemetryClient {

    /**
     * Constructor of the class TelemetryClient.
     * 
     * @param iKey the instrumentation key
     * @param context application telemetryContext from the caller
     */
    public TelemetryClient(String iKey, android.content.Context context) {
        super(new TelemetryClientConfig(iKey, context));
        this.telemetryContext = new TelemetryContext((TelemetryClientConfig)this.config);
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
    public void trackEvent(String eventName, HashMap<String, String> properties) {
        trackEvent(eventName, properties, null);
    }

    /**
     * Track the event by event name and customized properties and metrics.
     * 
     * @param eventName event name
     * @param properties customized properties
     * @param metrics customized metrics
     */
    public void trackEvent(String eventName,
                           HashMap<String, String> properties,
                           HashMap<String, Double> metrics) {
        String localEventName = eventName;
        EventData telemetry = new EventData();
        telemetry.setName(localEventName);
        telemetry.setProperties(properties);
        telemetry.setMeasurements(metrics);

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
     * @param message message for transmission to Application insight
     * @param properties properties of the message
     */
    public void trackTrace(String message, HashMap<String, String> properties) {
        String localMessage = message;
        if (TextUtils.isEmpty(localMessage)) {
            localMessage = "";
        }

        MessageData telemetry = new MessageData();
        telemetry.setMessage(localMessage);
        telemetry.setProperties(properties);

        track(telemetry, MessageData.EnvelopeName, MessageData.BaseType);
    }

    /**
     * track the metric.
     *
     * @param name name of the metric
     * @param value value of the metric
     */
    public void trackMetric(String name, Double value) {
        this.trackMetric(name, value, null);
    }

    /**
     * Track the metric with properties.
     *
     * @param name metric name
     * @param value metric value
     * @param properties metric properties
     */
    public void trackMetric(String name, double value, HashMap<String, String> properties) {
        String localName = name;
        if (TextUtils.isEmpty(localName)) {
            localName = "";
        }

        MetricData telemetry = new MetricData();
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
     * @param exception exception data object
     * @param properties exception properties
     */
    public void trackException(ExceptionData exception, HashMap<String, String> properties) {
        ExceptionData localException = exception;
        if (localException == null) {
            localException = new ExceptionData();
        }

        exception.setProperties(properties);

        track(localException, ExceptionData.EnvelopeName, ExceptionData.BaseType);
    }

    /**
     * Sends information about the page viewed in the application to Application
     * Insights.
     *
     * @param pageName Name of the page
     */
    public void trackPageView(String pageName) {
        this.trackPageView(pageName, null);
    }

    /**
     * Sends information about the page viewed in the application to Application
     * Insights.
     *
     * @param pageName Name of the page
     * @param properties exception properties
     */
    public void trackPageView(String pageName, HashMap<String, String> properties) {
        String localPageName = pageName;
        if (TextUtils.isEmpty(localPageName)) {
            localPageName = "";
        }

        PageViewData telemetry = new PageViewData();
        telemetry.setName(localPageName);
        telemetry.setProperties(properties);

        track(telemetry, PageViewData.EnvelopeName, PageViewData.BaseType);
    }

    /**
     * send message to the recorder.
     * 
     * @param telemetry telemetry object
     * @param itemDataType data type
     * @param itemType item type
     */
    protected void track(ITelemetry telemetry, String itemDataType, String itemType) {
        this.channel.send(this.telemetryContext, telemetry, itemDataType, itemType);
    }
}
