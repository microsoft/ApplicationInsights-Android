/*
 * Generated from ContextTagKeys.bond (https://github.com/Microsoft/bond)
*/
package com.microsoft.applicationinsights.contracts;

import com.microsoft.telemetry.IJsonSerializable;
import com.microsoft.telemetry.JsonHelper;

import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.util.Map;

/**
 * Data contract class Location.
 */
public class Location implements IJsonSerializable, Serializable {
    /**
     * Backing field for property Ip.
     */
    private String ip;

    /**
     * Initializes a new instance of the Location class.
     */
    public Location() {
        this.InitializeFields();
    }

    /**
     * Gets the Ip property.
     */
    public String getIp() {
        return this.ip;
    }

    /**
     * Sets the Ip property.
     */
    public void setIp(String value) {
        this.ip = value;
    }


    /**
     * Adds all members of this class to a hashmap
     *
     * @param map to which the members of this class will be added.
     */
    public void addToHashMap(Map<String, String> map) {
        if (!(this.ip == null)) {
            map.put("ai.location.ip", this.ip);
        }
    }


    /**
     * Serializes the beginning of this object to the passed in writer.
     *
     * @param writer The writer to serialize this object to.
     */
    @Override
    public void serialize(Writer writer) throws IOException {
        if (writer == null) {
            throw new IllegalArgumentException("writer");
        }

        writer.write('{');
        this.serializeContent(writer);
        writer.write('}');
    }

    /**
     * Serializes the beginning of this object to the passed in writer.
     *
     * @param writer The writer to serialize this object to.
     */
    protected String serializeContent(Writer writer) throws IOException {
        String prefix = "";
        if (!(this.ip == null)) {
            writer.write(prefix + "\"ai.location.ip\":");
            writer.write(JsonHelper.convert(this.ip));
            prefix = ",";
        }

        return prefix;
    }

    /**
     * Optionally initializes fields for the current context.
     */
    protected void InitializeFields() {

    }
}
