package com.microsoft.applicationinsights.library;

import com.microsoft.applicationinsights.library.config.ApplicationInsightsConfig;

import junit.framework.Assert;
import junit.framework.TestCase;

import static org.mockito.Mockito.mock;

public class SenderTest extends TestCase {

    private Sender sut;

    public void setUp() throws Exception {
        super.setUp();
        ApplicationInsightsConfig config = new ApplicationInsightsConfig();
        this.sut = new Sender(config);
    }

    public void testInitialisationWorks(){
        Assert.assertNotNull(sut.config);
        Assert.assertNotNull(sut.persistence);
    }

    public void testCallGetInstanceTwiceReturnsSameObject(){

        Sender.initialize(new ApplicationInsightsConfig());
        Sender sender1 = Sender.getInstance();
        Sender sender2 = Sender.getInstance();

        Assert.assertSame(sender1, sender2);
    }




}

