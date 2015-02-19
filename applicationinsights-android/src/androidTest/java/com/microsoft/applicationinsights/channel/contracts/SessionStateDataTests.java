package com.microsoft.applicationinsights.channel.contracts;

import junit.framework.Assert;
import junit.framework.TestCase;

import java.io.IOException;
import java.io.StringWriter;

/// <summary>
/// Data contract test class SessionStateDataTests.
/// </summary>
public class SessionStateDataTests extends TestCase
{
    public void testVerPropertyWorksAsExpected()
    {
        int expected = 42;
        SessionStateData item = new SessionStateData();
        item.setVer(expected);
        int actual = item.getVer();
        Assert.assertEquals(expected, actual);
        
        expected = 13;
        item.setVer(expected);
        actual = item.getVer();
        Assert.assertEquals(expected, actual);
    }
    
    public void testStatePropertyWorksAsExpected()
    {
        int expected = 5;
        SessionStateData item = new SessionStateData();
        item.setState(expected);
        int actual = item.getState();
        Assert.assertEquals(expected, actual);
        
        expected = 3;
        item.setState(expected);
        actual = item.getState();
        Assert.assertEquals(expected, actual);
    }
    
    public void testSerialize() throws IOException
    {
        SessionStateData item = new SessionStateData();
        item.setVer(42);
        item.setState(5);
        StringWriter writer = new StringWriter();
        item.serialize(writer);
        String expected = "{\"ver\":42,\"state\":5}";
        Assert.assertEquals(expected, writer.toString());
    }

}
