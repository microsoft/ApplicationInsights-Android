package com.microsoft.applicationinsights.channel.contracts;

import junit.framework.Assert;
import junit.framework.TestCase;

import java.io.IOException;
import java.io.StringWriter;

/// <summary>
/// Data contract test class CrashDataBinaryTests.
/// </summary>
public class CrashDataBinaryTests extends TestCase
{
    public void testStart_addressPropertyWorksAsExpected()
    {
        String expected = "Test string";
        CrashDataBinary item = new CrashDataBinary();
        item.setStartAddress(expected);
        String actual = item.getStartAddress();
        Assert.assertEquals(expected, actual);
        
        expected = "Other string";
        item.setStartAddress(expected);
        actual = item.getStartAddress();
        Assert.assertEquals(expected, actual);
    }
    
    public void testEnd_addressPropertyWorksAsExpected()
    {
        String expected = "Test string";
        CrashDataBinary item = new CrashDataBinary();
        item.setEndAddress(expected);
        String actual = item.getEndAddress();
        Assert.assertEquals(expected, actual);
        
        expected = "Other string";
        item.setEndAddress(expected);
        actual = item.getEndAddress();
        Assert.assertEquals(expected, actual);
    }
    
    public void testNamePropertyWorksAsExpected()
    {
        String expected = "Test string";
        CrashDataBinary item = new CrashDataBinary();
        item.setName(expected);
        String actual = item.getName();
        Assert.assertEquals(expected, actual);
        
        expected = "Other string";
        item.setName(expected);
        actual = item.getName();
        Assert.assertEquals(expected, actual);
    }
    
    public void testCpu_typePropertyWorksAsExpected()
    {
        long expected = 42;
        CrashDataBinary item = new CrashDataBinary();
        item.setCpuType(expected);
        long actual = item.getCpuType();
        Assert.assertEquals(expected, actual);
        
        expected = 13;
        item.setCpuType(expected);
        actual = item.getCpuType();
        Assert.assertEquals(expected, actual);
    }
    
    public void testCpu_sub_typePropertyWorksAsExpected()
    {
        long expected = 42;
        CrashDataBinary item = new CrashDataBinary();
        item.setCpuSubType(expected);
        long actual = item.getCpuSubType();
        Assert.assertEquals(expected, actual);
        
        expected = 13;
        item.setCpuSubType(expected);
        actual = item.getCpuSubType();
        Assert.assertEquals(expected, actual);
    }
    
    public void testUuidPropertyWorksAsExpected()
    {
        String expected = "Test string";
        CrashDataBinary item = new CrashDataBinary();
        item.setUuid(expected);
        String actual = item.getUuid();
        Assert.assertEquals(expected, actual);
        
        expected = "Other string";
        item.setUuid(expected);
        actual = item.getUuid();
        Assert.assertEquals(expected, actual);
    }
    
    public void testPathPropertyWorksAsExpected()
    {
        String expected = "Test string";
        CrashDataBinary item = new CrashDataBinary();
        item.setPath(expected);
        String actual = item.getPath();
        Assert.assertEquals(expected, actual);
        
        expected = "Other string";
        item.setPath(expected);
        actual = item.getPath();
        Assert.assertEquals(expected, actual);
    }
    
    public void testSerialize() throws IOException
    {
        CrashDataBinary item = new CrashDataBinary();
        item.setStartAddress("Test string");
        item.setEndAddress("Test string");
        item.setName("Test string");
        item.setCpuType(42);
        item.setCpuSubType(42);
        item.setUuid("Test string");
        item.setPath("Test string");
        StringWriter writer = new StringWriter();
        item.serialize(writer);
        String expected = "{\"startAddress\":\"Test string\",\"endAddress\":\"Test string\",\"name\":\"Test string\",\"cpuType\":42,\"cpuSubType\":42,\"uuid\":\"Test string\",\"path\":\"Test string\"}";
        Assert.assertEquals(expected, writer.toString());
    }

}
