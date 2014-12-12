package com.microsoft.applicationinsights;

import android.content.Context;
import android.test.AndroidTestCase;

import com.microsoft.applicationinsights.channel.SenderConfig;

public class TelemetryClientTest extends AndroidTestCase {

    private TelemetryClient tc;

    public void setUp() throws Exception {
        super.setUp();
        String iKey = "2b240a15-4b1c-4c40-a4f0-0e8142116250";
        Context context = this.getContext();
        SenderConfig.maxBatchIntervalMs = 1;
        this.tc = new TelemetryClient(iKey, context);
    }

    public void tearDown() throws Exception {
        
    }

    public void testTrackEvent() throws Exception {
        this.tc.trackEvent("event");
    }

    public void testTrackTrace() throws Exception {
        this.tc.trackTrace("trace");
    }

    public void testTrackMetric() throws Exception {
        this.tc.trackMetric("metric", 0.0);
    }

    public void testTrackException() throws Exception {
        //this.tc.trackException();
    }

    public void testTrackPageView() throws Exception {
        this.tc.trackPageView("page");
    }
}