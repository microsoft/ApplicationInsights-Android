package com.microsoft.commonlogging.channel;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;

import java.util.Queue;

public class TelemetryChannelConfig {

    /**
     * The instrumentation key for this telemetry channel
     */
    private String instrumentationKey;

    /**
     * The application context for this telemetry channel
     */
    private final Context appContext;

    /**
     * The persisted data for this application
     */
    private final Persistence persist;

    /**
     * Gets the instrumentation key for this telemetry channel
     */
    public String getInstrumentationKey() {
        return instrumentationKey;
    }

    /**
     * Sets the instrumentation key for this telemetry channel
     */
    public void setInstrumentationKey(String instrumentationKey) {
        this.instrumentationKey = instrumentationKey;
    }

    /**
     * Gets the sender config instance for this channel.
     */
    public TelemetryQueueConfig getGlobalQueueConfig() {
        return TelemetryQueue.instance.getConfig();
    }

    /**
     * The application telemetry channel for this recorder
     */
    public Context getAppContext() {
        return appContext;
    }

    /**
     * The persisted data for the application
     */
    public Persistence getPersistence() {
        return persist;
    }

    /**
     * Constructs a new instance of TelemetryChannelConfig
     * @param context The android activity context
     */
    public TelemetryChannelConfig(Context context) {
        this.persist = Persistence.getInstance();
        persist.setPersistenceContext(context);
        this.appContext = context;
        this.instrumentationKey = TelemetryChannelConfig.readInstrumentationKey(context);
    }

    /**
     * Reads the instrumentation key from application resources if it is available
     * @param context the application context to check the manifest from
     * @return the instrumentation key configured for the activity
     */
    private static String readInstrumentationKey(Context context) {
        String iKey = "";

        try {
            Bundle bundle = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA)
                    .metaData;

            iKey = bundle.getString("com.microsoft.applicationinsights.instrumentationKey");
        } catch (PackageManager.NameNotFoundException exception) {
            InternalLogging._warn("TelemetryClient",
                    "set instrumentation key in AndroidManifest.xml");
        }

        return iKey;
    }
}
