package com.microsoft.applicationinsights.library;

import com.microsoft.applicationinsights.logging.InternalLogging;
import com.microsoft.telemetry.ITelemetry;

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
     * A flag, which determines if auto page views should be disabled from the start.
     * Default is false.
     */
    private boolean autoPageViewsDisabled;

    /**
     * A flag, which determines if auto session management should be disabled from the start.
     * Default is false.
     */
    private boolean autoSessionManagementDisabled;

    /**
     * A flag, which determines if auto appearance should be disabled from the start.
     * Default is false.
     */
    private boolean autoAppearanceDisabled;

    /**
     * The shared TelemetryClient instance.
     */
    private static TelemetryClient instance;

    /**
     * A flag, which determines telemetry data can be tracked.
     */
    private boolean telemetryEnabled;

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
        this.autoAppearanceDisabled = !telemetryEnabled;
        this.autoPageViewsDisabled = !telemetryEnabled;
        this.autoSessionManagementDisabled = !telemetryEnabled;
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
    protected static void initialize(TelemetryContext telemetryContext, boolean telemetryEnabled) {
        if (!TelemetryClient.isTelemetryClientLoaded) {
            synchronized (TelemetryClient.LOCK) {
                if (!TelemetryClient.isTelemetryClientLoaded) {
                    TelemetryClient.isTelemetryClientLoaded = true;
                    TelemetryClient.instance = new TelemetryClient(telemetryEnabled);
                    AutoCollection.initialize(telemetryContext, ApplicationInsights.getConfig());
                    TelemetryClient.instance.startAutoCollection();//TODO move this to separate call?
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
    public void track(ITelemetry telemetry) {
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
        if (isTelemetryEnabled()) {
            this.executorService.execute(new TrackDataOperation(TrackDataOperation.DataType.METRIC, name, value));
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
        if (isTelemetryEnabled()) {
            this.executorService.execute(new TrackDataOperation(TrackDataOperation.DataType.HANDLED_EXCEPTION,
                  handledException, properties));
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
        if (isTelemetryEnabled()) {
            this.executorService.execute(new TrackDataOperation(TrackDataOperation.DataType.PAGE_VIEW,
                  pageName, properties, null));
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
     * Start/Stop AutoCollection features depending on user's settings.
     * Will be called during start of the SDK and when enabling/disabling ALL autocollection features
     */
    public void startAutoCollection() {
        if (autoCollectionPossible("Start of AutoCollection at app start.")) {
            //if the SDK has already been started, activate/deactivate features
            if (TelemetryClient.getInstance().autoAppearanceDisabled) {
                disableAutoAppearanceTracking();
            } else {
                enableAutoAppearanceTracking();
            }
            if (TelemetryClient.getInstance().autoPageViewsDisabled) {
                disableAutoPageViewTracking();
            } else {
                enableAutoPageViewTracking();
            }
            if (TelemetryClient.getInstance().autoSessionManagementDisabled) {
                disableAutoSessionManagement();
            } else {
                enableAutoSessionManagement();
            }
        }
    }

    /**
     * Enables all auto-collection features. Call this before
     * {@link ApplicationInsights#start()} or when ApplicationInsights is already running to change
     * AutoCollection settings at runtime.
     * Requires ApplicationInsights to be setup with an Application object
     */
    public void enableAllAutoCollection() {
        getInstance().autoAppearanceDisabled = false;
        getInstance().autoPageViewsDisabled = false;
        getInstance().autoSessionManagementDisabled = false;
        enableAutoAppearanceTracking();
        enableAutoPageViewTracking();
        enableAutoSessionManagement();
    }

    /**
     * disables all auto-collection features
     */
    public void disableAllAutoCollection() {
        getInstance().autoAppearanceDisabled = true;
        getInstance().autoPageViewsDisabled = true;
        getInstance().autoSessionManagementDisabled = true;
        disableAutoAppearanceTracking();
        disableAutoPageViewTracking();
        disableAutoSessionManagement();
    }

    /**
     * Enable auto page view tracking before calling {@link ApplicationInsights#start()} or
     * at runtime. This feature only works if ApplicationInsights has been setup
     * with an application.
     */
    public void enableAutoPageViewTracking() {
        getInstance().autoPageViewsDisabled = false;
        if (autoCollectionPossible("Auto PageView Tracking")) {
            AutoCollection.enableAutoPageViews(ApplicationInsights.getApplication());
        }
    }

    /**
     * Disable auto page view tracking before calling {@link ApplicationInsights#start()} or
     * at runtime. This feature only works if ApplicationInsights has been setup
     * with an application.
     */
    public void disableAutoPageViewTracking() {
        getInstance().autoPageViewsDisabled = true;
        if (autoCollectionPossible("Auto PageView Tracking")) {
            AutoCollection.disableAutoPageViews();
        }
    }

    /**
     * Enable auto session management tracking before calling {@link ApplicationInsights#start()} or
     * at runtime. This feature only works if ApplicationInsights has been setup
     * with an application.
     */
    public void enableAutoSessionManagement() {
        getInstance().autoSessionManagementDisabled = false;
        if (autoCollectionPossible("Auto Session Management")) {
            AutoCollection.enableAutoSessionManagement(ApplicationInsights.getApplication());
        }
    }

    /**
     * Disable auto session management tracking before calling {@link ApplicationInsights#start()} or
     * at runtime. This feature only works if ApplicationInsights has been setup
     * with an application.
     */
    public void disableAutoSessionManagement() {
        getInstance().autoSessionManagementDisabled = true;
        if (autoCollectionPossible("Auto Session Management")) {
            AutoCollection.disableAutoSessionManagement();
        }
    }

    /**
     * Enable auto appearance management tracking before calling {@link ApplicationInsights#start()} or
     * at runtime. This feature only works if ApplicationInsights has been setup
     * with an application.
     */
    public void enableAutoAppearanceTracking() {
        getInstance().autoAppearanceDisabled = false;
        if (autoCollectionPossible("Auto Appearance")) {
            AutoCollection.enableAutoAppearanceTracking(ApplicationInsights.getApplication());
        }
    }

    /**
     * Disable auto appearance management tracking before calling {@link ApplicationInsights#start()} or
     * at runtime. This feature only works if ApplicationInsights has been setup
     * with an application.
     */
    public void disableAutoAppearanceTracking() {
        getInstance().autoAppearanceDisabled = true;
        if (autoCollectionPossible("Auto Appearance")) {
            AutoCollection.disableAutoAppearanceTracking();
        }
    }

    /**
     * Will check if autocollection is possible
     *
     * @param featureName The name of the feature which will be logged in case autocollection is not
     *                    possible
     * @return a flag indicating if autocollection features can be activated
     */
    private boolean autoCollectionPossible(String featureName) {
        if (!Util.isLifecycleTrackingAvailable()) {
            InternalLogging.warn(TAG, "AutoCollection feature " + featureName +
                  " can't be enabled/disabled, because " +
                  "it is not supported on this OS version.");
            return false;
        } else if (!isTelemetryClientLoaded) {
            InternalLogging.warn(TAG, "AutoCollection feature " + featureName +
                  " has been set and will be activated when calling start().");
            return false;
        } else if (ApplicationInsights.getApplication() == null) {
            InternalLogging.warn(TAG, "AutoCollection feature " + featureName +
                  " can't be enabled/disabled, because " +
                  "ApplicationInsights has not been setup with an application.");
            return false;
        } else {
            return true;
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

    protected void setTelemetryEnabled(boolean enabled) {
        this.telemetryEnabled = enabled;
    }
}
