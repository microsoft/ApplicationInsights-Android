package com.microsoft.applicationinsights;

import android.content.Intent;
import android.test.ActivityUnitTestCase;

import com.microsoft.mocks.MockActivity;

import junit.framework.Assert;

import java.io.InvalidObjectException;
import java.util.LinkedHashMap;
import java.util.Map;

public class TelemetryClientTest extends ActivityUnitTestCase<MockActivity> {

    MockActivity mockActivity;

    public TelemetryClientTest() {
        super(MockActivity.class);
    }


    public void setUp() throws Exception {
        super.setUp();

        Intent intent = new Intent(getInstrumentation().getTargetContext(), MockActivity.class);
        this.mockActivity = this.startActivity(intent, null, null);
    }

    public void testRegister() throws Exception {
        TelemetryClient client = TelemetryClient.getInstance();
        Assert.assertNotNull("static registration returns non-null client", client);
    }

    //TODO test at applicable location
//    public void testGetContext() throws Exception {
//        TelemetryClient client = TelemetryClient.getInstance(this.mockActivity);
//        Assert.assertNotNull("context is initialized", client.getContext());
//    }

    //TODO test at applicable location
//    public void testGetConfig() throws Exception {
//        TelemetryClient client = TelemetryClient.getInstance(this.mockActivity);
//        Assert.assertNotNull("config is initialized", client.getConfig());
//    }

    //TODO test at applicable location
//    public void testCommonProperties() throws Exception {
//        TelemetryClient client = TelemetryClient.getInstance(this.mockActivity);
//
//        // add a property
//        LinkedHashMap<String, String> properties1 = new LinkedHashMap<>();
//        properties1.put("p1", "v1");
//        client.setCommonProperties(properties1);
//
//        // check that it exists
//        Map<String, String> properties2 = client.getCommonProperties();
//        Assert.assertEquals("Property 1 matches", "v1", properties2.get("p1"));
//    }

    public void testTrackEvent() throws Exception {
        TelemetryClient client = TelemetryClient.getInstance();
        LinkedHashMap<String, String> properties = new LinkedHashMap<>();
        LinkedHashMap<String, Double> measurements = new LinkedHashMap<>();

        client.trackEvent(null);
        client.trackEvent("event1");
        client.trackEvent("event2", properties);
        client.trackEvent("event3", properties, measurements);
    }

    public void testTrackTrace() throws Exception {
        TelemetryClient client = TelemetryClient.getInstance();
        LinkedHashMap<String, String> properties = new LinkedHashMap<>();

        client.trackTrace(null);
        client.trackTrace("trace1");
        client.trackTrace("trace2", properties);
    }

    public void testTrackMetric() throws Exception {
        TelemetryClient client = TelemetryClient.getInstance();

        client.trackMetric(null, 0);
        client.trackMetric("metric1", 1.1);
        client.trackMetric("metric2", 3);
        client.trackMetric("metric3", 3.3);
        client.trackMetric("metric3", 4);
    }

    public void testTrackException() throws Exception {
        TelemetryClient client = TelemetryClient.getInstance();
        LinkedHashMap<String, String> properties = new LinkedHashMap<>();
        LinkedHashMap<String, Double> measurements = new LinkedHashMap<>();

        client.trackHandledException(null);
        client.trackHandledException(new Exception());
        try {
            throw new InvalidObjectException("this is expected");
        } catch (InvalidObjectException exception) {
            client.trackHandledException(exception);
            client.trackHandledException(exception, properties);
        }
    }

    public void testTrackPageView() throws Exception {
        TelemetryClient client = TelemetryClient.getInstance();
        LinkedHashMap<String, String> properties = new LinkedHashMap<>();
        LinkedHashMap<String, Double> measurements = new LinkedHashMap<>();

        client.trackPageView("android page");
        client.trackPageView("android page");
        client.trackPageView("android page", properties);
        client.trackPageView("android page", properties, measurements);
    }

    //TODO test at applicable location
//    public void testFlush() throws Exception {
//        TelemetryClient client = TelemetryClient.getInstance(this.mockActivity);
//        client.sendPendingData(); // todo: mock sender and verify that sendPendingData is called
//    }
}