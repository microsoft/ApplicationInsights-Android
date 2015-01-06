package com.microsoft.applicationinsights.core;

import com.microsoft.applicationinsights.channel.Sender;
import com.microsoft.applicationinsights.channel.TelemetryChannel;
import com.microsoft.applicationinsights.channel.contracts.ExceptionData;
import com.microsoft.applicationinsights.channel.contracts.ExceptionDetails;
import com.microsoft.applicationinsights.channel.contracts.shared.IJsonSerializable;

import junit.framework.Assert;
import junit.framework.TestCase;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class TelemetryClientTest extends TestCase {

    private TelemetryClient client;
    private TestSender sender;

    public void setUp() throws Exception {
        super.setUp();
        this.client = new TelemetryClient("2b240a15-4b1c-4c40-a4f0-0e8142116250");
        this.sender = new TestSender(1);
        this.client.channel.setSender(this.sender);
        this.client.config.getSenderConfig().setMaxBatchIntervalMs(10);
    }

    public void tearDown() throws Exception {
        try {
            CountDownLatch signal = this.sender.responseSignal;
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
        try {
            throw new InvalidObjectException("this is expected");
        } catch (InvalidObjectException exception) {
            this.client.trackException(exception, "core handler", null, null);
        }
    }

    public void testTrackPageView() throws Exception {
        this.client.trackPageView("core page", null, 10, null, null);
    }

    public void testTrackRequest() throws Exception {
        this.client.trackRequest(
                "core request", "http://google.com", "GET", new Date(), 50, 200, true, null, null);
    }

    public void testTrackAllRequests() throws Exception {

        this.sender = new TestSender(6);
        this.client.channel.setSender(this.sender);

        this.sender.getConfig().setMaxBatchCount(100);
        for(int i = 0; i < 100; i++) {
            this.testTrackEvent();
            this.testTrackException();
            this.testTrackMetric();
            this.testTrackPageView();
            this.testTrackRequest();
            this.testTrackTrace();
        }

        this.sender.flush();
    }

    private class TestSender extends Sender {

        public int responseCode;
        public CountDownLatch responseSignal;
        public boolean useFakeWriter;
        public StringWriter writer;

        public TestSender(int expectedSendCount) {
            super();
            this.responseCode = 0;
            this.responseSignal = new CountDownLatch(expectedSendCount);
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

        @Override
        protected Writer getWriter(HttpURLConnection connection) throws IOException {
            if(this.useFakeWriter){
                this.writer = new StringWriter();
                return this.writer;
            } else {
                return new OutputStreamWriter(connection.getOutputStream());
            }
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