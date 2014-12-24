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
/// Data contract test class EventDataTests.
/// </summary>
public class EventDataTests extends TestCase
{
    public void testVerPropertyWorksAsExpected()
    {
        int expected = 42;
        EventData item = new EventData();
        item.setVer(expected);
        int actual = item.getVer();
        Assert.assertEquals(expected, actual);
        
        expected = 13;
        item.setVer(expected);
        actual = item.getVer();
        Assert.assertEquals(expected, actual);
    }
    
    public void testNamePropertyWorksAsExpected()
    {
        String expected = "Test string";
        EventData item = new EventData();
        item.setName(expected);
        String actual = item.getName();
        Assert.assertEquals(expected, actual);
        
        expected = "Other string";
        item.setName(expected);
        actual = item.getName();
        Assert.assertEquals(expected, actual);
    }
    
    public void testPropertiesPropertyWorksAsExpected()
    {
        EventData item = new EventData();
        LinkedHashMap<String, String> actual = (LinkedHashMap<String, String>)item.getProperties();
        Assert.assertNotNull(actual);
    }
    
    public void testMeasurementsPropertyWorksAsExpected()
    {
        EventData item = new EventData();
        LinkedHashMap<String, Double> actual = (LinkedHashMap<String, Double>)item.getMeasurements();
        Assert.assertNotNull(actual);
    }
    
    public void testSerialize() throws IOException
    {
        EventData item = new EventData();
        item.setVer(42);
        item.setName("Test string");
        for (Map.Entry<String, String> entry : new LinkedHashMap<String, String>() {{put("key1", "test value 1"); put("key2", "test value 2"); }}.entrySet())
        {
            item.getProperties().put(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<String, Double> entry : new LinkedHashMap<String, Double>() {{put("key1", 3.1415); put("key2", 42.2); }}.entrySet())
        {
            item.getMeasurements().put(entry.getKey(), entry.getValue());
        }
        StringWriter writer = new StringWriter();
        item.serialize(writer);
        String expected = "{\"ver\":42,\"name\":\"Test string\",\"properties\":{\"key1\":\"test value 1\",\"key2\":\"test value 2\"},\"measurements\":{\"key1\":3.1415,\"key2\":42.2}}";
        Assert.assertEquals(expected, writer.toString());
    }

}
