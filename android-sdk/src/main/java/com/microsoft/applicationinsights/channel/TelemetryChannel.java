package com.microsoft.applicationinsights.channel;

import com.microsoft.applicationinsights.channel.contracts.Data;
import com.microsoft.applicationinsights.channel.contracts.Envelope;
import com.microsoft.applicationinsights.channel.contracts.shared.ITelemetry;
import com.microsoft.applicationinsights.channel.contracts.shared.ITelemetryData;
import com.microsoft.applicationinsights.common.AbstractTelemetryClientConfig;

import java.util.Date;
import java.util.LinkedHashMap;

/**
 * This class records telemetry for application insights.
 */
public class TelemetryChannel {

    /**
     * The configuration for this recorder
     */
    private final IChannelConfig config;

    /**
     * Test hook to the sender
     */
    private Sender sender;

    /**
     * Instantiates a new instance of Sender
     * @param config The configuration for this channel
     */
    public TelemetryChannel(AbstractTelemetryClientConfig config) {
        this.sender = Sender.instance;
        this.config = config;
    }

    /**
     * Sets the sender to be used by this channel
     * @param sender the sender to use for this channel
     */
    public void setSender(Sender sender) {
        this.sender = sender;
    }

    /**
     * @return the sender for this channel.
     */
    public Sender getSender() {
        return this.sender;
    }

    /**
     * Records the passed in data.
     *
     * @param telemetryContext The telemetry telemetryContext for this record
     * @param telemetry The telemetry to record
     */
    public void send(AbstractTelemetryContext telemetryContext, ITelemetry telemetry) {
        // wrap the telemetry data in the common schema data
        Data<ITelemetryData> data = new Data<ITelemetryData>();
        data.setBaseData(telemetry);
        data.setBaseType(telemetry.getBaseType());

        // wrap the data in the common schema envelope
        Envelope envelope = new Envelope();
        envelope.setIKey(this.config.getInstrumentationKey());
        envelope.setData(data);
        envelope.setName(telemetry.getEnvelopeName());
        envelope.setTime(Util.dateToISO8601(new Date()));
        envelope.setTags(telemetryContext.getContextTags());

        // send to queue
        this.sender.enqueue(envelope);
    }
}
