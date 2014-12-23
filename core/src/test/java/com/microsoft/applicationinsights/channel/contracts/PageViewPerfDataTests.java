package com.microsoft.applicationinsights.channel.contracts;

import junit.framework.TestCase;
import junit.framework.Assert;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/// <summary>
/// Data contract test class PageViewPerfDataTests.
/// </summary>
public class PageViewPerfDataTests extends TestCase
{
    public void testPerf_totalPropertyWorksAsExpected()
    {
        String expected = "Test string";
        PageViewPerfData item = new PageViewPerfData();
        item.setPerfTotal(expected);
        String actual = item.getPerfTotal();
        Assert.assertEquals(expected, actual);
        
        expected = "Other string";
        item.setPerfTotal(expected);
        actual = item.getPerfTotal();
        Assert.assertEquals(expected, actual);
    }
    
    public void testNetwork_connectPropertyWorksAsExpected()
    {
        String expected = "Test string";
        PageViewPerfData item = new PageViewPerfData();
        item.setNetworkConnect(expected);
        String actual = item.getNetworkConnect();
        Assert.assertEquals(expected, actual);
        
        expected = "Other string";
        item.setNetworkConnect(expected);
        actual = item.getNetworkConnect();
        Assert.assertEquals(expected, actual);
    }
    
    public void testSent_requestPropertyWorksAsExpected()
    {
        String expected = "Test string";
        PageViewPerfData item = new PageViewPerfData();
        item.setSentRequest(expected);
        String actual = item.getSentRequest();
        Assert.assertEquals(expected, actual);
        
        expected = "Other string";
        item.setSentRequest(expected);
        actual = item.getSentRequest();
        Assert.assertEquals(expected, actual);
    }
    
    public void testReceived_responsePropertyWorksAsExpected()
    {
        String expected = "Test string";
        PageViewPerfData item = new PageViewPerfData();
        item.setReceivedResponse(expected);
        String actual = item.getReceivedResponse();
        Assert.assertEquals(expected, actual);
        
        expected = "Other string";
        item.setReceivedResponse(expected);
        actual = item.getReceivedResponse();
        Assert.assertEquals(expected, actual);
    }
    
    public void testDom_processingPropertyWorksAsExpected()
    {
        String expected = "Test string";
        PageViewPerfData item = new PageViewPerfData();
        item.setDomProcessing(expected);
        String actual = item.getDomProcessing();
        Assert.assertEquals(expected, actual);
        
        expected = "Other string";
        item.setDomProcessing(expected);
        actual = item.getDomProcessing();
        Assert.assertEquals(expected, actual);
    }
    
    public void testSerialize() throws IOException
    {
        PageViewPerfData item = new PageViewPerfData();
        item.setPerfTotal("Test string");
        item.setNetworkConnect("Test string");
        item.setSentRequest("Test string");
        item.setReceivedResponse("Test string");
        item.setDomProcessing("Test string");
        StringWriter writer = new StringWriter();
        item.serialize(writer);
        String expected = "{\"perfTotal\":\"Test string\",\"networkConnect\":\"Test string\",\"sentRequest\":\"Test string\",\"receivedResponse\":\"Test string\",\"domProcessing\":\"Test string\"}";
        Assert.assertEquals(expected, writer.toString());
    }

}
