package com.microsoft.applicationinsights.channel;

import junit.framework.TestCase;

public class SenderTest extends TestCase {

    private Sender sender;

    public void setUp() throws Exception {
        super.setUp();
        TelemetryQueueConfig config = new TelemetryQueueConfig();
        this.sender = new Sender(config);
    }

    public void tearDown() throws Exception {

    }

    public void testGetConfig() throws Exception {

    }

    public void testSend() throws Exception {

    }

    public void testOnResponse() throws Exception {
    }
}

