package com.microsoft.applicationinsights.channel;

import android.content.Context;

import com.microsoft.applicationinsights.channel.contracts.Data;
import com.microsoft.applicationinsights.channel.contracts.Envelope;
import com.microsoft.applicationinsights.channel.contracts.shared.ITelemetry;
import com.microsoft.applicationinsights.channel.contracts.shared.ITelemetryData;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class records telemetry for application insights.
 */
public class TelemetryChannel {

    private static final String TAG = "TelemetryChannel";

    /**
     * The configuration for this recorder
     */
    private final TelemetryChannelConfig config;

    /**
     * The context for this recorder
     */
    private final TelemetryContext context;

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
    private TelemetryQueue queue;

    /**
     * Instantiates a new instance of Sender
     *
     * @param config The configuration for this channel
     * @param appContext The app context for this channel
     */
    public TelemetryChannel(TelemetryChannelConfig config, Context appContext) {
        this.queue = TelemetryQueue.instance;
        this.config = config;
        this.context = new TelemetryContext(appContext);

        Random random = new Random();
        this.channelId = Math.abs(random.nextLong());
        this.seqCounter = new AtomicInteger(0);
    }

    /**
     * @return the sender for this channel.
     */
    public TelemetryQueue getQueue() {
        return this.queue;
    }

    /**
     * Test hook to set the queue for this channel
     * @param queue the queue to use for this channel
     */
    protected void setQueue(TelemetryQueue queue) {
        this.queue = queue;
    }

    /**
     * Records the passed in data.
     *
     * @param telemetry The telemetry to record
     */
    public void send(ITelemetry telemetry) {
        this.send(telemetry, null);
    }

    /**
     * Records the passed in data.
     *
     * @param telemetry The telemetry to record
     * @param tags The optional context tags for this telemetry
     */
    public void send(ITelemetry telemetry, LinkedHashMap<String, String> tags) {

        // wrap the data in the common schema envelope
        Envelope envelope = this.getEnvelope(telemetry, tags);

        // send to queue
        this.queue.enqueue(envelope);
    }

    /**
     * Wraps the telemetry item in a common schema envelope with context.
     *
     * @param telemetry The telemetry item to wrap.
     * @param tags The optional context tags for this telemetry
     * @return a contextual Envelope containing the telemetry item.
     */
    protected Envelope getEnvelope(ITelemetry telemetry, LinkedHashMap<String, String> tags) {
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

        envelope.setUserId(this.context.getUser().getId());
        envelope.setDeviceId(this.context.getDevice().getId());
        envelope.setOsVer(this.context.getDevice().getOsVersion());
        envelope.setOs(this.context.getDevice().getOs());
        envelope.setAppId(this.context.getPackageName());
        envelope.setAppVer(this.context.getApplication().getVer());

        if(tags != null) {
            envelope.setTags(tags);
        }

        return envelope;
    }
}
