package com.microsoft.applicationinsights.channel.contracts;

import com.microsoft.applicationinsights.channel.contracts.shared.ITelemetry;
import com.microsoft.applicationinsights.channel.contracts.shared.JsonHelper;

import java.io.IOException;
import java.io.Writer;
import java.util.LinkedHashMap;

/**
 * Data contract class SessionStateData.
 */
public class SessionStateData extends Domain implements
    ITelemetry
{
    /**
     * Envelope Name for this telemetry.
     */
    public String getEnvelopeName() {
        return "Microsoft.ApplicationInsights.SessionState";
    }
    
    /**
     * Base Type for this telemetry.
     */
    public String getBaseType() {
        return "Microsoft.ApplicationInsights.SessionStateData";
    }
    
    /**
     * Backing field for property Ver.
     */
    private int ver = 2;
    
    /**
     * Backing field for property State.
     */
    private int state = SessionState.Start;
    
    /**
     * Initializes a new instance of the <see cref="SessionStateData"/> class.
     */
    public SessionStateData()
    {
        this.InitializeFields();
    }
    
    /**
     * Gets the Ver property.
     */
    public int getVer() {
        return this.ver;
    }
    
    /**
     * Sets the Ver property.
     */
    public void setVer(int value) {
        this.ver = value;
    }
    
    /**
     * Gets the State property.
     */
    public int getState() {
        return this.state;
    }
    
    /**
     * Sets the State property.
     */
    public void setState(int value) {
        this.state = value;
    }
    

    /**
    * Gets the Properties property.
    */
    public LinkedHashMap<String, String> getProperties() {
        //Do nothing - does not currently take properties
        return null;
    }

    /**
    * Sets the Properties property.
    */
    public void setProperties(LinkedHashMap<String, String> value) {
        //Do nothing - does not currently take properties
    }

    /**
     * Serializes the beginning of this object to the passed in writer.
     * @param writer The writer to serialize this object to.
     */
    protected String serializeContent(Writer writer) throws IOException
    {
        String prefix = super.serializeContent(writer);
        writer.write(prefix + "\"ver\":");
        writer.write(JsonHelper.convert(this.ver));
        prefix = ",";
        
        writer.write(prefix + "\"state\":");
        writer.write(JsonHelper.convert(this.state));
        prefix = ",";
        
        return prefix;
    }
    
    /**
     * Optionally initializes fields for the current context.
     */
    protected void InitializeFields() {
        
    }
}
