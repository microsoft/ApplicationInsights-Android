package com.microsoft.applicationinsights.contracts;

import junit.framework.Assert;
import junit.framework.TestCase;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/// <summary>
/// Data contract test class MetricDataTests.
/// </summary>
public class MetricDataTests extends TestCase
{
    public void testVerPropertyWorksAsExpected()
    {
        int expected = 42;
        MetricData item = new MetricData();
        item.setVer(expected);
        int actual = item.getVer();
        Assert.assertEquals(expected, actual);
        
        expected = 13;
        item.setVer(expected);
        actual = item.getVer();
        Assert.assertEquals(expected, actual);
    }
    
    public void testMetricsPropertyWorksAsExpected()
    {
        MetricData item = new MetricData();
        ArrayList<DataPoint> actual = (ArrayList<DataPoint>)item.getMetrics();
        Assert.assertNotNull(actual);
    }
    
    public void testPropertiesPropertyWorksAsExpected()
    {
        MetricData item = new MetricData();
        LinkedHashMap<String, String> actual = (LinkedHashMap<String, String>)item.getProperties();
        Assert.assertNotNull(actual);
    }
    
    public void testSerialize() throws IOException
    {
        MetricData item = new MetricData();
        item.setVer(42);
        for (DataPoint entry : new ArrayList<DataPoint>() {{add(new DataPoint());}})
        {
            item.getMetrics().add(entry);
        }
        for (Map.Entry<String, String> entry : new LinkedHashMap<String, String>() {{put("key1", "test value 1"); put("key2", "test value 2"); }}.entrySet())
        {
            item.getProperties().put(entry.getKey(), entry.getValue());
        }
        StringWriter writer = new StringWriter();
        item.serialize(writer);
        String expected = "{\"ver\":42,\"metrics\":[{\"name\":null,\"value\":0.0}],\"properties\":{\"key1\":\"test value 1\",\"key2\":\"test value 2\"}}";
        Assert.assertEquals(expected, writer.toString());
    }

}
