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
/// Data contract test class CrashDataThreadFrameTests.
/// </summary>
public class CrashDataThreadFrameTests extends TestCase
{
    public void testAddressPropertyWorksAsExpected()
    {
        String expected = "Test string";
        CrashDataThreadFrame item = new CrashDataThreadFrame();
        item.setAddress(expected);
        String actual = item.getAddress();
        Assert.assertEquals(expected, actual);
        
        expected = "Other string";
        item.setAddress(expected);
        actual = item.getAddress();
        Assert.assertEquals(expected, actual);
    }
    
    public void testSymbolPropertyWorksAsExpected()
    {
        String expected = "Test string";
        CrashDataThreadFrame item = new CrashDataThreadFrame();
        item.setSymbol(expected);
        String actual = item.getSymbol();
        Assert.assertEquals(expected, actual);
        
        expected = "Other string";
        item.setSymbol(expected);
        actual = item.getSymbol();
        Assert.assertEquals(expected, actual);
    }
    
    public void testRegistersPropertyWorksAsExpected()
    {
        CrashDataThreadFrame item = new CrashDataThreadFrame();
        LinkedHashMap<String, String> actual = (LinkedHashMap<String, String>)item.getRegisters();
        Assert.assertNotNull(actual);
    }
    
    public void testSerialize() throws IOException
    {
        CrashDataThreadFrame item = new CrashDataThreadFrame();
        item.setAddress("Test string");
        item.setSymbol("Test string");
        for (Map.Entry<String, String> entry : new LinkedHashMap<String, String>() {{put("key1", "test value 1"); put("key2", "test value 2"); }}.entrySet())
        {
            item.getRegisters().put(entry.getKey(), entry.getValue());
        }
        StringWriter writer = new StringWriter();
        item.serialize(writer);
        String expected = "{\"address\":\"Test string\",\"symbol\":\"Test string\",\"registers\":{\"key1\":\"test value 1\",\"key2\":\"test value 2\"}}";
        Assert.assertEquals(expected, writer.toString());
    }

}
