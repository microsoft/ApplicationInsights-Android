/*
 * Generated from CrashDataThread.bond (https://github.com/Microsoft/bond)
*/
package com.microsoft.applicationinsights.contracts;
import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.util.List;
import java.util.ArrayList;

import com.microsoft.telemetry.IJsonSerializable;
import com.microsoft.telemetry.JsonHelper;

/**
 * Data contract class CrashDataThread.
 */
public class CrashDataThread
     implements IJsonSerializable, Serializable
{
    /**
     * Backing field for property Id.
     */
    private int id;
    
    /**
     * Backing field for property Frames.
     */
    private List<CrashDataThreadFrame> frames;
    
    /**
     * Initializes a new instance of the CrashDataThread class.
     */
    public CrashDataThread()
    {
        this.InitializeFields();
    }
    
    /**
     * Gets the Id property.
     */
    public int getId() {
        return this.id;
    }
    
    /**
     * Sets the Id property.
     */
    public void setId(int value) {
        this.id = value;
    }
    
    /**
     * Gets the Frames property.
     */
    public List<CrashDataThreadFrame> getFrames() {
        if (this.frames == null) {
            this.frames = new ArrayList<CrashDataThreadFrame>();
        }
        return this.frames;
    }
    
    /**
     * Sets the Frames property.
     */
    public void setFrames(List<CrashDataThreadFrame> value) {
        this.frames = value;
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
        writer.write(prefix + "\"id\":");
        writer.write(JsonHelper.convert(this.id));
        prefix = ",";
        
        if (!(this.frames == null))
        {
            writer.write(prefix + "\"frames\":");
            JsonHelper.writeList(writer, this.frames);
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
