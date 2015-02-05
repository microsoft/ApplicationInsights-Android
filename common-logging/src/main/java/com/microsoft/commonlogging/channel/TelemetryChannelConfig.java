package com.microsoft.commonlogging.channel;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;

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
    public SenderConfig getGlobalSenderConfig() {
        return Sender.instance.getConfig();
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
     * @param activity The android activity context
     */
    public TelemetryChannelConfig(Activity activity){
        Context context = activity.getApplicationContext();
        this.persist = Persistence.getInstance();
        persist.setPersistenceContext(context);
        this.appContext = context;
        this.instrumentationKey = TelemetryChannelConfig.readInstrumentationKey(activity);
    }

    /**
     * Reads the instrumentation key from application resources if it is available
     * @param activity the activity to check resources from
     * @return the instrumentation key configured for the activity
     */
    private static String readInstrumentationKey(Activity activity) {
        Resources resources = activity.getResources();
        int identifier = resources.getIdentifier("ai_instrumentationKey", "string",
                activity.getPackageName());

        String iKey = null;
        if(identifier != 0) {
            iKey = resources.getString(identifier);
        } else {
            InternalLogging._warn("TelemetryClient",
                    "set instrumentation key in res/values/application_insights.xml");
        }

        return iKey;
    }
}
