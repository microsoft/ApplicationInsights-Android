package com.microsoft.applicationinsights.channel;

import com.microsoft.applicationinsights.channel.contracts.Envelope;
import com.microsoft.applicationinsights.channel.contracts.shared.IJsonSerializable;

import junit.framework.Assert;
import junit.framework.TestCase;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.util.LinkedList;
import java.util.Timer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class SenderTest extends TestCase {

    private TestSender sender;
    private IJsonSerializable item;

    private final int batchMargin = 25;

    public void setUp() throws Exception {
        super.setUp();
        this.sender = new TestSender();
        int batchInterval = 100;
        this.sender.getConfig().setMaxBatchIntervalMs(batchInterval);
        this.item = new Envelope();
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

    public void testBatchingLimit() {
        this.sender.getConfig().setMaxBatchCount(3);
        this.sender.enqueue(this.item);

        // enqueue one item and verify that it did not trigger a send
        try {
            this.sender.sendSignal.await(batchMargin, TimeUnit.MILLISECONDS);
            Assert.assertEquals("batch was not sent before MaxIntervalMs",
                    1, this.sender.sendSignal.getCount());
            Assert.assertNotSame("queue is not empty prior to sending data",
                    this.sender.getQueue().size(), 0);
        } catch (InterruptedException e) {
            Assert.fail("Failed to validate API\n\n" + e.toString());
        }

        // enqueue two items (to reach maxBatchCount) and verify that data was flushed
        this.sender.enqueue(this.item);
        this.sender.enqueue(this.item);
        try {
            this.sender.sendSignal.await(batchMargin, TimeUnit.MILLISECONDS);
            Assert.assertEquals("batch was sent before maxIntervalMs after reaching MaxBatchCount",
                    0, this.sender.sendSignal.getCount());
            Assert.assertEquals("queue is empty after sending data",
                    this.sender.getQueue().size(), 0);
        } catch (InterruptedException e) {
            Assert.fail("Failed to validate API\n\n" + e.toString());
        }
    }

    public void testBatchingLimitExceed() {
        this.sender.getConfig().setMaxBatchCount(3);

        // send 4 items (exceeding maxBatchCount is supported) and verify that data was flushed
        this.sender.enqueue(this.item);
        this.sender.enqueue(this.item);
        this.sender.enqueue(this.item);
        this.sender.enqueue(this.item);

        try {
            this.sender.sendSignal.await(batchMargin, TimeUnit.MILLISECONDS);
            Assert.assertEquals("second batch was sent before maxIntervalMs after reaching MaxBatchCount",
                    0, this.sender.sendSignal.getCount());
            Assert.assertEquals("queue is empty after sending data",
                    this.sender.getQueue().size(), 0);
        } catch (InterruptedException e) {
            Assert.fail("Failed to validate API\n\n" + e.toString());
        }
    }

    public void testBatchingTimer() {
        this.sender.getConfig().setMaxBatchCount(3);

        // send one item and wait for the queue to flush via the timer
        this.sender.enqueue(this.item);
        try {
            this.sender.sendSignal.await(batchMargin + this.sender.getConfig().getMaxBatchIntervalMs() + 1, TimeUnit.MILLISECONDS);
            Assert.assertEquals("single item was sent after reaching MaxInterval",
                    0, this.sender.sendSignal.getCount());
            Assert.assertEquals("queue is empty after sending data",
                    this.sender.getQueue().size(), 0);
        } catch (InterruptedException e) {
            Assert.fail("Failed to validate API\n\n" + e.toString());
        }
    }

    public void testBatchingFlush() {
        this.sender.getConfig().setMaxBatchCount(3);

        // send one item and flush it to bypass the timer
        this.sender.enqueue(this.item);
        try {

            this.sender.sendSignal.await(batchMargin, TimeUnit.MILLISECONDS);
            Assert.assertEquals("single item was not sent before reaching MaxInterval",
                    1, this.sender.sendSignal.getCount());
            Assert.assertNotSame("queue is not empty prior to sending data",
                    this.sender.getQueue().size(), 0);

            this.sender.flush();
            this.sender.sendSignal.await(batchMargin, TimeUnit.MILLISECONDS);
            Assert.assertEquals("single item was sent after calling sender.flush",
                    0, this.sender.sendSignal.getCount());
            Assert.assertEquals("queue is empty after sending data",
                    this.sender.getQueue().size(), 0);
        } catch (InterruptedException e) {
            Assert.fail("Failed to validate API\n\n" + e.toString());
        }
    }

    public void testDisableTelemetry() {
        this.sender.getConfig().setTelemetryDisabled(true);

        this.sender.enqueue(this.item);
        long queueSize = this.sender.getQueue().size();
        Assert.assertEquals("item is not queued when telemetry is disabled", 0, queueSize);

        this.sender.getConfig().setTelemetryDisabled(false);

        this.sender.enqueue(this.item);
        queueSize = this.sender.getQueue().size();
        Assert.assertEquals("item is queued when telemetry is enabled", 1, queueSize);
    }

    private class TestSender extends Sender {

        public CountDownLatch sendSignal;
        public CountDownLatch responseSignal;
        public StringWriter writer;

        public TestSender() {
            super();
            this.sendSignal = new CountDownLatch(1);
            this.responseSignal = new CountDownLatch(1);
        }

        public LinkedList<IJsonSerializable> getQueue() {
            return this.queue;
        }

        public Timer getTimer() {
            return this.timer;
        }

        @Override
        protected void send(IJsonSerializable[] data) {
            this.sendSignal.countDown();
            super.send(data);
        }

        @Override
        protected String onResponse(HttpURLConnection connection, int responseCode) {
            this.responseSignal.countDown();
            return null;
        }

        @Override
        protected Writer getWriter(HttpURLConnection connection) throws IOException {
            this.writer = new StringWriter();
            return this.writer;
        }
    }
}