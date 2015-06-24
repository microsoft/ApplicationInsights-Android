package com.microsoft.applicationinsights.library;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import com.microsoft.applicationinsights.contracts.User;
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
     * A flag, which determines if auto collection of sessions and page views should be disabled from the start.
     * Default is false.
     * The features can be enabled/disabled at runtime later
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
     *
     * @deprecated use user-property instead
     */
    private String userId;

    /**
     * A custom user object for sending telemetry data. Replaces
     * userId as we allow more configuration of the user object
     */
    private User user;

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
     * The type of channel to use for logging
     */
    private ChannelType channelType;

    /**
     * Create ApplicationInsights instance
     */
    ApplicationInsights() {
        this.telemetryDisabled = false;
        this.exceptionTrackingDisabled = false;
        this.autoLifecycleCollectionDisabled = false;
        this.channelType = ChannelType.Default;
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
            Context context = this.weakContext.get();

            if (context == null) {
                InternalLogging.warn(TAG, "Could not start Application Insights as context is null");
                return;
            }

            if (this.instrumentationKey == null) {
                this.instrumentationKey = readInstrumentationKey(context);
            }

            if(this.user != null) {
                //the dev has use setCustomUserContext to configure the user object
                this.telemetryContext = new TelemetryContext(context, this.instrumentationKey, this.user);
            }
            else if(this.userId != null) {
                //in case the dev uses deprecated method to set the user's ID
                this.user = new User();
                this.user.setId(this.userId);
                this.telemetryContext = new TelemetryContext(context, this.instrumentationKey, this.user);
            }
            else {
                //in case the dev doesn't use a custom user object
                this.telemetryContext = new TelemetryContext(context, this.instrumentationKey, new User());
            }

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
        if (INSTANCE.autoLifecycleCollectionDisabled) {
            InternalLogging.info(TAG, "Auto collection has been disabled at app start, it can" +
                  " be enabled using the various enableAuto...-Methods.");
        } else if (autoCollectionPossible("Initialization of AutoCollection at app start")) {
            AutoCollection.initialize(telemetryContext, this.config);
            enableAutoCollection();
        }
    }

    private void startSyncWhenBackgrounding() {
        if (!Util.isLifecycleTrackingAvailable()) {
            return;
        }

        if (INSTANCE.getApplication() != null) {
            SyncUtil.getInstance().start(INSTANCE.getApplication());
        } else {
            InternalLogging.warn(TAG, "Couldn't turn on SyncUtil because given application " +
                  "was null");
        }
    }

    private void initializePipeline(Context context) {
        EnvelopeFactory.initialize(telemetryContext, this.commonProperties);

        Persistence.initialize(context);
        Sender.initialize(this.config);
        ChannelManager.initialize(channelType);
        //Channel.initialize(this.config);

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
        ChannelManager.getInstance().getChannel().synchronize();
    }

    /**
     * enables all auto-collection features
     * Requires ApplicationInsights to be setup with an Application object
     */
    public static void enableAutoCollection() {
        enableAutoAppearanceTracking();
        enableAutoPageViewTracking();
        enableAutoSessionManagement();
    }

    /**
     * disables all auto-collection features
     */
    public static void disableAutoCollection() {
        disableAutoAppearanceTracking();
        disableAutoPageViewTracking();
        disableAutoSessionManagement();
    }

    /**
     * Enable auto page view tracking. This will only work, if ApplicationInsights has been setup
     * with an application. This method should only be called after
     * {@link com.microsoft.applicationinsights.library.ApplicationInsights#start()}.
     */
    public static void enableAutoPageViewTracking() {
        if (autoCollectionPossible("Auto PageView Tracking")) {
            AutoCollection.enableAutoPageViews(INSTANCE.getApplication());
        }
    }

    /**
     * Disable auto page view tracking. This will only work, if ApplicationInsights has been setup
     * with an application. This method should only be called after
     * {@link com.microsoft.applicationinsights.library.ApplicationInsights#start()}.
     */
    public static void disableAutoPageViewTracking() {
        if (autoCollectionPossible("Auto PageView Tracking")) {
            AutoCollection.disableAutoPageViews();
        }
    }

    /**
     * Enable auto session tracking. This will only work, if ApplicationInsights has been setup
     * with an application. This method should only be called after
     * {@link com.microsoft.applicationinsights.library.ApplicationInsights#start()}.
     */
    public static void enableAutoSessionManagement() {
        if (autoCollectionPossible("Auto Session Management")) {
            AutoCollection.enableAutoSessionManagement(INSTANCE.getApplication());
        }
    }

    /**
     * Disable auto session tracking. This will only work, if ApplicationInsights has been setup
     * with an application. This method should only be called after
     * {@link com.microsoft.applicationinsights.library.ApplicationInsights#start()}.
     */
    public static void disableAutoSessionManagement() {
        if (autoCollectionPossible("Auto Session Management")) {
            AutoCollection.disableAutoSessionManagement();
        }
    }

    /**
     * Enable auto appearance tracking. This will only work, if ApplicationInsights has been setup
     * with an application. This method should only be called after
     * {@link com.microsoft.applicationinsights.library.ApplicationInsights#start()}.
     */
    public static void enableAutoAppearanceTracking() {
        if (autoCollectionPossible("Auto Appearance")) {
            AutoCollection.enableAutoAppearanceTracking(INSTANCE.getApplication());
        }
    }

    /**
     * Disable auto appearance tracking. This will only work, if ApplicationInsights has been setup
     * with an application. This method should only be called after
     * {@link com.microsoft.applicationinsights.library.ApplicationInsights#start()}.
     */
    public static void disableAutoAppearanceTracking() {
        if (autoCollectionPossible("Auto Appearance")) {
            AutoCollection.disableAutoAppearanceTracking();
        }
    }

    private static boolean autoCollectionPossible(String featureName) {
        if (!Util.isLifecycleTrackingAvailable()) {
            InternalLogging.warn(TAG, "AutoCollection feature " + featureName +
                  " can't be enabled/disabled, because " +
                  "it is not supported on this OS version.");
            return false;
        } else if (!isSetupAndRunning) {
            InternalLogging.warn(TAG, "AutoCollection feature " + featureName +
                  " can't be enabled/disabled, because " +
                  "ApplicationInsights has not been started yet.");
            return false;
        } else if (INSTANCE.getApplication() == null) {
            InternalLogging.warn(TAG, "AutoCollection feature " + featureName +
                  " can't be enabled/disabled, because " +
                  "ApplicationInsights has not been setup with an application.");
            return false;
        } else {
            return true;
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
     * To enable/disable at runtime, use {@link ApplicationInsights#disableAutoCollection()} or the more specific
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
     * Enable / disable auto collection of telemetry data at startup.
     *
     * @param disabled if set to true, the auto collection feature will be disabled at app start
     *                 To enable/disable auto collection features at runtime, use
     *                 {@link ApplicationInsights#disableAutoCollection()} or the more specific
     *                 {@link ApplicationInsights#disableAutoSessionManagement()},
     *                 {@link ApplicationInsights#disableAutoAppearanceTracking()} and
     *                 {@link ApplicationInsights#disableAutoPageViewTracking()}
     */
    public static void setAutoCollectionDisabledAtStartup(boolean disabled) {
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
     * @param commonProperties a dictionary of properties to log with all telemetry.
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
        if (this.weakContext != null) {
            context = this.weakContext.get();
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
     * @deprecated use {@link ApplicationInsights#setCustomUserContext(User)} instead
     */
    public static void setUserId(String userId) {
        if (isSetupAndRunning) {
            INSTANCE.telemetryContext.configUserContext(userId);
        } else {
            INSTANCE.userId = userId;
        }
    }

    /**
     * Set a custom user to be associated with the telemetry data.
     *
     * @param user a custom user object. If the param is null or one of the supported properties is null,
     *            the missing property will be generated by the SDK
     */
    public static void setCustomUserContext(User user) {
        if (isSetupAndRunning) {
            INSTANCE.telemetryContext.configUserContext(user);
        } else {
            INSTANCE.user = user;
        }
    }

    /**
     * Sets the channel type to be used for logging
     *
     * @param channelType The channel type to use
     */
    public static void setChannelType(ChannelType channelType) {
        if (isSetupAndRunning) {
            InternalLogging.warn(TAG, "Cannot set channel type, because " +
                  "ApplicationInsights has already been started.");
            return;
        }

        INSTANCE.channelType = channelType;
    }

    /**
     * Gets the currently used channel type
     *
     * @return The current channel type.
     */
    public static ChannelType getChannelType() {
        return INSTANCE.channelType;
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
