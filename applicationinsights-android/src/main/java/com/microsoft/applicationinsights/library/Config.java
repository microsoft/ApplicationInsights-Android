package com.microsoft.applicationinsights.library;

abstract class Config {

    /**
     * Lock object to ensure thread safety of the configuration
     */
    protected final Object lock;

    /**
     * Constructs a new INSTANCE of a config
     */
    public Config() {
        // TODO: Create several configs for queue and sender
        this.lock = new Object();
    }
}
