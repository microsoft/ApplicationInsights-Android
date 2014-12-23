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
/// Data contract test class DomainTests.
/// </summary>
public class DomainTests extends TestCase
{
    public void testSerialize() throws IOException
    {
        Domain item = new Domain();
        StringWriter writer = new StringWriter();
        item.serialize(writer);
        String expected = "{}";
        Assert.assertEquals(expected, writer.toString());
    }

}
