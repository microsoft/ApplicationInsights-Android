package com.microsoft.commonlogging.channel;

import junit.framework.Assert;
import junit.framework.TestCase;

public class SenderTest extends TestCase {

    private Sender sender;

    public void setUp() throws Exception {
        super.setUp();
        this.sender = new Sender();
    }

    public void tearDown() throws Exception {

    }

    public void testGetConfig() throws Exception {
        Assert.assertNotNull("Sender constructor should initialize config", this.sender.getConfig());
    }

    public void testSend() throws Exception {

    }

    public void testOnResponse() throws Exception {
    }
}

