/*
 * Generated from CrashData.bond (https://github.com/Microsoft/bond)
*/
package com.microsoft.applicationinsights.contracts;

import com.microsoft.telemetry.JsonHelper;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * Data contract class CrashData.
 */
public class CrashData extends TelemetryData {
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
    private List<CrashDataThread> threads;

    /**
     * Backing field for property Binaries.
     */
    private List<CrashDataBinary> binaries;

    /**
     * Initializes a new instance of the CrashData class.
     */
    public CrashData() {
        this.InitializeFields();
        this.SetupAttributes();
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
    public List<CrashDataThread> getThreads() {
        if (this.threads == null) {
            this.threads = new ArrayList<CrashDataThread>();
        }
        return this.threads;
    }

    /**
     * Sets the Threads property.
     */
    public void setThreads(List<CrashDataThread> value) {
        this.threads = value;
    }

    /**
     * Gets the Binaries property.
     */
    public List<CrashDataBinary> getBinaries() {
        if (this.binaries == null) {
            this.binaries = new ArrayList<CrashDataBinary>();
        }
        return this.binaries;
    }

    /**
     * Sets the Binaries property.
     */
    public void setBinaries(List<CrashDataBinary> value) {
        this.binaries = value;
    }


    /**
     * Gets the Properties property.
     */
    public Map<String, String> getProperties() {
        //Do nothing - does not currently take properties
        return null;
    }

    /**
     * Sets the Properties property.
     */
    public void setProperties(Map<String, String> value) {
        //Do nothing - does not currently take properties
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

        writer.write(prefix + "\"headers\":");
        JsonHelper.writeJsonSerializable(writer, this.headers);
        prefix = ",";

        if (!(this.threads == null)) {
            writer.write(prefix + "\"threads\":");
            JsonHelper.writeList(writer, this.threads);
            prefix = ",";
        }

        if (!(this.binaries == null)) {
            writer.write(prefix + "\"binaries\":");
            JsonHelper.writeList(writer, this.binaries);
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
        QualifiedName = "com.microsoft.applicationinsights.contracts.CrashData";
    }
}
