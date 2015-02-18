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
     * Synchronization lock for setting the iKey
     */
    private static final Object lock = new Object();

    /**
     * The instrumentationKey from AndroidManifest.xml
     */
    private static String iKeyFromManifest = null;

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
     * Gets the static instrumentation key from AndroidManifest.xml if it is available
     * @param context the application context to check the manifest from
     * @return the instrumentation key for the application or empty string if not available
     */
    private static String getInstrumentationKey(Context context) {
        // bypass the sync block if the key is set
        if(TelemetryChannelConfig.iKeyFromManifest == null) {
            synchronized (TelemetryChannelConfig.lock) {
                // re-check if it is set after taking the lock
                if(TelemetryChannelConfig.iKeyFromManifest == null) {
                    String iKey = TelemetryChannelConfig.readInstrumentationKey(context);
                    TelemetryChannelConfig.iKeyFromManifest = iKey;
                }
            }
        }

        return TelemetryChannelConfig.iKeyFromManifest;
    }

    /**
     * Reads the instrumentation key from AndroidManifest.xml if it is available
     * @param context the application context to check the manifest from
     * @return the instrumentation key configured for the application
     */
    private static String readInstrumentationKey(Context context) {
        String iKey = "";
        try {
            iKey = context
                    .getPackageManager()
                    .getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA)
                    .metaData
                    .getString("com.microsoft.applicationinsights.instrumentationKey");
        } catch (PackageManager.NameNotFoundException exception) {
            InternalLogging._warn("TelemetryClient",
                    "set instrumentation key in AndroidManifest.xml");
        }

        return iKey;
    }
}
