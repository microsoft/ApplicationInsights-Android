package com.microsoft.applicationinsights.library;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import com.microsoft.applicationinsights.library.config.ApplicationInsightsConfig;
import com.microsoft.applicationinsights.logging.InternalLogging;

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
    private static AtomicBoolean DEVELOPER_MODE;

    /**
     * The configuration of the SDK.
     */
    private ApplicationInsightsConfig config;

    /**
     * A flag, which determines if auto collection of sessions and page views should be disabled.
     * Default is false.
     */
    private boolean autoCollectionDisabled;

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
     * The context which contains additional information for the telemetry data sent out.
     */
    private TelemetryContext telemetryContext;

    /**
     * A custom user ID used for sending telemetry data.
     */
    private String userId;

    /**
     * The context associated with Application Insights.
     */
    private Context context;

    /**
     * The application needed for auto collecting telemetry data
     */
    private Application application;

    /**
     * Properties associated with this telemetryContext.
     */
    private Map<String, String> commonProperties;

    private static boolean isRunning;
    private static boolean isSetup;

    /**
     * Create ApplicationInsights instance
     */
    ApplicationInsights() {
        this.telemetryDisabled = false;
        this.exceptionTrackingDisabled = false;
        this.autoCollectionDisabled = false;
        this.config = new ApplicationInsightsConfig();
        setDeveloperMode(Util.isEmulator() || Util.isDebuggerAttached());
    }

    /**
     * Configure Application Insights
     * Note: This should be called before start
     *
     * @param context the context associated with Application Insights
     */
    public static void setup(Context context) {
        ApplicationInsights.INSTANCE.setupInstance(context, null, null);
    }

    /**
     * Configure Application Insights
     * Note: This should be called before start
     *
     * @param context     the context associated with Application Insights
     * @param application the application needed for auto collecting telemetry data
     */
    public static void setup(Context context, Application application) {
        ApplicationInsights.INSTANCE.setupInstance(context, application, null);
    }

    /**
     * Configure Application Insights
     * Note: This should be called before start
     *
     * @param context            the context associated with Application Insights
     * @param instrumentationKey the instrumentation key associated with the app
     */
    public static void setup(Context context, String instrumentationKey) {
        ApplicationInsights.INSTANCE.setupInstance(context, null, instrumentationKey);
    }

    /**
     * Configure Application Insights
     * Note: This should be called before start
     *
     * @param context            the context associated with Application Insights
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
     * @param context            the context associated with Application Insights
     * @param instrumentationKey the instrumentation key associated with the app
     */
    public void setupInstance(Context context, Application application, String instrumentationKey) {
        if (!isSetup) {
            if (context != null) {
                this.context = context;
                this.instrumentationKey = instrumentationKey;
                this.application = application;
                isSetup = true;
                InternalLogging.info(TAG, "ApplicationInsights has been setup correctly.", null);
            } else {
                InternalLogging.warn(TAG, "ApplicationInsights could not be setup correctly " +
                      "because the given context was null");
            }
        }

    }

    /**
     * Start ApplicationInsights
     * Note: This should be called after {@link #isSetup}
     */
    public static void start() {
        INSTANCE.startInstance();
    }

    /**
     * Start ApplicationInsights
     * Note: This should be called after {@link #isSetup}
     */
    public void startInstance() {
        if (!isSetup) {
            InternalLogging.warn(TAG, "Could not start ApplicationInsight since it has not been " +
                  "setup correctly.");
            return;
        }
        if (!isRunning) {

            if (this.instrumentationKey == null) {
                this.instrumentationKey = readInstrumentationKey(this.context);
            }

            this.telemetryContext = new TelemetryContext(this.context, this.instrumentationKey, userId);
            EnvelopeFactory.INSTANCE.configure(telemetryContext, this.commonProperties);

            Persistence.initialize(this.context);
            Sender.initialize(this.config);
            Channel.initialize(this.config);

            // Start autocollection feature
            TelemetryClient.initialize(!telemetryDisabled);
            LifeCycleTracking.initialize(telemetryContext, this.config);
            if (this.application != null && !this.autoCollectionDisabled) {
                LifeCycleTracking.registerPageViewCallbacks(this.application);
                LifeCycleTracking.registerSessionManagementCallbacks(this.application);
            } else {
                InternalLogging.warn(TAG, "Auto collection of page views could not be " +
                          "started, since the given application was null");
            }

            // Start crash reporting
            if (!this.exceptionTrackingDisabled) {
                ExceptionTracking.registerExceptionHandler(this.context);
            }

            isRunning = true;
            Sender.getInstance().sendDataOnAppStart();
            InternalLogging.info(TAG, "ApplicationInsights has been started.", null);
        }
    }

    /**
     * Triggers persisting and if applicable sending of queued data
     * note: this will be called
     * {@link com.microsoft.applicationinsights.library.config.ApplicationInsightsConfig#maxBatchIntervalMs} after
     * tracking any telemetry so it is not necessary to call this in most cases.
     */
    public static void sendPendingData() {
        if (!isRunning) {
            InternalLogging.warn(TAG, "Could not set send pending data, because " +
                  "ApplicationInsights has not been started, yet.");
            return;
        }
        Channel.getInstance().synchronize();
    }

    /**
     * Enable auto page view tracking as well as auto session tracking. This will only work, if
     * {@link ApplicationInsights#telemetryDisabled} is set to false.
     *
     * @param application the application used to register the life cycle callbacks
     * @deprecated This method is deprecated: Use setAutoCollectionDisabled instead.
     */
    public static void enableActivityTracking(Application application) {
        if (!isRunning) { //TODO fix log warning
            InternalLogging.warn(TAG, "Could not set activity tracking, because " +
                  "ApplicationInsights has not been started, yet.");
            return;
        }
        if (!INSTANCE.telemetryDisabled) {
            LifeCycleTracking.registerActivityLifecycleCallbacks(application);
        }
    }

    /**
     * Enable auto page view tracking. This will only work, if ApplicationInsights has been setup
     * with an application. This method should only be called after
     * {@link com.microsoft.applicationinsights.library.ApplicationInsights#start()}.
     */
    public static void enableAutoPageViewTracking() {
        if(!isRunning){
            InternalLogging.warn(TAG, "Could not set page view tracking, because " +
                    "ApplicationInsights has not been started yet.");
            return;
        }else if (INSTANCE.application == null) {
            InternalLogging.warn(TAG, "Could not set page view tracking, because " +
                    "ApplicationInsights has not been setup with an application.");
            return;
        }else{
            LifeCycleTracking.registerPageViewCallbacks(INSTANCE.application);
        }
    }

    /**
     * Disable auto page view tracking. This will only work, if ApplicationInsights has been setup
     * with an application. This method should only be called after
     * {@link com.microsoft.applicationinsights.library.ApplicationInsights#start()}.
     */
    public static void disableAutoPageViewTracking() {
        if(!isRunning){
            InternalLogging.warn(TAG, "Could not unset page view tracking, because " +
                    "ApplicationInsights has not been started yet.");
            return;
        }else if (INSTANCE.application == null) {
            InternalLogging.warn(TAG, "Could not unset page view tracking, because " +
                    "ApplicationInsights has not been setup with an application.");
            return;
        }else{
            LifeCycleTracking.unregisterPageViewCallbacks(INSTANCE.application);
        }
    }

    /**
     * Enable auto session tracking. This will only work, if ApplicationInsights has been setup
     * with an application. This method should only be called after
     * {@link com.microsoft.applicationinsights.library.ApplicationInsights#start()}.
     */
    public static void enableAutoSessionManagement() {
        if(!isRunning){
            InternalLogging.warn(TAG, "Could not set session management, because " +
                    "ApplicationInsights has not been started yet.");
            return;
        }else if (INSTANCE.application == null) {
            InternalLogging.warn(TAG, "Could not set session management, because " +
                    "ApplicationInsights has not been setup with an application.");
            return;
        }else{
            LifeCycleTracking.registerSessionManagementCallbacks(INSTANCE.application);
        }
    }

    /**
     * Disable auto session tracking. This will only work, if ApplicationInsights has been setup
     * with an application. This method should only be called after
     * {@link com.microsoft.applicationinsights.library.ApplicationInsights#start()}.
     */
    public static void disableAutoSessionManagement() {
        if(!isRunning){
            InternalLogging.warn(TAG, "Could not unset session management, because " +
                    "ApplicationInsights has not been started yet.");
            return;
        }else if (INSTANCE.application == null) {
            InternalLogging.warn(TAG, "Could not unset session management, because " +
                    "ApplicationInsights has not been setup with an application.");
            return;
        }else{
            LifeCycleTracking.unregisterSessionManagementCallbacks(INSTANCE.application);
        }
    }

    /**
     * Enable / disable tracking of unhandled exceptions.
     *
     * @param disabled if set to true, crash reporting will be disabled
     */
    public static void setExceptionTrackingDisabled(boolean disabled) {
        if (!isSetup) {
            InternalLogging.warn(TAG, "Could not enable/disable exception tracking, because " +
                  "ApplicationInsights has not been setup correctly.");
            return;
        }
        if (isRunning) {
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
        if (!isSetup) {
            InternalLogging.warn(TAG, "Could not enable/disable telemetry, because " +
                  "ApplicationInsights has not been setup correctly.");
            return;
        }
        if (isRunning) {
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
     */
    public static void setAutoCollectionDisabled(boolean disabled) {
        if (!isSetup) {
            InternalLogging.warn(TAG, "Could not enable/disable auto collection, because " +
                  "ApplicationInsights has not been setup correctly.");
            return;
        }
        if (isRunning) {
            InternalLogging.warn(TAG, "Could not enable/disable auto collection, because " +
                  "ApplicationInsights has already been started.");
            return;
        }
        INSTANCE.autoCollectionDisabled = disabled;
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
        if (!isSetup) {
            InternalLogging.warn(TAG, "Could not set common properties, because " +
                  "ApplicationInsights has not been setup correctly.");
            return;
        }
        if (isRunning) {
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
     * @param context the application context to check the manifest from
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
     * Returns the application context that Application Insights uses.
     *
     * @return context the Context that's used by the Application Insights SDK
     */
    public Context getContext() {
        return this.context;
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
        if (!isSetup) {
            InternalLogging.warn(TAG, "Could not set telemetry configuration, because " +
                  "ApplicationInsights has not been setup correctly.");
            return;
        }
        if (isRunning) {
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
    public static void renewSession(String sessionId){
        if(!INSTANCE.telemetryDisabled && INSTANCE.telemetryContext != null){
            INSTANCE.telemetryContext.renewSessionId(sessionId);
        }
    }

    /**
     * Set the user Id associated with the telemetry data. If userId == null, ApplicationInsights
     * will generate a random ID.
     *
     * @param userId a user ID associated with the telemetry data
     */
    public static void setUserId(String userId){
        if(isRunning){
            INSTANCE.telemetryContext.configUserContext(userId);
        }else{
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
