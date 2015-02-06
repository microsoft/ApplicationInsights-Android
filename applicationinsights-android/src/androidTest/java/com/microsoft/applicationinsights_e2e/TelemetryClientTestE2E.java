package com.microsoft.applicationinsights_e2e;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.test.ActivityTestCase;
import android.util.Log;

import com.microsoft.applicationinsights.TelemetryClient;
import com.microsoft.applicationinsights.TelemetryClientConfig;
import com.microsoft.applicationinsights.channel.TelemetryContext;
import com.microsoft.commonlogging.channel.Sender;
import com.microsoft.commonlogging.channel.TelemetryChannel;
import com.microsoft.commonlogging.channel.TelemetryChannelConfig;
import com.microsoft.commonlogging.channel.TelemetryQueue;
import com.microsoft.commonlogging.channel.contracts.shared.IJsonSerializable;

import junit.framework.Assert;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.util.LinkedHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class TelemetryClientTestE2E extends ActivityTestCase {

    private TestClient client;
    private LinkedHashMap<String, String> properties;
    private LinkedHashMap<String, Double> measurements;

    public void setUp() throws Exception {
        super.setUp();

        MockActivity activity = new MockActivity(getInstrumentation().getContext());
        this.client = new TestClient(activity);
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

    public void testTrackPageView() throws Exception {
        this.client.trackPageView("android page");
        this.client.trackPageView("android page");
        this.client.trackPageView("android page", properties);
        this.client.trackPageView("android page", properties, measurements);
        this.validate();
    }

    public void testTrackAllRequests() throws Exception {
        TestQueue queue = new TestQueue(5);
        String endpoint = queue.sender.getConfig().getEndpointUrl();
        queue.sender.getConfig().setEndpointUrl(endpoint.replace("https", "http"));
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
            TestQueue queue = this.client.getChannel().getQueue();
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

    private class TestClient extends TelemetryClient {
        public TestClient(Activity activity) {
            this(new TelemetryClientConfig(activity));
        }

        protected TestClient(TelemetryClientConfig config) {
            this(config, new TestChannel(config));
        }

        protected TestClient(TelemetryClientConfig config, TestChannel channel) {
            super(config,new TelemetryContext(config), channel);
            channel.setQueue(new TestQueue(1));
        }

        public TestChannel getChannel() {
            return (TestChannel)this.channel;
        }
    }

    private class TestChannel extends TelemetryChannel{
        public TestChannel(TelemetryClientConfig config) {
            super(config);
        }

        @Override
        public void setQueue(TelemetryQueue queue) {
            super.setQueue(queue);
        }

        @Override
        public TestQueue getQueue() {
            return (TestQueue)super.getQueue();
        }
    }

    private class TestQueue extends TelemetryQueue {

        public int responseCode;
        public CountDownLatch sendSignal;
        public CountDownLatch responseSignal;
        public TestSender sender;

        public TestQueue(int expectedSendCount) {
            super();
            this.responseCode = 0;
            this.sendSignal = new CountDownLatch(expectedSendCount);
            this.responseSignal = new CountDownLatch(expectedSendCount);
            this.sender = new TestSender(sendSignal, responseSignal);
            super.sender = this.sender;
        }

        public long getQueueSize() {
            return this.linkedList.size();
        }
    }

    private class TestSender extends Sender {

        public int responseCode;
        public CountDownLatch sendSignal;
        public CountDownLatch responseSignal;
        private String lastResponse;

        public TestSender(CountDownLatch sendSignal, CountDownLatch responseSignal) {
            super();
            this.responseCode = 0;
            this.sendSignal = sendSignal;
            this.responseSignal = responseSignal;
            this.lastResponse = null;
        }

        public String getLastResponse() {
            if (this.lastResponse == null) {
                return "";
            } else {
                return this.lastResponse;
            }
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