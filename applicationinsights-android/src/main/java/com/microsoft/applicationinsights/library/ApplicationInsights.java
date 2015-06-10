package com.microsoft.applicationinsights.library;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import com.microsoft.applicationinsights.library.config.ApplicationInsightsConfig;
import com.microsoft.applicationinsights.logging.InternalLogging;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public enum ApplicationInsights {
    INSTANCE;

    /**
     * The tag for logging.
     */
    private static final String TAG = "ApplicationInsights";

    /**
     * A flag which determines, if developer mode (logging) should be enabled.
     */
    private static AtomicBoolean DEVELOPER_MODE = new AtomicBoolean(Util.isEmulator() || Util.isDebuggerAttached());

    /**
     * The configuration of the SDK.
     */
    private ApplicationInsightsConfig config;

    /**
     * A flag, which determines if auto collection of sessions and page views should be disabled.
     * Default is false.
     *
     * @deprecated 1.0-beta.5 will have a feature to turn of autocollection at runtime
     */
    private boolean autoLifecycleCollectionDisabled;

    /**
     * A flag, which determines if sending telemetry data should be disabled. Default is false.
     */
    private boolean telemetryDisabled;

    /**
     * A flag, which determines if crash reporting should be disabled. Default is false.
     */
    private boolean exceptionTrackingDisabled;

    /**
     * The instrumentation key associated with the app.
     */
    private String instrumentationKey;

    /**
     * The weakContext which contains additional information for the telemetry data sent out.
     */
    private TelemetryContext telemetryContext;

    /**
     * A custom user ID used for sending telemetry data.
     */
    private String userId;

    /**
     * The weakContext associated with Application Insights.
     */
    private WeakReference<Context> weakContext;

    /**
     * The application needed for auto collecting telemetry data
     */
    private WeakReference<Application> weakApplication;

    /**
     * Properties associated with this telemetryContext.
     */
    private Map<String, String> commonProperties;

    /**
     * Flag that indicates that the user has called a setup-method before
     */
    private static boolean isConfigured;

    /**
     * Flag that indicates that the pipeline (Channel, Persistence, etc.) have been setup
     */
    private static boolean isSetupAndRunning;

    /**
     * Create ApplicationInsights instance
     */
    ApplicationInsights() {
        this.telemetryDisabled = false;
        this.exceptionTrackingDisabled = false;
        this.autoLifecycleCollectionDisabled = false;
        this.config = new ApplicationInsightsConfig();
    }

    /**
     * Configure Application Insights
     * Note: This should be called before start
     *
     * @param application the application context the application needed for auto collecting telemetry data
     */
    public static void setup(Context context, Application application) {
        ApplicationInsights.INSTANCE.setupInstance(context, application, null);
    }

    /**
     * Configure Application Insights
     * Note: This should be called before start
     *
     * @param context            the application context associated with Application Insights
     * @param application        the application needed for auto collecting telemetry data
     * @param instrumentationKey the instrumentation key associated with the app
     */
    public static void setup(Context context, Application application, String instrumentationKey) {
        ApplicationInsights.INSTANCE.setupInstance(context, application, instrumentationKey);
    }

    /**
     * Configure Application Insights
     * Note: This should be called before start
     *
     * @param context            the application context associated with Application Insights
     * @param instrumentationKey the instrumentation key associated with the app
     */
    public void setupInstance(Context context, Application application, String instrumentationKey) {
        if (!isConfigured) {
            if (context != null) {
                this.weakContext = new WeakReference<Context>(context);
                this.instrumentationKey = instrumentationKey;
                this.weakApplication = new WeakReference<Application>(application);
                isConfigured = true;
                InternalLogging.info(TAG, "ApplicationInsights has been setup correctly.", null);
            } else {
                InternalLogging.warn(TAG, "ApplicationInsights could not be setup correctly " +
                      "because the given weakContext was null");
            }
        }

    }

    /**
     * Start ApplicationInsights
     * Note: This should be called after {@link #isConfigured}
     */
    public static void start() {
        INSTANCE.startInstance();
    }

    /**
     * Start ApplicationInsights
     * Note: This should be called after {@link #isConfigured}
     */
    public void startInstance() {
        if (!isConfigured) {
            InternalLogging.warn(TAG, "Could not start Application Insights since it has not been " +
                  "setup correctly.");
            return;
        }
        if (!isSetupAndRunning) {
            Context context = INSTANCE.getContext();

            if (context == null) {
                InternalLogging.warn(TAG, "Could not start Application Insights as context is null");
                return;
            }

            if (this.instrumentationKey == null) {
                this.instrumentationKey = readInstrumentationKey(context);
            }

            this.telemetryContext = new TelemetryContext(context, this.instrumentationKey, userId);

            initializePipeline(context);
            startSyncWhenBackgrounding();
            setupAndStartAutocollection();
            startCrashReporting();

            Sender.getInstance().sendDataOnAppStart();
            InternalLogging.info(TAG, "ApplicationInsights has been started.", "");
        }
    }

    private void startCrashReporting() {
        // Start crash reporting
        if (!this.exceptionTrackingDisabled) {
            ExceptionTracking.registerExceptionHandler();
        }
    }

    private void setupAndStartAutocollection() {
        if ((INSTANCE.getApplication() != null) && !this.autoLifecycleCollectionDisabled) {
            AutoCollection.initialize(telemetryContext, this.config);
            enableAutoCollection();
        } else {
            InternalLogging.warn(TAG, "Auto collection of page views could not be " +
                  "started. Either the given application was null, the device's API level " +
                  "is lower than 14, or the user actively disabled the feature.");
        }
    }

    private void startSyncWhenBackgrounding() {
        if (INSTANCE.getApplication() != null) {
            SyncUtil.getInstance().start(INSTANCE.getApplication());
        } else {
            InternalLogging.warn(TAG, "Couldn't turn on SyncUtil becuase given application " +
                  "was null");
        }
    }

    private void initializePipeline(Context context) {
        EnvelopeFactory.initialize(telemetryContext, this.commonProperties);

        Persistence.initialize(context);
        Sender.initialize(this.config);
        Channel.initialize(this.config);

        // Initialize Telemetry
        TelemetryClient.initialize(!telemetryDisabled);

        isSetupAndRunning = true;
    }

    /**
     * Triggers persisting and if applicable sending of queued data
     * note: this will be called
     * {@link com.microsoft.applicationinsights.library.config.ApplicationInsightsConfig#maxBatchIntervalMs} after
     * tracking any telemetry so it is not necessary to call this in most cases.
     */
    public static void sendPendingData() {
        if (!isSetupAndRunning) {
            InternalLogging.warn(TAG, "Could not set send pending data, because " +
                  "ApplicationInsights has not been started, yet.");
            return;
        }
        Channel.getInstance().synchronize();
    }

    /**
     * enables all auto-collection features
     *
     * @warning requires ApplicationInsights to be setup with an Application object
     */
    public static void enableAutoCollection() {
        enableAutoAppearanceTracking();
        if (Util.isLifecycleTrackingAvailable()) {
            enableAutoPageViewTracking();
            enableAutoSessionManagement();
        }
    }

    /**
     * disables all auto-collection features
     */
    public static void disableAutoCollection() {
        disableAutoAppearanceTracking();
        if (Util.isLifecycleTrackingAvailable()) {
            disableAutoPageViewTracking();
            disableAutoSessionManagement();
        }
    }

    /**
     * Enable auto page view tracking. This will only work, if ApplicationInsights has been setup
     * with an application. This method should only be called after
     * {@link com.microsoft.applicationinsights.library.ApplicationInsights#start()}.
     */
    public static void enableAutoPageViewTracking() {
        if (!Util.isLifecycleTrackingAvailable()) {
            InternalLogging.warn(TAG, "Could not enable page view tracking, because " +
                  "it is not supported on this OS version.");
        } else if (!isSetupAndRunning) {
            InternalLogging.warn(TAG, "Could not enable page view tracking, because " +
                  "ApplicationInsights has not been started yet.");
            return;
        } else if (INSTANCE.getApplication() == null) {
            InternalLogging.warn(TAG, "Could not enable page view tracking, because " +
                  "ApplicationInsights has not been setup with an application.");
            return;
        } else {
            AutoCollection.enableAutoPageViews(INSTANCE.getApplication());
        }
    }

    /**
     * Disable auto page view tracking. This will only work, if ApplicationInsights has been setup
     * with an application. This method should only be called after
     * {@link com.microsoft.applicationinsights.library.ApplicationInsights#start()}.
     */
    public static void disableAutoPageViewTracking() {
        if (!Util.isLifecycleTrackingAvailable()) {
            InternalLogging.warn(TAG, "Could not disable page view tracking, because " +
                  "it is not supported on this OS version.");
        } else if (!isSetupAndRunning) {
            InternalLogging.warn(TAG, "Could not disable page view tracking, because " +
                  "ApplicationInsights has not been started yet.");
            return;
        } else if (INSTANCE.getApplication() == null) {
            InternalLogging.warn(TAG, "Could not disable page view tracking, because " +
                  "ApplicationInsights has not been setup with an application.");
            return;
        } else {
            AutoCollection.disableAutoPageViews();
        }
    }

    /**
     * Enable auto session tracking. This will only work, if ApplicationInsights has been setup
     * with an application. This method should only be called after
     * {@link com.microsoft.applicationinsights.library.ApplicationInsights#start()}.
     */
    public static void enableAutoSessionManagement() {
        if (!Util.isLifecycleTrackingAvailable()) {
            InternalLogging.warn(TAG, "Could not enable auto session management, because " +
                  "it is not supported on this OS version.");
        } else if (!isSetupAndRunning) {
            InternalLogging.warn(TAG, "Could not enable auto session management, because " +
                  "ApplicationInsights has not been started yet.");
            return;
        } else if (INSTANCE.getApplication() == null) {
            InternalLogging.warn(TAG, "Could not enable auto session management, because " +
                  "ApplicationInsights has not been setup with an application.");
            return;
        } else {
            AutoCollection.enableAutoSessionManagement(INSTANCE.getApplication());
        }
    }

    /**
     * Disable auto session tracking. This will only work, if ApplicationInsights has been setup
     * with an application. This method should only be called after
     * {@link com.microsoft.applicationinsights.library.ApplicationInsights#start()}.
     */
    public static void disableAutoSessionManagement() {
        if (!Util.isLifecycleTrackingAvailable()) {
            InternalLogging.warn(TAG, "Could not disable page view tracking, because " +
                  "it is not supported on this OS version.");
        } else if (!isSetupAndRunning) {
            InternalLogging.warn(TAG, "Could not disable page view tracking, because " +
                  "ApplicationInsights has not been started yet.");
            return;
        } else if (INSTANCE.getApplication() == null) {
            InternalLogging.warn(TAG, "Could not disable page view tracking, because " +
                  "ApplicationInsights has not been setup with an application.");
            return;
        } else {
            AutoCollection.disableAutoSessionManagement();
        }
    }

    /**
     * Enable auto appearance tracking. This will only work, if ApplicationInsights has been setup
     * with an application. This method should only be called after
     * {@link com.microsoft.applicationinsights.library.ApplicationInsights#start()}.
     */
    public static void enableAutoAppearanceTracking() {
        if (!isSetupAndRunning) {
            InternalLogging.warn(TAG, "Could not enable auto appearance tracking, because " +
                  "ApplicationInsights has not been started yet.");
            return;
        } else if (INSTANCE.getApplication() == null) {
            InternalLogging.warn(TAG, "Could not enable auto appearance tracking, because " +
                  "ApplicationInsights has not been setup with an application.");
            return;
        } else {
            AutoCollection.enableAutoAppearanceTracking(INSTANCE.getApplication());
        }
    }

    /**
     * Disable auto appearance tracking. This will only work, if ApplicationInsights has been setup
     * with an application. This method should only be called after
     * {@link com.microsoft.applicationinsights.library.ApplicationInsights#start()}.
     */
    public static void disableAutoAppearanceTracking() {
        if (!isSetupAndRunning) {
            InternalLogging.warn(TAG, "Could not disable auto appearance tracking, because " +
                  "ApplicationInsights has not been started yet.");
            return;
        } else if (INSTANCE.getApplication() == null) {
            InternalLogging.warn(TAG, "Could not disable auto appearance tracking, because " +
                  "ApplicationInsights has not been setup with an application.");
            return;
        } else {
            AutoCollection.disableAutoAppearanceTracking();
        }
    }

    /**
     * Enable / disable tracking of unhandled exceptions.
     *
     * @param disabled if set to true, crash reporting will be disabled
     */
    public static void setExceptionTrackingDisabled(boolean disabled) {
        if (!isConfigured) {
            InternalLogging.warn(TAG, "Could not enable/disable exception tracking, because " +
                  "ApplicationInsights has not been setup correctly.");
            return;
        }
        if (isSetupAndRunning) {
            InternalLogging.warn(TAG, "Could not enable/disable exception tracking, because " +
                  "ApplicationInsights has already been started.");
            return;
        }
        INSTANCE.exceptionTrackingDisabled = disabled;
    }

    /**
     * Enable / disable tracking of telemetry data.
     *
     * @param disabled if set to true, the telemetry feature will be disabled
     */
    public static void setTelemetryDisabled(boolean disabled) {
        if (!isConfigured) {
            InternalLogging.warn(TAG, "Could not enable/disable telemetry, because " +
                  "ApplicationInsights has not been setup correctly.");
            return;
        }
        if (isSetupAndRunning) {
            InternalLogging.warn(TAG, "Could not enable/disable telemetry, because " +
                  "ApplicationInsights has already been started.");
            return;
        }
        INSTANCE.telemetryDisabled = disabled;
    }

    /**
     * Enable / disable auto collection of telemetry data.
     *
     * @param disabled if set to true, the auto collection feature will be disabled
     * @deprecated with 1.0-beta.5
     * Use {@link ApplicationInsights#disableAutoCollection()} or the more specific
     * {@link ApplicationInsights#disableAutoSessionManagement()},
     * {@link ApplicationInsights#disableAutoAppearanceTracking()} and
     * {@link ApplicationInsights#disableAutoPageViewTracking()}
     */
    public static void setAutoCollectionDisabled(boolean disabled) {
        if (!isConfigured) {
            InternalLogging.warn(TAG, "Could not enable/disable auto collection, because " +
                  "ApplicationInsights has not been setup correctly.");
            return;
        }
        if (isSetupAndRunning) {
            InternalLogging.warn(TAG, "Could not enable/disable auto collection, because " +
                  "ApplicationInsights has already been started.");
            return;
        }
        INSTANCE.autoLifecycleCollectionDisabled = disabled;
    }

    /**
     * Gets the properties which are common to all telemetry sent from this client.
     *
     * @return common properties for this telemetry client
     */
    public static Map<String, String> getCommonProperties() {
        return INSTANCE.commonProperties;
    }

    /**
     * Sets properties which are common to all telemetry sent form this client.
     *
     * @param commonProperties a dictionary of properties to enqueue with all telemetry.
     */
    public static void setCommonProperties(Map<String, String> commonProperties) {
        if (!isConfigured) {
            InternalLogging.warn(TAG, "Could not set common properties, because " +
                  "ApplicationInsights has not been setup correctly.");
            return;
        }
        if (isSetupAndRunning) {
            InternalLogging.warn(TAG, "Could not set common properties, because " +
                  "ApplicationInsights has already been started.");
            return;
        }
        INSTANCE.commonProperties = commonProperties;
    }

    public static void setDeveloperMode(boolean developerMode) {
        DEVELOPER_MODE.set(developerMode);
    }

    public static boolean isDeveloperMode() {
        return DEVELOPER_MODE.get();
    }

    /**
     * Reads the instrumentation key from AndroidManifest.xml if it is available
     *
     * @param context the application weakContext to check the manifest from
     * @return the instrumentation key configured for the application
     */
    private String readInstrumentationKey(Context context) {
        String iKey = "";
        if (context != null) {
            try {
                Bundle bundle = context
                      .getPackageManager()
                      .getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA)
                      .metaData;
                if (bundle != null) {
                    iKey = bundle.getString("com.microsoft.applicationinsights.instrumentationKey");
                } else {
                    logInstrumentationInstructions();
                }
            } catch (PackageManager.NameNotFoundException exception) {
                logInstrumentationInstructions();
                Log.v(TAG, exception.toString());
            }
        }

        return iKey;
    }

    /**
     * Returns the application reference that Application Insights needs.
     *
     * @return the Context that's used by the Application Insights SDK
     */
    protected Context getContext() {
        Context context = null;
        if (weakContext != null) {
            context = weakContext.get();
        }

        return context;
    }

    /**
     * Get the reference to the Application (used for life-cycle tracking)
     *
     * @return the reference to the application that was used during initialization of the SDK
     */
    protected Application getApplication() {
        Application application = null;
        if (weakApplication != null) {
            application = weakApplication.get();
        }

        return application;
    }


    /* Writes instructions on how to configure the instrumentation key.
        */
    private static void logInstrumentationInstructions() {
        String instructions = "No instrumentation key found.\n" +
              "Set the instrumentation key in AndroidManifest.xml";
        String manifestSnippet = "<meta-data\n" +
              "android:name=\"com.microsoft.applicationinsights.instrumentationKey\"\n" +
              "android:value=\"${AI_INSTRUMENTATION_KEY}\" />";
        InternalLogging.error("MissingInstrumentationkey", instructions + "\n" + manifestSnippet);
    }

    /**
     * Gets the configuration for the ApplicationInsights instance
     *
     * @return the instance ApplicationInsights configuration
     */
    public static ApplicationInsightsConfig getConfig() {
        return INSTANCE.config;
    }

    /**
     * Sets the session configuration for the instance
     */
    public void setConfig(ApplicationInsightsConfig config) {
        if (!isConfigured) {
            InternalLogging.warn(TAG, "Could not set telemetry configuration, because " +
                  "ApplicationInsights has not been setup correctly.");
            return;
        }
        if (isSetupAndRunning) {
            InternalLogging.warn(TAG, "Could not set telemetry configuration, because " +
                  "ApplicationInsights has already been started.");
            return;
        }
        INSTANCE.config = config;
    }

    /**
     * Force Application Insights to create a new session with a custom sessionID.
     *
     * @param sessionId a custom session ID used of the session to create
     */
    public static void renewSession(String sessionId) {
        if (!INSTANCE.telemetryDisabled && INSTANCE.telemetryContext != null) {
            INSTANCE.telemetryContext.renewSessionId(sessionId);
        }
    }

    /**
     * Set the user Id associated with the telemetry data. If userId == null, ApplicationInsights
     * will generate a random ID.
     *
     * @param userId a user ID associated with the telemetry data
     */
    public static void setUserId(String userId) {
        if (isSetupAndRunning) {
            INSTANCE.telemetryContext.configUserContext(userId);
        } else {
            INSTANCE.userId = userId;
        }
    }

    /**
     * Get the instrumentation key associated with this app.
     *
     * @return the Application Insights instrumentation key set for this app
     */
    protected static String getInstrumentationKey() {
        return INSTANCE.instrumentationKey;
    }


}
