package com.microsoft.applicationinsights.library;

import android.app.Application;

import com.microsoft.applicationinsights.contracts.TelemetryData;
import com.microsoft.applicationinsights.library.config.Configuration;
import com.microsoft.applicationinsights.logging.InternalLogging;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * The public API for recording application insights telemetry.
 */
public class TelemetryClient {

    private static final String TAG = "TelemetryClient";

    /**
     * The configuration of the SDK.
     */
    private Configuration config;

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
    private ThreadPoolExecutor threadPoolExecutor;

    /**
     * A flag, which determines if auto page views should be disabled.
     * Default is true.
     */
    private boolean autoPageViewsDisabled = true;

    /**
     * A flag, which determines if auto session management should be disabled.
     * Default is true.
     */
    private boolean autoSessionManagementDisabled = true;

    /**
     * A flag, which determines if auto appearance should be disabled.
     * Default is true.
     */
    private boolean autoAppearanceDisabled = true;

    /**
     * The application needed for auto collecting telemetry data
     */
    private WeakReference<Application> weakApplication;

    /**
     * Restrict access to the default constructor
     *
     * @param telemetryEnabled YES if tracking telemetry data manually should be enabled
     */
    protected TelemetryClient(boolean telemetryEnabled) {
        this.telemetryEnabled = telemetryEnabled;
        configThreadPool();
    }

