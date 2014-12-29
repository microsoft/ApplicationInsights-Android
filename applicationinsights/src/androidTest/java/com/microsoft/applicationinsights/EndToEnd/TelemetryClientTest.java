package com.microsoft.applicationinsights.EndToEnd;

import android.content.Context;
import android.test.AndroidTestCase;
import android.util.Log;

import com.microsoft.applicationinsights.Framework.TelemetryClientWrapper;
import com.microsoft.applicationinsights.Framework.SenderWrapper;

import junit.framework.Assert;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class TelemetryClientTest extends AndroidTestCase {

    private TelemetryClientWrapper tc;
    private CountDownLatch signal;
    private SenderWrapper sender;

    private final int batchInterval = 1000;
    private final int batchMargin = 250;

    public void setUp() throws Exception {
        super.setUp();
        String iKey = "2b240a15-4b1c-4c40-a4f0-0e8142116250";
        Context context = this.getContext();

        this.signal = new CountDownLatch(1);
        this.sender = new SenderWrapper(this.signal);
        this.sender.getConfig().setMaxBatchIntervalMs(batchInterval);

        this.tc = new TelemetryClientWrapper(iKey, context, sender);
    }

    public void tearDown() throws Exception {

    }

    public void testBatchingLimit() {
        this.sender.useFakeWriter = true;
        this.sender.getConfig().setMaxBatchCount(3);
        this.tc.trackEvent("batchEvent1");

        // enqueue one item and verify that it did not trigger a send
        try {
            this.signal.await(batchMargin, TimeUnit.MILLISECONDS);
            Assert.assertEquals("batch was not sent before MaxIntervalMs",
                    1, this.signal.getCount());
        } catch (InterruptedException e) {
            Assert.fail("Failed to validate API\n\n" + e.toString());
        }

        // enqueue two items (to reach maxBatchCount) and verify that data was flushed
        this.tc.trackEvent("batchEvent2");
        this.tc.trackEvent("batchEvent3");
        try {
            this.signal.await(batchMargin, TimeUnit.MILLISECONDS);
            Assert.assertEquals("batch was sent before maxIntervalMs after reaching MaxBatchCount",
                    0, this.signal.getCount());
        } catch (InterruptedException e) {
            Assert.fail("Failed to validate API\n\n" + e.toString());
        }
    }

    public void testBatchingLimitExceed() {
        this.sender.useFakeWriter = true;
        this.sender.getConfig().setMaxBatchCount(3);

        // send 4 items (exceeding maxBatchCount is supported) and verify that data was flushed
        this.signal = new CountDownLatch(1);
        this.sender.signal = signal;
        this.tc.trackEvent("batchEvent4");
        this.tc.trackEvent("batchEvent5");
        this.tc.trackEvent("batchEvent6");
        this.tc.trackEvent("batchEvent7");
        try {
            this.signal.await(batchMargin, TimeUnit.MILLISECONDS);
            Assert.assertEquals("second batch was sent before maxIntervalMs after reaching MaxBatchCount",
                    0, this.signal.getCount());
        } catch (InterruptedException e) {
            Assert.fail("Failed to validate API\n\n" + e.toString());
        }
    }

    public void testBatchingTimer() {
        this.sender.useFakeWriter = true;
        this.sender.getConfig().setMaxBatchCount(3);

        // send one item and wait for the queue to flush via the timer
        this.signal = new CountDownLatch(1);
        this.sender.signal = signal;
        this.tc.trackEvent("batchEvent8");
        try {
            this.signal.await(batchMargin + this.sender.getConfig().getMaxBatchIntervalMs(), TimeUnit.MILLISECONDS);
            Assert.assertEquals("single item was sent after reaching MaxInterval",
                    0, this.signal.getCount());
        } catch (InterruptedException e) {
            Assert.fail("Failed to validate API\n\n" + e.toString());
        }
    }

    public void testBatchingFlush() {
        this.sender.useFakeWriter = true;
        this.sender.getConfig().setMaxBatchCount(3);

        // send one item and flush it to bypass the timer
        this.signal = new CountDownLatch(1);
        this.sender.signal = signal;
        this.tc.trackEvent("batchEvent9");
        try {

            this.signal.await(batchMargin, TimeUnit.MILLISECONDS);
            Assert.assertEquals("single item was not sent before reaching MaxInterval",
                    1, this.signal.getCount());

            this.sender.flush();
            this.signal.await(batchMargin, TimeUnit.MILLISECONDS);
            Assert.assertEquals("single item was sent after calling sender.flush",
                    0, this.signal.getCount());
        } catch (InterruptedException e) {
            Assert.fail("Failed to validate API\n\n" + e.toString());
        }
    }

    public void testTrackEvent() {
        Log.w("TelemetryClentTest.testTrackEvent", "Starting test");
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

    public void testTrackTraceSerialization() {
        this.sender.useFakeWriter = true;
        this.tc.trackTrace("trace");
        this.validatePayload();
    }

    public void testTrackMetric() throws Exception {
        this.tc.trackMetric("metric", 0.0);
        this.validateApi();
    }

    public void testTrackMetricSerialization() throws Exception {
        this.sender.useFakeWriter = true;
        this.tc.trackMetric("metric", 0.0);
        this.validatePayload();
    }

    public void testTrackException() throws Exception {
        //this.tc.trackException();
    }

    public void testTrackExceptionSerialization() throws Exception {
        //this.tc.trackException();
    }

    public void testTrackPageView() throws Exception {
        this.tc.trackPageView("page");
        this.validateApi();
    }

    public void testTrackPageViewSerialization() throws Exception {
        this.sender.useFakeWriter = true;
        this.tc.trackPageView("page");
        this.validatePayload();
    }

    public static void validateResponse(int responseCode, String responsePayload) {
        Log.i("SenderWrapper.onResponse", "Response code is: " + responseCode);
        prettyPrintPayload(responsePayload);
        Assert.assertEquals("Expected response code", 200, responseCode);
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
            String payload = this.sender.writer.toString();
            Log.i("TelemetryClientTest.validatePayload", payload);
            prettyPrintPayload(payload);
            Assert.assertTrue("Payload length is greater than zero", payload.length() > 0);
        } catch (InterruptedException e) {
            Assert.fail("Failed to validate API\n\n" + e.toString());
        }
    }

    private static void prettyPrintPayload(String payload) {
        if(payload == null)
            return;

        char[] chars = payload.toCharArray();
        StringBuilder sb = new StringBuilder();
        String tabs = "";

        // logcat doesn't like leading spaces, so add '|' to the start of each line
        String logCatNewLine = "\n|";
        sb.append(logCatNewLine);
        for (char c : chars) {
            switch (c) {
                case '[':
                case '{':
                    tabs += "\t";
                    sb.append(" " + c + logCatNewLine + tabs);
                    break;
                case ']':
                case '}':
                    tabs = tabs.substring(0, tabs.length() - 1);
                    sb.append(logCatNewLine + tabs + c);
                    break;
                case ',':
                    sb.append(c + logCatNewLine + tabs);
                    break;
                default:
                    sb.append(c);
            }
        }

        String result = sb.toString();
        result.replaceAll("\t", "  ");

        Log.i("TelemetryClientTest.prettyPrintPayload", result);
    }
}