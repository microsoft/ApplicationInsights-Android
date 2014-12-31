package com.microsoft.applicationinsights;

import com.microsoft.applicationinsights.channel.Sender;
import com.microsoft.applicationinsights.channel.TelemetryChannel;
import com.microsoft.applicationinsights.channel.contracts.shared.IJsonSerializable;

import junit.framework.Assert;
import junit.framework.TestCase;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.util.LinkedList;
import java.util.Timer;
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
            CountDownLatch signal =this.client.getSender().responseSignal;
            signal.await(10, TimeUnit.SECONDS);
            Assert.assertEquals("response was received", 0, signal.getCount());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void testTrackEvent() throws Exception {
        this.client.trackEvent("core event");
    }

    public void testTrackTrace() throws Exception {

    }

    public void testTrackMetric() throws Exception {

    }

    public void testTrackException() throws Exception {

    }

    public void testTrackPageView() throws Exception {

    }

    public void testTrackRequest() throws Exception {

    }

    public void testTrack() throws Exception {

    }

    private class TestTelemetryClient extends AbstractTelemetryClient<TelemetryClientConfig>{
        public TestTelemetryClient(String iKey) {
            super(new TelemetryClientConfig(iKey));
            this.channel = new TestChannel(this.config);
        }

        public TestSender getSender(){
            return ((TestChannel)this.channel).getSender();
        }
    }

    private class TestChannel extends TelemetryChannel {
        public TestChannel(TelemetryClientConfig config){
            super(config, new TestSender());
        }

        public TestSender getSender() {
            return (TestSender)this.sender;
        }
    }

    private class TestSender extends Sender {

        public CountDownLatch responseSignal;

        public TestSender() {
            super();
            this.responseSignal = new CountDownLatch(1);
        }

        @Override
        protected String onResponse(HttpURLConnection connection, int responseCode) {
            Assert.assertEquals("Response is 200", 200, responseCode);
            String response = super.onResponse(connection, responseCode);
            this.responseSignal.countDown();
            return response;
        }
    }
}