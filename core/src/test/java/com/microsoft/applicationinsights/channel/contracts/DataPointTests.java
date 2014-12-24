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
/// Data contract test class DataPointTests.
/// </summary>
public class DataPointTests extends TestCase
{
    public void testNamePropertyWorksAsExpected()
    {
        String expected = "Test string";
        DataPoint item = new DataPoint();
        item.setName(expected);
        String actual = item.getName();
        Assert.assertEquals(expected, actual);
        
        expected = "Other string";
        item.setName(expected);
        actual = item.getName();
        Assert.assertEquals(expected, actual);
    }
    
    public void testKindPropertyWorksAsExpected()
    {
        int expected = 5;
        DataPoint item = new DataPoint();
        item.setKind(expected);
        int actual = item.getKind();
        Assert.assertEquals(expected, actual);
        
        expected = 3;
        item.setKind(expected);
        actual = item.getKind();
        Assert.assertEquals(expected, actual);
    }
    
    public void testValuePropertyWorksAsExpected()
    {
        double expected = 1.5;
        DataPoint item = new DataPoint();
        item.setValue(expected);
        double actual = item.getValue();
        Assert.assertEquals(expected, actual);
        
        expected = 4.8;
        item.setValue(expected);
        actual = item.getValue();
        Assert.assertEquals(expected, actual);
    }
    
    public void testCountPropertyWorksAsExpected()
    {
        Integer expected = 42;
        DataPoint item = new DataPoint();
        item.setCount(expected);
        Integer actual = item.getCount();
        Assert.assertEquals(expected, actual);
        
        expected = 13;
        item.setCount(expected);
        actual = item.getCount();
        Assert.assertEquals(expected, actual);
    }
    
    public void testMinPropertyWorksAsExpected()
    {
        Double expected = 1.5;
        DataPoint item = new DataPoint();
        item.setMin(expected);
        Double actual = item.getMin();
        Assert.assertEquals(expected, actual);
        
        expected = 4.8;
        item.setMin(expected);
        actual = item.getMin();
        Assert.assertEquals(expected, actual);
    }
    
    public void testMaxPropertyWorksAsExpected()
    {
        Double expected = 1.5;
        DataPoint item = new DataPoint();
        item.setMax(expected);
        Double actual = item.getMax();
        Assert.assertEquals(expected, actual);
        
        expected = 4.8;
        item.setMax(expected);
        actual = item.getMax();
        Assert.assertEquals(expected, actual);
    }
    
    public void testStd_devPropertyWorksAsExpected()
    {
        Double expected = 1.5;
        DataPoint item = new DataPoint();
        item.setStdDev(expected);
        Double actual = item.getStdDev();
        Assert.assertEquals(expected, actual);
        
        expected = 4.8;
        item.setStdDev(expected);
        actual = item.getStdDev();
        Assert.assertEquals(expected, actual);
    }
    
    public void testSerialize() throws IOException
    {
        DataPoint item = new DataPoint();
        item.setName("Test string");
        item.setKind(5);
        item.setValue(1.5);
        item.setCount(42);
        item.setMin(1.5);
        item.setMax(1.5);
        item.setStdDev(1.5);
        StringWriter writer = new StringWriter();
        item.serialize(writer);
        String expected = "{\"name\":\"Test string\",\"kind\":5,\"value\":1.5,\"count\":42,\"min\":1.5,\"max\":1.5,\"stdDev\":1.5}";
        Assert.assertEquals(expected, writer.toString());
    }

}
