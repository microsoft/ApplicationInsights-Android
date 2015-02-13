package com.microsoft.mocks;

import com.microsoft.applicationinsights.TelemetryClientConfig;
import com.microsoft.commonlogging.channel.TelemetryChannel;
import com.microsoft.commonlogging.channel.TelemetryQueue;

public class MockChannel extends TelemetryChannel {
    public MockChannel(TelemetryClientConfig config) {
        super(config);
    }

    @Override
    public void setQueue(TelemetryQueue queue) {
        super.setQueue(queue);
    }

    @Override
    public MockQueue getQueue() {
        return (MockQueue)super.getQueue();
    }
}