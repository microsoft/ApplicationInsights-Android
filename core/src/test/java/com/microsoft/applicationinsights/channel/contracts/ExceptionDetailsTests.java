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
/// Data contract test class ExceptionDetailsTests.
/// </summary>
public class ExceptionDetailsTests extends TestCase
{
    public void testIdPropertyWorksAsExpected()
    {
        int expected = 42;
        ExceptionDetails item = new ExceptionDetails();
        item.setId(expected);
        int actual = item.getId();
        Assert.assertEquals(expected, actual);
        
        expected = 13;
        item.setId(expected);
        actual = item.getId();
        Assert.assertEquals(expected, actual);
    }
    
    public void testOuter_idPropertyWorksAsExpected()
    {
        int expected = 42;
        ExceptionDetails item = new ExceptionDetails();
        item.setOuterId(expected);
        int actual = item.getOuterId();
        Assert.assertEquals(expected, actual);
        
        expected = 13;
        item.setOuterId(expected);
        actual = item.getOuterId();
        Assert.assertEquals(expected, actual);
    }
    
    public void testType_namePropertyWorksAsExpected()
    {
        String expected = "Test string";
        ExceptionDetails item = new ExceptionDetails();
        item.setTypeName(expected);
        String actual = item.getTypeName();
        Assert.assertEquals(expected, actual);
        
        expected = "Other string";
        item.setTypeName(expected);
        actual = item.getTypeName();
        Assert.assertEquals(expected, actual);
    }
    
    public void testMessagePropertyWorksAsExpected()
    {
        String expected = "Test string";
        ExceptionDetails item = new ExceptionDetails();
        item.setMessage(expected);
        String actual = item.getMessage();
        Assert.assertEquals(expected, actual);
        
        expected = "Other string";
        item.setMessage(expected);
        actual = item.getMessage();
        Assert.assertEquals(expected, actual);
    }
    
    public void testHas_full_stackPropertyWorksAsExpected()
    {
        boolean expected = true;
        ExceptionDetails item = new ExceptionDetails();
        item.setHasFullStack(expected);
        boolean actual = item.getHasFullStack();
        Assert.assertEquals(expected, actual);
        
        expected = false;
        item.setHasFullStack(expected);
        actual = item.getHasFullStack();
        Assert.assertEquals(expected, actual);
    }
    
    public void testStackPropertyWorksAsExpected()
    {
        String expected = "Test string";
        ExceptionDetails item = new ExceptionDetails();
        item.setStack(expected);
        String actual = item.getStack();
        Assert.assertEquals(expected, actual);
        
        expected = "Other string";
        item.setStack(expected);
        actual = item.getStack();
        Assert.assertEquals(expected, actual);
    }
    
    public void testParsed_stackPropertyWorksAsExpected()
    {
        ExceptionDetails item = new ExceptionDetails();
        ArrayList<StackFrame> actual = (ArrayList<StackFrame>)item.getParsedStack();
        Assert.assertNotNull(actual);
    }
    
    public void testSerialize() throws IOException
    {
        ExceptionDetails item = new ExceptionDetails();
        item.setId(42);
        item.setOuterId(42);
        item.setTypeName("Test string");
        item.setMessage("Test string");
        item.setHasFullStack(true);
        item.setStack("Test string");
        for (StackFrame entry : new ArrayList<StackFrame>() {{add(new StackFrame());}})
        {
            item.getParsedStack().add(entry);
        }
        StringWriter writer = new StringWriter();
        item.serialize(writer);
        String expected = "{\"id\":42,\"outerId\":42,\"typeName\":\"Test string\",\"message\":\"Test string\",\"hasFullStack\":true,\"stack\":\"Test string\",\"parsedStack\":[{}]}";
        Assert.assertEquals(expected, writer.toString());
    }

}
