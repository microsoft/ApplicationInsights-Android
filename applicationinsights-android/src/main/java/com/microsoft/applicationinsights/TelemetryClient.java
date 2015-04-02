package com.microsoft.applicationinsights;

import android.app.Application;
import android.content.Context;
import android.os.AsyncTask;

import com.microsoft.applicationinsights.contracts.Envelope;
import com.microsoft.applicationinsights.contracts.SessionState;
import com.microsoft.applicationinsights.contracts.SessionStateData;
import com.microsoft.applicationinsights.internal.Channel;
import com.microsoft.applicationinsights.internal.EnvelopeFactory;
import com.microsoft.applicationinsights.internal.TelemetryContext;
import com.microsoft.applicationinsights.contracts.CrashData;
import com.microsoft.applicationinsights.contracts.DataPoint;
import com.microsoft.applicationinsights.contracts.DataPointType;
import com.microsoft.applicationinsights.contracts.EventData;
import com.microsoft.applicationinsights.contracts.MessageData;
import com.microsoft.applicationinsights.contracts.MetricData;
import com.microsoft.applicationinsights.contracts.PageViewData;
import com.microsoft.applicationinsights.contracts.shared.ITelemetry;
import com.microsoft.applicationinsights.internal.logging.InternalLogging;

import java.util.ArrayList;
import java.util.List;
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
    protected TelemetryClient(
            // TODO: TelemetryClientConfig should be member of LifecycleTracking
            TelemetryClientConfig config,
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
     * Triggers an asynchronous flush of queued telemetry.
     * note: this will be called
     * {@link com.microsoft.applicationinsights.internal.TelemetryQueueConfig#maxBatchIntervalMs} after
     * tracking any telemetry so it is not necessary to call this in most cases.
     */
    public void flush() { //TODO call flus() on the channel and not on the queue!
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

    /**
     * Creates information about an event for Application Insights. This method gets called by a
     * CreateTelemetryDataTask in order to create and forward data on a background thread.
     *
     * @param eventName    The name of the event
     * @param properties   Custom properties associated with the event
     * @param measurements Custom measurements associated with the event
     *
     * @return a EventData object
     */
    private EventData createEvent(String eventName,
                                   Map<String, String> properties,
                                   Map<String, Double> measurements) {
        EventData telemetry = new EventData();
        telemetry.setName(ensureNotNull(eventName));
        telemetry.setProperties(properties);
        telemetry.setMeasurements(measurements);
        return telemetry;
    }

    /**
     * Creates tracing information for Application Insights. This method gets called by a
     * CreateTelemetryDataTask in order to create and forward data on a background thread.
     *
     * @param message    The message associated with this trace
     * @param properties Custom properties associated with the event
     *
     * @return a MessageData object
     */
    private MessageData createTrace(String message, Map<String, String> properties) {
        MessageData telemetry = new MessageData();
        telemetry.setMessage(this.ensureNotNull(message));
        telemetry.setProperties(properties);
        return telemetry;
    }

    /**
     * Creates information about an aggregated metric for Application Insights. This method gets
     * called by a CreateTelemetryDataTask in order to create and forward data on a background thread.
     *
     * @param name  The name of the metric
     * @param value The value of the metric
     *
     * @return a MetricData object
     */
    private MetricData createMetric(String name, double value) {
        MetricData telemetry = new MetricData();
        DataPoint data = new DataPoint();
        data.setCount(1);
        data.setKind(DataPointType.Measurement);
        data.setMax(value);
        data.setMax(value);
        data.setName(ensureNotNull(name));
        data.setValue(value);
        List<DataPoint> metricsList = new ArrayList<DataPoint>();
        metricsList.add(data);
        telemetry.setMetrics(metricsList);
        return telemetry;
    }

    /**
     * Creates information about an handled or unhandled exception to Application Insights. This
     * method gets called by a CreateTelemetryDataTask in order to create and forward data on a
     * background thread.
     *
     * @param exception  The exception to track
     * @param properties Custom properties associated with the event
     *
     * @return a CrashData object
     */
    private CrashData createException(Throwable exception, Map<String, String> properties) {
        CrashData telemetry = ExceptionUtil.getCrashData(exception, properties, this.context.getPackageName());
        return telemetry;
    }

    /**
     * Creates information about a page view for Application Insights. This method gets called by a
     * CreateTelemetryDataTask in order to create and forward data on a background thread.
     *
     * @param pageName     The name of the page
     * @param properties   Custom properties associated with the event
     * @param measurements Custom measurements associated with the event
     *
     * @return a PageViewData object
     */
    private ITelemetry createPageView(
            String pageName,
            Map<String, String> properties,
            Map<String, Double> measurements) {
        PageViewData telemetry = new PageViewData();
        telemetry.setName(ensureNotNull(pageName));
        telemetry.setUrl(null);
        telemetry.setProperties(properties);
        telemetry.setMeasurements(measurements);
        return telemetry;
    }

    /**
     * Creates information about a new session view for Application Insights. This method gets
     * called by a CreateTelemetryDataTask in order to create and forward data on a background thread.
     *
     * @return a SessionData object
     */
    private ITelemetry createNewSession() {
        SessionStateData telemetry = new SessionStateData();
        telemetry.setState(SessionState.Start);
        return telemetry;
    }

    /**
     * Adds common properties to the given telemetry data.
     *
     * @param telemetry The telemetry data
     *
     * @return a ITelemetry object, which contains all common properties.
     */
    private ITelemetry addCommonProperties(ITelemetry telemetry){
        telemetry.setVer(TelemetryClient.CONTRACT_VERSION);
        if (commonProperties != null) {
            Map<String, String> map = telemetry.getProperties();
            if (map != null) {
                map.putAll(commonProperties);
            }
            telemetry.setProperties(map);
        }
        return telemetry;
    }

    private class CreateTelemetryDataTask extends AsyncTask<Void, Void, Void> {

        private String name;
        private Map<String,String> properties;
        private Map<String, Double> measurements;
        private TelemetryType type;
        private double metric;
        private Throwable exception;

        public CreateTelemetryDataTask(TelemetryType type){
            this.type = type;
        }

        public CreateTelemetryDataTask(TelemetryType type, String metricName, double metric){
            this.type = type;
            this.name = metricName;
            this.metric = metric;
        }

        public CreateTelemetryDataTask(TelemetryType type,
                                       String name,
                                       Map<String,String> properties,
                                       Map<String, Double> measurements){
            this.type = type;
            this.name = name;
            this.properties = properties;
            this.measurements = measurements;
        }

        public CreateTelemetryDataTask(TelemetryType type,
                                       Throwable exception,
                                       Map<String,String> properties){
            this.type = type;
            this.exception = exception;
            this.properties = properties;
        }

        @Override
        protected Void doInBackground(Void... params) {
            ITelemetry telemetryData = null;
            switch (this.type){
                case EVENT:
                    telemetryData = createEvent(this.name, this.properties, this.measurements);
                    break;
                case PAGE_VIEW:
                    telemetryData = createPageView(this.name, this.properties, this.measurements);
                    break;
                case TRACE:
                    telemetryData = createTrace(this.name, this.properties);
                    break;
                case METRIC:
                    telemetryData = createMetric(this.name, this.metric);
                    break;
                case NEW_SESSION:
                    telemetryData = createNewSession();
                    break;
                case HANDLED_EXCEPTION:
                case UNHANDLED_EXCEPTION:
                    telemetryData = createException(this.exception, this.properties);
                    break;
                default:
                    break;
            }

            if(telemetryData != null){
                telemetryData = addCommonProperties(telemetryData);
                Envelope envelope = EnvelopeFactory.INSTANCE.createEnvelope(telemetryData);

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
