package com.microsoft.applicationinsights;

import android.content.Context;

import com.microsoft.applicationinsights.channel.TelemetryContext;
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
import com.microsoft.applicationinsights.channel.InternalLogging;
import com.microsoft.applicationinsights.channel.TelemetryChannel;
import com.microsoft.applicationinsights.channel.Util;
import com.microsoft.applicationinsights.channel.contracts.shared.ITelemetry;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.UUID;

/**
 * The public API for recording application insights telemetry.
 */
public class TelemetryClient {

    public static final int CONTRACT_VERSION = 2;

    /**
     * Get a TelemetryClient instance
     *
     * @param   context the activity to associate with this instance
     * @return  an instance of {@code TelemetryClient} associated with the activity, or null if the
     *          activity is null.
     */
    public static TelemetryClient getInstance(Context context) {
        TelemetryClient client = null;
        if(context == null) {
            InternalLogging._warn("TelemetryClient.getInstance", "activity is null");
        } else {
            client = new TelemetryClient(context);
        }

        return client;
    }

    /**
     * The configuration for this telemetry client.
     */
    protected final TelemetryClientConfig config;

    /**
     * The telemetry telemetryContext object.
     */
    protected final TelemetryContext context;

    /**
     * The telemetry channel for this client.
     */
    protected final TelemetryChannel channel;

    /**
     * Properties associated with this telemetryContext.
     */
    private LinkedHashMap<String, String> commonProperties;

    /**
     * The telemetry telemetryContext object.
     */
    public TelemetryContext getContext() {
        // todo: add cloneContext (possibly rename getContext to make it's scope clear)
        return this.context;
    }

    /**
     * The telemetry channel for this client.
     */
    public TelemetryClientConfig getConfig() {
        return config;
    }

    /**
     * Gets the properties which are common to all telemetry sent from this client.
     * @return common properties for this telemetry client
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
     * <p>
     *     Use {@code TelemetryClient.getInstance} to get an instance.
     * </p>
     * @param context the application context for this client
     */
    protected TelemetryClient(Context context) {
        this(new TelemetryClientConfig(context),  context);
    }

    /**
     * Constructor of the class TelemetryClient.
     * <p>
     *     Use {@code TelemetryClient.getInstance} to get an instance.
     * </p>
     * @param config the configuration for this client
     */
    private TelemetryClient(TelemetryClientConfig config, Context context) {
        this(config, new TelemetryContext(context),
                new TelemetryChannel(config, context));
    }

    /**
     * Constructor of the class TelemetryClient.
     * <p>
     *     Use {@code TelemetryClient.getInstance} to get an instance.
     * </p>
     * @param config the configuration for this client
     * @param context the context for this client
     * @param channel the channel for this client
     */
    protected TelemetryClient(
            TelemetryClientConfig config,
            TelemetryContext context,
            TelemetryChannel channel) {
        this.config = config;
        this.context = context;
        this.channel = channel;
    }

    /**
     * {@code properties} defaults to {@code null}.
     * {@code measurements} defaults to {@code null}.
     *
     * @see TelemetryClient#trackEvent(String, LinkedHashMap, LinkedHashMap)
     */
    public void trackEvent(String eventName) {
        trackEvent(eventName, null, null);
    }

    /**
     * {@code measurements} defaults to {@code null}.
     *
     * @see TelemetryClient#trackEvent(String, LinkedHashMap, LinkedHashMap)
     */
    public void trackEvent(String eventName, LinkedHashMap<String, String> properties) {
        trackEvent(eventName, properties, null);
    }

    /**
     * Sends information about an event to Application Insights.
     *
     * @param eventName    The name of the event
     * @param properties   Custom properties associated with the event. Note: values set here will
     *                     supersede values set in {@link TelemetryClient#setCommonProperties}.
     * @param measurements Custom measurements associated with the event.
     */
    public void trackEvent(
            String eventName,
            LinkedHashMap<String, String> properties,
            LinkedHashMap<String, Double> measurements) {

        EventData telemetry = new EventData();

        telemetry.setName(this.ensureValid(eventName));
        telemetry.setProperties(properties);
        telemetry.setMeasurements(measurements);

        track(telemetry);
    }

    /**
     * {@code measurements} defaults to {@code null}.
     *
     * @see TelemetryClient#trackEvent(String, LinkedHashMap, LinkedHashMap)
     */
    public void trackTrace(String message) {
        trackTrace(message, null);
    }

    /**
     * Sends tracing information to Application Insights.
     *
     * @param message    The message associated with this trace.
     * @param properties Custom properties associated with the event. Note: values set here will
     *                   supersede values set in {@link TelemetryClient#setCommonProperties}.
     */
    public void trackTrace(String message, LinkedHashMap<String, String> properties) {
        MessageData telemetry = new MessageData();

        telemetry.setMessage(this.ensureValid(message));
        telemetry.setProperties(properties);

        track(telemetry);
    }

