package com.microsoft.applicationinsights.library;

public class MockChannel extends Channel {
    public MockChannel() {
        super();
    }

    @Override
    public void setQueue(ChannelQueue queue) {
        super.setQueue(queue);
    }

    public static MockChannel getInstance() {
        return (MockChannel)Channel.getInstance();
    }
}