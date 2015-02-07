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
/// Data contract test class CrashDataThreadTests.
/// </summary>
public class CrashDataThreadTests extends TestCase
{
    public void testIdPropertyWorksAsExpected()
    {
        int expected = 42;
        CrashDataThread item = new CrashDataThread();
        item.setId(expected);
        int actual = item.getId();
        Assert.assertEquals(expected, actual);
        
        expected = 13;
        item.setId(expected);
        actual = item.getId();
        Assert.assertEquals(expected, actual);
    }
    
    public void testFramesPropertyWorksAsExpected()
    {
        CrashDataThread item = new CrashDataThread();
        ArrayList<CrashDataThreadFrame> actual = (ArrayList<CrashDataThreadFrame>)item.getFrames();
        Assert.assertNotNull(actual);
    }
    
    public void testSerialize() throws IOException
    {
        CrashDataThread item = new CrashDataThread();
        item.setId(42);
        for (CrashDataThreadFrame entry : new ArrayList<CrashDataThreadFrame>() {{add(new CrashDataThreadFrame());}})
        {
            item.getFrames().add(entry);
        }
        StringWriter writer = new StringWriter();
        item.serialize(writer);
        String expected = "{\"id\":42,\"frames\":[{}]}";
        Assert.assertEquals(expected, writer.toString());
    }

}
