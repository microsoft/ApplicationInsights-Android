package com.microsoft.applicationinsights.internal;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import com.microsoft.applicationinsights.internal.logging.InternalLogging;

public class TelemetryChannelConfig {


    private static final String TAG = "TelemetryChannelConfig";

    /**
     * Synchronization LOCK for setting the iKey
     */
    private static final Object LOCK = new Object();

    /**
     * The instrumentationKey from AndroidManifest.xml
     */
    private static String iKeyFromManifest = null;

    /**
     * The instrumentation key for this telemetry channel
     */
    private String instrumentationKey;

    /**
     * Constructs a new INSTANCE of TelemetryChannelConfig
     *
     * @param context The android activity context
     */
    public TelemetryChannelConfig(Context context) {
        this.instrumentationKey = TelemetryChannelConfig.readInstrumentationKey(context);
    }

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
     * Gets the sender config INSTANCE for this channel.
     */
    public TelemetryQueueConfig getStaticConfig() {
        return TelemetryQueue.INSTANCE.getConfig();
    }

    /**
     * Gets the static instrumentation key from AndroidManifest.xml if it is available
     *
     * @param context the application context to check the manifest from
     * @return the instrumentation key for the application or empty string if not available
     */
    private static String getInstrumentationKey(Context context) {
        synchronized (TelemetryChannelConfig.LOCK) {
            if (TelemetryChannelConfig.iKeyFromManifest == null) {
                String iKey = TelemetryChannelConfig.readInstrumentationKey(context);
                TelemetryChannelConfig.iKeyFromManifest = iKey;
            }
        }

        return TelemetryChannelConfig.iKeyFromManifest;
    }

    /**
     * Reads the instrumentation key from AndroidManifest.xml if it is available
     *
     * @param context the application context to check the manifest from
     * @return the instrumentation key configured for the application
     */
    private static String readInstrumentationKey(Context context) {
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
