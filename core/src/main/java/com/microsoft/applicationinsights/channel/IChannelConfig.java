package com.microsoft.applicationinsights.channel;

import android.content.Context;

public interface IChannelConfig {
    /**
     * @return The instrumentation key for this telemetryContext
     */
    public String getInstrumentationKey();

    /**
     * The application telemetryContext for this channel
     */
    public Context getAppContext();
}