package com.microsoft.applicationinsights.library;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import com.microsoft.applicationinsights.logging.InternalLogging;

import java.util.Map;

public enum ApplicationInsights {
    INSTANCE;

    /**
     * The tag for logging.
     */
    private static final String TAG = "ApplicationInsights";

    /**
     * A flag which determines, if developer mode (logging) should be enabled.
     */
    private static boolean DEVELOPER_MODE;

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
    private ApplicationInsights(){
        this.telemetryDisabled = false;
        this.exceptionTrackingDisabled = false;
        this.autoCollectionDisabled = false;
        setDeveloperMode(Util.isEmulator() || Util.isDebuggerAttached());
    }

    /**
     * Configure Application Insights
     * Note: This should be called before start
     *
     * @param context the context associated with Application Insights
     */
    public static void setup(Context context){
        ApplicationInsights.INSTANCE.setupInstance(context, null, null);
    }

    /**
     * Configure Application Insights
     * Note: This should be called before start
     *
     * @param context the context associated with Application Insights
     * @param application the application needed for auto collecting telemetry data
     */
    public static void setup(Context context, Application application){
        ApplicationInsights.INSTANCE.setupInstance(context, null, null);
    }

    /**
     * Configure Application Insights
     * Note: This should be called before start
     *
     * @param context the context associated with Application Insights
     * @param instrumentationKey the instrumentation key associated with the app
     */
    public static void setup(Context context, String instrumentationKey){
        ApplicationInsights.INSTANCE.setupInstance(context, null, instrumentationKey);
    }

    /**
     * Configure Application Insights
     * Note: This should be called before start
     *
     * @param context the context associated with Application Insights
     * @param application the application needed for auto collecting telemetry data
     * @param instrumentationKey the instrumentation key associated with the app
     */
    public static void setup(Context context, Application application,  String instrumentationKey){
        ApplicationInsights.INSTANCE.setupInstance(context, application, instrumentationKey);
    }

    /**
     * Configure Application Insights
     * Note: This should be called before start
     *
     * @param context the context associated with Application Insights
     * @param instrumentationKey the instrumentation key associated with the app
     */
    public void setupInstance(Context context, Application application, String instrumentationKey){
        if(!isSetup) {
            if(context != null){
                this.context = context;
                this.instrumentationKey = instrumentationKey;
                this.application = application;
                this.isSetup = true;
                InternalLogging.info(TAG, "ApplicationInsights has been setup correctly.", null);
            }else{
                InternalLogging.warn(TAG, "ApplicationInsights could not be setup correctly " +
                        "because the given context was null");
            }
        }

    }

    /**
     * Start ApplicationInsights
     * Note: This should be called after {@link #isSetup}
     */
    public static void start(){
        INSTANCE.startInstance();
    }

    /**
     * Start ApplicationInsights
     * Note: This should be called after {@link #isSetup}
     */
    public void startInstance(){
        if(!isSetup){
            InternalLogging.warn(TAG, "Could not start ApplicationInsight since it has not been " +
                    "setup correctly.");
            return;
        }
        if (!isRunning) {

            // Get telemetry context
            String iKey = null;
            if(this.instrumentationKey != null){
                iKey = this.instrumentationKey;
            }else{
                iKey = readInstrumentationKey(this.context);
            }

            TelemetryContext telemetryContext = new TelemetryContext(this.context, iKey);
            EnvelopeFactory.INSTANCE.configure(telemetryContext, this.commonProperties);

            // Start autocollection feature
            TelemetryClient.initialize(!telemetryDisabled);
            if(!this.telemetryDisabled && !this.autoCollectionDisabled){
                LifeCycleTracking.initialize(telemetryContext);
                if(this.application != null){
                    TelemetryClient.getInstance().enableActivityTracking(this.application);
                }else{
                    InternalLogging.warn(TAG, "Auto collection of page views could not be " +
                            "started, since the given application was null");
                }
            }

            // Start crash reporting
            if(!this.exceptionTrackingDisabled){
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
     * {@link com.microsoft.applicationinsights.library.config.QueueConfig#maxBatchIntervalMs} after
     * tracking any telemetry so it is not necessary to call this in most cases.
     */
    public static void sendPendingData() {
        if(!isRunning){
            InternalLogging.warn(TAG, "Could not set send pending data, because " +
                    "ApplicationInsights has not been started, yet.");
            return;
        }
        Channel.getInstance().synchronize();
    }

    /**
     * Enable auto page view tracking as well as auto session tracking. This will only work, if
     * {@link ApplicationInsights#telemetryDisabled} is set to false.
     * @deprecated This method is deprecated: Use setAutoCollectionDisabled instead.
     * @param application the application used to register the life cycle callbacks
     */
    public static void enableActivityTracking(Application application){
        if(!isRunning){
            InternalLogging.warn(TAG, "Could not set exception tracking, because " +
                    "ApplicationInsights has not been started, yet.");
            return;
        }
        if(!INSTANCE.telemetryDisabled){
            TelemetryClient.getInstance().enableActivityTracking(application);
        }
    }

    /**
     * Enable / disable tracking of unhandled exceptions.
     *
     * @param disabled if set to true, crash reporting will be disabled
     */
    public static void setExceptionTrackingDisabled(boolean disabled){
        if(!isSetup){
            InternalLogging.warn(TAG, "Could not enable/disable exception tracking, because " +
                    "ApplicationInsights has not been setup correctly.");
            return;
        }
        if(isRunning){
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
    public static void setTelemetryDisabled(boolean disabled){
        if(!isSetup){
            InternalLogging.warn(TAG, "Could not enable/disable telemetry, because " +
                    "ApplicationInsights has not been setup correctly.");
            return;
        }
        if(isRunning){
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
    public static void setAutoCollectionDisabled(boolean disabled){
        if(!isSetup){
            InternalLogging.warn(TAG, "Could not enable/disable auto collection, because " +
                    "ApplicationInsights has not been setup correctly.");
            return;
        }
        if(isRunning){
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
        if(!isSetup){
            InternalLogging.warn(TAG, "Could not set common properties, because " +
                    "ApplicationInsights has not been setup correctly.");
            return;
        }
        if(isRunning){
            InternalLogging.warn(TAG, "Could not set common properties, because " +
                    "ApplicationInsights has already been started.");
            return;
        }
        INSTANCE.commonProperties = commonProperties;
    }

    public static void setDeveloperMode(boolean developerMode) {
        DEVELOPER_MODE = developerMode;
    }

    public static boolean isDeveloperMode() {
        return DEVELOPER_MODE;
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
     * Writes instructions on how to configure the instrumentation key.
     */
    private static void logInstrumentationInstructions() {
        String instructions = "No instrumentation key found.\n" +
                "Set the instrumentation key in AndroidManifest.xml";
        String manifestSnippet = "<meta-data\n" +
                "android:name=\"com.microsoft.applicationinsights.instrumentationKey\"\n" +
                "android:value=\"${AI_INSTRUMENTATION_KEY}\" />";
        InternalLogging.error("MissingInstrumentationkey", instructions + "\n" + manifestSnippet);
    }
}