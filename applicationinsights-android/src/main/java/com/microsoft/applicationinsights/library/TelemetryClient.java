package com.microsoft.applicationinsights.library;

import android.app.Application;

import com.microsoft.applicationinsights.contracts.shared.ITelemetry;
import com.microsoft.applicationinsights.logging.InternalLogging;

import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * The public API for recording application insights telemetry.
 */
public class TelemetryClient {

    public static final int THREADS = 10;
    public static final String TAG = "TelemetryClient";

    /**
     * The shared TelemetryClient instance.
     */
    private static TelemetryClient instance;

    /**
     * A flag, which determines if page views should be tracked automatically.
     */
    private boolean activityTrackingEnabled;

    /**
     * A flag, which determines telemetry data can be tracked.
     */
    private final boolean telemetryEnabled;

    /**
     * Volatile boolean for double checked synchronize block
     */
    private static volatile boolean isTelemetryClientLoaded = false;

    /**
     * Synchronization LOCK for setting static context
     */
    private static final Object LOCK = new Object();

    /**
     * Executor service for running track operations on several threads.
     */
    private ExecutorService executorService;

    /**
     * Restrict access to the default constructor
     *
     * @param telemetryEnabled YES if tracking telemetry data manually should be enabled
     */
    protected TelemetryClient(boolean telemetryEnabled) {
        this.telemetryEnabled = telemetryEnabled;
        this.executorService = Executors.newFixedThreadPool(THREADS, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setDaemon(false);
                return thread;
            }
        });
    }

    /**
     * Initialize the INSTANCE of the telemetryclient
     *
     * @param telemetryEnabled YES if tracking telemetry data manually should be enabled
     */
    protected static void initialize(boolean telemetryEnabled) {
        if (!TelemetryClient.isTelemetryClientLoaded) {
            synchronized (TelemetryClient.LOCK) {
                if (!TelemetryClient.isTelemetryClientLoaded) {
                    TelemetryClient.isTelemetryClientLoaded = true;
                    TelemetryClient.instance = new TelemetryClient(telemetryEnabled);
                }
            }
        }
    }

    /**
     * @return the INSTANCE of persistence or null if not yet initialized
     */
    public static TelemetryClient getInstance() {
        if (TelemetryClient.instance == null) {
            InternalLogging.error(TAG, "getInstance was called before initialization");
        }
        return TelemetryClient.instance;
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
     * Sends information about any object that implements the ITelemetry interface to Application Insights.
     * For most use-cases, the other tracking methods will be sufficient. Providing this generic method
     * for very specific uses.
     *
     * @param telemetry an object that implements the ITelemetry interface
     */
    public void track(ITelemetry telemetry){
        if(isTelemetryEnabled()){
            new CreateDataTask(telemetry).execute();
        }
    }

    /**
     * Sends information about an event to Application Insights.
     *
     * @param eventName    The name of the event
     * @param properties   Custom properties associated with the event. Note: values set here will
     *                     supersede values set in {@link ApplicationInsights#setCommonProperties}.
     * @param measurements Custom measurements associated with the event.
     */
    public void trackEvent(
          String eventName,
          Map<String, String> properties,
          Map<String, Double> measurements) {
        if(isTelemetryEnabled()){
            new CreateDataTask(CreateDataTask.DataType.EVENT, eventName, properties, measurements).execute();
        }

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
     *                   supersede values set in {@link ApplicationInsights#setCommonProperties}.
     */
    public void trackTrace(String message, Map<String, String> properties) {
        if(isTelemetryEnabled()){
            new CreateDataTask(CreateDataTask.DataType.TRACE, message, properties, null).execute();
        }
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
        if(isTelemetryEnabled()){
            new CreateDataTask(CreateDataTask.DataType.METRIC, name, value).execute();
        }
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
     * @param handledException The handledException to track.
     * @param properties       Custom properties associated with the event. Note: values set here will
     *                         supersede values set in {@link ApplicationInsights#setCommonProperties}.
     */
    public void trackHandledException(Throwable handledException, Map<String, String> properties) {
        if(isTelemetryEnabled()){
            new CreateDataTask(CreateDataTask.DataType.HANDLED_EXCEPTION, handledException, properties).execute();
        }
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
     *                     supersede values set in {@link ApplicationInsights#setCommonProperties}.
     * @param measurements Custom measurements associated with the event.
     */
    public void trackPageView(
          String pageName,
          Map<String, String> properties,
          Map<String, Double> measurements) {
        if(isTelemetryEnabled()){
            new CreateDataTask(CreateDataTask.DataType.PAGE_VIEW, pageName, properties, null).execute();
        }
    }

    /**
     * Sends information about a new Session to Application Insights.
     */
    public void trackNewSession() {
        if(isTelemetryEnabled()){
            new CreateDataTask(CreateDataTask.DataType.NEW_SESSION).execute();
        }
    }

    /**
     * Registers an activity life cycle callback handler to track page views and sessions.
     *
     * @param application the application used to register the life cycle callbacks
     */
    protected void enableActivityTracking(Application application) {
        if(!activityTrackingEnabled){
            activityTrackingEnabled = true;
            LifeCycleTracking.registerActivityLifecycleCallbacks(application);
        }
    }

    /**
     * Determines, whether tracking telemetry data is enabled or not.
     *
     * @return YES if telemetry data can be tracked
     */
    protected boolean isTelemetryEnabled() {
        if(!this.telemetryEnabled){
            InternalLogging.warn(TAG, "Could not track telemetry item, because telemetry " +
                    "feature is disabled.");
        }
        return this.telemetryEnabled;
    }
}
