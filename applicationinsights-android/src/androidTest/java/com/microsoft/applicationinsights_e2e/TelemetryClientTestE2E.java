package com.microsoft.applicationinsights_e2e;

import android.content.Intent;
import android.test.ActivityUnitTestCase;
import android.util.Log;

import com.microsoft.mocks.MockActivity;
import com.microsoft.mocks.MockQueue;
import com.microsoft.mocks.MockTelemetryClient;

import junit.framework.Assert;

import java.io.InvalidObjectException;
import java.util.LinkedHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class TelemetryClientTestE2E extends ActivityUnitTestCase<MockActivity> {

    public TelemetryClientTestE2E() {
        super(MockActivity.class);
    }

    private MockTelemetryClient client;
    private LinkedHashMap<String, String> properties;
    private LinkedHashMap<String, Double> measurements;

    public void setUp() throws Exception {
        super.setUp();

        Intent intent = new Intent(getInstrumentation().getTargetContext(), MockActivity.class);
        this.setActivity(this.startActivity(intent, null, null));

        this.client = new MockTelemetryClient(this.getActivity());
        this.client.mockTrackMethod = false;
        this.client.getChannel().getQueue().getConfig().setMaxBatchIntervalMs(20);

        this.properties = new LinkedHashMap<>();
        this.properties.put("core property", "core value");
        this.measurements = new LinkedHashMap<>();
        this.measurements.put("core measurement", 5.5);
    }

    public void testTrackEvent() throws Exception {
        this.client.trackEvent(null);
        this.client.trackEvent("event1");
        this.client.trackEvent("event2", properties);
        this.client.trackEvent("event3", properties, measurements);
        this.validate();
    }

    public void testTrackTrace() throws Exception {
        this.client.trackTrace(null);
        this.client.trackTrace("trace1");
        this.client.trackTrace("trace2", properties);
        this.validate();
    }

    public void testTrackMetric() throws Exception {
        this.client.trackMetric(null, 0);
        this.client.trackMetric("metric1", 1.1);
        this.client.trackMetric("metric2", 3);
        this.client.trackMetric("metric3", 3.3);
        this.client.trackMetric("metric3", 4);
        this.validate();
    }

    public void testTrackException() throws Exception {
        this.client.trackException(null);
        this.client.trackException(new Exception());
        try {
            throw new InvalidObjectException("this is expected");
        } catch (InvalidObjectException exception) {
            this.client.trackException(exception);
            this.client.trackException(exception, "core handler");
            this.client.trackException(exception, "core handler1", properties);
            this.client.trackException(exception, "core handler2", properties, measurements);
        }

        this.validate();
    }

    public void testCatchCrash() throws Exception {
      this.client.catchCrash(null, null);
      this.client.catchCrash(new Exception(), null);
      try {
        throw new InvalidObjectException("this is expected");
      }
      catch (InvalidObjectException exception) {
        this.client.catchCrash(exception, null);
      }

      this.validate();
    }

    public void testTrackPageView() throws Exception {
        this.client.trackPageView("android page");
        this.client.trackPageView("android page");
        this.client.trackPageView("android page", properties);
        this.client.trackPageView("android page", properties, measurements);
        this.validate();
    }

    public void testTrackAllRequests() throws Exception {
        MockQueue queue = new MockQueue(5);
        String endpoint = queue.getConfig().getEndpointUrl();
        queue.getConfig().setEndpointUrl(endpoint.replace("https", "http"));
        this.client.getChannel().setQueue(queue);

        Exception exception;
        try {
            throw new Exception();
        } catch (Exception e) {
            exception = e;
        }

        queue.getConfig().setMaxBatchCount(10);
        for (int i = 0; i < 10; i++) {
            this.client.trackEvent("android event");
            this.client.trackTrace("android trace");
            this.client.trackMetric("android metric", 0.0);
            this.client.trackException(exception, "android handler");
            this.client.trackPageView("android page");
            Thread.sleep(10);
        }

        queue.flush();
        Thread.sleep(10);
        this.validate();
    }

    public void validate() throws Exception {
        try {
            MockQueue queue = this.client.getChannel().getQueue();
            CountDownLatch rspSignal = queue.sender.responseSignal;
            CountDownLatch sendSignal = queue.sender.sendSignal;
            rspSignal.await(30, TimeUnit.SECONDS);

            Log.i("RESPONSE", queue.sender.getLastResponse());

            if (rspSignal.getCount() < sendSignal.getCount()) {
                Log.w("BACKEND_ERROR", "response count is lower than send count");
            } else if (queue.sender.responseCode == 206) {
                Log.w("BACKEND_ERROR", "response is 206, some telemetry was rejected");
            }

            if (queue.sender.responseCode != 200) {
                Assert.fail("response rejected with: " + queue.sender.getLastResponse());
            }

            Assert.assertEquals("response was received", 0, rspSignal.getCount());
            Assert.assertEquals("queue is empty", 0, queue.getQueueSize());
        } catch (InterruptedException e) {
            Assert.fail(e.toString());
        }
    }
}