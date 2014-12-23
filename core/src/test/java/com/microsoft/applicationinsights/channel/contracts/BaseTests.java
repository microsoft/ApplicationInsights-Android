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
/// Data contract test class BaseTests.
/// </summary>
public class BaseTests extends TestCase
{
    public void testBase_typePropertyWorksAsExpected()
    {
        String expected = "Test string";
        Base item = new Base();
        item.setBaseType(expected);
        String actual = item.getBaseType();
        Assert.assertEquals(expected, actual);
        
        expected = "Other string";
        item.setBaseType(expected);
        actual = item.getBaseType();
        Assert.assertEquals(expected, actual);
    }
    
    public void testSerialize() throws IOException
    {
        Base item = new Base();
        item.setBaseType("Test string");
        StringWriter writer = new StringWriter();
        item.serialize(writer);
        String expected = "{\"baseType\":\"Test string\"}";
        Assert.assertEquals(expected, writer.toString());
    }

}
