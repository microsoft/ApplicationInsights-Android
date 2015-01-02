package com.microsoft.applicationinsights.channel;

import com.microsoft.applicationinsights.channel.contracts.Data;
import com.microsoft.applicationinsights.channel.contracts.Envelope;
import com.microsoft.applicationinsights.channel.contracts.shared.ITelemetry;
import com.microsoft.applicationinsights.channel.contracts.shared.ITelemetryData;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.TimeZone;

/**
 * This class records telemetry for application insights.
 */
public class TelemetryChannel {
    /**
     * TAG for log cat.
     */
    private static final String TAG = "TelemetryChannel";

    /**
     * The configuration for this recorder
     */
    private final IChannelConfig config;

    /**
     * Test hook to the sender
     */
    protected Sender sender;

    /**
     * Properties associated with this telemetryContext.
     */
    public LinkedHashMap<String, String> properties;

    /**
     * Instantiates a new instance of Sender
     * @param config The configuration for this channel
     */
    public TelemetryChannel(IChannelConfig config) {
        this.sender = Sender.instance;
        this.config = config;
        this.properties = null;
    }

    /**
     * Records the passed in data.
     *
     * @param telemetryContext The telemetry telemetryContext for this record
     * @param telemetry The telemetry to record
     * @param envelopeName Value to fill Envelope's Content
     * @param baseType Value to fill Envelope's ItemType field
     */
    public void send(TelemetryContext telemetryContext,
                       ITelemetry telemetry,
                       String envelopeName,
                       String baseType) {

        // add common properties to this telemetry object
        if(this.properties != null) {
            LinkedHashMap<String, String> map = telemetry.getProperties();
            if(map != null) {
                map.putAll(this.properties);
            }

            telemetry.setProperties(map);
        }

        // wrap the telemetry data in the common schema data
        Data<ITelemetryData> data = new Data<ITelemetryData>();
        data.setBaseData(telemetry);
        data.setBaseType(baseType);

        // wrap the data in the common schema envelope
        Envelope envelope = new Envelope();
        envelope.setIKey(this.config.getInstrumentationKey());
        envelope.setData(data);
        envelope.setName(envelopeName);
        envelope.setTime(Util.dateToISO8601(new Date()));
        envelope.setTags(telemetryContext.getContextTags());

        // send to queue
        this.sender.enqueue(envelope);
    }
}
