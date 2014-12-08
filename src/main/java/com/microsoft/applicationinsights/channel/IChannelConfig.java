package com.microsoft.applicationinsights.channel;

import android.content.Context;

public interface IChannelConfig {
    /**
     * @return The instrumentation key for this context
     */
    public String getInstrumentationKey();

    /**
     * The application context for this recorder
     */
    public Context getAppContext();
}