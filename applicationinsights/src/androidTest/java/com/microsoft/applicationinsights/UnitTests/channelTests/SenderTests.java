package com.microsoft.applicationinsights.UnitTests.channelTests;

import android.test.AndroidTestCase;

import com.microsoft.applicationinsights.Framework.SenderWrapper;
import com.microsoft.applicationinsights.Framework.TestJson;
import com.microsoft.applicationinsights.channel.Sender;

import junit.framework.Assert;
import java.util.concurrent.CountDownLatch;

public class SenderTests extends AndroidTestCase {

    private CountDownLatch signal;
    private SenderWrapper senderWrapper;
    private Sender sender;

    public void setUp() throws Exception {
        super.setUp();
        this.signal = new CountDownLatch(1);
        senderWrapper = new SenderWrapper(this.signal);
        sender = SenderWrapper.instance;
        sender.maxBatchIntervalMs = 1000;
        sender.maxBatchCount = 5;
        sender.DisableTelemetry=false;
        sender.endpointUrl="https://testing";
    }

    public void tearDown() throws Exception {
        Assert.assertTrue(true);
    }

    public void queueOneItem()
    {
        TestJson testJson = new TestJson("hello", "world");

        sender.queue(testJson);
        Assert.assertEquals("Got 1 item", 1, senderWrapper.getQueue().size());
    }

    public void queueUpToMaxQueueLength()
    {
        TestJson testJson = new TestJson("hello", "world");

        for (int i = 0; i < 5; i++) {
            sender.queue(testJson);
            Assert.assertEquals(String.format("Got {0} items", i+1), i+1, senderWrapper.getQueue().size());
        }
        Assert.assertEquals("Items were flushed", 0, senderWrapper.getQueue().size());
        Assert.fail(); ///Not sure why this test is passing

    }


}
