package com.microsoft.applicationinsights;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.test.ActivityTestCase;

import com.microsoft.applicationinsights.channel.contracts.EventData;

import junit.framework.Assert;

import java.io.InvalidObjectException;
import java.util.LinkedHashMap;

public class TelemetryClientTest extends ActivityTestCase {

    MockActivity mockActivity;

    public void setUp() throws Exception {
        super.setUp();
        this.mockActivity = new MockActivity(getInstrumentation().getContext());
    }

    public void testRegister() throws Exception {
        TelemetryClient client = TelemetryClient.getInstance(this.mockActivity);
        Assert.assertNotNull("static registration returns non-null client", client);
    }

    public void testGetContext() throws Exception {
        TelemetryClient client = TelemetryClient.getInstance(this.mockActivity);
        Assert.assertNotNull("context is initialized", client.getContext());
    }

    public void testGetConfig() throws Exception {
        TelemetryClient client = TelemetryClient.getInstance(this.mockActivity);
        Assert.assertNotNull("config is initialized", client.getConfig());
    }

    public void testCommonProperties() throws Exception {
        TelemetryClient client = TelemetryClient.getInstance(this.mockActivity);

        // add a property
        LinkedHashMap<String, String> properties1 = new LinkedHashMap<>();
        properties1.put("p1", "v1");
        client.setCommonProperties(properties1);

        // check that it exists
        LinkedHashMap<String, String> properties2 = client.getCommonProperties();
        Assert.assertEquals("Property 1 matches", "v1", properties2.get("p1"));
    }

    public void testTrackEvent() throws Exception {
        TelemetryClient client = TelemetryClient.getInstance(this.mockActivity);
        LinkedHashMap<String, String> properties = new LinkedHashMap<>();
        LinkedHashMap<String, Double> measurements = new LinkedHashMap<>();

        client.trackEvent(null);
        client.trackEvent("event1");
        client.trackEvent("event2", properties);
        client.trackEvent("event3", properties, measurements);
    }

    public void testTrackTrace() throws Exception {
        TelemetryClient client = TelemetryClient.getInstance(this.mockActivity);
        LinkedHashMap<String, String> properties = new LinkedHashMap<>();

        client.trackTrace(null);
        client.trackTrace("trace1");
        client.trackTrace("trace2", properties);
    }

    public void testTrackMetric() throws Exception {
        TelemetryClient client = TelemetryClient.getInstance(this.mockActivity);
        LinkedHashMap<String, String> properties = new LinkedHashMap<>();

        client.trackMetric(null, 0);
        client.trackMetric("metric1", 1.1);
        client.trackMetric("metric2", 3);
        client.trackMetric("metric3", 3.3, properties);
        client.trackMetric("metric3", 4, properties);
    }

    public void testTrackException() throws Exception {
        TelemetryClient client = TelemetryClient.getInstance(this.mockActivity);
        LinkedHashMap<String, String> properties = new LinkedHashMap<>();
        LinkedHashMap<String, Double> measurements = new LinkedHashMap<>();

        client.trackException(null);
        client.trackException(new Exception());
        try {
            throw new InvalidObjectException("this is expected");
        } catch (InvalidObjectException exception) {
            client.trackException(exception);
            client.trackException(exception, "core handler");
            client.trackException(exception, "core handler1", properties);
            client.trackException(exception, "core handler2", properties, measurements);
        }
    }

    public void testTrackPageView() throws Exception {
        TelemetryClient client = TelemetryClient.getInstance(this.mockActivity);
        LinkedHashMap<String, String> properties = new LinkedHashMap<>();
        LinkedHashMap<String, Double> measurements = new LinkedHashMap<>();

        client.trackPageView("android page");
        client.trackPageView("android page", 50);
        client.trackPageView("android page", 50, properties);
        client.trackPageView("android page", 50, properties, measurements);
    }

    public void testTrack() throws Exception {
        TelemetryClient client = TelemetryClient.getInstance(this.mockActivity);
        EventData event = new EventData();

        try {
            client.track(event);
        } catch (Exception exception) {
            Assert.fail(exception.toString());
        }

        LinkedHashMap<String, String> properties1 = new LinkedHashMap<>();
        properties1.put("p", "v1");
        client.setCommonProperties(properties1);

        LinkedHashMap<String, String> properties2 = new LinkedHashMap<>();
        properties1.put("p", "v2");
        event.setProperties(properties2);

        // todo: mock channel and verify that property "p" has value "v2"
        client.track(event);
    }

    public void testFlush() throws Exception {
        TelemetryClient client = TelemetryClient.getInstance(this.mockActivity);
        client.flush(); // todo: mock sender and verify that flush is called
    }

    private class MockActivity extends Activity {
        public Context context;
        public MockActivity(Context context) {
            this.context = context;
        }

        @Override
        public Resources getResources() {
            return this.context.getResources();
        }

        @Override
        public Context getApplicationContext() {
            return this.context;
        }

        @Override
        public String getPackageName() {
            return "com.microsoft.applicationinsights.test";
        }
    }
}