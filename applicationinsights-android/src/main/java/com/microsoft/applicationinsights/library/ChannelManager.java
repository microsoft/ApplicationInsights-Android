package com.microsoft.applicationinsights.library;

import com.microsoft.applicationinsights.logging.InternalLogging;

import com.microsoft.cll.android.AndroidCll;

import com.microsoft.telemetry.IChannel;

/**
 * A single class that manages the different types of channels we support
 */
public class ChannelManager {
    private static final String TAG = "ChannelManager";

    /**
     * Volatile boolean for double checked synchronize block
     */
    private static volatile boolean isInitialized = false;

    /**
     * Synchronization LOCK for setting static context
     */
    private static final Object LOCK = new Object();

    /**
     * The singleton INSTANCE of this class
     */
    private static ChannelManager instance;

    /**
     * A Singleton instance of an IChannel set by default to Channel but can
     * be override using setChannel
     */
    private IChannel channel;
    /**
     * Instantiates a new INSTANCE of ChannelManager
     */

    protected ChannelManager(ChannelType channelType) {
        Channel.initialize(ApplicationInsights.getConfiguration());
        setChannel(channelType);
    }

    /**
     * Initializes the ChannelManager to it's default IChannel instance
     */
    public static void initialize(ChannelType channelType) {
        if (!isInitialized) {
            synchronized (ChannelManager.LOCK) {
                if (!isInitialized) {
                    isInitialized = true;
                    instance = new ChannelManager(channelType);
                }
            }
        }
    }

    /**
     * @return the INSTANCE of ChannelManager or null if not yet initialized
     */
    protected static ChannelManager getInstance() {
        if (ChannelManager.instance == null) {
            InternalLogging.error(TAG, "getSharedInstance was called before initialization");
        }

        return ChannelManager.instance;
    }

    /**
     * Returns the current channel
     * @return The channel that is currently in use
     */
    protected IChannel getChannel() {
        return channel;
    }

    /**
     * Sets the current channel to use
     * @param channelType The new channel to use
     */
    protected void setChannel(ChannelType channelType) {
        if(channelType == null) {
            InternalLogging.warn(TAG, "ChannelType is null, setting up using default channel type");
            this.channel = createDefaultChannel();
            return;
        }

        switch (channelType) {
            case Default:
                this.channel = createDefaultChannel();
                break;
            case CommonLoggingLibraryChannel:
                this.channel = createTelemetryClientChannel();
                break;
        }
    }

    /**
     * Resets this instance of the channel manager
     */
    protected void reset() {
        if(channel != null) {
            channel = null;
        }

        if(instance != null) {
            instance = null;
        }

        isInitialized = false;
    }

    /**
     * Creates a channel of default type
     * @return The new default channel
     */
    private IChannel createDefaultChannel() {
        IChannel defaultChannel = Channel.getInstance();
        if(defaultChannel == null) {
            Channel.initialize(ApplicationInsights.getConfiguration());
            defaultChannel = Channel.getInstance();
        }

        return defaultChannel;
    }

    /**
     * Creates a channel of the Telemetry Client type
     * @return The new Telemetry Client channel
     */
    private IChannel createTelemetryClientChannel() {
        String iKey = ApplicationInsights.getInstrumentationKey() == null ? "" : ApplicationInsights.getInstrumentationKey();
        AndroidCll cll = (AndroidCll)AndroidCll.initialize(iKey, ApplicationInsights.INSTANCE.getContext(), ApplicationInsights.getConfiguration().getEndpointUrl());
        cll.useLagacyCS(true);
        return cll;
    }
}
