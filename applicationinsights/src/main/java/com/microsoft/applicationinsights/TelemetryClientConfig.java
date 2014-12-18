package com.microsoft.applicationinsights;

import android.content.Context;

/**
 * Configuration object when instantiating TelemetryClient
 */
public class TelemetryClientConfig extends CoreTelemetryClientConfig {


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
     * @param context The android app context
     */
    public TelemetryClientConfig(String iKey, Context context){
        super(iKey);
        this.appContext = context;
    }
}
