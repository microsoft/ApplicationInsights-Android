package com.microsoft.applicationinsights.library;

import android.test.ApplicationTestCase;
import android.util.Log;

import com.microsoft.applicationinsights.library.config.ApplicationInsightsConfig;

import junit.framework.Assert;

import java.util.LinkedHashMap;
import java.util.concurrent.TimeUnit;

public class TelemetryClientTestE2E extends ApplicationTestCase<MockApplication> {

    public TelemetryClientTestE2E() {
        super(MockApplication.class);
    }

    private LinkedHashMap<String, String> properties;
    private LinkedHashMap<String, Double> measurements;
    private static int batchCount = 10;

    public void setUp() throws Exception {
        super.setUp();

        ApplicationInsights.setup(this.getContext(), this.getApplication());
        ApplicationInsights.setDeveloperMode(true);

        ApplicationInsightsConfig config = ApplicationInsights.getConfig();
        config.setEndpointUrl(config.getEndpointUrl().replace("https", "http"));
        config.setMaxBatchIntervalMs(10);
        config.setMaxBatchCount(batchCount);

        ApplicationInsights.start();

        this.properties = new LinkedHashMap<String, String>();
        this.properties.put("core property", "core value");
        this.measurements = new LinkedHashMap<String, Double>();
        this.measurements.put("core measurement", 5.5);
    }

    public void testTrackEvent() throws Exception {
        TelemetryClient.getInstance().trackEvent(null);
        TelemetryClient.getInstance().trackEvent("event1");
        TelemetryClient.getInstance().trackEvent("event2", properties);
        TelemetryClient.getInstance().trackEvent("event3", properties, measurements);
        Thread.sleep(50);
        this.validate(1);
    }

    public void testTrackTrace() throws Exception {
        TelemetryClient.getInstance().trackTrace(null);
        TelemetryClient.getInstance().trackTrace("trace1");
        TelemetryClient.getInstance().trackTrace("trace2", properties);
        Thread.sleep(50);
        this.validate(1);
    }

    public void testTrackMetric() throws Exception {
        TelemetryClient.getInstance().trackMetric(null, 0);
        TelemetryClient.getInstance().trackMetric("metric1", 1.1);
        TelemetryClient.getInstance().trackMetric("metric2", 3);
        TelemetryClient.getInstance().trackMetric("metric3", 3.3);
        TelemetryClient.getInstance().trackMetric("metric3", 4);
        Thread.sleep(50);
        this.validate(1);
    }

    public void testTrackPageView() throws Exception {
        TelemetryClient.getInstance().trackPageView("android page");
        TelemetryClient.getInstance().trackPageView("android page");
        TelemetryClient.getInstance().trackPageView("android page", properties);
        TelemetryClient.getInstance().trackPageView("android page", properties, measurements);
        Thread.sleep(50);
        this.validate(1);
    }

    public void testTrackAllRequests() throws Exception {
        Exception exception;
        try {
            throw new Exception();
        } catch (Exception e) {
            exception = e;
        }

        ((Channel)Channel.getInstance()).queue.config.setMaxBatchCount(10);
        for (int i = 0; i < batchCount; i++) {
            TelemetryClient.getInstance().trackEvent("android event");
            TelemetryClient.getInstance().trackTrace("android trace");
            TelemetryClient.getInstance().trackMetric("android metric", 0.0);
            TelemetryClient.getInstance().trackHandledException(exception);
            TelemetryClient.getInstance().trackPageView("android page");
            Thread.sleep(10);
        }

        Thread.sleep(50);
        this.validate(1);
    }

    public void validate(int count) {
        try {
            ApplicationInsightsConfig config = new ApplicationInsightsConfig();
            MockSender sender = new MockSender(count, config);

            sender.flush(count);

            // wait 30 seconds for all responses
            sender.responseSignal.await(5, TimeUnit.SECONDS);

            if (sender.responseSignal.getCount() != 0) {
                Log.w("BACKEND_ERROR", "response count is lower than enqueue count");
            }

            for(int i = 0; i < count; i++) {
                if(i < sender.responseCodes.size()) {
                    Assert.assertTrue("response is 206, some telemetry was rejected",
                            sender.responseCodes.get(i) == 200);
                }
            }

            Assert.assertEquals("response was received", 0, sender.responseSignal.getCount());
        } catch (InterruptedException e) {
            Assert.fail(e.toString());
        }
   }
}