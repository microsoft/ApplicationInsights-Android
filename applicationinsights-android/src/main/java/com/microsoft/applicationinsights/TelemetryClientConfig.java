package com.microsoft.applicationinsights;

import android.app.Activity;

import com.microsoft.commonlogging.channel.TelemetryChannelConfig;

/**
 * Configuration object when instantiating TelemetryClient
 */
public class TelemetryClientConfig extends TelemetryChannelConfig {

    /**
     * Constructs a new instance of TelemetryClientConfig
     *
     * @param activity The android app context
     */
    public TelemetryClientConfig(Activity activity) {
        super(activity);
    }
}
