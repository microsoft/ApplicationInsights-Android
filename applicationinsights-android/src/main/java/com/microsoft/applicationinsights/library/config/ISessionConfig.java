package com.microsoft.applicationinsights.library.config;

/**
 * Created by christophwendt on 21/04/15.
 */
public interface ISessionConfig {

    /**
     * Gets the interval at which sessions are renewed
     */
    public long getSessionIntervalMs();

    /**
     * Sets the interval at which sessions are renewed
     */
    public void setSessionIntervalMs(long sessionIntervalMs);
}
