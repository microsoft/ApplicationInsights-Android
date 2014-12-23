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
/// Data contract test class LocationTests.
/// </summary>
public class LocationTests extends TestCase
{
    public void testIpPropertyWorksAsExpected()
    {
        String expected = "Test string";
        Location item = new Location();
        item.setIp(expected);
        String actual = item.getIp();
        Assert.assertEquals(expected, actual);
        
        expected = "Other string";
        item.setIp(expected);
        actual = item.getIp();
        Assert.assertEquals(expected, actual);
    }
    
    public void testSerialize() throws IOException
    {
        Location item = new Location();
        item.setIp("Test string");
        StringWriter writer = new StringWriter();
        item.serialize(writer);
        String expected = "{\"ai.location.ip\":\"Test string\"}";
        Assert.assertEquals(expected, writer.toString());
    }

}
