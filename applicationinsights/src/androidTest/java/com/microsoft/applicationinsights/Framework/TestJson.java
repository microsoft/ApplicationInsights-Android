package com.microsoft.applicationinsights.Framework;

import com.microsoft.applicationinsights.channel.contracts.shared.IJsonSerializable;

import org.json.JSONException;

import java.io.IOException;
import java.io.Writer;

public class TestJson implements
        IJsonSerializable
{

    public String name;

    public String value;

    public TestJson(String name, String value)
    {
        this.name = name;
        this.value = value;
    }

    public void serialize(Writer writer) throws IOException, JSONException
    {
        if (writer == null)
        {
            throw new IllegalArgumentException("writer");
        }

        writer.write('{');
        writer.write(String.format("\"name\": \"{0}\",", name));
        writer.write(String.format("\"value\": \"{0}\",", value));
        writer.write('}');
    }

}