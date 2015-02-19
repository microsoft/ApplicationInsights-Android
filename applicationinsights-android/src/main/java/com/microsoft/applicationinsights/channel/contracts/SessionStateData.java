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
    private SessionState state = SessionState.Start;
    
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
    public SessionState getState() {
        return this.state;
    }
    
    /**
     * Sets the State property.
     */
    public void setState(SessionState value) {
        this.state = value;
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
        JsonHelper.writeJsonSerializable(writer, this.state);
        prefix = ",";
        
        return prefix;
    }
    
    /**
     * Optionally initializes fields for the current context.
     */
    protected void InitializeFields() {
        
    }
}
