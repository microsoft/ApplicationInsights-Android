package com.microsoft.applicationinsights.contracts;

import junit.framework.Assert;
import junit.framework.TestCase;

import java.io.IOException;
import java.io.StringWriter;

/// <summary>
/// Data contract test class PageViewDataTests.
/// </summary>
public class PageViewDataTests extends TestCase
{
    public void testUrlPropertyWorksAsExpected()
    {
        String expected = "Test string";
        PageViewData item = new PageViewData();
        item.setUrl(expected);
        String actual = item.getUrl();
        Assert.assertEquals(expected, actual);
        
        expected = "Other string";
        item.setUrl(expected);
        actual = item.getUrl();
        Assert.assertEquals(expected, actual);
    }
    
    public void testDurationPropertyWorksAsExpected()
    {
        String expected = "Test string";
        PageViewData item = new PageViewData();
        item.setDuration(expected);
        String actual = item.getDuration();
        Assert.assertEquals(expected, actual);
        
        expected = "Other string";
        item.setDuration(expected);
        actual = item.getDuration();
        Assert.assertEquals(expected, actual);
    }
    
    public void testSerialize() throws IOException
    {
        PageViewData item = new PageViewData();
        item.setUrl("Test string");
        item.setDuration("Test string");
        StringWriter writer = new StringWriter();
        item.serialize(writer);
        String expected = "{\"ver\":2,\"name\":null,\"url\":\"Test string\",\"duration\":\"Test string\"}";
        Assert.assertEquals(expected, writer.toString());
    }

}
