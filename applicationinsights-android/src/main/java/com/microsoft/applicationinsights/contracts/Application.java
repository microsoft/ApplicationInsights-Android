/*
 * Generated from ContextTagKeys.bond (https://github.com/Microsoft/bond)
*/
package com.microsoft.applicationinsights.contracts;

import com.microsoft.applicationinsights.contracts.shared.IJsonSerializable;
import com.microsoft.applicationinsights.contracts.shared.JsonHelper;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

/**
 * Data contract class Application.
 */
public class Application implements
    IJsonSerializable
{
    /**
     * Backing field for property Ver.
     */
    private String ver;
    
    /**
     * Backing field for property Build.
     */
    private String build;
    
    /**
     * Initializes a new instance of the Application class.
     */
    public Application()
    {
        this.InitializeFields();
    }
    
    /**
     * Gets the Ver property.
     */
    public String getVer() {
        return this.ver;
    }
    
    /**
     * Sets the Ver property.
     */
    public void setVer(String value) {
        this.ver = value;
    }
    
    /**
     * Gets the Build property.
     */
    public String getBuild() {
        return this.build;
    }
    
    /**
     * Sets the Build property.
     */
    public void setBuild(String value) {
        this.build = value;
    }
    

    /**
     * Adds all members of this class to a hashmap
     * @param map to which the members of this class will be added.
     */
    public void addToHashMap(Map<String, String> map)
    {
        if (!(this.ver == null)) {
            map.put("ai.application.ver", this.ver);
        }
        if (!(this.build == null)) {
            map.put("ai.application.build", this.build);
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
        if (!(this.ver == null))
        {
            writer.write(prefix + "\"ai.application.ver\":");
            writer.write(JsonHelper.convert(this.ver));
            prefix = ",";
        }
        
        if (!(this.build == null))
        {
            writer.write(prefix + "\"ai.application.build\":");
            writer.write(JsonHelper.convert(this.build));
            prefix = ",";
        }
        
        return prefix;
    }
    
    /**
     * Optionally initializes fields for the current context.
     */
    protected void InitializeFields() {
        // method stub to initialize fields for the current context
    }
}
