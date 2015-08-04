/*
 * Generated from AppInsightsTypes.bond (https://github.com/Microsoft/bond)
*/
package com.microsoft.applicationinsights.contracts;
import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;

import com.microsoft.telemetry.IJsonSerializable;
import com.microsoft.telemetry.JsonHelper;

/**
 * Data contract class DataPoint.
 */
public class DataPoint
     implements IJsonSerializable, Serializable
{
    /**
     * Backing field for property Name.
     */
    private String name;
    
    /**
     * Backing field for property Kind.
     */
    private DataPointType kind = DataPointType.MEASUREMENT;
    
    /**
     * Backing field for property Value.
     */
    private double value;
    
    /**
     * Backing field for property Count.
     */
    private Integer count;
    
    /**
     * Backing field for property Min.
     */
    private Double min;
    
    /**
     * Backing field for property Max.
     */
    private Double max;
    
    /**
     * Backing field for property StdDev.
     */
    private Double stdDev;
    
    /**
     * Initializes a new instance of the DataPoint class.
     */
    public DataPoint()
    {
        this.InitializeFields();
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
     * Gets the Kind property.
     */
    public DataPointType getKind() {
        return this.kind;
    }
    
    /**
     * Sets the Kind property.
     */
    public void setKind(DataPointType value) {
        this.kind = value;
    }
    
    /**
     * Gets the Value property.
     */
    public double getValue() {
        return this.value;
    }
    
    /**
     * Sets the Value property.
     */
    public void setValue(double value) {
        this.value = value;
    }
    
    /**
     * Gets the Count property.
     */
    public Integer getCount() {
        return this.count;
    }
    
    /**
     * Sets the Count property.
     */
    public void setCount(Integer value) {
        this.count = value;
    }
    
    /**
     * Gets the Min property.
     */
    public Double getMin() {
        return this.min;
    }
    
    /**
     * Sets the Min property.
     */
    public void setMin(Double value) {
        this.min = value;
    }
    
    /**
     * Gets the Max property.
     */
    public Double getMax() {
        return this.max;
    }
    
    /**
     * Sets the Max property.
     */
    public void setMax(Double value) {
        this.max = value;
    }
    
    /**
     * Gets the StdDev property.
     */
    public Double getStdDev() {
        return this.stdDev;
    }
    
    /**
     * Sets the StdDev property.
     */
    public void setStdDev(Double value) {
        this.stdDev = value;
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
        writer.write(prefix + "\"name\":");
        writer.write(JsonHelper.convert(this.name));
        prefix = ",";
        
        if (!(this.kind == DataPointType.MEASUREMENT))
        {
            writer.write(prefix + "\"kind\":");
            writer.write(JsonHelper.convert(this.kind.getValue()));
            prefix = ",";
        }
        
        writer.write(prefix + "\"value\":");
        writer.write(JsonHelper.convert(this.value));
        prefix = ",";
        
        if (!(this.count == null))
        {
            writer.write(prefix + "\"count\":");
            writer.write(JsonHelper.convert(this.count));
            prefix = ",";
        }
        
        if (!(this.min == null))
        {
            writer.write(prefix + "\"min\":");
            writer.write(JsonHelper.convert(this.min));
            prefix = ",";
        }
        
        if (!(this.max == null))
        {
            writer.write(prefix + "\"max\":");
            writer.write(JsonHelper.convert(this.max));
            prefix = ",";
        }
        
        if (!(this.stdDev == null))
        {
            writer.write(prefix + "\"stdDev\":");
            writer.write(JsonHelper.convert(this.stdDev));
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
