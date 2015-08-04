package com.microsoft.applicationinsights.contracts;

import com.microsoft.telemetry.Data;
import com.microsoft.telemetry.Domain;

import junit.framework.Assert;
import junit.framework.TestCase;

import java.io.IOException;
import java.io.StringWriter;

/// <summary>
/// Data contract test class DataTests.
/// </summary>
public class DataTests extends TestCase
{
    public void testBase_dataPropertyWorksAsExpected()
    {
        ITelemetry expected = new EventData();
        Data item = new Data();
        item.setBaseData(expected);
        Domain actual = item.getBaseData();
        Assert.assertEquals(expected, actual);
        
        expected = new EventData();
        item.setBaseData(expected);
        actual = item.getBaseData();
        Assert.assertEquals(expected, actual);
    }
    
    public void testSerialize() throws IOException
    {
        Data item = new Data();
        item.setBaseData(new EventData());
        StringWriter writer = new StringWriter();
        item.serialize(writer);
        String expected = "{\"baseData\":{\"ver\":2,\"name\":null}}";
        Assert.assertEquals(expected, writer.toString());
    }

}
