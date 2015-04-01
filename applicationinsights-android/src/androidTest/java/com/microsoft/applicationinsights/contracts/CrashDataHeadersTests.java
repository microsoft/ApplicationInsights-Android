package com.microsoft.applicationinsights.contracts;

import junit.framework.Assert;
import junit.framework.TestCase;

import java.io.IOException;
import java.io.StringWriter;

/// <summary>
/// Data contract test class CrashDataHeadersTests.
/// </summary>
public class CrashDataHeadersTests extends TestCase
{
    public void testIdPropertyWorksAsExpected()
    {
        String expected = "Test string";
        CrashDataHeaders item = new CrashDataHeaders();
        item.setId(expected);
        String actual = item.getId();
        Assert.assertEquals(expected, actual);
        
        expected = "Other string";
        item.setId(expected);
        actual = item.getId();
        Assert.assertEquals(expected, actual);
    }
    
    public void testProcessPropertyWorksAsExpected()
    {
        String expected = "Test string";
        CrashDataHeaders item = new CrashDataHeaders();
        item.setProcess(expected);
        String actual = item.getProcess();
        Assert.assertEquals(expected, actual);
        
        expected = "Other string";
        item.setProcess(expected);
        actual = item.getProcess();
        Assert.assertEquals(expected, actual);
    }
    
    public void testProcess_idPropertyWorksAsExpected()
    {
        int expected = 42;
        CrashDataHeaders item = new CrashDataHeaders();
        item.setProcessId(expected);
        int actual = item.getProcessId();
        Assert.assertEquals(expected, actual);
        
        expected = 13;
        item.setProcessId(expected);
        actual = item.getProcessId();
        Assert.assertEquals(expected, actual);
    }
    
    public void testParent_processPropertyWorksAsExpected()
    {
        String expected = "Test string";
        CrashDataHeaders item = new CrashDataHeaders();
        item.setParentProcess(expected);
        String actual = item.getParentProcess();
        Assert.assertEquals(expected, actual);
        
        expected = "Other string";
        item.setParentProcess(expected);
        actual = item.getParentProcess();
        Assert.assertEquals(expected, actual);
    }
    
    public void testParent_process_idPropertyWorksAsExpected()
    {
        int expected = 42;
        CrashDataHeaders item = new CrashDataHeaders();
        item.setParentProcessId(expected);
        int actual = item.getParentProcessId();
        Assert.assertEquals(expected, actual);
        
        expected = 13;
        item.setParentProcessId(expected);
        actual = item.getParentProcessId();
        Assert.assertEquals(expected, actual);
    }
    
    public void testCrash_threadPropertyWorksAsExpected()
    {
        int expected = 42;
        CrashDataHeaders item = new CrashDataHeaders();
        item.setCrashThread(expected);
        int actual = item.getCrashThread();
        Assert.assertEquals(expected, actual);
        
        expected = 13;
        item.setCrashThread(expected);
        actual = item.getCrashThread();
        Assert.assertEquals(expected, actual);
    }
    
    public void testApplication_pathPropertyWorksAsExpected()
    {
        String expected = "Test string";
        CrashDataHeaders item = new CrashDataHeaders();
        item.setApplicationPath(expected);
        String actual = item.getApplicationPath();
        Assert.assertEquals(expected, actual);
        
        expected = "Other string";
        item.setApplicationPath(expected);
        actual = item.getApplicationPath();
        Assert.assertEquals(expected, actual);
    }
    
    public void testApplication_identifierPropertyWorksAsExpected()
    {
        String expected = "Test string";
        CrashDataHeaders item = new CrashDataHeaders();
        item.setApplicationIdentifier(expected);
        String actual = item.getApplicationIdentifier();
        Assert.assertEquals(expected, actual);
        
        expected = "Other string";
        item.setApplicationIdentifier(expected);
        actual = item.getApplicationIdentifier();
        Assert.assertEquals(expected, actual);
    }
    
    public void testApplication_buildPropertyWorksAsExpected()
    {
        String expected = "Test string";
        CrashDataHeaders item = new CrashDataHeaders();
        item.setApplicationBuild(expected);
        String actual = item.getApplicationBuild();
        Assert.assertEquals(expected, actual);
        
        expected = "Other string";
        item.setApplicationBuild(expected);
        actual = item.getApplicationBuild();
        Assert.assertEquals(expected, actual);
    }
    
    public void testException_typePropertyWorksAsExpected()
    {
        String expected = "Test string";
        CrashDataHeaders item = new CrashDataHeaders();
        item.setExceptionType(expected);
        String actual = item.getExceptionType();
        Assert.assertEquals(expected, actual);
        
        expected = "Other string";
        item.setExceptionType(expected);
        actual = item.getExceptionType();
        Assert.assertEquals(expected, actual);
    }
    
    public void testException_codePropertyWorksAsExpected()
    {
        String expected = "Test string";
        CrashDataHeaders item = new CrashDataHeaders();
        item.setExceptionCode(expected);
        String actual = item.getExceptionCode();
        Assert.assertEquals(expected, actual);
        
        expected = "Other string";
        item.setExceptionCode(expected);
        actual = item.getExceptionCode();
        Assert.assertEquals(expected, actual);
    }
    
    public void testException_addressPropertyWorksAsExpected()
    {
        String expected = "Test string";
        CrashDataHeaders item = new CrashDataHeaders();
        item.setExceptionAddress(expected);
        String actual = item.getExceptionAddress();
        Assert.assertEquals(expected, actual);
        
        expected = "Other string";
        item.setExceptionAddress(expected);
        actual = item.getExceptionAddress();
        Assert.assertEquals(expected, actual);
    }
    
    public void testException_reasonPropertyWorksAsExpected()
    {
        String expected = "Test string";
        CrashDataHeaders item = new CrashDataHeaders();
        item.setExceptionReason(expected);
        String actual = item.getExceptionReason();
        Assert.assertEquals(expected, actual);
        
        expected = "Other string";
        item.setExceptionReason(expected);
        actual = item.getExceptionReason();
        Assert.assertEquals(expected, actual);
    }
    
    public void testSerialize() throws IOException
    {
        CrashDataHeaders item = new CrashDataHeaders();
        item.setId("Test string");
        item.setProcess("Test string");
        item.setProcessId(42);
        item.setParentProcess("Test string");
        item.setParentProcessId(42);
        item.setCrashThread(42);
        item.setApplicationPath("Test string");
        item.setApplicationIdentifier("Test string");
        item.setApplicationBuild("Test string");
        item.setExceptionType("Test string");
        item.setExceptionCode("Test string");
        item.setExceptionAddress("Test string");
        item.setExceptionReason("Test string");
        StringWriter writer = new StringWriter();
        item.serialize(writer);
        String expected = "{\"id\":\"Test string\",\"process\":\"Test string\",\"processId\":42,\"parentProcess\":\"Test string\",\"parentProcessId\":42,\"crashThread\":42,\"applicationPath\":\"Test string\",\"applicationIdentifier\":\"Test string\",\"applicationBuild\":\"Test string\",\"exceptionType\":\"Test string\",\"exceptionCode\":\"Test string\",\"exceptionAddress\":\"Test string\",\"exceptionReason\":\"Test string\"}";
        Assert.assertEquals(expected, writer.toString());
    }

}
