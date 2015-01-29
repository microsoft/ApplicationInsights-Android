package com.microsoft.applicationinsights.channel.contracts;

import junit.framework.Assert;
import junit.framework.TestCase;

import java.io.IOException;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.Map;

/// <summary>
/// Data contract test class RemoteDependencyDataTests.
/// </summary>
public class RemoteDependencyDataTests extends TestCase
{
    public void testVerPropertyWorksAsExpected()
    {
        int expected = 42;
        RemoteDependencyData item = new RemoteDependencyData();
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
        RemoteDependencyData item = new RemoteDependencyData();
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
        RemoteDependencyData item = new RemoteDependencyData();
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
        RemoteDependencyData item = new RemoteDependencyData();
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
        RemoteDependencyData item = new RemoteDependencyData();
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
        RemoteDependencyData item = new RemoteDependencyData();
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
        RemoteDependencyData item = new RemoteDependencyData();
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
        RemoteDependencyData item = new RemoteDependencyData();
        item.setStdDev(expected);
        Double actual = item.getStdDev();
        Assert.assertEquals(expected, actual);
        
        expected = 4.8;
        item.setStdDev(expected);
        actual = item.getStdDev();
        Assert.assertEquals(expected, actual);
    }
    
    public void testDependency_kindPropertyWorksAsExpected()
    {
        int expected = 5;
        RemoteDependencyData item = new RemoteDependencyData();
        item.setDependencyKind(expected);
        int actual = item.getDependencyKind();
        Assert.assertEquals(expected, actual);
        
        expected = 3;
        item.setDependencyKind(expected);
        actual = item.getDependencyKind();
        Assert.assertEquals(expected, actual);
    }
    
    public void testSuccessPropertyWorksAsExpected()
    {
        Boolean expected = true;
        RemoteDependencyData item = new RemoteDependencyData();
        item.setSuccess(expected);
        Boolean actual = item.getSuccess();
        Assert.assertEquals(expected, actual);
        
        expected = false;
        item.setSuccess(expected);
        actual = item.getSuccess();
        Assert.assertEquals(expected, actual);
    }
    
    public void testAsyncPropertyWorksAsExpected()
    {
        Boolean expected = true;
        RemoteDependencyData item = new RemoteDependencyData();
        item.setAsync(expected);
        Boolean actual = item.getAsync();
        Assert.assertEquals(expected, actual);
        
        expected = false;
        item.setAsync(expected);
        actual = item.getAsync();
        Assert.assertEquals(expected, actual);
    }
    
    public void testDependency_sourcePropertyWorksAsExpected()
    {
        int expected = 5;
        RemoteDependencyData item = new RemoteDependencyData();
        item.setDependencySource(expected);
        int actual = item.getDependencySource();
        Assert.assertEquals(expected, actual);
        
        expected = 3;
        item.setDependencySource(expected);
        actual = item.getDependencySource();
        Assert.assertEquals(expected, actual);
    }
    
    public void testPropertiesPropertyWorksAsExpected()
    {
        RemoteDependencyData item = new RemoteDependencyData();
        LinkedHashMap<String, String> actual = (LinkedHashMap<String, String>)item.getProperties();
        Assert.assertNotNull(actual);
    }
    
    public void testSerialize() throws IOException
    {
        RemoteDependencyData item = new RemoteDependencyData();
        item.setVer(42);
        item.setName("Test string");
        item.setKind(5);
        item.setValue(1.5);
        item.setCount(42);
        item.setMin(1.5);
        item.setMax(1.5);
        item.setStdDev(1.5);
        item.setDependencyKind(5);
        item.setSuccess(true);
        item.setAsync(true);
        item.setDependencySource(5);
        for (Map.Entry<String, String> entry : new LinkedHashMap<String, String>() {{put("key1", "test value 1"); put("key2", "test value 2"); }}.entrySet())
        {
            item.getProperties().put(entry.getKey(), entry.getValue());
        }
        StringWriter writer = new StringWriter();
        item.serialize(writer);
        String expected = "{\"ver\":42,\"name\":\"Test string\",\"kind\":5,\"value\":1.5,\"count\":42,\"min\":1.5,\"max\":1.5,\"stdDev\":1.5,\"dependencyKind\":5,\"success\":true,\"async\":true,\"dependencySource\":5,\"properties\":{\"key1\":\"test value 1\",\"key2\":\"test value 2\"}}";
        Assert.assertEquals(expected, writer.toString());
    }

}
