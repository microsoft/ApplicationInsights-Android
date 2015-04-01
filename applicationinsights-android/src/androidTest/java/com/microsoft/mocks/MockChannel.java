package com.microsoft.mocks;

import android.content.Context;

import com.microsoft.applicationinsights.TelemetryClientConfig;
import com.microsoft.applicationinsights.internal.Channel;
import com.microsoft.applicationinsights.internal.TelemetryQueue;

public class MockChannel extends Channel {
    public MockChannel(TelemetryClientConfig config, Context context) {
        super(config, context);
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