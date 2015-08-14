package com.microsoft.applicationinsights.library;

import com.microsoft.applicationinsights.library.config.Configuration;

import junit.framework.Assert;
import junit.framework.TestCase;

import static org.mockito.Mockito.mock;

public class SenderTest extends TestCase {

    private Sender sut;

    public void setUp() throws Exception {
        super.setUp();
        Configuration config = new Configuration();
        this.sut = new Sender(config);
    }

    public void testInitialisationWorks(){
        Assert.assertNotNull(sut.config);
        Assert.assertNotNull(sut.persistence);
    }

    public void testCallGetInstanceTwiceReturnsSameObject(){

        Sender.initialize(new Configuration());
        Sender sender1 = Sender.getInstance();
        Sender sender2 = Sender.getInstance();

        Assert.assertSame(sender1, sender2);
    }

    public void testExpectedResponseCode() {
        for(int statusCode = 100; statusCode <= 510; statusCode++){
            if(199 < statusCode && statusCode <= 203) {
                assertTrue(sut.isExpected(statusCode));
            }else{
                assertFalse(sut.isExpected(statusCode));
            }
        }
    }

    public void testRecoverableResponseCode() {
        for(int statusCode = 100; statusCode <= 510; statusCode++){
            if((statusCode == 429) || (statusCode == 408) || (statusCode == 500) || (statusCode == 503) || (statusCode == 511)) {
                assertTrue(sut.isRecoverableError(statusCode));
            }else{
                assertFalse(sut.isRecoverableError(statusCode));
            }
        }
    }
}

