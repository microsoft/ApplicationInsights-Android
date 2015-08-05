/*
 * Generated from AppInsightsTypes.bond (https://github.com/Microsoft/bond)
*/
package com.microsoft.applicationinsights.contracts;

import com.microsoft.telemetry.Domain;
import com.microsoft.telemetry.ITelemetryData;
import com.microsoft.telemetry.JsonHelper;

import java.io.IOException;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Data contract class AvailabilityData.
 */
public class AvailabilityData extends Domain implements
      ITelemetryData {
    /**
     * Backing field for property Ver.
     */
    private int ver = 2;

    /**
     * Backing field for property TestRunId.
     */
    private String testRunId;

    /**
     * Backing field for property TestTimeStamp.
     */
    private String testTimeStamp;

    /**
     * Backing field for property TestName.
     */
    private String testName;

    /**
     * Backing field for property Duration.
     */
    private String duration;

    /**
     * Backing field for property Result.
     */
    private TestResult result = TestResult.PASS;

    /**
     * Backing field for property RunLocation.
     */
    private String runLocation;

    /**
     * Backing field for property Message.
     */
    private String message;

    /**
     * Backing field for property DataSize.
     */
    private double dataSize;

    /**
     * Backing field for property Properties.
     */
    private Map<String, String> properties;

    /**
     * Backing field for property Measurements.
     */
    private Map<String, Double> measurements;

    /**
     * Initializes a new instance of the AvailabilityData class.
     */
    public AvailabilityData() {
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
     * Gets the TestRunId property.
     */
    public String getTestRunId() {
        return this.testRunId;
    }

    /**
     * Sets the TestRunId property.
     */
    public void setTestRunId(String value) {
        this.testRunId = value;
    }

    /**
     * Gets the TestTimeStamp property.
     */
    public String getTestTimeStamp() {
        return this.testTimeStamp;
    }

    /**
     * Sets the TestTimeStamp property.
     */
    public void setTestTimeStamp(String value) {
        this.testTimeStamp = value;
    }

    /**
     * Gets the TestName property.
     */
    public String getTestName() {
        return this.testName;
    }

    /**
     * Sets the TestName property.
     */
    public void setTestName(String value) {
        this.testName = value;
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
     * Gets the Result property.
     */
    public TestResult getResult() {
        return this.result;
    }

    /**
     * Sets the Result property.
     */
    public void setResult(TestResult value) {
        this.result = value;
    }

    /**
     * Gets the RunLocation property.
     */
    public String getRunLocation() {
        return this.runLocation;
    }

    /**
     * Sets the RunLocation property.
     */
    public void setRunLocation(String value) {
        this.runLocation = value;
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
     * Gets the DataSize property.
     */
    public double getDataSize() {
        return this.dataSize;
    }

    /**
     * Sets the DataSize property.
     */
    public void setDataSize(double value) {
        this.dataSize = value;
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
     * Gets the Measurements property.
     */
    public Map<String, Double> getMeasurements() {
        if (this.measurements == null) {
            this.measurements = new LinkedHashMap<String, Double>();
        }
        return this.measurements;
    }

    /**
     * Sets the Measurements property.
     */
    public void setMeasurements(Map<String, Double> value) {
        this.measurements = value;
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

        writer.write(prefix + "\"testRunId\":");
        writer.write(JsonHelper.convert(this.testRunId));
        prefix = ",";

        writer.write(prefix + "\"testTimeStamp\":");
        writer.write(JsonHelper.convert(this.testTimeStamp));
        prefix = ",";

        writer.write(prefix + "\"testName\":");
        writer.write(JsonHelper.convert(this.testName));
        prefix = ",";

        writer.write(prefix + "\"duration\":");
        writer.write(JsonHelper.convert(this.duration));
        prefix = ",";

        writer.write(prefix + "\"result\":");
        writer.write(JsonHelper.convert(this.result.getValue()));
        prefix = ",";

        if (!(this.runLocation == null)) {
            writer.write(prefix + "\"runLocation\":");
            writer.write(JsonHelper.convert(this.runLocation));
            prefix = ",";
        }

        if (!(this.message == null)) {
            writer.write(prefix + "\"message\":");
            writer.write(JsonHelper.convert(this.message));
            prefix = ",";
        }

        if (this.dataSize > 0.0d) {
            writer.write(prefix + "\"dataSize\":");
            writer.write(JsonHelper.convert(this.dataSize));
            prefix = ",";
        }

        if (!(this.properties == null)) {
            writer.write(prefix + "\"properties\":");
            JsonHelper.writeDictionary(writer, this.properties);
            prefix = ",";
        }

        if (!(this.measurements == null)) {
            writer.write(prefix + "\"measurements\":");
            JsonHelper.writeDictionary(writer, this.measurements);
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
        QualifiedName = "AI.AvailabilityData";
    }
}
