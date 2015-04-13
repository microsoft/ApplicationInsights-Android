package com.microsoft.applicationinsights.contracts;

import junit.framework.Assert;
import junit.framework.TestCase;

import java.io.IOException;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.Map;

/// <summary>
/// Data contract test class RequestDataTests.
/// </summary>
public class RequestDataTests extends TestCase
{
    public void testVerPropertyWorksAsExpected()
    {
        int expected = 42;
        RequestData item = new RequestData();
        item.setVer(expected);
        int actual = item.getVer();
        Assert.assertEquals(expected, actual);
        
        expected = 13;
        item.setVer(expected);
        actual = item.getVer();
        Assert.assertEquals(expected, actual);
    }
    
    public void testIdPropertyWorksAsExpected()
    {
        String expected = "Test string";
        RequestData item = new RequestData();
        item.setId(expected);
        String actual = item.getId();
        Assert.assertEquals(expected, actual);
        
        expected = "Other string";
        item.setId(expected);
        actual = item.getId();
        Assert.assertEquals(expected, actual);
    }
    
    public void testNamePropertyWorksAsExpected()
    {
        String expected = "Test string";
        RequestData item = new RequestData();
        item.setName(expected);
        String actual = item.getName();
        Assert.assertEquals(expected, actual);
        
        expected = "Other string";
        item.setName(expected);
        actual = item.getName();
        Assert.assertEquals(expected, actual);
    }
    
    public void testStart_timePropertyWorksAsExpected()
    {
        String expected = "Test string";
        RequestData item = new RequestData();
        item.setStartTime(expected);
        String actual = item.getStartTime();
        Assert.assertEquals(expected, actual);
        
        expected = "Other string";
        item.setStartTime(expected);
        actual = item.getStartTime();
        Assert.assertEquals(expected, actual);
    }
    
    public void testDurationPropertyWorksAsExpected()
    {
        String expected = "Test string";
        RequestData item = new RequestData();
        item.setDuration(expected);
        String actual = item.getDuration();
        Assert.assertEquals(expected, actual);
        
        expected = "Other string";
        item.setDuration(expected);
        actual = item.getDuration();
        Assert.assertEquals(expected, actual);
    }
    
    public void testResponse_codePropertyWorksAsExpected()
    {
        String expected = "Test string";
        RequestData item = new RequestData();
        item.setResponseCode(expected);
        String actual = item.getResponseCode();
        Assert.assertEquals(expected, actual);
        
        expected = "Other string";
        item.setResponseCode(expected);
        actual = item.getResponseCode();
        Assert.assertEquals(expected, actual);
    }
    
    public void testSuccessPropertyWorksAsExpected()
    {
        boolean expected = true;
        RequestData item = new RequestData();
        item.setSuccess(expected);
        boolean actual = item.getSuccess();
        Assert.assertEquals(expected, actual);
        
        expected = false;
        item.setSuccess(expected);
        actual = item.getSuccess();
        Assert.assertEquals(expected, actual);
    }
    
    public void testHttp_methodPropertyWorksAsExpected()
    {
        String expected = "Test string";
        RequestData item = new RequestData();
        item.setHttpMethod(expected);
        String actual = item.getHttpMethod();
        Assert.assertEquals(expected, actual);
        
        expected = "Other string";
        item.setHttpMethod(expected);
        actual = item.getHttpMethod();
        Assert.assertEquals(expected, actual);
    }
    
    public void testUrlPropertyWorksAsExpected()
    {
        String expected = "Test string";
        RequestData item = new RequestData();
        item.setUrl(expected);
        String actual = item.getUrl();
        Assert.assertEquals(expected, actual);
        
        expected = "Other string";
        item.setUrl(expected);
        actual = item.getUrl();
        Assert.assertEquals(expected, actual);
    }
    
    public void testPropertiesPropertyWorksAsExpected()
    {
        RequestData item = new RequestData();
        LinkedHashMap<String, String> actual = (LinkedHashMap<String, String>)item.getProperties();
        Assert.assertNotNull(actual);
    }
    
    public void testMeasurementsPropertyWorksAsExpected()
    {
        RequestData item = new RequestData();
        LinkedHashMap<String, Double> actual = (LinkedHashMap<String, Double>)item.getMeasurements();
        Assert.assertNotNull(actual);
    }
    
    public void testSerialize() throws IOException
    {
        RequestData item = new RequestData();
        item.setVer(42);
        item.setId("Test string");
        item.setName("Test string");
        item.setStartTime("Test string");
        item.setDuration("Test string");
        item.setResponseCode("Test string");
        item.setSuccess(true);
        item.setHttpMethod("Test string");
        item.setUrl("Test string");
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
        String expected = "{\"ver\":42,\"id\":\"Test string\",\"name\":\"Test string\",\"startTime\":\"Test string\",\"duration\":\"Test string\",\"responseCode\":\"Test string\",\"success\":true,\"httpMethod\":\"Test string\",\"url\":\"Test string\",\"properties\":{\"key1\":\"test value 1\",\"key2\":\"test value 2\"},\"measurements\":{\"key1\":3.1415,\"key2\":42.2}}";
        Assert.assertEquals(expected, writer.toString());
    }

}
