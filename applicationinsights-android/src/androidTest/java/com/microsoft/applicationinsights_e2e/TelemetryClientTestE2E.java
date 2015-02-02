package com.microsoft.applicationinsights_e2e;

import android.app.Activity;
import android.content.Context;
import android.test.AndroidTestCase;
import android.util.Log;

import com.microsoft.applicationinsights.TelemetryClient;
import com.microsoft.applicationinsights.TelemetryClientConfig;
import com.microsoft.applicationinsights.channel.Sender;
import com.microsoft.applicationinsights.channel.TelemetryChannel;
import com.microsoft.applicationinsights.channel.contracts.shared.IJsonSerializable;

import junit.framework.Assert;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.util.LinkedHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class TelemetryClientTestE2E extends AndroidTestCase {

    private TestTelemetryClient client;
    private TestSender sender;
    private LinkedHashMap<String, String> properties;
    private LinkedHashMap<String, Double> measurements;

    public void setUp() throws Exception {
        super.setUp();
        String iKey = "2b240a15-4b1c-4c40-a4f0-0e8142116250";

        this.client = new TestTelemetryClient(this.getContext(), iKey);
        this.sender = new TestSender(1);
        this.sender.getConfig().setMaxBatchIntervalMs(20);
        this.client.getChannel().setSender(this.sender);

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

    public void testTrackPageView() throws Exception {
        this.client.trackPageView("android page");
        this.client.trackPageView("android page");
        this.client.trackPageView("android page", properties);
        this.client.trackPageView("android page", properties, measurements);
        this.validate();
    }

    public void testTrackAllRequests() throws Exception {

        this.sender = new TestSender(5);
        this.client.getChannel().setSender(this.sender);
        Exception exception;
        try {
            throw new Exception();
        } catch (Exception e) {
            exception = e;
        }

        this.sender.getConfig().setMaxBatchCount(10);
        for (int i = 0; i < 10; i++) {
            this.client.trackEvent("android event");
            this.client.trackTrace("android trace");
            this.client.trackMetric("android metric", 0.0);
            this.client.trackException(exception, "android handler");
            this.client.trackPageView("android page");
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

            Log.i("RESPONSE", this.sender.getLastResponse());

            if(rspSignal.getCount() < sendSignal.getCount()) {
                Log.w("BACKEND_ERROR", "response count is lower than send count");
            } else if(sender.responseCode == 206) {
                Log.w("BACKEND_ERROR", "response is 206, some telemetry was rejected");
            }

            if(sender.responseCode != 200) {
                Assert.fail("response rejected with: " + this.sender.getLastResponse());
            }

            Assert.assertEquals("response was received", 0, rspSignal.getCount());
            Assert.assertEquals("queue is empty", 0, this.sender.getQueueSize());
        } catch (InterruptedException e) {
            Assert.fail(e.toString());
        }
    }

    protected class TestTelemetryClient extends TelemetryClient {
        public TestTelemetryClient (Context context, String iKey) {
            super(new TelemetryClientConfig(iKey, context));
        }

        public TelemetryChannel getChannel() {
            return this.channel;
        }
    }

    private class TestSender extends Sender {

        public int responseCode;
        public CountDownLatch sendSignal;
        public CountDownLatch responseSignal;
        private String lastResponse;

        public TestSender(int expectedSendCount) {
            super();
            this.responseCode = 0;
            this.sendSignal = new CountDownLatch(expectedSendCount);
            this.responseSignal = new CountDownLatch(expectedSendCount);
            this.lastResponse = null;
        }

        public String getLastResponse() {
            if (this.lastResponse == null) {
                return "";
            } else {
                return this.lastResponse;
            }
        }

        public long getQueueSize() {
            return this.queue.size();
        }

        @Override
        protected void send(IJsonSerializable[] data) {
            this.sendSignal.countDown();
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

        private class WriterListener extends Writer {

            private Writer baseWriter;
            private StringBuilder stringBuilder;

            public WriterListener(Writer baseWriter) {
                this.baseWriter = baseWriter;
                this.stringBuilder = new StringBuilder();
            }

            @Override
            public void close() throws IOException {
                baseWriter.close();
            }

            @Override
            public void flush() throws IOException {
                baseWriter.flush();
            }

            @Override
            public void write(char[] buf, int offset, int count) throws IOException {
                stringBuilder.append(buf);
                baseWriter.write(buf);
            }
        }
    }
}