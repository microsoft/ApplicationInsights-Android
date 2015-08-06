/*
 * Generated from AppInsightsTypes.bond (https://github.com/Microsoft/bond)
*/
package com.microsoft.applicationinsights.contracts;

import com.microsoft.telemetry.JsonHelper;

import java.io.IOException;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Data contract class MessageData.
 */
public class MessageData extends TelemetryData {
    /**
     * Backing field for property Ver.
     */
    private int ver = 2;

    /**
     * Backing field for property Message.
     */
    private String message;

    /**
     * Backing field for property SeverityLevel.
     */
    private SeverityLevel severityLevel = SeverityLevel.VERBOSE;

    /**
     * Backing field for property Properties.
     */
    private Map<String, String> properties;

    /**
     * Initializes a new instance of the MessageData class.
     */
    public MessageData() {
        this.InitializeFields();
        this.SetupAttributes();
    }

    /**
     * Envelope Name for this telemetry.
     */
    public String getEnvelopeName() {
        return "Microsoft.ApplicationInsights.Message";
    }

    /**
     * Base Type for this telemetry.
     */
    public String getBaseType() {
        return "Microsoft.ApplicationInsights.MessageData";
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
     * Gets the Message property.
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * Sets the Message property.
     */
    public void setMessage(String value) {
        this.message = value;
    }

    /**
     * Gets the SeverityLevel property.
     */
    public SeverityLevel getSeverityLevel() {
        return this.severityLevel;
    }

    /**
     * Sets the SeverityLevel property.
     */
    public void setSeverityLevel(SeverityLevel value) {
        this.severityLevel = value;
    }

    /**
     * Gets the Properties property.
     */
    public Map<String, String> getProperties() {
        if (this.properties == null) {
            this.properties = new LinkedHashMap<String, String>();
        }
        return this.properties;
    }

    /**
     * Sets the Properties property.
     */
    public void setProperties(Map<String, String> value) {
        this.properties = value;
    }


    /**
     * Serializes the beginning of this object to the passed in writer.
     *
     * @param writer The writer to serialize this object to.
     */
    protected String serializeContent(Writer writer) throws IOException {
        String prefix = super.serializeContent(writer);
        writer.write(prefix + "\"ver\":");
        writer.write(JsonHelper.convert(this.ver));
        prefix = ",";

        writer.write(prefix + "\"message\":");
        writer.write(JsonHelper.convert(this.message));
        prefix = ",";

        if (!(this.severityLevel == SeverityLevel.VERBOSE)) {
            writer.write(prefix + "\"severityLevel\":");
            writer.write(JsonHelper.convert(this.severityLevel.getValue()));
            prefix = ",";
        }

        if (!(this.properties == null)) {
            writer.write(prefix + "\"properties\":");
            JsonHelper.writeDictionary(writer, this.properties);
            prefix = ",";
        }

        return prefix;
    }

    /**
     * Sets up the events attributes
     */
    public void SetupAttributes() {
    }

    /**
     * Optionally initializes fields for the current context.
     */
    protected void InitializeFields() {
        QualifiedName = "com.microsoft.applicationinsights.contracts.MessageData";
    }
}
