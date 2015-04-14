package com.microsoft.applicationinsights;

import android.app.Application;
import android.content.Context;

import com.microsoft.applicationinsights.internal.Channel;
import com.microsoft.applicationinsights.internal.EnvelopeFactory;
import com.microsoft.applicationinsights.internal.TelemetryContext;
import com.microsoft.applicationinsights.internal.logging.InternalLogging;

import java.util.Map;

public enum ApplicationInsights {
    INSTANCE;

    /**
     * A flag which determines, if developer mode (logging) should be enabled.
     */
    private static boolean DEVELOPER_MODE;
    private boolean telemetryDisabled;
    private boolean exceptionTrackingDisabled;
    private String instrumentationKey;
    private Context context;

    /**
     * The configuration for this telemetry client.
     */
    protected SessionConfig config;

    /**
     * Properties associated with this telemetryContext.
     */
    private Map<String, String> commonProperties;

    private static boolean isRunning;

    /**
     * Create AppInsights instance
     */
    private ApplicationInsights(){
        this.telemetryDisabled = false;
        this.exceptionTrackingDisabled = false;
    }

    /**
     * Configure Application Insights
     * Note: This should be called before start
     *
     * @param context the context associated with Application Insights
     */
    public static void setup(Context context){
        ApplicationInsights.INSTANCE.setupInstance(context, null);
    }

    /**
     * Configure Application Insights
     * Note: This should be called before start
     *
     * @param context the context associated with Application Insights
     * @param instrumentationKey the instrumentation key associated with the app
     */
    public static void setup(Context context, String instrumentationKey){
        ApplicationInsights.INSTANCE.setupInstance(context, instrumentationKey);
    }

    /**
     * Configure Application Insights
     * Note: This should be called before start
     *
     * @param context the context associated with Application Insights
     * @param instrumentationKey the instrumentation key associated with the app
     */
    public void setupInstance(Context context, String instrumentationKey){
        if(isRunning) {
            return;
        }
        this.context = context;
        this.instrumentationKey = instrumentationKey;
        this.config = new SessionConfig(this.context);
    }

    /**
     * Start Application Insights
     * Note: This should be called after {@link #setup}
     */
    public static void start(){

        INSTANCE.startInstance();

    }

    /**
     * Start Application Insights
     * Note: This should be called after {@link #setup}
     */
    public void startInstance(){
        if(!isRunning) {
            isRunning = true;
            String iKey = null;

            if(this.instrumentationKey != null){
                iKey = this.instrumentationKey;
            }else{
                iKey = config.getInstrumentationKey();
            }

            TelemetryContext telemetryContext = new TelemetryContext(this.context, iKey);
            EnvelopeFactory.INSTANCE.configure(telemetryContext, this.commonProperties);

            if(!this.telemetryDisabled){
                LifeCycleTracking.initialize(config, telemetryContext);
            }
            if(!this.exceptionTrackingDisabled){
                ExceptionTracking.registerExceptionHandler(this.context);
            }
            sendPendingData();
        }
    }

    /**
     * Triggers persisting and if applicable sending of queued data
     * note: this will be called
     * {@link com.microsoft.applicationinsights.internal.TelemetryConfig#maxBatchIntervalMs} after
     * tracking any telemetry so it is not necessary to call this in most cases.
     */
    public static void sendPendingData() {
        Channel.getInstance().synchronize();
    }

    /**
     * Enable auto page view tracking as well as auto session tracking. This will only work, if
     * {@link ApplicationInsights#telemetryDisabled} is set to false.
     *
     * @param application the application used to register the life cycle callbacks
     */
    public static void enableActivityTracking(Application application){
        if(!INSTANCE.telemetryDisabled){
            TelemetryClient.getInstance().enableActivityTracking(application);
        }
    }

    /**
     * Enable / disable tracking of unhandled exceptions.
     *
     * @param disabled if set to true, crash reporting will be disabled
     */
    public static void setExceptionTracking(boolean disabled){
        INSTANCE.exceptionTrackingDisabled = disabled;
    }

    /**
     * Enable / disable tracking of telemetry data.
     *
     * @param disabled if set to true, the telemetry feature will be disabled
     */
    public static void setTelemetryDisabled(boolean disabled){
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
     * @param commonProperties a dictionary of properties to enqueue with all telemetry.
     */
    public static void setCommonProperties(Map<String, String> commonProperties) {
        if(!isRunning) {
            INSTANCE.commonProperties = commonProperties;
        }
    }

    /**
     * Gets the session configuration for the instance
     * @return the instance session configuration
     */
    public static SessionConfig getConfig() {
        return INSTANCE.config;
    }

    /**
     * Sets the session configuration for the instance
     */
    public void setConfig(SessionConfig config) {
        INSTANCE.config = config;
    }

    public static void setDeveloperMode(boolean developerMode) {
        DEVELOPER_MODE = developerMode;
    }

    public static boolean isDeveloperMode() {
        return DEVELOPER_MODE;
    }
}