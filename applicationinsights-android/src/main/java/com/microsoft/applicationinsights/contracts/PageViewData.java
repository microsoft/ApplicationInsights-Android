/*
 * Generated from AppInsightsTypes.bond (https://github.com/Microsoft/bond)
*/
package com.microsoft.applicationinsights.contracts;

import com.microsoft.telemetry.JsonHelper;

import java.io.IOException;
import java.io.Writer;

/**
 * Data contract class PageViewData.
 */
public class PageViewData extends EventData {
    /**
     * Backing field for property Url.
     */
    private String url;

    /**
     * Backing field for property Duration.
     */
    private String duration;

    /**
     * Backing field for property Referrer.
     */
    private String referrer;

    /**
     * Backing field for property ReferrerData.
     */
    private String referrerData;

    /**
     * Initializes a new instance of the PageViewData class.
     */
    public PageViewData() {
        this.InitializeFields();
        this.SetupAttributes();
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
     * Gets the Referrer property.
     */
    public String getReferrer() {
        return this.referrer;
    }

    /**
     * Sets the Referrer property.
     */
    public void setReferrer(String value) {
        this.referrer = value;
    }

    /**
     * Gets the ReferrerData property.
     */
    public String getReferrerData() {
        return this.referrerData;
    }

    /**
     * Sets the ReferrerData property.
     */
    public void setReferrerData(String value) {
        this.referrerData = value;
    }


    /**
     * Serializes the beginning of this object to the passed in writer.
     *
     * @param writer The writer to serialize this object to.
     */
    protected String serializeContent(Writer writer) throws IOException {
        String prefix = super.serializeContent(writer);
        if (!(this.url == null)) {
            writer.write(prefix + "\"url\":");
            writer.write(JsonHelper.convert(this.url));
            prefix = ",";
        }

        if (!(this.duration == null)) {
            writer.write(prefix + "\"duration\":");
            writer.write(JsonHelper.convert(this.duration));
            prefix = ",";
        }

        if (!(this.referrer == null)) {
            writer.write(prefix + "\"referrer\":");
            writer.write(JsonHelper.convert(this.referrer));
            prefix = ",";
        }

        if (!(this.referrerData == null)) {
            writer.write(prefix + "\"referrerData\":");
            writer.write(JsonHelper.convert(this.referrerData));
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
        QualifiedName = "com.microsoft.applicationinsights.contracts.PageViewData";
    }
}
