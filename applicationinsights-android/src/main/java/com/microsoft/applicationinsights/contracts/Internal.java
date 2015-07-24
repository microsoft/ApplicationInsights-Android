/*
 * Generated from ContextTagKeys.bond (https://github.com/Microsoft/bond)
*/
package com.microsoft.applicationinsights.contracts;
import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import com.microsoft.telemetry.ITelemetry;
import com.microsoft.telemetry.ITelemetryData;
import com.microsoft.telemetry.IContext;
import com.microsoft.telemetry.IJsonSerializable;
import com.microsoft.telemetry.Base;
import com.microsoft.telemetry.Data;
import com.microsoft.telemetry.Domain;
import com.microsoft.telemetry.Extension;
import com.microsoft.telemetry.JsonHelper;

/**
 * Data contract class Internal.
 */
public class Internal
     implements IJsonSerializable, Serializable
{
    /**
     * Backing field for property SdkVersion.
     */
    private String sdkVersion;
    
    /**
     * Backing field for property AgentVersion.
     */
    private String agentVersion;
    
    /**
     * Initializes a new instance of the Internal class.
     */
    public Internal()
    {
        this.InitializeFields();
    }
    
    /**
     * Gets the SdkVersion property.
     */
    public String getSdkVersion() {
        return this.sdkVersion;
    }
    
    /**
     * Sets the SdkVersion property.
     */
    public void setSdkVersion(String value) {
        this.sdkVersion = value;
    }
    
    /**
     * Gets the AgentVersion property.
     */
    public String getAgentVersion() {
        return this.agentVersion;
    }
    
    /**
     * Sets the AgentVersion property.
     */
    public void setAgentVersion(String value) {
        this.agentVersion = value;
    }
    

    /**
     * Adds all members of this class to a hashmap
     * @param map to which the members of this class will be added.
     */
    public void addToHashMap(Map<String, String> map)
    {
        if (!(this.sdkVersion == null)) {
            map.put("ai.internal.sdkVersion", this.sdkVersion);
        }
        if (!(this.agentVersion == null)) {
            map.put("ai.internal.agentVersion", this.agentVersion);
        }
    }
    

    /**
     * Serializes the beginning of this object to the passed in writer.
     * @param writer The writer to serialize this object to.
     */
    @Override
    public void serialize(Writer writer) throws IOException
    {
        if (writer == null)
        {
            throw new IllegalArgumentException("writer");
        }
        
        writer.write('{');
        this.serializeContent(writer);
        writer.write('}');
    }

    /**
     * Serializes the beginning of this object to the passed in writer.
     * @param writer The writer to serialize this object to.
     */
    protected String serializeContent(Writer writer) throws IOException
    {
        String prefix = "";
        if (!(this.sdkVersion == null))
        {
            writer.write(prefix + "\"ai.internal.sdkVersion\":");
            writer.write(JsonHelper.convert(this.sdkVersion));
            prefix = ",";
        }
        
        if (!(this.agentVersion == null))
        {
            writer.write(prefix + "\"ai.internal.agentVersion\":");
            writer.write(JsonHelper.convert(this.agentVersion));
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
