package com.microsoft.applicationinsights;

import android.app.Application;
import android.content.Context;

import com.microsoft.applicationinsights.channel.TelemetryChannel;
import com.microsoft.applicationinsights.channel.TelemetryContext;
import com.microsoft.applicationinsights.channel.contracts.CrashData;
import com.microsoft.applicationinsights.channel.contracts.DataPoint;
import com.microsoft.applicationinsights.channel.contracts.DataPointType;
import com.microsoft.applicationinsights.channel.contracts.EventData;
import com.microsoft.applicationinsights.channel.contracts.MessageData;
import com.microsoft.applicationinsights.channel.contracts.MetricData;
import com.microsoft.applicationinsights.channel.contracts.PageViewData;
import com.microsoft.applicationinsights.channel.contracts.shared.ITelemetry;
import com.microsoft.applicationinsights.channel.logging.InternalLogging;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The public API for recording application insights telemetry.
 */
public class TelemetryClient {

    public static final String TAG = "TelemetryClient";
    public static final int CONTRACT_VERSION = 2;

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
    private Map<String, String> commonProperties;

    /**
     * Constructor of the class TelemetryClient.
     * <p>
     * Use {@code TelemetryClient.getInstance} to get an INSTANCE.
     * </p>
     *
     * @param context the application context for this client
     */
    protected TelemetryClient(Context context) {
        this(new TelemetryClientConfig(context), context);
    }

    /**
     * Constructor of the class TelemetryClient.
     * <p>
     * Use {@code TelemetryClient.getInstance} to get an INSTANCE.
     * </p>
     *
     * @param config the configuration for this client
     */
    private TelemetryClient(TelemetryClientConfig config, Context context) {
        this(config, new TelemetryContext(context), new TelemetryChannel(config, context));
    }

    /**
     * Constructor of the class TelemetryClient.
     * <p>
     * Use {@code TelemetryClient.getInstance} to get an INSTANCE.
     * </p>
     *
     * @param config  the configuration for this client
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
     * Get a TelemetryClient INSTANCE
     *
     * @param context the activity to associate with this INSTANCE
     * @return an INSTANCE of {@code TelemetryClient} associated with the activity, or null if the
     * activity is null.
     */
    public static TelemetryClient getInstance(Context context) {
        TelemetryClient client = null;
        if (context == null) {
            InternalLogging.warn("TelemetryClient.getInstance", "context is null");
        } else {
            client = new TelemetryClient(context);
        }

        return client;
    }

