package com.microsoft.applicationinsights.library;

import com.microsoft.applicationinsights.library.config.IQueueConfig;

public class PublicChannelQueue extends ChannelQueue {
    protected PublicChannelQueue(IQueueConfig config) {
        super(config);
    }
}
