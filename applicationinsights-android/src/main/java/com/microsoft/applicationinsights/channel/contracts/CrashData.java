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
 * Data contract class CrashData.
 */
public class CrashData extends Domain implements
    ITelemetry
{
    /**
     * Envelope Name for this telemetry.
     */
    public String getEnvelopeName() {
        return "Microsoft.ApplicationInsights.Crash";
    }
    
    /**
     * Base Type for this telemetry.
     */
    public String getBaseType() {
        return "Microsoft.ApplicationInsights.CrashData";
    }
    
    /**
     * Backing field for property Ver.
     */
    private int ver = 2;
    
    /**
     * Backing field for property Headers.
     */
    private CrashDataHeaders headers;
    
    /**
     * Backing field for property Threads.
     */
    private ArrayList<CrashDataThread> threads;
    
    /**
     * Backing field for property Binaries.
     */
    private ArrayList<CrashDataBinary> binaries;
    
    /**
     * Initializes a new instance of the <see cref="CrashData"/> class.
     */
    public CrashData()
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
     * Gets the Headers property.
     */
    public CrashDataHeaders getHeaders() {
        return this.headers;
    }
    
    /**
     * Sets the Headers property.
     */
    public void setHeaders(CrashDataHeaders value) {
        this.headers = value;
    }
    
    /**
     * Gets the Threads property.
     */
    public ArrayList<CrashDataThread> getThreads() {
        if (this.threads == null) {
            this.threads = new ArrayList<CrashDataThread>();
        }
        return this.threads;
    }
    
    /**
     * Sets the Threads property.
     */
    public void setThreads(ArrayList<CrashDataThread> value) {
        this.threads = value;
    }
    
    /**
     * Gets the Binaries property.
     */
    public ArrayList<CrashDataBinary> getBinaries() {
        if (this.binaries == null) {
            this.binaries = new ArrayList<CrashDataBinary>();
        }
        return this.binaries;
    }
    
    /**
     * Sets the Binaries property.
     */
    public void setBinaries(ArrayList<CrashDataBinary> value) {
        this.binaries = value;
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
        
        writer.write(prefix + "\"headers\":");
        JsonHelper.writeJsonSerializable(writer, this.headers);
        prefix = ",";
        
        if (!(this.threads == null))
        {
            writer.write(prefix + "\"threads\":");
            JsonHelper.writeList(writer, this.threads);
            prefix = ",";
        }
        
        if (!(this.binaries == null))
        {
            writer.write(prefix + "\"binaries\":");
            JsonHelper.writeList(writer, this.binaries);
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