    /**
     * Sends information about an aggregated metric to Application Insights. Note: all data sent via
     * this method will be aggregated. To send non-aggregated data use
     * {@link TelemetryClient#trackEvent(String, LinkedHashMap, LinkedHashMap)} with measurements.
     *
     * @param name       The name of the metric
     * @param value      The value of the metric
     */
    public void trackMetric(String name, double value) {
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
        track(telemetry);
    }

    /**
     * {@code handledAt} defaults to {@code null}.
     * {@code properties} defaults to {@code null}.
     * {@code measurements} defaults to {@code null}.
     *
     * @see TelemetryClient#trackException(Throwable, String, LinkedHashMap, LinkedHashMap)
     */
    public void trackException(Throwable exception) {
        this.trackException(exception, null);
    }

    /**
     * {@code properties} defaults to {@code null}.
     * {@code measurements} defaults to {@code null}.
     *
     * @see TelemetryClient#trackException(Throwable, String, LinkedHashMap, LinkedHashMap)
     */
    public void trackException(Throwable exception, String handledAt) {
        this.trackException(exception, handledAt, null);
    }

    /**
     * {@code measurements} defaults to {@code null}.
     *
     * @see TelemetryClient#trackException(Throwable, String, LinkedHashMap, LinkedHashMap)
     */
    public void trackException(
            Throwable exception,
            String handledAt,
            LinkedHashMap<String, String> properties) {
        this.trackException(exception, handledAt, properties, null);
    }

    /**
     * Sends information about an exception to Application Insights.
     *
     * @param exception    The exception to track.
     * @param handledAt    The location at which this exception was handled (null if unhandled)
     * @param properties   Custom properties associated with the event. Note: values set here will
     *                     supersede values set in {@link TelemetryClient#setCommonProperties}.
     * @param measurements Custom measurements associated with the event.
     */
    public void trackException(
            Throwable exception,
            String handledAt,
            LinkedHashMap<String, String> properties,
            LinkedHashMap<String, Double> measurements) {

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
        telemetry.setMeasurements(measurements);

        track(telemetry);
    }

    /**
     * {@code properties} defaults to {@code null}.
     * {@code measurements} defaults to {@code null}.
     *
     * @see TelemetryClient#trackPageView(String, LinkedHashMap, LinkedHashMap)
     */
    public void trackPageView(String pageName) {
        this.trackPageView(pageName, null, null);
    }

    /**
     * {@code measurements} defaults to {@code null}.
     *
     * @see TelemetryClient#trackPageView(String, LinkedHashMap, LinkedHashMap)
     */
    public void trackPageView(
            String pageName,
            LinkedHashMap<String, String> properties) {
        this.trackPageView(pageName, properties, null);
    }

    /**
     * Sends information about a page view to Application Insights.
     *
     * @param pageName     The name of the page.
     * @param properties   Custom properties associated with the event. Note: values set here will
     *                     supersede values set in {@link TelemetryClient#setCommonProperties}.
     * @param measurements Custom measurements associated with the event.
     */
    public void trackPageView(
            String pageName,
            LinkedHashMap<String, String> properties,
            LinkedHashMap<String, Double> measurements) {

        PageViewData telemetry = new PageViewData();

        telemetry.setName(this.ensureValid(pageName));
        telemetry.setUrl(null);
        //telemetry.setDuration(Util.msToTimeSpan(pageLoadDurationMs));
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
     * @param measurements    custom measurements
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

        // todo: expose this publicly via a method that instruments a RequestQueue
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
        telemetry.setMeasurements(measurements);

        track(telemetry);
    }

    /**
     * Sends telemetry to the queue for transmission to Application Insights.
     *
     * @param telemetry The telemetry object to enqueue.
     */
    public void track(ITelemetry telemetry) {

        // set the version
        telemetry.setVer(TelemetryClient.CONTRACT_VERSION);

        // add common properties to this telemetry object
        if(this.commonProperties != null) {
            LinkedHashMap<String, String> map = telemetry.getProperties();
            if(map != null) {
                map.putAll(this.commonProperties);
            }

            telemetry.setProperties(map);
        }

        // send to channel
        this.channel.send(telemetry, context.getContextTags());
    }

    /**
     * Triggers an asynchronous flush of queued telemetry.
     * note: this will be called
     * {@link com.microsoft.applicationinsights.channel.TelemetryQueueConfig#maxBatchIntervalMs} after
     * tracking any telemetry so it is not necessary to call this in most cases.
     */
    public void flush() {
        this.channel.getQueue().flush();
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
