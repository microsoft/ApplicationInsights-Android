package com.microsoft.mocks;

import com.microsoft.commonlogging.channel.TelemetryQueue;

import java.util.concurrent.CountDownLatch;

public class MockQueue extends TelemetryQueue {

    public int responseCode;
    public CountDownLatch sendSignal;
    public CountDownLatch responseSignal;
    public MockSender sender;

    public MockQueue(int expectedSendCount) {
        super();
        this.responseCode = 0;
        this.sendSignal = new CountDownLatch(expectedSendCount);
        this.responseSignal = new CountDownLatch(expectedSendCount);
        this.sender = new MockSender(sendSignal, responseSignal, this.config);
        super.sender = this.sender;
    }

    public long getQueueSize() {
        return this.linkedList.size();
    }
}