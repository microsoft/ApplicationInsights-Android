package com.microsoft.applicationinsights.channel.contracts;

import junit.framework.Assert;
import junit.framework.TestCase;

import java.io.IOException;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.Map;

/// <summary>
/// Data contract test class MessageDataTests.
/// </summary>
public class MessageDataTests extends TestCase
{
    public void testVerPropertyWorksAsExpected()
    {
        int expected = 42;
        MessageData item = new MessageData();
        item.setVer(expected);
        int actual = item.getVer();
        Assert.assertEquals(expected, actual);
        
        expected = 13;
        item.setVer(expected);
        actual = item.getVer();
        Assert.assertEquals(expected, actual);
    }
    
    public void testMessagePropertyWorksAsExpected()
    {
        String expected = "Test string";
        MessageData item = new MessageData();
        item.setMessage(expected);
        String actual = item.getMessage();
        Assert.assertEquals(expected, actual);
        
        expected = "Other string";
        item.setMessage(expected);
        actual = item.getMessage();
        Assert.assertEquals(expected, actual);
    }
    
    public void testSeverity_levelPropertyWorksAsExpected()
    {
        int expected = 5;
        MessageData item = new MessageData();
        item.setSeverityLevel(expected);
        int actual = item.getSeverityLevel();
        Assert.assertEquals(expected, actual);
        
        expected = 3;
        item.setSeverityLevel(expected);
        actual = item.getSeverityLevel();
        Assert.assertEquals(expected, actual);
    }
    
    public void testPropertiesPropertyWorksAsExpected()
    {
        MessageData item = new MessageData();
        LinkedHashMap<String, String> actual = (LinkedHashMap<String, String>)item.getProperties();
        Assert.assertNotNull(actual);
    }
    
    public void testSerialize() throws IOException
    {
        MessageData item = new MessageData();
        item.setVer(42);
        item.setMessage("Test string");
        item.setSeverityLevel(5);
        for (Map.Entry<String, String> entry : new LinkedHashMap<String, String>() {{put("key1", "test value 1"); put("key2", "test value 2"); }}.entrySet())
        {
            item.getProperties().put(entry.getKey(), entry.getValue());
        }
        StringWriter writer = new StringWriter();
        item.serialize(writer);
        String expected = "{\"ver\":42,\"message\":\"Test string\",\"severityLevel\":5,\"properties\":{\"key1\":\"test value 1\",\"key2\":\"test value 2\"}}";
        Assert.assertEquals(expected, writer.toString());
    }

}