    /**
     * The telemetry telemetryContext object.
     */
    public TelemetryContext getContext() {
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
     *
     * @return common properties for this telemetry client
     */
    public Map<String, String> getCommonProperties() {
        return commonProperties;
    }

    /**
     * Sets properties which are common to all telemetry sent form this client.
     *
     * @param commonProperties a dictionary of properties to enqueue with all telemetry.
     */
    public void setCommonProperties(Map<String, String> commonProperties) {
        this.commonProperties = commonProperties;
    }

    /**
     * {@code properties} defaults to {@code null}.
     * {@code measurements} defaults to {@code null}.
     *
     * @see TelemetryClient#trackEvent(String, Map, Map)
     */
    public void trackEvent(String eventName) {
        trackEvent(eventName, null, null);
    }

    /**
     * {@code measurements} defaults to {@code null}.
     *
     * @see TelemetryClient#trackEvent(String, Map, Map)
     */
    public void trackEvent(String eventName, Map<String, String> properties) {
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
            Map<String, String> properties,
            Map<String, Double> measurements) {

        EventData telemetry = new EventData();

        telemetry.setName(this.ensureNotNull(eventName));
        telemetry.setProperties(properties);
        telemetry.setMeasurements(measurements);

        track(telemetry);
    }

    /**
     * {@code measurements} defaults to {@code null}.
     *
     * @see TelemetryClient#trackTrace(String, Map)
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
    public void trackTrace(String message, Map<String, String> properties) {
        MessageData telemetry = new MessageData();

        telemetry.setMessage(this.ensureNotNull(message));
        telemetry.setProperties(properties);

        track(telemetry);
    }

    /**
     * Sends information about an aggregated metric to Application Insights. Note: all data sent via
     * this method will be aggregated. To enqueue non-aggregated data use
     * {@link TelemetryClient#trackEvent(String, Map, Map)} with measurements.
     *
     * @param name  The name of the metric
     * @param value The value of the metric
     */
    public void trackMetric(String name, double value) {
        MetricData telemetry = new MetricData();

        DataPoint data = new DataPoint();
        data.setCount(1);
        data.setKind(DataPointType.Measurement);
        data.setMax(value);
        data.setMax(value);
        data.setName(this.ensureNotNull(name));
        data.setValue(value);
        List<DataPoint> metricsList = new ArrayList<DataPoint>();
        metricsList.add(data);

        telemetry.setMetrics(metricsList);
        track(telemetry);
    }

    /**
     * {@code properties} defaults to {@code null}.
     * {@code measurements} defaults to {@code null}.
     *
     * @see TelemetryClient#trackHandledException(Throwable, Map)
     */
    public void trackHandledException(Throwable handledException) {
        this.trackHandledException(handledException, null);
    }

    /**
     * Sends information about an handledException to Application Insights.
     *
     * @param handledException  The handledException to track.
     * @param properties Custom properties associated with the event. Note: values set here will
     *                   supersede values set in {@link TelemetryClient#setCommonProperties}.
     */
    public void trackHandledException(Throwable handledException, Map<String, String> properties) {
        CrashData crashData = ExceptionUtil.createCrashData(handledException, properties, this.context.getPackageName());
        track(crashData);
    }

    public void trackUnhandledException(Throwable unhandledException, Map<String, String> properties) {
        CrashData crashData = ExceptionUtil.createCrashData(unhandledException, properties, this.context.getPackageName());

        // set the version
        crashData.setVer(TelemetryClient.CONTRACT_VERSION);

        // add common properties to this telemetry object
        if (this.commonProperties != null) {
            Map<String, String> map = crashData.getProperties();
            if (map != null) {
                map.putAll(this.commonProperties);
            }

            crashData.setProperties(map);
        }

        this.channel.processUnhandledException(crashData, context.getContextTags());
    }

    /**
     * {@code properties} defaults to {@code null}.
     * {@code measurements} defaults to {@code null}.
     *
     * @see TelemetryClient#trackPageView(String, Map, Map)
     */
    public void trackPageView(String pageName) {
        this.trackPageView(pageName, null, null);
    }

    /**
     * {@code measurements} defaults to {@code null}.
     *
     * @see TelemetryClient#trackPageView(String, Map, Map)
     */
    public void trackPageView(String pageName, Map<String, String> properties) {
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
            Map<String, String> properties,
            Map<String, Double> measurements) {

        PageViewData telemetry = new PageViewData();

        telemetry.setName(this.ensureNotNull(pageName));
        telemetry.setUrl(null);

        // todo: measure page-load duration and set telemetry.setDuration

        telemetry.setProperties(properties);
        telemetry.setMeasurements(measurements);

        track(telemetry);
    }


    /**
     * Sends telemetry to the queue for transmission to Application Insights.
     * If isUnhandledException is true, the queue will persist instead of send the data.
     *
     * @param telemetry The telemetry object to enqueue.
     */
    public void track(ITelemetry telemetry) {
        // set the version
        telemetry.setVer(TelemetryClient.CONTRACT_VERSION);

        // add common properties to this telemetry object
        if (this.commonProperties != null) {
            Map<String, String> map = telemetry.getProperties();
            if (map != null) {
                map.putAll(this.commonProperties);
            }

            telemetry.setProperties(map);
        }

        // enqueue to channel
        this.channel.enqueue(telemetry, context.getContextTags());
    }

    /**
     * Triggers an asynchronous flush of queued telemetry.
     * note: this will be called
     * {@link com.microsoft.applicationinsights.channel.TelemetryQueueConfig#maxBatchIntervalMs} after
     * tracking any telemetry so it is not necessary to call this in most cases.
     */
    public void flush() { //TODO remove this because the client shouldn't have to deal with flushing the queue!
        this.channel.getQueue().flush();
    }

    /**
     * Registers a custom exceptionHandler to catch unhandled exceptions. Unhandled exceptions will be
     * persisted and sent when starting the app again.
     *
     * @param context the application context used to register the exceptionHandler to catch unhandled
     *                exceptions
     */
    public void enableCrashTracking(Context context) {
        if (context != null) {
            ExceptionTracking.registerExceptionHandler(context);
        } else {
            InternalLogging.warn(TAG, "Unable to register ExceptionHandler, context is null");
        }
    }

    /**
     * Registers an activity life cycle callback handler to track page views and sessions.
     *
     * @param application the application used to register the life cycle callbacks
     */
    public void enableActivityTracking(Application application) {
        if (context != null) {
            LifeCycleTracking.registerActivityLifecycleCallbacks(application);
        } else {
            InternalLogging.warn(TAG, "Unable to register activity lifecycle callbacks, context is null");
        }
    }

    /**
     * Ensures required string values are non-null
     */
    private String ensureNotNull(String input) {
        if (input == null) {
            return "";
        } else {
            return input;
        }
    }
}
