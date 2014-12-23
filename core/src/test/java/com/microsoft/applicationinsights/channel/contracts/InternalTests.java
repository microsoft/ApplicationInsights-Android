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
/// Data contract test class InternalTests.
/// </summary>
public class InternalTests extends TestCase
{
    public void testSdk_versionPropertyWorksAsExpected()
    {
        String expected = "Test string";
        Internal item = new Internal();
        item.setSdkVersion(expected);
        String actual = item.getSdkVersion();
        Assert.assertEquals(expected, actual);
        
        expected = "Other string";
        item.setSdkVersion(expected);
        actual = item.getSdkVersion();
        Assert.assertEquals(expected, actual);
    }
    
    public void testAgent_versionPropertyWorksAsExpected()
    {
        String expected = "Test string";
        Internal item = new Internal();
        item.setAgentVersion(expected);
        String actual = item.getAgentVersion();
        Assert.assertEquals(expected, actual);
        
        expected = "Other string";
        item.setAgentVersion(expected);
        actual = item.getAgentVersion();
        Assert.assertEquals(expected, actual);
    }
    
    public void testSerialize() throws IOException
    {
        Internal item = new Internal();
        item.setSdkVersion("Test string");
        item.setAgentVersion("Test string");
        StringWriter writer = new StringWriter();
        item.serialize(writer);
        String expected = "{\"ai.internal.sdkVersion\":\"Test string\",\"ai.internal.agentVersion\":\"Test string\"}";
        Assert.assertEquals(expected, writer.toString());
    }

}
