package com.microsoft.applicationinsights.channel.e2eTest;

import com.microsoft.applicationinsights.channel.AbstractTelemetryContext;
import com.microsoft.applicationinsights.channel.IContextConfig;
import com.microsoft.applicationinsights.channel.Sender;
import com.microsoft.applicationinsights.channel.TelemetryChannel;
import com.microsoft.applicationinsights.channel.contracts.shared.IJsonSerializable;
import com.microsoft.applicationinsights.common.AbstractTelemetryClient;
import com.microsoft.applicationinsights.common.AbstractTelemetryClientConfig;

import junit.framework.Assert;
import junit.framework.TestCase;

import java.io.InvalidObjectException;
import java.net.HttpURLConnection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class TelemetryClientTest extends TestCase {

    private TestTelemetryClient client;
    private TestSender sender;
    private LinkedHashMap<String, String> properties;
    private LinkedHashMap<String, Double> measurements;

    public void setUp() throws Exception {
        super.setUp();
        this.client = new TestTelemetryClient("2b240a15-4b1c-4c40-a4f0-0e8142116250");
        this.sender = new TestSender(1);
        this.client.getChannel().setSender(this.sender);
        this.properties = new LinkedHashMap<String, String>();
        this.properties.put("core property", "core value");
        this.measurements = new LinkedHashMap<String, Double>();
        this.measurements.put("core measurement", 5.5);
    }

    public void testTrackEvent() throws Exception {
        this.client.trackEvent(null);
        this.client.trackEvent("core event1");
        this.client.trackEvent("core event2", properties);
        this.client.trackEvent("core event3", properties, measurements);
        this.validate();
    }

    public void testTrackTrace() throws Exception {
        this.client.trackTrace(null);
        this.client.trackTrace("core trace1");
        this.client.trackTrace("core trace2", properties);
        this.validate();
    }

    public void testTrackMetric() throws Exception {
        this.client.trackMetric(null, 0);
        this.client.trackMetric("core metric1", 1.1);
        this.client.trackMetric("core metric2", 3);
        this.client.trackMetric("core metric3", 3.3, properties);
        this.client.trackMetric("core metric3", 4, properties);
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

    public void testTrackPageView() throws Exception {
        this.client.trackPageView(null, null, 0, null, null);
        this.client.trackPageView("core page", null, 10, null, null);
        this.validate();
    }

    public void testTrackRequest() throws Exception {
        this.client.trackRequest(null, null, null, null, 0, 0, true, null, null);
        this.client.trackRequest("core request", "http://google.com", "GET",
                new Date(), 50, 200, true, null, null);
        this.validate();
    }

    public void testTrackAllRequests() throws Exception {

        this.sender = new TestSender(6);
        this.client.getChannel().setSender(this.sender);
        Exception exception;
        try {
            throw new InvalidObjectException("this is expected");
        } catch (InvalidObjectException e) {
            exception = e;
        }

        this.sender.getConfig().setMaxBatchCount(10);
        for(int i = 0; i < 10; i++) {
            this.client.trackEvent("core event");
            this.client.trackTrace("core trace");
            this.client.trackMetric("core metric", 0.0);
            this.client.trackException(exception, "core handler", null, null);
            this.client.trackPageView("core page", null, 10, null, null);
            this.client.trackRequest("core request", "http://google.com", "GET",
                    new Date(), 50, 200, true, null, null);
            Thread.sleep(10);
        }

        this.sender.flush();
        Thread.sleep(10);
        this.validate();
    }

    public void validate() throws Exception {
        try {
            CountDownLatch rspSignal = this.sender.responseSignal;
            CountDownLatch sendSignal = this.sender.sendSignal;
            rspSignal.await(30, TimeUnit.SECONDS);

            System.out.println(this.sender.getLastResponse());

            if(rspSignal.getCount() < sendSignal.getCount()) {
                System.out.println("response count is lower than send count");
            } else if(sender.responseCode == 206) {
                System.out.println("response is 206, some telemetry was rejected");
            }

            if(sender.responseCode != 200) {
                Assert.fail("response rejected with: " + this.sender.getLastResponse());
            }

            Assert.assertEquals("response was received", 0, rspSignal.getCount());
        } catch (InterruptedException e) {
            Assert.fail(e.toString());
        }
    }

    private class TelemetryContext extends AbstractTelemetryContext {
        public TelemetryContext(IContextConfig config) {
            super(config);
        }
    }

    private class TelemetryClientConfig extends AbstractTelemetryClientConfig {
        public TelemetryClientConfig(String iKey){
            super(iKey);
        }
    }

    private class TestTelemetryClient extends
            AbstractTelemetryClient<TelemetryClientConfig, TelemetryContext, TelemetryChannel> {

        /**
         * Construct a new instance of the telemetry client
         */
        public TestTelemetryClient(String instrumentationKey) {
            this(new TelemetryClientConfig(instrumentationKey));
        }

        /**
         * Construct a new instance of the telemetry client
         */
        protected TestTelemetryClient(TelemetryClientConfig config) {
            super(config, new TelemetryContext(config), new TelemetryChannel(config));
        }

        /**
         * Construct a new instance of the telemetry client
         */
        protected TestTelemetryClient (
                TelemetryClientConfig config,
                TelemetryContext context,
                TelemetryChannel channel) {
            super(config, context, channel);
        }

        /**
         * Sends information about a page view to Application Insights.
         *
         * @param pageName the name of the page
         * @param url the url of the page
         * @param pageLoadDurationMs the duration of the page load
         * @param properties custom properties
         * @param measurements    custom metrics
         */
        public void trackPageView(
                String pageName,
                String url,
                long pageLoadDurationMs,
                LinkedHashMap<String, String> properties,
                LinkedHashMap<String, Double> measurements) {
            super.trackPageView(pageName, url, pageLoadDurationMs, properties, measurements);
        }

        /**
         * Sends information about a request to Application Insights.
         *
         * @param name the name of this request
         * @param url the url for this request
         * @param httpMethod the http method for this request
         * @param startTime the start time of this request
         * @param durationMs the duration of this request in milliseconds
         * @param responseCode the response code for this request
         * @param isSuccess the success status of this request
         * @param properties custom properties
         * @param measurements    custom metrics
         */
        public void trackRequest(
                String name,
                String url,
                String httpMethod,
                Date startTime,
                long durationMs,
                int responseCode,
                boolean isSuccess,
                LinkedHashMap<String, String> properties,
                LinkedHashMap<String, Double> measurements) {
            super.trackRequest(name, url, httpMethod, startTime, durationMs,
                    responseCode, isSuccess, properties, measurements);
        }

        public TelemetryChannel getChannel() {
            return this.channel;
        }
    }

    protected class TestSender extends Sender {

        public int responseCode;
        public CountDownLatch sendSignal;
        public CountDownLatch responseSignal;
        private String lastResponse;

        public TestSender(int expectedSendCount) {
            super();
            this.config.setMaxBatchIntervalMs(20);
            this.config.setEndpointUrl("http://dc.services.visualstudio.com/v2/track");
            this.responseCode = 0;
            this.sendSignal = new CountDownLatch(expectedSendCount);
            this.responseSignal = new CountDownLatch(expectedSendCount);
            this.lastResponse = null;
        }

        public String getLastResponse() {
            return this.lastResponse;
        }

        @Override
        protected void send(IJsonSerializable[] data) {
            super.send(data);
        }

        @Override
        protected String onResponse(HttpURLConnection connection, int responseCode) {
            String response = super.onResponse(connection, responseCode);
            this.lastResponse = prettyPrintJSON(response);
            this.responseCode = responseCode;
            this.responseSignal.countDown();
            return response;
        }

        private String prettyPrintJSON(String payload) {
            if (payload == null)
                return "";

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

            return result;
        }
    }
}