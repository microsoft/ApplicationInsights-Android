package com.microsoft.applicationinsights;

import android.content.Context;

import com.microsoft.applicationinsights.channel.TelemetryChannel;
import com.microsoft.applicationinsights.channel.TelemetryContext;
import com.microsoft.applicationinsights.channel.Util;
import com.microsoft.applicationinsights.channel.contracts.DataPoint;
import com.microsoft.applicationinsights.channel.contracts.DataPointType;
import com.microsoft.applicationinsights.channel.contracts.EventData;
import com.microsoft.applicationinsights.channel.contracts.ExceptionData;
import com.microsoft.applicationinsights.channel.contracts.ExceptionDetails;
import com.microsoft.applicationinsights.channel.contracts.MessageData;
import com.microsoft.applicationinsights.channel.contracts.MetricData;
import com.microsoft.applicationinsights.channel.contracts.PageViewData;
import com.microsoft.applicationinsights.channel.contracts.RequestData;
import com.microsoft.applicationinsights.channel.contracts.StackFrame;
import com.microsoft.applicationinsights.channel.contracts.shared.ITelemetry;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.UUID;

/**
 * The public API for recording application insights telemetry.
 * Users would call TelemetryClient.track*
 */
public class TelemetryClient {

    /**
     * The configuration for this telemetry client.
     */
    protected TelemetryClientConfig config;

    /**
     * The telemetry telemetryContext object.
     */
    private TelemetryContext context;

    /**
     * The telemetry channel for this client.
     */
    protected TelemetryChannel channel;

    /**
     * Properties associated with this telemetryContext.
     */
    private LinkedHashMap<String, String> commonProperties;

    /**
     * The telemetry telemetryContext object.
     */
    public TelemetryContext getContext() {

        // todo: add cloneContext (possibly rename getContext to make it's scope clear)

        return context;
    }

    /**
     * The telemetry channel for this client.
     */
    public TelemetryClientConfig getConfig() {
        return config;
    }


    /**
     * Gets the properties which are common to all telemetry sent from this client.
     * @return
     */
    public LinkedHashMap<String, String> getCommonProperties() {
        return commonProperties;
    }

