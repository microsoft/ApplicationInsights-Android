package com.microsoft.applicationinsights;

import android.content.Context;

import com.microsoft.applicationinsights.channel.LoggingInternal;
import com.microsoft.applicationinsights.channel.Sender;
import com.microsoft.applicationinsights.channel.SenderConfig;
import com.microsoft.applicationinsights.common.AbstractTelemetryClientConfig;

/**
 * Configuration object when instantiating TelemetryClient
 */
public class TelemetryClientConfig extends
        AbstractTelemetryClientConfig {

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

    /**
     * Assign the android internal logger
     */
    static {
        LoggingInternal logger = new LoggingInternal();
        Sender.instance.setInternalLogger(logger);
    }
}