    private void configThreadPool(){
        int corePoolSize = 5;
        int maxPoolSize = 10;
        int queueSize = 5;
        long timeout = 1;
        ArrayBlockingQueue<Runnable> queue= new ArrayBlockingQueue<Runnable>(queueSize);
        ThreadFactory threadFactory = new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setDaemon(false);
                return thread;
            }
        };
        RejectedExecutionHandler handler = new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                InternalLogging.error(TAG, "too many track() calls at a time");
            }
        };
        threadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maxPoolSize, timeout, TimeUnit.SECONDS, queue, threadFactory, handler);
    }

    /**
     * Initialize the INSTANCE of the telemetryclient
     *
     * @param telemetryEnabled YES if tracking telemetry data manually should be enabled
     * @param application      application used for auto collection features
     */
    protected static void initialize(boolean telemetryEnabled, Application application) {
        if (!TelemetryClient.isTelemetryClientLoaded) {
            synchronized (TelemetryClient.LOCK) {
                if (!TelemetryClient.isTelemetryClientLoaded) {
                    TelemetryClient.isTelemetryClientLoaded = true;
                    TelemetryClient.instance = new TelemetryClient(telemetryEnabled);
                    TelemetryClient.instance.weakApplication = new WeakReference<Application>(application);


                }
            }
        }
    }

    /**
     * Start auto collection features.
     */
    protected static void startAutoCollection(TelemetryContext context,
                                              Configuration config,
                                              boolean autoAppearanceEnabled,
                                              boolean autoPageViewsEnabled,
                                              boolean autoSessionManagementEnabled) {
        AutoCollection.initialize(context, config);

        if (autoAppearanceEnabled) {
            TelemetryClient.instance.enableAutoAppearanceTracking();
        }
        if (autoSessionManagementEnabled) {
            TelemetryClient.instance.enableAutoSessionManagement();
        }
        if (autoPageViewsEnabled) {
            TelemetryClient.instance.enableAutoPageViewTracking();
        }

        TelemetryClient.getInstance().startSyncWhenBackgrounding();
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
            this.threadPoolExecutor.execute(new TrackDataOperation(telemetry));
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
            this.threadPoolExecutor.execute(new TrackDataOperation(TrackDataOperation.DataType.EVENT,
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
            this.threadPoolExecutor.execute(new TrackDataOperation(TrackDataOperation.DataType.TRACE,
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
            this.threadPoolExecutor.execute(new TrackDataOperation(TrackDataOperation.DataType.METRIC, name, value, properties));
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
     * @see TelemetryClient#trackPageView(String, Map, Map)
     */
    public void trackPageView(String pageName, Map<String, String> properties, Map<String, Double> measurements) {
        if (isTelemetryEnabled()) {
            this.threadPoolExecutor.execute(new TrackDataOperation(TrackDataOperation.DataType.PAGE_VIEW,
                    pageName, properties, measurements));
        }
    }

    /**
     * Sends information about a new Session to Application Insights.
     */
    public void trackNewSession() {
        if (isTelemetryEnabled()) {
            this.threadPoolExecutor.execute(new TrackDataOperation(TrackDataOperation.DataType.NEW_SESSION));
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

    /**
     * Enable auto page view tracking. This feature only works if
     * {@link TelemetryClient#initialize} has been called before.
     */
    protected void enableAutoPageViewTracking() {
        synchronized (TelemetryClient.LOCK) {
            if (isAutoCollectionPossible("Auto PageViews") && this.autoPageViewsDisabled) {
                this.autoPageViewsDisabled = false;
                AutoCollection.enableAutoPageViews(getApplication());
            }
        }
    }

    /**
     * Disable auto page view tracking. This feature only works if
     * {@link TelemetryClient#initialize} has been called before.
     */
    protected void disableAutoPageViewTracking() {
        synchronized (TelemetryClient.LOCK) {
            if (isAutoCollectionPossible("Auto PageViews") && !this.autoPageViewsDisabled) {
                this.autoPageViewsDisabled = true;
                AutoCollection.disableAutoPageViews();
            }
        }
    }

    /**
     * Enable auto session management tracking. This feature only works if
     * {@link TelemetryClient#initialize} has been called before.
     */
    protected void enableAutoSessionManagement() {
        synchronized (TelemetryClient.LOCK) {
            if (isAutoCollectionPossible("Session Management") && this.autoSessionManagementDisabled) {
                this.autoSessionManagementDisabled = false;
                AutoCollection.enableAutoSessionManagement(getApplication());
            }
        }
    }

    /**
     * Disable auto session management tracking. This feature only works if
     * {@link TelemetryClient#initialize} has been called before.
     */
    protected void disableAutoSessionManagement() {
        synchronized (TelemetryClient.LOCK) {
            if (isAutoCollectionPossible("Session Management") && !this.autoSessionManagementDisabled) {
                this.autoSessionManagementDisabled = true;
                AutoCollection.disableAutoSessionManagement();
            }
        }
    }

    /**
     * Enable auto appearance management tracking. This feature only works if
     * {@link TelemetryClient#initialize} has been called before.
     */
    protected void enableAutoAppearanceTracking() {
        synchronized (TelemetryClient.LOCK) {
            if (isAutoCollectionPossible("Auto Appearance") && this.autoAppearanceDisabled) {
                this.autoAppearanceDisabled = false;
                AutoCollection.enableAutoAppearanceTracking(getApplication());
            }
        }
    }

    /**
     * Disable auto appearance management tracking. This feature only works if
     * {@link TelemetryClient#initialize} has been called before.
     */
    protected void disableAutoAppearanceTracking() {
        synchronized (TelemetryClient.LOCK) {
            if (isAutoCollectionPossible("Auto Appearance") && !this.autoAppearanceDisabled) {
                this.autoAppearanceDisabled = true;
                AutoCollection.disableAutoAppearanceTracking();
            }
        }
    }

    protected void startSyncWhenBackgrounding() {
        if (!Util.isLifecycleTrackingAvailable()) {
            return;
        }

        Application app = getApplication();
        if (app != null) {
            SyncUtil.getInstance().start(app);
        } else {
            InternalLogging.warn(TAG, "Couldn't turn on SyncUtil because given application " +
                  "was null");
        }
    }

    /**
     * Will check if autocollection is possible
     *
     * @param featureName The name of the feature which will be logged in case autocollection is not
     *                    possible
     * @return a flag indicating if autocollection features can be activated
     */
    private boolean isAutoCollectionPossible(String featureName) {
        if (!Util.isLifecycleTrackingAvailable()) {
            InternalLogging.warn(TAG, "AutoCollection feature " + featureName +
                  " can't be enabled/disabled, because " +
                  "it is not supported on this OS version.");
            return false;
        } else if (getApplication() == null) {
            InternalLogging.warn(TAG, "AutoCollection feature " + featureName +
                  " can't be enabled/disabled, because " +
                  "ApplicationInsights has not been setup with an application.");
            return false;
        } else {
            return true;
        }
    }

    /**
     * Get the reference to the Application (used for life-cycle tracking)
     *
     * @return the reference to the application that was used during initialization of the SDK
     */
    private Application getApplication() {
        Application application = null;
        if (weakApplication != null) {
            application = weakApplication.get();
        }

        return application;
    }
}
