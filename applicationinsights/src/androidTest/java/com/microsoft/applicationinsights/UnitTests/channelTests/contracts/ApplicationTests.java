package com.microsoft.applicationinsights.UnitTests.channelTests.contracts;

import junit.*;

import android.test.AndroidTestCase;
import com.microsoft.applicationinsights.channel.contracts.Application;
import junit.framework.Assert;
import org.json.JSONException;

import java.io.IOException;
import java.io.StringWriter;

public class ApplicationTests extends AndroidTestCase {

    public void setUp() throws Exception {
        super.setUp();

    }

    public void testPropertyWorksAsExpected()
    {
        Application app = new Application();

        String expected = "Test string";

        app.setVer(expected);

        Assert.assertEquals(expected, app.getVer());
    }

    public void testSerialize() throws IOException, JSONException
    {
        String version = "Test string";
        String expected = "{\"ai.application.ver\":\"Test string\"}";

        Application app = new Application();
        app.setVer(version);

        StringWriter writer = new StringWriter();

        app.serialize(writer);
        Assert.assertEquals(expected, writer.toString());
    }


}
