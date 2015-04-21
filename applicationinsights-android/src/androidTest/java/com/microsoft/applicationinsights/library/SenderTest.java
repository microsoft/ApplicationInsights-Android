package com.microsoft.applicationinsights.library;

import com.microsoft.applicationinsights.library.config.SenderConfig;
import com.microsoft.applicationinsights.library.config.ApplicationInsightsConfig;

import junit.framework.TestCase;

public class SenderTest extends TestCase {

    private Sender sender;

    //TODO write more tests

    public void setUp() throws Exception {
        super.setUp();
        ApplicationInsightsConfig config = new ApplicationInsightsConfig();
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

