package com.microsoft.applicationinsights.channel;

import com.microsoft.applicationinsights.channel.contracts.Envelope;
import com.microsoft.applicationinsights.channel.contracts.shared.IJsonSerializable;

import junit.framework.Assert;
import junit.framework.TestCase;

import java.util.LinkedList;
import java.util.Timer;
import java.util.concurrent.LinkedBlockingQueue;

public class SenderTest extends TestCase {

    TestSender sender;

    public void setUp() throws Exception {
        super.setUp();
        this.sender = new TestSender();
    }

    public void tearDown() throws Exception {

    }

    public void testGetConfig() throws Exception {
        Assert.assertNotNull("Sender constructor should initialize config", this.sender.getConfig());
    }

    public void testQueue() throws Exception {
        Envelope env = new Envelope();
        this.sender.enqueue(env);
        this.sender.getTimer().cancel();
        IJsonSerializable env2 = this.sender.getQueue().peek();
        Assert.assertTrue("item was successfully queued", env == env2);
    }

    public void testFlush() throws Exception {

    }

    public void testGetSenderTask() throws Exception {

    }

    public void testSend() throws Exception {

    }

    public void testOnResponse() throws Exception {

    }

    private class TestSender extends Sender {
        public TestSender() {
            super();
        }

        public LinkedList<IJsonSerializable> getQueue() {
            return this.queue;
        }

        public Timer getTimer() {
            return this.timer;
        }
    }
}