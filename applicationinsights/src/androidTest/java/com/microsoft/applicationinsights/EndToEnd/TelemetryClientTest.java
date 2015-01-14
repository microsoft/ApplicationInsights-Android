package com.microsoft.applicationinsights.EndToEnd;

import android.content.Context;
import android.test.AndroidTestCase;
import android.util.Log;

import com.microsoft.applicationinsights.TelemetryClient;
import com.microsoft.applicationinsights.channel.Sender;
import com.microsoft.applicationinsights.channel.contracts.shared.IJsonSerializable;

import junit.framework.Assert;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.util.LinkedHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class TelemetryClientTest extends AndroidTestCase {

    private TelemetryClient client;
    private TestSender sender;
    private LinkedHashMap<String, String> properties;
    private LinkedHashMap<String, Double> measurements;

    public void setUp() throws Exception {
        super.setUp();
        String iKey = "2b240a15-4b1c-4c40-a4f0-0e8142116250";
        Context context = this.getContext();

        this.client = new TelemetryClient(iKey, context);
        this.sender = new TestSender(1);
        this.sender.getConfig().setMaxBatchIntervalMs(20);
        this.client.getChannel().setSender(this.sender);

        this.properties = new LinkedHashMap<String, String>();
        this.properties.put("core property", "core value");
        this.measurements = new LinkedHashMap<String, Double>();
        this.measurements.put("core measurement", 5.5);
    }

    public void testTrackEvent() throws Exception {
        this.client.trackEvent("android event");
        this.validate();
    }

    public void testTrackTrace() throws Exception {
        this.client.trackTrace("android trace");
        this.validate();
    }

    public void testTrackMetric() throws Exception {
        this.client.trackMetric("android metric", 0.0);
        this.validate();
    }

    public void testTrackException() throws Exception {
        try {
            throw new InvalidObjectException("this is expected");
        } catch (InvalidObjectException exception) {
            this.client.trackException(exception, "android handler");
        }

        this.validate();
    }

    public void testTrackPageView() throws Exception {
        this.client.trackPageView("android page");
        this.client.trackPageView("android page", 50);
        this.client.trackPageView("android page", 50, this.properties);
        this.client.trackPageView("android page", 50, this.properties, this.measurements);
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