package com.microsoft.applicationinsights;

import com.microsoft.applicationinsights.channel.Sender;
import com.microsoft.applicationinsights.channel.TelemetryChannel;
import com.microsoft.applicationinsights.channel.contracts.ExceptionData;
import com.microsoft.applicationinsights.channel.contracts.ExceptionDetails;
import com.microsoft.applicationinsights.channel.contracts.shared.IJsonSerializable;

import junit.framework.Assert;
import junit.framework.TestCase;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class AbstractTelemetryClientTest extends TestCase {

    private TestTelemetryClient client;

    public void setUp() throws Exception {
        super.setUp();
        this.client = new TestTelemetryClient("2b240a15-4b1c-4c40-a4f0-0e8142116250");
        this.client.config.getSenderConfig().setMaxBatchIntervalMs(10);
    }

    public void tearDown() throws Exception {
        try {
            TestSender sender = this.client.getSender();
            CountDownLatch signal = sender.responseSignal;
            signal.await(30, TimeUnit.SECONDS);
            Assert.assertEquals("response was received", 0, signal.getCount());
            Assert.assertEquals("response is success", 200, sender.responseCode);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void testTrackEvent() throws Exception {
        this.client.trackEvent("core event");
    }

    public void testTrackTrace() throws Exception {
        this.client.trackTrace("core trace");
    }

    public void testTrackMetric() throws Exception {
        this.client.trackMetric("core metric", 0.0);
    }

    public void testTrackException() throws Exception {
        ExceptionData exception = new ExceptionData();
        exception.setHandledAt("here");
        ExceptionDetails detail = new ExceptionDetails();
        detail.setMessage("message");
        detail.setStack("stack");
        detail.setTypeName("core exception");
        ArrayList<ExceptionDetails> details = new ArrayList<ExceptionDetails>();
        details.add(detail);
        exception.setExceptions(details);
        this.client.trackException(exception);
    }

    public void testTrackPageView() throws Exception {
        this.client.trackPageView("core page", null, 10);
    }

    public void testTrackRequest() throws Exception {
        this.client.trackRequest("core request", "http://google.com", "GET");
    }

    private class TestTelemetryClient extends AbstractTelemetryClient<TelemetryClientConfig>{
        public TestTelemetryClient(String iKey) {
            super(new TelemetryClientConfig(iKey));
            this.channel = new TestChannel(this.config);
        }

        public TestSender getSender(){
            return ((TestChannel)this.channel).getSender();
        }

        protected void trackPageView(String pageName, String url, long pageLoadDurationMs) {
            super.trackPageView(pageName, url, pageLoadDurationMs, null, null);
        }

        protected void trackRequest(String name, String url, String httpMethod) {
            super.trackRequest(name, url, httpMethod, new Date(), 50, 200, true, null, null);
        }
    }

    private class TestChannel extends TelemetryChannel {
        public TestChannel(TelemetryClientConfig config){
            super(config);
            this.sender = new TestSender();
        }

        public TestSender getSender() {
            return (TestSender)this.sender;
        }
    }

    private class TestSender extends Sender {

        public int responseCode;
        public CountDownLatch responseSignal;

        public TestSender() {
            super();
            this.responseCode = 0;
            this.responseSignal = new CountDownLatch(1);
        }

        @Override
        protected void send(IJsonSerializable[] data) {
            super.send(data);
        }

        @Override
        protected String onResponse(HttpURLConnection connection, int responseCode) {
            String response = super.onResponse(connection, responseCode);
            String prettyResponse = this.prettyPrintJSON(response);
            System.out.println(prettyResponse);
            this.responseCode = responseCode;
            this.responseSignal.countDown();
            return response;
        }

        private String prettyPrintJSON(String payload) {
            if(payload == null)
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