package com.microsoft.applicationinsights.library;

import com.microsoft.applicationinsights.contracts.TelemetryData;
import com.microsoft.applicationinsights.logging.InternalLogging;

import java.util.Map;
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
            InternalLogging.error(TAG, "getSharedInstance was called before initialization");
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
     * Sends information about any object that extend TelemetryData interface to Application Insights.
     * For most use-cases, the other tracking methods will be sufficient. Providing this generic method
     * for very specific uses.
     *
     * @param telemetry an object that extends TelemetryData
     */
    public void track(TelemetryData telemetry) {
        if (isTelemetryEnabled()) {
            this.executorService.execute(new TrackDataOperation(telemetry));
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
        if (isTelemetryEnabled()) {
            this.executorService.execute(new TrackDataOperation(TrackDataOperation.DataType.EVENT,
                  eventName, properties, measurements));
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
        if (isTelemetryEnabled()) {
            this.executorService.execute(new TrackDataOperation(TrackDataOperation.DataType.TRACE,
                  message, properties, null));
        }
    }

    /**
     * Sends information about an aggregated metric to Application Insights. Note: all data sent via
     * this method will be aggregated. To log non-aggregated data use
     * {@link TelemetryClient#trackEvent(String, Map, Map)} with measurements.
     *
     * @param name  The name of the metric
     * @param value The value of the metric
     */
    public void trackMetric(String name, double value) {
        trackMetric(name, value, null);
    }

    /**
     * Sends information about an aggregated metric to Application Insights. Note: all data sent via
     * this method will be aggregated. To log non-aggregated data use
     * {@link TelemetryClient#trackEvent(String, Map, Map)} with measurements.
     *
     * @param name  The name of the metric
     * @param value The value of the metric
     */
    public void trackMetric(String name, double value, Map<String, String> properties) {
        if (isTelemetryEnabled()) {
            this.executorService.execute(new TrackDataOperation(TrackDataOperation.DataType.METRIC, name, value, properties));
        }
    }

    /**
     * {@code properties} defaults to {@code null}.
     * {@code measurements} defaults to {@code null}.
     *
     * @see TelemetryClient#trackHandledException(Throwable, Map)
     */
    public void trackHandledException(Throwable handledException) {
        this.trackHandledException(handledException, null, null);
    }

    /**
     * Sends information about an handledException to Application Insights.
     *
     * @param handledException The handledException to track.
     * @param properties       Custom properties associated with the event. Note: values set here will
     *                         supersede values set in {@link ApplicationInsights#setCommonProperties}.
     */
    public void trackHandledException(Throwable handledException, Map<String, String> properties) {
        trackHandledException(handledException, properties, null);
    }

    /**
     * Sends information about an handledException to Application Insights.
     *
     * @param handledException The handledException to track.
     * @param properties       Custom properties associated with the event. Note: values set here will
     *                         supersede values set in {@link ApplicationInsights#setCommonProperties}.
     */
    public void trackHandledException(Throwable handledException, Map<String, String> properties, Map<String, Double> measurements) {
        if (isTelemetryEnabled()) {
            this.executorService.execute(new TrackDataOperation(TrackDataOperation.DataType.HANDLED_EXCEPTION,
                  handledException, properties, measurements));
        }
    }

    /**
     * Sends unhandled Exception to Application Insights. This method should be called from your
     * Xamarin code to send the C# stacktrace to ApplicationInsights and ignore the report created
     * by {@link ExceptionTracking}.
     *
     * @param type       the exception type
     * @param message    the exception message
     * @param stacktrace the stacktrace for the exception
     * @param handled    a flag which determines if the exception was handled or not
     */
    public void trackManagedException(String type, String message, String stacktrace, boolean handled) {
        ExceptionTracking.setIgnoreExceptions(!handled);
        new TrackDataOperation(TrackDataOperation.DataType.MANAGED_EXCEPTION, type, message, stacktrace, handled).run();

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
     * @see TelemetryClient#trackPageView(String, Map, Map)
     */
    public void trackPageView(String pageName, Map<String, String> properties, Map<String, Double> measurements) {
        if (isTelemetryEnabled()) {
            this.executorService.execute(new TrackDataOperation(TrackDataOperation.DataType.PAGE_VIEW,
                  pageName, properties, measurements));
        }
    }

    /**
     * {@code properties} defaults to {@code null}.
     * {@code measurements} defaults to {@code null}.
     *
     * @deprecated in 1.0-beta.8, duration won't be supported in 1.0 release*
     */
    public void trackPageView(String pageName, long duration) {
        this.trackPageView(pageName, duration, null, null);
    }

    /**
     * {@code measurements} defaults to {@code null}.
     *
     * @see TelemetryClient#trackPageView(String, long, Map, Map)
     *
     * @deprecated in 1.0-beta.8, duration won't be supported in 1.0 release
     */
    public void trackPageView(String pageName, long duration, Map<String, String> properties) {
        this.trackPageView(pageName, duration, properties, null);
    }

    /**
     * Sends information about a page view to Application Insights.
     *
     * @param pageName     The name of the page.
     * @param duration     The time the page needs to show up.
     * @param properties   Custom properties associated with the event. Note: values set here will
     *                     supersede values set in {@link ApplicationInsights#setCommonProperties}.
     * @param measurements Custom measurements associated with the event.
     *
     * @deprecated in 1.0-beta.8, duration won't be supported in 1.0 release
     */
    public void trackPageView(
          String pageName,
          long duration,
          Map<String, String> properties,
          Map<String, Double> measurements) {
        if (isTelemetryEnabled()) {
            this.executorService.execute(new TrackDataOperation(TrackDataOperation.DataType.PAGE_VIEW,
                  pageName, duration, properties, measurements));
        }
    }

    /**
     * Sends information about a new Session to Application Insights.
     */
    public void trackNewSession() {
        if (isTelemetryEnabled()) {
            this.executorService.execute(new TrackDataOperation(TrackDataOperation.DataType.NEW_SESSION));
        }
    }

    /**
     * Determines, whether tracking telemetry data is enabled or not.
     *
     * @return YES if telemetry data can be tracked
     */
    protected boolean isTelemetryEnabled() {
        if (!this.telemetryEnabled) {
            InternalLogging.warn(TAG, "Could not track telemetry item, because telemetry " +
                  "feature is disabled.");
        }
        return this.telemetryEnabled;
    }
}