    /**
     * Sets properties which are common to all telemetry sent form this client.
     * @param commonProperties a dictionary of properties to send with all telemetry.
     */
    public void setCommonProperties(LinkedHashMap<String, String> commonProperties) {
        this.commonProperties = commonProperties;
    }

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
        this.config = config;
        this.context = context;
        this.channel = channel;
    }

    /**
     * track the event by name.
     *
     * @param eventName the name of this event
     */
    public void trackEvent(String eventName) {
        trackEvent(eventName, null, null);
    }

    /**
     * track the event by name.
     *
     * @param eventName the name of this event
     */
    public void trackEvent(String eventName, LinkedHashMap<String, String> properties) {
        trackEvent(eventName, properties, null);
    }

    /**
     * Track the event by event name and customized properties and metrics.
     *
     * @param eventName  event name
     * @param properties custom properties
     * @param metrics    custom metrics
     */
    public void trackEvent(
            String eventName,
            LinkedHashMap<String, String> properties,
            LinkedHashMap<String, Double> metrics) {

        EventData telemetry = new EventData();

        telemetry.setName(this.ensureValid(eventName));
        telemetry.setProperties(properties);
        telemetry.setMeasurements(metrics);

        track(telemetry);
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

        telemetry.setMessage(this.ensureValid(message));
        telemetry.setProperties(properties);

        track(telemetry);
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
     * track the metric.
     *
     * @param name  name of the metric
     * @param value value of the metric
     */
    public void trackMetric(String name, long value) {
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

        DataPoint data = new DataPoint();
        data.setCount(1);
        data.setKind(DataPointType.Measurement);
        data.setMax(value);
        data.setMax(value);
        data.setName(this.ensureValid(name));
        data.setValue(value);
        ArrayList<DataPoint> metricsList = new ArrayList<DataPoint>();
        metricsList.add(data);

        telemetry.setMetrics(metricsList);
        telemetry.setProperties(properties);

        track(telemetry);
    }

    /**
     * Sends information about an exception to Application Insights.
     *
     * @param exception the exception to track
     */
    public void trackException(Exception exception) {
        this.trackException(exception, null);
    }

    /**
     * Sends information about an exception to Application Insights.
     *
     * @param exception the exception to track
     * @param handledAt the location this exception was handled (null if unhandled)
     */
    public void trackException(Exception exception, String handledAt) {
        this.trackException(exception, handledAt, null);
    }

    /**
     * Sends information about an exception to Application Insights.
     *
     * @param exception the exception to track
     * @param handledAt the location this exception was handled (null if unhandled)
     * @param properties properties associated with this exception
     */
    public void trackException(
            Exception exception,
            String handledAt,
            LinkedHashMap<String, String> properties) {
        this.trackException(exception, handledAt, properties, null);
    }

    /**
     * Sends information about an exception to Application Insights.
     *
     * @param exception the exception to track
     * @param handledAt the location this exception was handled (null if unhandled)
     * @param properties properties associated with this exception
     * @param metrics measurements associated with this exception
     */
    public void trackException(
            Exception exception,
            String handledAt,
            LinkedHashMap<String, String> properties,
            LinkedHashMap<String, Double> metrics) {

        if(exception == null) {
            exception = new Exception();
        }

        // read stack frames
        ArrayList<StackFrame> stackFrames = new ArrayList<StackFrame>();
        StackTraceElement[] stack = exception.getStackTrace();
        for(int i = stack.length - 1; i >= 0; i--){
            StackTraceElement rawFrame = stack[i];
            StackFrame frame = new StackFrame();
            frame.setFileName(rawFrame.getFileName());
            frame.setLine(rawFrame.getLineNumber());
            frame.setMethod(rawFrame.getClassName() + "." + rawFrame.getMethodName());
            frame.setLevel(i);
            stackFrames.add(frame);
        }

        // read exception detail
        ExceptionDetails detail = new ExceptionDetails();
        String message = exception.getMessage();
        detail.setMessage(this.ensureValid(message));
        detail.setTypeName(exception.getClass().getName());
        detail.setHasFullStack(true);
        detail.setParsedStack(stackFrames);
        ArrayList<ExceptionDetails> exceptions = new ArrayList<ExceptionDetails>();
        exceptions.add(detail);

        // populate ExceptionData
        ExceptionData telemetry = new ExceptionData();
        telemetry.setHandledAt(this.ensureValid(handledAt));
        telemetry.setExceptions(exceptions);
        telemetry.setProperties(properties);
        telemetry.setMeasurements(metrics);

        track(telemetry);
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
        this.trackPageView(pageName, pageLoadDurationMs, properties, null);
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

        PageViewData telemetry = new PageViewData();

        telemetry.setName(this.ensureValid(pageName));
        telemetry.setUrl(null);
        telemetry.setDuration(Util.msToTimeSpan(pageLoadDurationMs));
        telemetry.setProperties(properties);
        telemetry.setMeasurements(measurements);

        track(telemetry);
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
     * @param metrics    custom metrics
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
            LinkedHashMap<String, Double> metrics) {

        RequestData telemetry = new RequestData();

        telemetry.setId(UUID.randomUUID().toString());
        telemetry.setName(this.ensureValid(name));
        telemetry.setUrl(this.ensureValid(url));
        telemetry.setHttpMethod(this.ensureValid(httpMethod));
        telemetry.setStartTime(Util.dateToISO8601(startTime));
        telemetry.setDuration(Util.msToTimeSpan(durationMs));
        telemetry.setResponseCode(String.valueOf(responseCode));
        telemetry.setSuccess(isSuccess);
        telemetry.setProperties(properties);
        telemetry.setMeasurements(metrics);

        track(telemetry);
    }

    /**
     * Send telemetry to the queue.
     *
     * @param telemetry    telemetry object
     */
    public void track(ITelemetry telemetry) {
        this.track(telemetry, this.context);
    }

    /**
     * Send telemetry with context to the queue.
     */
    public void track(ITelemetry telemetry, TelemetryContext context) {

        // set the version
        telemetry.setVer(2);

        // add common properties to this telemetry object
        if(this.commonProperties != null) {
            LinkedHashMap<String, String> map = telemetry.getProperties();
            if(map != null) {
                map.putAll(this.commonProperties);
            }

            telemetry.setProperties(map);
        }

        this.channel.send(context, telemetry);
    }

    /**
     * Triggers an asynchronous flush of the queued telemetry.
     * note: this will be called after SenderConfig.maxBatchIntervalMs if flush is never called.
     */
    public void flush() {
        this.channel.getSender().flush();
    }

    /**
     * Ensures required string values are non-null
     */
    private String ensureValid(String input) {
        if(input == null) {
            return "";
        } else {
            return input;
        }
    }
}
