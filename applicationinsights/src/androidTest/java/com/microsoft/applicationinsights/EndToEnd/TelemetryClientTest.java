package com.microsoft.applicationinsights.EndToEnd;

import android.content.Context;
import android.test.AndroidTestCase;
import android.util.Log;

import com.microsoft.applicationinsights.Framework.SenderWrapper;
import com.microsoft.applicationinsights.Framework.TelemetryClientWrapper;
import com.microsoft.applicationinsights.channel.SenderConfig;

import junit.framework.Assert;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class TelemetryClientTest extends AndroidTestCase {

    private TelemetryClientWrapper tc;
    private CountDownLatch signal;
    private SenderWrapper sender;

    public void setUp() throws Exception {
        super.setUp();
        String iKey = "2b240a15-4b1c-4c40-a4f0-0e8142116250";
        Context context = this.getContext();
        SenderConfig.maxBatchIntervalMs = 1;

        this.signal = new CountDownLatch(1);
        this.sender = new SenderWrapper(this.signal, 200);

        this.tc = new TelemetryClientWrapper(iKey, context, sender);
    }

    public void tearDown() throws Exception {
        Assert.assertTrue(true);
    }

    public void testTrackEvent() {
        Log.w("TelemetryClientTest.testTrackEvent", "Starting test");
        this.tc.trackEvent("event");
        this.validateApi();
    }

    public void testTrackEventSerialization() {
        this.sender.useFakeWriter = true;
        this.tc.trackEvent("event");
        this.validatePayload();
    }

    public void testTrackTrace() {
        this.tc.trackTrace("trace");
        this.validateApi();
    }

    public void testTrackMetric() throws Exception {
//        this.tc.trackMetric("metric", 0.0);
//        this.validateApi();
    }

    public void testTrackException() throws Exception {
        //this.tc.trackException();
    }

    public void testTrackPageView() throws Exception {
        this.tc.trackPageView("page");
        this.validateApi();
    }

    private void validateApi() {
        try {
            this.signal.await(10, TimeUnit.SECONDS);
            Assert.assertEquals("Response was received", 0, this.signal.getCount());
        } catch (InterruptedException e) {
            Assert.fail("Failed to validate API\n\n" + e.toString());
        }
    }

    private void validatePayload() {
        try {
            this.signal.await(10, TimeUnit.SECONDS);
            String payload = this.sender.writer.getBuffer().toString();
            Assert.assertEquals("", payload);
        } catch (InterruptedException e) {
            Assert.fail("Failed to validate API\n\n" + e.toString());
        }
    }
}