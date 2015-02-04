package com.microsoft.applicationinsights.channel;

import com.microsoft.applicationinsights.TelemetryClientConfig;
import com.microsoft.applicationinsights.channel.contracts.Application;
import com.microsoft.applicationinsights.channel.contracts.Data;
import com.microsoft.applicationinsights.channel.contracts.Device;
import com.microsoft.applicationinsights.channel.contracts.Envelope;
import com.microsoft.applicationinsights.channel.contracts.User;
import com.microsoft.applicationinsights.channel.contracts.shared.ITelemetry;
import com.microsoft.applicationinsights.channel.contracts.shared.ITelemetryData;

import java.util.Date;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class records telemetry for application insights.
 */
public class TelemetryChannel {

    /**
     * The configuration for this recorder
     */
    private final TelemetryClientConfig config;

    /**
     * The id for this channel
     */
    private final long channelId;

    /**
     * The sequence counter for this channel
     */
    private final AtomicInteger seqCounter;

    /**
     * Test hook to the sender
     */
    private Sender sender;

    /**
     * Instantiates a new instance of Sender
     * @param config The configuration for this channel
     */
    public TelemetryChannel(TelemetryClientConfig config) {
        this.sender = Sender.instance;
        this.config = config;

        Random random = new Random();
        this.channelId = Math.abs(random.nextLong());
        this.seqCounter = new AtomicInteger(0);
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
    public void send(TelemetryContext telemetryContext, ITelemetry telemetry) {


        // wrap the data in the common schema envelope
        Envelope envelope = this.getEnvelope(telemetryContext, telemetry);

        // send to queue
        this.sender.enqueue(envelope);
    }

    /**
     * Wraps the telemetry item in a common schema envelope with context.
     *
     * @param telemetryContext The context to use for this envelope.
     * @param telemetry The telemetry item to wrap.
     * @return a contextual Envelope containing the telemetry item.
     */
    protected Envelope getEnvelope(TelemetryContext telemetryContext, ITelemetry telemetry) {
        Envelope envelope = new Envelope();

        // wrap the telemetry data in the common schema data
        Data<ITelemetryData> data = new Data<>();
        data.setBaseData(telemetry);
        data.setBaseType(telemetry.getBaseType());
        envelope.setData(data);

        envelope.setIKey(this.config.getInstrumentationKey());
        envelope.setTime(Util.dateToISO8601(new Date()));
        envelope.setName(telemetry.getEnvelopeName());
        envelope.setSeq(this.channelId + ":" + this.seqCounter.incrementAndGet());

        // TODO read sample rate event's metadata
        //envelope.setSampleRate(sampleRate);

        // TODO set flags
        //envelope.setFlags(SetFlags(persistence, latency));

        User user = telemetryContext.getUser();
        envelope.setUserId(user.getId());

        Device device = telemetryContext.getDevice();
        envelope.setDeviceId(device.getId());
        envelope.setOsVer(device.getOsVersion());
        envelope.setOs(device.getOs());

        Application app = telemetryContext.getApplication();
        envelope.setAppId(app.getId());
        envelope.setAppVer(app.getVer());

        envelope.setTags(telemetryContext.getContextTags());
        return envelope;
    }
}
