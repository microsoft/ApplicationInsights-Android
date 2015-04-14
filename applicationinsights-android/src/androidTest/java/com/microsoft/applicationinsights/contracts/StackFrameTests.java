package com.microsoft.applicationinsights.contracts;

import junit.framework.Assert;
import junit.framework.TestCase;

import java.io.IOException;
import java.io.StringWriter;

/// <summary>
/// Data contract test class StackFrameTests.
/// </summary>
public class StackFrameTests extends TestCase
{
    public void testLevelPropertyWorksAsExpected()
    {
        int expected = 42;
        StackFrame item = new StackFrame();
        item.setLevel(expected);
        int actual = item.getLevel();
        Assert.assertEquals(expected, actual);
        
        expected = 13;
        item.setLevel(expected);
        actual = item.getLevel();
        Assert.assertEquals(expected, actual);
    }
    
    public void testMethodPropertyWorksAsExpected()
    {
        String expected = "Test string";
        StackFrame item = new StackFrame();
        item.setMethod(expected);
        String actual = item.getMethod();
        Assert.assertEquals(expected, actual);
        
        expected = "Other string";
        item.setMethod(expected);
        actual = item.getMethod();
        Assert.assertEquals(expected, actual);
    }
    
    public void testAssemblyPropertyWorksAsExpected()
    {
        String expected = "Test string";
        StackFrame item = new StackFrame();
        item.setAssembly(expected);
        String actual = item.getAssembly();
        Assert.assertEquals(expected, actual);
        
        expected = "Other string";
        item.setAssembly(expected);
        actual = item.getAssembly();
        Assert.assertEquals(expected, actual);
    }
    
    public void testFile_namePropertyWorksAsExpected()
    {
        String expected = "Test string";
        StackFrame item = new StackFrame();
        item.setFileName(expected);
        String actual = item.getFileName();
        Assert.assertEquals(expected, actual);
        
        expected = "Other string";
        item.setFileName(expected);
        actual = item.getFileName();
        Assert.assertEquals(expected, actual);
    }
    
    public void testLinePropertyWorksAsExpected()
    {
        int expected = 42;
        StackFrame item = new StackFrame();
        item.setLine(expected);
        int actual = item.getLine();
        Assert.assertEquals(expected, actual);
        
        expected = 13;
        item.setLine(expected);
        actual = item.getLine();
        Assert.assertEquals(expected, actual);
    }
    
    public void testSerialize() throws IOException
    {
        StackFrame item = new StackFrame();
        item.setLevel(42);
        item.setMethod("Test string");
        item.setAssembly("Test string");
        item.setFileName("Test string");
        item.setLine(42);
        StringWriter writer = new StringWriter();
        item.serialize(writer);
        String expected = "{\"level\":42,\"method\":\"Test string\",\"assembly\":\"Test string\",\"fileName\":\"Test string\",\"line\":42}";
        Assert.assertEquals(expected, writer.toString());
    }

}
