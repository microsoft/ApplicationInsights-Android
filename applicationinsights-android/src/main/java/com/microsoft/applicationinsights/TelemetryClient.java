package com.microsoft.applicationinsights;

import android.app.Application;
import android.content.Context;
import android.os.AsyncTask;

import com.microsoft.applicationinsights.contracts.Envelope;
import com.microsoft.applicationinsights.internal.Channel;
import com.microsoft.applicationinsights.internal.EnvelopeFactory;
import com.microsoft.applicationinsights.internal.Sender;
import com.microsoft.applicationinsights.internal.TelemetryConfig;
import com.microsoft.applicationinsights.internal.TelemetryContext;
import com.microsoft.applicationinsights.internal.logging.InternalLogging;

import java.util.Map;

/**
 * The public API for recording application insights telemetry.
 */
public class TelemetryClient {

    private enum TelemetryType {
        NONE,
        EVENT,
        TRACE,
        METRIC,
        PAGE_VIEW,
        HANDLED_EXCEPTION,
        UNHANDLED_EXCEPTION,
        NEW_SESSION
    };
    public static final String TAG = "TelemetryClient";

    /**
     * The configuration for this telemetry client.
     */
    protected final SessionConfig config;

    /**
     * The telemetry telemetryContext object.
     */
    protected final TelemetryContext context;

    /**
     * The telemetry channel for this client.
     */
    protected final Channel channel;

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
        this(new SessionConfig(context), context);
    }

    /**
     * Constructor of the class TelemetryClient.
     * <p>
     * Use {@code TelemetryClient.getInstance} to get an INSTANCE.
     * </p>
     *
     * @param config the configuration for this client
     */
    private TelemetryClient(SessionConfig config, Context context) {
        this(config, new TelemetryContext(context, config.getInstrumentationKey()), new Channel());
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
    protected   TelemetryClient(
            // TODO: TelemetryClientConfig should be member of LifecycleTracking
            SessionConfig config,
            TelemetryContext context,
            Channel channel) {
        this.config = config;
        // TODO: Maybe the context should be owned by the factory, which creates envelops.
        this.context = context;
        this.channel = channel;
        EnvelopeFactory.INSTANCE.configureWithTelemetryContext(this.context);
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
    public SessionConfig getConfig() {
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
        EnvelopeFactory.INSTANCE.setCommonProperties(this.commonProperties);
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
        new CreateTelemetryDataTask(TelemetryType.EVENT, eventName, properties, measurements).execute();
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
        new CreateTelemetryDataTask(TelemetryType.TRACE, message, properties, null).execute();
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
        new CreateTelemetryDataTask(TelemetryType.METRIC, name, value).execute();
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
        new CreateTelemetryDataTask(TelemetryType.HANDLED_EXCEPTION, handledException, properties).execute();
    }

    public void trackUnhandledException(Throwable unhandledException, Map<String, String> properties) {
        new CreateTelemetryDataTask(TelemetryType.UNHANDLED_EXCEPTION, unhandledException, properties).execute();
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
        new CreateTelemetryDataTask(TelemetryType.PAGE_VIEW, pageName, properties, null).execute();

    }

    /**
     * Sends information about a new Session to Application Insights.
     */
    public void trackNewSession() {
        new CreateTelemetryDataTask(TelemetryType.NEW_SESSION).execute();
    }

    /**
     * Triggers persisting and if applicable sending of queued data
     * note: this will be called
     * {@link TelemetryConfig#maxBatchIntervalMs} after
     * tracking any telemetry so it is not necessary to call this in most cases.
     */
    public void sendPendingData() { //TODO call sendPendingData() on the channel and not on the queue!
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
            // TODO: In case of multiple client instance, this should be done somewhere else + only once
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

    private class CreateTelemetryDataTask extends AsyncTask<Void, Void, Void> {

        private String name;
        private Map<String,String> properties;
        private Map<String, Double> measurements;
        private TelemetryType type;
        private double metric;
        private Throwable exception;

        private CreateTelemetryDataTask(TelemetryType type){
            this.type = type;
        }

        private CreateTelemetryDataTask(TelemetryType type, String metricName, double metric){
            this.type = type;
            this.name = metricName;
            this.metric = metric;
        }

        private CreateTelemetryDataTask(TelemetryType type,
                                       String name,
                                       Map<String,String> properties,
                                       Map<String, Double> measurements){
            this.type = type;
            this.name = name;
            this.properties = properties;
            this.measurements = measurements;
        }

        private CreateTelemetryDataTask(TelemetryType type,
                                       Throwable exception,
                                       Map<String,String> properties){
            this.type = type;
            this.exception = exception;
            this.properties = properties;
        }

        @Override
        protected Void doInBackground(Void... params) {
            Envelope envelope = null;
            switch (this.type){
                case EVENT:
                    envelope = EnvelopeFactory.INSTANCE.createEventEnvelope(this.name, this.properties, this.measurements);
                    break;
                case PAGE_VIEW:
                    envelope = EnvelopeFactory.INSTANCE.createPageViewEnvelope(this.name, this.properties, this.measurements);
                    break;
                case TRACE:
                    envelope = EnvelopeFactory.INSTANCE.createTraceEnvelope(this.name, this.properties);
                    break;
                case METRIC:
                    envelope = EnvelopeFactory.INSTANCE.createMetricEnvelope(this.name, this.metric);
                    break;
                case NEW_SESSION:
                    envelope = EnvelopeFactory.INSTANCE.createNewSessionEnvelope();
                    break;
                case HANDLED_EXCEPTION:
                case UNHANDLED_EXCEPTION:
                    envelope = EnvelopeFactory.INSTANCE.createExceptionEnvelope(this.exception, this.properties);
                    break;
                default:
                    break;
            }

            if(envelope != null){
                if(type == TelemetryType.UNHANDLED_EXCEPTION){
                    channel.processUnhandledException(envelope);
                }else{
                    channel.enqueue(envelope);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            return ;
        }
    }
}
