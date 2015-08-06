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
        this.autoAppearanceDisabled = false;
        this.autoPageViewsDisabled = false;
        this.autoSessionManagementDisabled = false;
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
    private void setupInstance(Context context, Application application, String instrumentationKey) {
        if (!isConfigured) {
            if (context != null) {
                this.weakContext = new WeakReference<Context>(context);
                this.weakApplication = new WeakReference<Application>(application);
                isConfigured = true;
                this.instrumentationKey = instrumentationKey;

                if (this.instrumentationKey == null) {
                    this.instrumentationKey = readInstrumentationKey(context);
                }

                if (this.user == null && this.userId != null) {
                    //in case the dev uses deprecated method to set the user's ID
                    this.user = new User();
                    this.user.setId(this.userId);
                }
                TelemetryContext.initialize(context, this.instrumentationKey, this.user);
                this.telemetryContext = TelemetryContext.getSharedInstance();
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
    private void startInstance() {
        if (!isConfigured) {
            InternalLogging.warn(TAG, "Could not start Application Insights since it has not been " +
                  "setup correctly.");
            return;
        }
        if (!isSetupAndRunning) {
            Context context = this.weakContext.get();

            initializePipeline(context);
            startSyncWhenBackgrounding();
            startAutoCollection();
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

    /**
     * Start/Stop AutoCollection features depending on user's settings.
     * Will be called during start of the SDK and when enabling/disabling ALL autocollection features
     */
    private void startAutoCollection() {
        if (autoCollectionPossible("Start of AutoCollection at app start.")) {
            //if the SDK has already been started, activate/deactivate features
            if (INSTANCE.autoAppearanceDisabled) {
                disableAutoAppearanceTracking();
            } else {
                enableAutoAppearanceTracking();
            }
            if (INSTANCE.autoPageViewsDisabled) {
                disableAutoPageViewTracking();
            } else {
                enableAutoPageViewTracking();
            }
            if (INSTANCE.autoSessionManagementDisabled) {
                disableAutoSessionManagement();
            } else {
                enableAutoSessionManagement();
            }
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

    /**
     * Makes sure Persistence, Sender, ChannelManager, TelemetryClient and AutoCollection are initialized
     * Call this before starting AutoCollection
     *
     * @param context application context
     */
    private void initializePipeline(Context context) {
        EnvelopeFactory.initialize(telemetryContext, this.commonProperties);

        Persistence.initialize(context);
        Sender.initialize(this.config);
        ChannelManager.initialize(channelType);

        // Initialize Telemetry
        TelemetryClient.initialize(!telemetryDisabled);

        isSetupAndRunning = true;

        if (autoCollectionPossible("Initialise AutoCollection")) {
            AutoCollection.initialize(telemetryContext, this.config);
        }
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
     * Enable / disable ALL auto collection of telemetry data at startup.Call this before calling
     * {@link ApplicationInsights#start()}
     *
     * @param disabled if set to true, the auto collection features will be disabled at app start
     * @deprecated with 1.0-beta.6 use {@link ApplicationInsights#disableAutoCollection()} or
     * the more specific
     * {@link ApplicationInsights#disableAutoSessionManagement()},
     * {@link ApplicationInsights#disableAutoAppearanceTracking()} and
     * {@link ApplicationInsights#disableAutoPageViewTracking()} instead
     */
    public static void setAutoCollectionDisabledAtStartup(boolean disabled) {
        INSTANCE.autoAppearanceDisabled = disabled;
        INSTANCE.autoPageViewsDisabled = disabled;
        INSTANCE.autoSessionManagementDisabled = disabled;
        //don't call startAutoCollection() because setAutoCollectionDisabledAtStartup(disabled)
        //was intended to be called
        //before the developer calls start()
    }

    /**
     * Enables all auto-collection features. Call this before
     * {@link ApplicationInsights#start()} or when ApplicationInsights is already running to change
     * AutoCollection settings at runtime.
     * Requires ApplicationInsights to be setup with an Application object
     */
    public static void enableAutoCollection() {
        INSTANCE.autoAppearanceDisabled = false;
        INSTANCE.autoPageViewsDisabled = false;
        INSTANCE.autoSessionManagementDisabled = false;
        enableAutoAppearanceTracking();
        enableAutoPageViewTracking();
        enableAutoSessionManagement();
    }

    /**
     * disables all auto-collection features
     */
    public static void disableAutoCollection() {
        INSTANCE.autoAppearanceDisabled = true;
        INSTANCE.autoPageViewsDisabled = true;
        INSTANCE.autoSessionManagementDisabled = true;
        disableAutoAppearanceTracking();
        disableAutoPageViewTracking();
        disableAutoSessionManagement();
    }

    /**
     * Enable auto page view tracking before calling {@link ApplicationInsights#start()} or
     * at runtime. This featuee only works if ApplicationInsights has been setup
     * with an application.
     */
    public static void enableAutoPageViewTracking() {
        INSTANCE.autoPageViewsDisabled = false;
        if (autoCollectionPossible("Auto PageView Tracking")) {
            AutoCollection.enableAutoPageViews(INSTANCE.getApplication());
        }
    }

    /**
     * Disable auto page view tracking before calling {@link ApplicationInsights#start()} or
     * at runtime. This feature only works if ApplicationInsights has been setup
     * with an application.
     */
    public static void disableAutoPageViewTracking() {
        INSTANCE.autoPageViewsDisabled = true;
        if (autoCollectionPossible("Auto PageView Tracking")) {
            AutoCollection.disableAutoPageViews();
        }
    }

    /**
     * Enable auto session management tracking before calling {@link ApplicationInsights#start()} or
     * at runtime. This feature only works if ApplicationInsights has been setup
     * with an application.
     */
    public static void enableAutoSessionManagement() {
        INSTANCE.autoSessionManagementDisabled = false;
        if (autoCollectionPossible("Auto Session Management")) {
            AutoCollection.enableAutoSessionManagement(INSTANCE.getApplication());
        }
    }

    /**
     * Disable auto session management tracking before calling {@link ApplicationInsights#start()} or
     * at runtime. This feature only works if ApplicationInsights has been setup
     * with an application.
     */
    public static void disableAutoSessionManagement() {
        INSTANCE.autoSessionManagementDisabled = true;
        if (autoCollectionPossible("Auto Session Management")) {
            AutoCollection.disableAutoSessionManagement();
        }
    }

    /**
     * Enable auto appearance management tracking before calling {@link ApplicationInsights#start()} or
     * at runtime. This feature only works if ApplicationInsights has been setup
     * with an application.
     */
    public static void enableAutoAppearanceTracking() {
        INSTANCE.autoAppearanceDisabled = false;
        if (autoCollectionPossible("Auto Appearance")) {
            AutoCollection.enableAutoAppearanceTracking(INSTANCE.getApplication());
        }
    }

    /**
     * Disable auto appearance management tracking before calling {@link ApplicationInsights#start()} or
     * at runtime. This feature only works if ApplicationInsights has been setup
     * with an application.
     */
    public static void disableAutoAppearanceTracking() {
        INSTANCE.autoAppearanceDisabled = true;
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
    private static boolean autoCollectionPossible(String featureName) {
        if (!Util.isLifecycleTrackingAvailable()) {
            InternalLogging.warn(TAG, "AutoCollection feature " + featureName +
                  " can't be enabled/disabled, because " +
                  "it is not supported on this OS version.");
            return false;
        } else if (!isSetupAndRunning) {
            InternalLogging.warn(TAG, "AutoCollection feature " + featureName +
                  " has been set and will be activated when calling start().");
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

    /**
     * Activates the developer mode which. It enables extensive logging as well as use different
     * settings for batching. Batch Size in debug mode is 5 and sending interval is 3s.
     * @param developerMode if true, developer mode will be activated
     */
    public static void setDeveloperMode(boolean developerMode) {
        DEVELOPER_MODE.set(developerMode);
    }

    /**
     * Check if developerMode is activated
     * @return flag indicating activated developer mode
     */
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
    Context getContext() {
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
    private Application getApplication() {
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
     * Gets the configuration for the ApplicationInsights instance
     *
     * @return the instance ApplicationInsights configuration
     */
    public static TelemetryContext getTelemetryContext() {
        if(!isConfigured){
            InternalLogging.warn(TAG, "Global telemetry context has not been set up, yet. " +
                    "You need to call setup() first.");
            return null;
        }
        return INSTANCE.telemetryContext;
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
     * @deprecated use {@link ApplicationInsights#getTelemetryContext()} instead
     */
    public static void setUserId(String userId) {
        if (isConfigured) {
            INSTANCE.telemetryContext.configUserContext(userId);
        }
    }

    /**
     * Set a custom user to be associated with the telemetry data.
     *
     * @param user a custom user object. If the param is null or one of the supported properties is null,
     *             the missing property will be generated by the SDK
     * @deprecated use {@link ApplicationInsights#getTelemetryContext()} instead
     */
    public static void setCustomUserContext(User user) {
        if (isConfigured) {
            INSTANCE.telemetryContext.configUserContext(user);
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
