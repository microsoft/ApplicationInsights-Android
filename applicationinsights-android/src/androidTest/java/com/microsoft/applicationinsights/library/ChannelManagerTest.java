package com.microsoft.applicationinsights.library;

import junit.framework.Assert;
import junit.framework.TestCase;

public class ChannelManagerTest extends TestCase {

    public void testGetSetDefaultChannelManager() {
        ChannelManager.initialize(ChannelType.Default);
        ChannelManager channelManager = ChannelManager.getInstance();
        Assert.assertNotNull(channelManager);
    }

    public void testGetSetTelemetryClientChannelManager() {
        ChannelManager.initialize(ChannelType.CommonLoggingLibraryChannel);
        ChannelManager channelManager = ChannelManager.getInstance();
        Assert.assertNotNull(channelManager);
    }

    public void testNullChannelType() {
        ChannelManager.initialize(null);
        ChannelManager channelManager = ChannelManager.getInstance();

        // If null is passed in we revert to using the default channel instead of throwing an exception
        Assert.assertNotNull(channelManager);
    }

    public void testUninitializedChannelManager() {
        ChannelManager.getInstance().reset();
        ChannelManager channelManager = ChannelManager.getInstance();
        Assert.assertNull(channelManager);
    }

    public void testReinitializeChannelManager() {
        ChannelManager.initialize(ChannelType.Default);
        ChannelManager defaultChannelManager = ChannelManager.getInstance();
        Assert.assertNotNull(defaultChannelManager);

        ChannelManager.initialize(ChannelType.CommonLoggingLibraryChannel);
        ChannelManager telemetryClientChannelManager = ChannelManager.getInstance();
        Assert.assertNotNull(defaultChannelManager);

        // You can only initialize once so the object returned should be the same.
        Assert.assertSame(defaultChannelManager, telemetryClientChannelManager);
    }
}
