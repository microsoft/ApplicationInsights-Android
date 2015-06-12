/*
 * Generated from ContextTagKeys.bond (https://github.com/Microsoft/bond)
*/
package com.microsoft.applicationinsights.contracts;
import java.io.IOException;
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
 * Data contract class Operation.
 */
public class Operation
     implements IJsonSerializable
{
    /**
     * Backing field for property Id.
     */
    private String id;
    
    /**
     * Backing field for property Name.
     */
    private String name;
    
    /**
     * Backing field for property ParentId.
     */
    private String parentId;
    
    /**
     * Backing field for property RootId.
     */
    private String rootId;
    
    /**
     * Backing field for property SyntheticSource.
     */
    private String syntheticSource;
    
    /**
     * Initializes a new instance of the Operation class.
     */
    public Operation()
    {
        this.InitializeFields();
    }
    
    /**
     * Gets the Id property.
     */
    public String getId() {
        return this.id;
    }
    
    /**
     * Sets the Id property.
     */
    public void setId(String value) {
        this.id = value;
    }
    
    /**
     * Gets the Name property.
     */
    public String getName() {
        return this.name;
    }
    
    /**
     * Sets the Name property.
     */
    public void setName(String value) {
        this.name = value;
    }
    
    /**
     * Gets the ParentId property.
     */
    public String getParentId() {
        return this.parentId;
    }
    
    /**
     * Sets the ParentId property.
     */
    public void setParentId(String value) {
        this.parentId = value;
    }
    
    /**
     * Gets the RootId property.
     */
    public String getRootId() {
        return this.rootId;
    }
    
    /**
     * Sets the RootId property.
     */
    public void setRootId(String value) {
        this.rootId = value;
    }
    
    /**
     * Gets the SyntheticSource property.
     */
    public String getSyntheticSource() {
        return this.syntheticSource;
    }
    
    /**
     * Sets the SyntheticSource property.
     */
    public void setSyntheticSource(String value) {
        this.syntheticSource = value;
    }
    

    /**
     * Adds all members of this class to a hashmap
     * @param map to which the members of this class will be added.
     */
    public void addToHashMap(Map<String, String> map)
    {
        if (!(this.id == null)) {
            map.put("ai.operation.id", this.id);
        }
        if (!(this.name == null)) {
            map.put("ai.operation.name", this.name);
        }
        if (!(this.parentId == null)) {
            map.put("ai.operation.parentId", this.parentId);
        }
        if (!(this.rootId == null)) {
            map.put("ai.operation.rootId", this.rootId);
        }
        if (!(this.syntheticSource == null)) {
            map.put("ai.operation.syntheticSource", this.syntheticSource);
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
        if (!(this.id == null))
        {
            writer.write(prefix + "\"ai.operation.id\":");
            writer.write(JsonHelper.convert(this.id));
            prefix = ",";
        }
        
        if (!(this.name == null))
        {
            writer.write(prefix + "\"ai.operation.name\":");
            writer.write(JsonHelper.convert(this.name));
            prefix = ",";
        }
        
        if (!(this.parentId == null))
        {
            writer.write(prefix + "\"ai.operation.parentId\":");
            writer.write(JsonHelper.convert(this.parentId));
            prefix = ",";
        }
        
        if (!(this.rootId == null))
        {
            writer.write(prefix + "\"ai.operation.rootId\":");
            writer.write(JsonHelper.convert(this.rootId));
            prefix = ",";
        }
        
        if (!(this.syntheticSource == null))
        {
            writer.write(prefix + "\"ai.operation.syntheticSource\":");
            writer.write(JsonHelper.convert(this.syntheticSource));
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
