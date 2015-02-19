package com.microsoft.applicationinsights.channel.contracts;

import junit.framework.Assert;
import junit.framework.TestCase;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;

/// <summary>
/// Data contract test class CrashDataTests.
/// </summary>
public class CrashDataTests extends TestCase
{
    public void testVerPropertyWorksAsExpected()
    {
        int expected = 42;
        CrashData item = new CrashData();
        item.setVer(expected);
        int actual = item.getVer();
        Assert.assertEquals(expected, actual);
        
        expected = 13;
        item.setVer(expected);
        actual = item.getVer();
        Assert.assertEquals(expected, actual);
    }
    
    public void testHeadersPropertyWorksAsExpected()
    {
        CrashDataHeaders expected = new CrashDataHeaders();
        CrashData item = new CrashData();
        item.setHeaders(expected);
        CrashDataHeaders actual = item.getHeaders();
        Assert.assertEquals(expected, actual);
        
        expected = new CrashDataHeaders();
        item.setHeaders(expected);
        actual = item.getHeaders();
        Assert.assertEquals(expected, actual);
    }
    
    public void testThreadsPropertyWorksAsExpected()
    {
        CrashData item = new CrashData();
        ArrayList<CrashDataThread> actual = (ArrayList<CrashDataThread>)item.getThreads();
        Assert.assertNotNull(actual);
    }
    
    public void testBinariesPropertyWorksAsExpected()
    {
        CrashData item = new CrashData();
        ArrayList<CrashDataBinary> actual = (ArrayList<CrashDataBinary>)item.getBinaries();
        Assert.assertNotNull(actual);
    }
    
    public void testSerialize() throws IOException
    {
        CrashData item = new CrashData();
        item.setVer(42);
        item.setHeaders(new CrashDataHeaders());
        for (CrashDataThread entry : new ArrayList<CrashDataThread>() {{add(new CrashDataThread());}})
        {
            item.getThreads().add(entry);
        }
        for (CrashDataBinary entry : new ArrayList<CrashDataBinary>() {{add(new CrashDataBinary());}})
        {
            item.getBinaries().add(entry);
        }
        StringWriter writer = new StringWriter();
        item.serialize(writer);
        String expected = "{\"ver\":42,\"headers\":{\"id\":null},\"threads\":[{\"id\":0}],\"binaries\":[{}]}";
        Assert.assertEquals(expected, writer.toString());
    }

}
