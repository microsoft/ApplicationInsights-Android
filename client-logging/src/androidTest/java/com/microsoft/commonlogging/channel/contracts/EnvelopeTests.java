package com.microsoft.commonlogging.channel.contracts;

import junit.framework.Assert;
import junit.framework.TestCase;

import java.io.IOException;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.Map;

/// <summary>
/// Data contract test class EnvelopeTests.
/// </summary>
public class EnvelopeTests extends TestCase
{
    public void testVerPropertyWorksAsExpected()
    {
        int expected = 42;
        Envelope item = new Envelope();
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
        Envelope item = new Envelope();
        item.setName(expected);
        String actual = item.getName();
        Assert.assertEquals(expected, actual);
        
        expected = "Other string";
        item.setName(expected);
        actual = item.getName();
        Assert.assertEquals(expected, actual);
    }
    
    public void testTimePropertyWorksAsExpected()
    {
        String expected = "Test string";
        Envelope item = new Envelope();
        item.setTime(expected);
        String actual = item.getTime();
        Assert.assertEquals(expected, actual);
        
        expected = "Other string";
        item.setTime(expected);
        actual = item.getTime();
        Assert.assertEquals(expected, actual);
    }
    
    public void testSample_ratePropertyWorksAsExpected()
    {
        double expected = 1.5;
        Envelope item = new Envelope();
        item.setSampleRate(expected);
        double actual = item.getSampleRate();
        Assert.assertEquals(expected, actual);
        
        expected = 4.8;
        item.setSampleRate(expected);
        actual = item.getSampleRate();
        Assert.assertEquals(expected, actual);
    }
    
    public void testSeqPropertyWorksAsExpected()
    {
        String expected = "Test string";
        Envelope item = new Envelope();
        item.setSeq(expected);
        String actual = item.getSeq();
        Assert.assertEquals(expected, actual);
        
        expected = "Other string";
        item.setSeq(expected);
        actual = item.getSeq();
        Assert.assertEquals(expected, actual);
    }
    
    public void testI_keyPropertyWorksAsExpected()
    {
        String expected = "Test string";
        Envelope item = new Envelope();
        item.setIKey(expected);
        String actual = item.getIKey();
        Assert.assertEquals(expected, actual);
        
        expected = "Other string";
        item.setIKey(expected);
        actual = item.getIKey();
        Assert.assertEquals(expected, actual);
    }
    
    public void testFlagsPropertyWorksAsExpected()
    {
        long expected = 42;
        Envelope item = new Envelope();
        item.setFlags(expected);
        long actual = item.getFlags();
        Assert.assertEquals(expected, actual);
        
        expected = 13;
        item.setFlags(expected);
        actual = item.getFlags();
        Assert.assertEquals(expected, actual);
    }
    
    public void testDevice_idPropertyWorksAsExpected()
    {
        String expected = "Test string";
        Envelope item = new Envelope();
        item.setDeviceId(expected);
        String actual = item.getDeviceId();
        Assert.assertEquals(expected, actual);
        
        expected = "Other string";
        item.setDeviceId(expected);
        actual = item.getDeviceId();
        Assert.assertEquals(expected, actual);
    }
    
    public void testOsPropertyWorksAsExpected()
    {
        String expected = "Test string";
        Envelope item = new Envelope();
        item.setOs(expected);
        String actual = item.getOs();
        Assert.assertEquals(expected, actual);
        
        expected = "Other string";
        item.setOs(expected);
        actual = item.getOs();
        Assert.assertEquals(expected, actual);
    }
    
    public void testOs_verPropertyWorksAsExpected()
    {
        String expected = "Test string";
        Envelope item = new Envelope();
        item.setOsVer(expected);
        String actual = item.getOsVer();
        Assert.assertEquals(expected, actual);
        
        expected = "Other string";
        item.setOsVer(expected);
        actual = item.getOsVer();
        Assert.assertEquals(expected, actual);
    }
    
    public void testApp_idPropertyWorksAsExpected()
    {
        String expected = "Test string";
        Envelope item = new Envelope();
        item.setAppId(expected);
        String actual = item.getAppId();
        Assert.assertEquals(expected, actual);
        
        expected = "Other string";
        item.setAppId(expected);
        actual = item.getAppId();
        Assert.assertEquals(expected, actual);
    }
    
    public void testApp_verPropertyWorksAsExpected()
    {
        String expected = "Test string";
        Envelope item = new Envelope();
        item.setAppVer(expected);
        String actual = item.getAppVer();
        Assert.assertEquals(expected, actual);
        
        expected = "Other string";
        item.setAppVer(expected);
        actual = item.getAppVer();
        Assert.assertEquals(expected, actual);
    }
    
    public void testUser_idPropertyWorksAsExpected()
    {
        String expected = "Test string";
        Envelope item = new Envelope();
        item.setUserId(expected);
        String actual = item.getUserId();
        Assert.assertEquals(expected, actual);
        
        expected = "Other string";
        item.setUserId(expected);
        actual = item.getUserId();
        Assert.assertEquals(expected, actual);
    }
    
    public void testTagsPropertyWorksAsExpected()
    {
        Envelope item = new Envelope();
        LinkedHashMap<String, String> actual = (LinkedHashMap<String, String>)item.getTags();
        Assert.assertNotNull(actual);
    }
    
    public void testDataPropertyWorksAsExpected()
    {
        Base expected = new Base();
        Envelope item = new Envelope();
        item.setData(expected);
        Base actual = item.getData();
        Assert.assertEquals(expected, actual);
        
        expected = new Base();
        item.setData(expected);
        actual = item.getData();
        Assert.assertEquals(expected, actual);
    }
    
    public void testSerialize() throws IOException
    {
        Envelope item = new Envelope();
        item.setVer(42);
        item.setName("Test string");
        item.setTime("Test string");
        item.setSampleRate(1.5);
        item.setSeq("Test string");
        item.setIKey("Test string");
        item.setFlags(42);
        item.setDeviceId("Test string");
        item.setOs("Test string");
        item.setOsVer("Test string");
        item.setAppId("Test string");
        item.setAppVer("Test string");
        item.setUserId("Test string");
        for (Map.Entry<String, String> entry : new LinkedHashMap<String, String>() {{put("key1", "test value 1"); put("key2", "test value 2"); }}.entrySet())
        {
            item.getTags().put(entry.getKey(), entry.getValue());
        }
        item.setData(new Base());
        StringWriter writer = new StringWriter();
        item.serialize(writer);
        String expected = "{\"ver\":42,\"name\":\"Test string\",\"time\":\"Test string\",\"sampleRate\":1.5,\"seq\":\"Test string\",\"iKey\":\"Test string\",\"flags\":42,\"deviceId\":\"Test string\",\"os\":\"Test string\",\"osVer\":\"Test string\",\"appId\":\"Test string\",\"appVer\":\"Test string\",\"userId\":\"Test string\",\"tags\":{\"key1\":\"test value 1\",\"key2\":\"test value 2\"},\"data\":{}}";
        Assert.assertEquals(expected, writer.toString());
    }

}
