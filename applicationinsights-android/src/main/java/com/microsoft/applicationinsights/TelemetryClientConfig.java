package com.microsoft.applicationinsights;

import android.app.Activity;
import android.content.Context;

import com.microsoft.applicationinsights.channel.Sender;
import com.microsoft.applicationinsights.channel.SenderConfig;

/**
 * Configuration object when instantiating TelemetryClient
 */
public class TelemetryClientConfig {

    /**
     * The instrumentation key for this telemetryContext
     */
    private String instrumentationKey;

    /**
     * Gets the instrumentation key for this telemetryContext
     */
    public String getInstrumentationKey() {
        return instrumentationKey;
    }

    /**
     * Sets the instrumentation key for this telemetryContext
     */
    public void setInstrumentationKey(String instrumentationKey) {
        this.instrumentationKey = instrumentationKey;
    }

    /**
     * Gets the sender instance configuration for this channel.
     */
    public SenderConfig getGlobalSenderConfig() {
        return Sender.instance.getConfig();
    }
    /**
     * The application telemetryContext for this recorder
     */
    private final Context appContext;

    /**
     * The application telemetryContext for this recorder
     */
    public Context getAppContext() {
        return appContext;
    }

    /**
     * Constructs a new instance of TelemetryClientConfig
     * @param iKey The instrumentation key for this app
     * @param activity The android activity context
     */
    public TelemetryClientConfig(String iKey, Activity activity){
        this(iKey, activity.getApplicationContext());
    }

    /**
     * Constructs a new instance of TelemetryClientConfig
     * @param iKey The instrumentation key for this app
     * @param context The android app context
     */
    public TelemetryClientConfig(String iKey, Context context) {
        this.instrumentationKey = iKey;
        this.appContext = context;
    }
}
