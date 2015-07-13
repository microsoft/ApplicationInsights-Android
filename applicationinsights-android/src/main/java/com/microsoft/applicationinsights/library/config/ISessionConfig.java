package com.microsoft.applicationinsights.library.config;

public interface ISessionConfig {

    /**
     * Gets the interval at which sessions are renewed
     */
    long getSessionIntervalMs();

    /**
     * Sets the interval at which sessions are renewed
     */
    void setSessionIntervalMs(long sessionIntervalMs);
}
