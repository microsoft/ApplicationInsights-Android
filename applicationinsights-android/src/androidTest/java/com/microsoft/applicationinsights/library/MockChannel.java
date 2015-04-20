package com.microsoft.applicationinsights.library;

public class MockChannel extends Channel {
    public MockChannel() {
        super();
    }

    @Override
    public void setQueue(ChannelQueue queue) {
        super.setQueue(queue);
    }

    @Override
    public MockQueue getQueue() {
        return (MockQueue)super.getQueue();
    }

    public static MockChannel getInstance() {
        return (MockChannel)Channel.getInstance();
    }
}