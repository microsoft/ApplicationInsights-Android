package com.microsoft.commonlogging.channel;

import android.app.Activity;
import android.test.AndroidTestCase;

import com.microsoft.commonlogging.channel.contracts.Envelope;
import com.microsoft.commonlogging.channel.contracts.shared.IJsonSerializable;
import com.microsoft.commonlogging.channel.contracts.shared.ITelemetry;

import junit.framework.Assert;
import junit.framework.TestCase;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedList;
import java.util.Timer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class TelemetryQueueTest extends TestCase {

    private TestQueue queue;
    private IJsonSerializable item;

    private final int batchMargin = 25;

    public void setUp() throws Exception {
        super.setUp();
        this.queue = new TestQueue();
        int batchInterval = 100;
        this.queue.getConfig().setMaxBatchIntervalMs(batchInterval);
        this.item = new Envelope();
    }

    public void tearDown() throws Exception {

    }

    public void testGetConfig() throws Exception {
        Assert.assertNotNull("Sender constructor should initialize config", this.queue.getConfig());
    }

    public void testQueue() throws Exception {
        Envelope env = new Envelope();
        this.queue.enqueue(env);
        this.queue.getTimer().cancel();
        IJsonSerializable env2 = this.queue.getQueue().peek();
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
        this.queue.getConfig().setMaxBatchCount(3);
        this.queue.enqueue(this.item);

        // enqueue one item and verify that it did not trigger a send
        try {
            this.queue.sendSignal.await(batchMargin, TimeUnit.MILLISECONDS);
            Assert.assertEquals("batch was not sent before MaxIntervalMs",
                    1, this.queue.sendSignal.getCount());
            Assert.assertNotSame("queue is not empty prior to sending data",
                    this.queue.getQueue().size(), 0);
        } catch (InterruptedException e) {
            Assert.fail("Failed to validate API\n\n" + e.toString());
        }

        // enqueue two items (to reach maxBatchCount) and verify that data was flushed
        this.queue.enqueue(this.item);
        this.queue.enqueue(this.item);
        try {
            this.queue.sendSignal.await(batchMargin, TimeUnit.MILLISECONDS);
            Assert.assertEquals("batch was sent before maxIntervalMs after reaching MaxBatchCount",
                    0, this.queue.sendSignal.getCount());
            Assert.assertEquals("queue is empty after sending data",
                    this.queue.getQueue().size(), 0);
        } catch (InterruptedException e) {
            Assert.fail("Failed to validate API\n\n" + e.toString());
        }
    }

    public void testBatchingLimitExceed() {
        this.queue.getConfig().setMaxBatchCount(3);

        // send 4 items (exceeding maxBatchCount is supported) and verify that data was flushed
        this.queue.enqueue(this.item);
        this.queue.enqueue(this.item);
        this.queue.enqueue(this.item);
        this.queue.enqueue(this.item);

        try {
            this.queue.sendSignal.await(batchMargin, TimeUnit.MILLISECONDS);
            Assert.assertEquals("second batch was sent before maxIntervalMs after reaching MaxBatchCount",
                    0, this.queue.sendSignal.getCount());
            Assert.assertEquals("queue is empty after sending data",
                    this.queue.getQueue().size(), 0);
        } catch (InterruptedException e) {
            Assert.fail("Failed to validate API\n\n" + e.toString());
        }
    }

    public void testBatchingTimer() {
        this.queue.getConfig().setMaxBatchCount(3);

        // send one item and wait for the queue to flush via the timer
        this.queue.enqueue(this.item);
        try {
            this.queue.sendSignal.await(batchMargin + this.queue.getConfig().getMaxBatchIntervalMs() + 1, TimeUnit.MILLISECONDS);
            Assert.assertEquals("single item was sent after reaching MaxInterval",
                    0, this.queue.sendSignal.getCount());
            Assert.assertEquals("queue is empty after sending data",
                    this.queue.getQueue().size(), 0);
        } catch (InterruptedException e) {
            Assert.fail("Failed to validate API\n\n" + e.toString());
        }
    }

    public void testBatchingFlush() {
        this.queue.getConfig().setMaxBatchCount(3);

        // send one item and flush it to bypass the timer
        this.queue.enqueue(this.item);
        try {

            this.queue.sendSignal.await(batchMargin, TimeUnit.MILLISECONDS);
            Assert.assertEquals("single item was not sent before reaching MaxInterval",
                    1, this.queue.sendSignal.getCount());
            Assert.assertNotSame("queue is not empty prior to sending data",
                    this.queue.getQueue().size(), 0);

            this.queue.flush();
            this.queue.sendSignal.await(batchMargin, TimeUnit.MILLISECONDS);
            Assert.assertEquals("single item was sent after calling sender.flush",
                    0, this.queue.sendSignal.getCount());
            Assert.assertEquals("queue is empty after sending data",
                    this.queue.getQueue().size(), 0);
        } catch (InterruptedException e) {
            Assert.fail("Failed to validate API\n\n" + e.toString());
        }
    }

    public void testDisableTelemetry() {
        this.queue.sender.getConfig().setTelemetryDisabled(true);

        this.queue.enqueue(this.item);
        long queueSize = this.queue.getQueue().size();
        Assert.assertEquals("item is not queued when telemetry is disabled", 0, queueSize);

        this.queue.sender.getConfig().setTelemetryDisabled(false);

        this.queue.enqueue(this.item);
        queueSize = this.queue.getQueue().size();
        Assert.assertEquals("item is queued when telemetry is enabled", 1, queueSize);
    }

    private class TestQueue extends TelemetryQueue {

        public CountDownLatch sendSignal;
        public CountDownLatch responseSignal;
        public StringWriter writer;

        public TestQueue() {
            super();
            this.sendSignal = new CountDownLatch(1);
            this.responseSignal = new CountDownLatch(1);
            this.sender = new TestSender(sendSignal);
        }

        public LinkedList<IJsonSerializable> getQueue() {
            return this.linkedList;
        }

        public Timer getTimer() {
            return this.timer;
        }
    }

    private class TestSender extends Sender {
        public CountDownLatch sendSignal;

        public TestSender(CountDownLatch sendSignal) {
            super();
            this.sendSignal = sendSignal;
        }

        @Override
        protected void send(IJsonSerializable[] data) {
            this.sendSignal.countDown();
            super.send(data);
        }
    }
}

