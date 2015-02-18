package com.microsoft.applicationinsights;

import android.content.Context;

import com.microsoft.commonlogging.channel.TelemetryChannelConfig;

/**
 * Configuration object when instantiating TelemetryClient
 */
public class TelemetryClientConfig extends TelemetryChannelConfig {

    /**
     * Constructs a new instance of TelemetryClientConfig
     *
     * @param context The android app context
     */
    public TelemetryClientConfig(Context context) {
        super(context);
    }
}
