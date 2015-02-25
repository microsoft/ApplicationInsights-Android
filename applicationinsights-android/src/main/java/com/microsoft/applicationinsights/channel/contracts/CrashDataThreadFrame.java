package com.microsoft.applicationinsights.channel.contracts;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import com.microsoft.commonlogging.channel.contracts.shared.ITelemetry;
import com.microsoft.commonlogging.channel.contracts.shared.ITelemetryData;
import com.microsoft.commonlogging.channel.contracts.shared.IContext;
import com.microsoft.commonlogging.channel.contracts.shared.IJsonSerializable;
import com.microsoft.commonlogging.channel.contracts.shared.JsonHelper;

/**
 * Data contract class CrashDataThreadFrame.
 */
public class CrashDataThreadFrame implements
    IJsonSerializable
{
    /**
     * Backing field for property Address.
     */
    private String address;
    
    /**
     * Backing field for property Symbol.
     */
    private String symbol;
    
    /**
     * Backing field for property Registers.
     */
    private LinkedHashMap<String, String> registers;
    
    /**
     * Initializes a new instance of the <see cref="CrashDataThreadFrame"/> class.
     */
    public CrashDataThreadFrame()
    {
        this.InitializeFields();
    }
    
    /**
     * Gets the Address property.
     */
    public String getAddress() {
        return this.address;
    }
    
    /**
     * Sets the Address property.
     */
    public void setAddress(String value) {
        this.address = value;
    }
    
    /**
     * Gets the Symbol property.
     */
    public String getSymbol() {
        return this.symbol;
    }
    
    /**
     * Sets the Symbol property.
     */
    public void setSymbol(String value) {
        this.symbol = value;
    }
    
    /**
     * Gets the Registers property.
     */
    public LinkedHashMap<String, String> getRegisters() {
        if (this.registers == null) {
            this.registers = new LinkedHashMap<String, String>();
        }
        return this.registers;
    }
    
    /**
     * Sets the Registers property.
     */
    public void setRegisters(LinkedHashMap<String, String> value) {
        this.registers = value;
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
        writer.write(prefix + "\"address\":");
        writer.write(JsonHelper.convert(this.address));
        prefix = ",";
        
        if (!(this.symbol == null))
        {
            writer.write(prefix + "\"symbol\":");
            writer.write(JsonHelper.convert(this.symbol));
            prefix = ",";
        }
        
        if (!(this.registers == null))
        {
            writer.write(prefix + "\"registers\":");
            JsonHelper.writeDictionary(writer, this.registers);
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
