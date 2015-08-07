/*
 * Generated from CrashDataBinary.bond (https://github.com/Microsoft/bond)
*/
package com.microsoft.applicationinsights.contracts;
import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;

import com.microsoft.telemetry.IJsonSerializable;
import com.microsoft.telemetry.JsonHelper;

/**
 * Data contract class CrashDataBinary.
 */
public class CrashDataBinary
     implements IJsonSerializable, Serializable
{
    /**
     * Backing field for property StartAddress.
     */
    private String startAddress;
    
    /**
     * Backing field for property EndAddress.
     */
    private String endAddress;
    
    /**
     * Backing field for property Name.
     */
    private String name;
    
    /**
     * Backing field for property CpuType.
     */
    private long cpuType;
    
    /**
     * Backing field for property CpuSubType.
     */
    private long cpuSubType;
    
    /**
     * Backing field for property Uuid.
     */
    private String uuid;
    
    /**
     * Backing field for property Path.
     */
    private String path;
    
    /**
     * Initializes a new instance of the CrashDataBinary class.
     */
    public CrashDataBinary()
    {
        this.InitializeFields();
    }
    
    /**
     * Gets the StartAddress property.
     */
    public String getStartAddress() {
        return this.startAddress;
    }
    
    /**
     * Sets the StartAddress property.
     */
    public void setStartAddress(String value) {
        this.startAddress = value;
    }
    
    /**
     * Gets the EndAddress property.
     */
    public String getEndAddress() {
        return this.endAddress;
    }
    
    /**
     * Sets the EndAddress property.
     */
    public void setEndAddress(String value) {
        this.endAddress = value;
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
     * Gets the CpuType property.
     */
    public long getCpuType() {
        return this.cpuType;
    }
    
    /**
     * Sets the CpuType property.
     */
    public void setCpuType(long value) {
        this.cpuType = value;
    }
    
    /**
     * Gets the CpuSubType property.
     */
    public long getCpuSubType() {
        return this.cpuSubType;
    }
    
    /**
     * Sets the CpuSubType property.
     */
    public void setCpuSubType(long value) {
        this.cpuSubType = value;
    }
    
    /**
     * Gets the Uuid property.
     */
    public String getUuid() {
        return this.uuid;
    }
    
    /**
     * Sets the Uuid property.
     */
    public void setUuid(String value) {
        this.uuid = value;
    }
    
    /**
     * Gets the Path property.
     */
    public String getPath() {
        return this.path;
    }
    
    /**
     * Sets the Path property.
     */
    public void setPath(String value) {
        this.path = value;
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
        if (!(this.startAddress == null))
        {
            writer.write(prefix + "\"startAddress\":");
            writer.write(JsonHelper.convert(this.startAddress));
            prefix = ",";
        }
        
        if (!(this.endAddress == null))
        {
            writer.write(prefix + "\"endAddress\":");
            writer.write(JsonHelper.convert(this.endAddress));
            prefix = ",";
        }
        
        if (!(this.name == null))
        {
            writer.write(prefix + "\"name\":");
            writer.write(JsonHelper.convert(this.name));
            prefix = ",";
        }
        
        if (!(this.cpuType == 0L))
        {
            writer.write(prefix + "\"cpuType\":");
            writer.write(JsonHelper.convert(this.cpuType));
            prefix = ",";
        }
        
        if (!(this.cpuSubType == 0L))
        {
            writer.write(prefix + "\"cpuSubType\":");
            writer.write(JsonHelper.convert(this.cpuSubType));
            prefix = ",";
        }
        
        if (!(this.uuid == null))
        {
            writer.write(prefix + "\"uuid\":");
            writer.write(JsonHelper.convert(this.uuid));
            prefix = ",";
        }
        
        if (!(this.path == null))
        {
            writer.write(prefix + "\"path\":");
            writer.write(JsonHelper.convert(this.path));
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
