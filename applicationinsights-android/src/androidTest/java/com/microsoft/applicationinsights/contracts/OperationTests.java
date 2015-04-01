package com.microsoft.applicationinsights.contracts;

import junit.framework.Assert;
import junit.framework.TestCase;

import java.io.IOException;
import java.io.StringWriter;

/// <summary>
/// Data contract test class OperationTests.
/// </summary>
public class OperationTests extends TestCase
{
    public void testIdPropertyWorksAsExpected()
    {
        String expected = "Test string";
        Operation item = new Operation();
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
        Operation item = new Operation();
        item.setName(expected);
        String actual = item.getName();
        Assert.assertEquals(expected, actual);
        
        expected = "Other string";
        item.setName(expected);
        actual = item.getName();
        Assert.assertEquals(expected, actual);
    }
    
    public void testParent_idPropertyWorksAsExpected()
    {
        String expected = "Test string";
        Operation item = new Operation();
        item.setParentId(expected);
        String actual = item.getParentId();
        Assert.assertEquals(expected, actual);
        
        expected = "Other string";
        item.setParentId(expected);
        actual = item.getParentId();
        Assert.assertEquals(expected, actual);
    }
    
    public void testRoot_idPropertyWorksAsExpected()
    {
        String expected = "Test string";
        Operation item = new Operation();
        item.setRootId(expected);
        String actual = item.getRootId();
        Assert.assertEquals(expected, actual);
        
        expected = "Other string";
        item.setRootId(expected);
        actual = item.getRootId();
        Assert.assertEquals(expected, actual);
    }
    
    public void testSerialize() throws IOException
    {
        Operation item = new Operation();
        item.setId("Test string");
        item.setName("Test string");
        item.setParentId("Test string");
        item.setRootId("Test string");
        StringWriter writer = new StringWriter();
        item.serialize(writer);
        String expected = "{\"ai.operation.id\":\"Test string\",\"ai.operation.name\":\"Test string\",\"ai.operation.parentId\":\"Test string\",\"ai.operation.rootId\":\"Test string\"}";
        Assert.assertEquals(expected, writer.toString());
    }

}
