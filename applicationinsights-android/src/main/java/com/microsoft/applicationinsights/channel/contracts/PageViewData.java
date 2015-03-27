/*
 * Generated from AppInsightsTypes.bond (https://github.com/Microsoft/bond)
*/
package com.microsoft.applicationinsights.channel.contracts;

import com.microsoft.applicationinsights.channel.contracts.shared.ITelemetry;
import com.microsoft.applicationinsights.channel.contracts.shared.JsonHelper;

import java.io.IOException;
import java.io.Writer;

/**
 * Data contract class PageViewData.
 */
public class PageViewData extends EventData implements
    ITelemetry
{
    /**
     * Backing field for property Url.
     */
    private String url;
    
    /**
     * Backing field for property Duration.
     */
    private String duration;
    
    /**
     * Initializes a new instance of the <see cref="PageViewData"/> class.
     */
    public PageViewData()
    {
        this.InitializeFields();
    }
    
    /**
     * Envelope Name for this telemetry.
     */
    public String getEnvelopeName() {
        return "Microsoft.ApplicationInsights.PageView";
    }
    
    /**
     * Base Type for this telemetry.
     */
    public String getBaseType() {
        return "Microsoft.ApplicationInsights.PageViewData";
    }
    
    /**
     * Gets the Url property.
     */
    public String getUrl() {
        return this.url;
    }
    
    /**
     * Sets the Url property.
     */
    public void setUrl(String value) {
        this.url = value;
    }
    
    /**
     * Gets the Duration property.
     */
    public String getDuration() {
        return this.duration;
    }
    
    /**
     * Sets the Duration property.
     */
    public void setDuration(String value) {
        this.duration = value;
    }
    

    /**
     * Serializes the beginning of this object to the passed in writer.
     * @param writer The writer to serialize this object to.
     */
    protected String serializeContent(Writer writer) throws IOException
    {
        String prefix = super.serializeContent(writer);
        if (!(this.url == null))
        {
            writer.write(prefix + "\"url\":");
            writer.write(JsonHelper.convert(this.url));
            prefix = ",";
        }
        
        if (!(this.duration == null))
        {
            writer.write(prefix + "\"duration\":");
            writer.write(JsonHelper.convert(this.duration));
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
