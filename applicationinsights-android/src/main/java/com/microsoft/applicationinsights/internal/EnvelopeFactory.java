package com.microsoft.applicationinsights.internal;


import com.microsoft.applicationinsights.contracts.CrashData;
import com.microsoft.applicationinsights.contracts.Data;
import com.microsoft.applicationinsights.contracts.Envelope;
import com.microsoft.applicationinsights.contracts.shared.IJsonSerializable;
import com.microsoft.applicationinsights.contracts.shared.ITelemetry;
import com.microsoft.applicationinsights.contracts.shared.ITelemetryData;
import com.microsoft.applicationinsights.internal.logging.InternalLogging;

import java.util.Date;
import java.util.Map;

public enum EnvelopeFactory {
    INSTANCE;

    static final int schemaVersion = 2;

    /**
     * The context for this recorder
     */
    private TelemetryContext context;

    public void configureWithTelemetryContext(TelemetryContext context){
        this.context = context;
    }

    /**
     * Create an envelope template
     */
    private Envelope createEnvelope() {
        Envelope envelope = new Envelope();
        envelope.setAppId(this.context.getPackageName());
        envelope.setAppVer(this.context.getApplication().getVer());
        envelope.setTime(Util.dateToISO8601(new Date()));
        envelope.setIKey(this.context.getInstrumentationKey());
        envelope.setUserId(this.context.getUser().getId());
        envelope.setDeviceId(this.context.getDevice().getId());
        envelope.setOsVer(this.context.getDevice().getOsVersion());
        envelope.setOs(this.context.getDevice().getOs());

        Map<String, String> tags = this.context.getContextTags();
        if (tags != null) {
            envelope.setTags(tags);
        }
        return envelope;
    }

    /**
     * Create an envelope with the given object as its base data
     */
    public Envelope createEnvelope(ITelemetry telemetryData){
        telemetryData.setVer(schemaVersion);

        Data<ITelemetryData> data = new Data<>();
        data.setBaseData(telemetryData);
        data.setBaseType(telemetryData.getBaseType());

        Envelope envelope = createEnvelope();
        envelope.setData(data);
        envelope.setName(telemetryData.getEnvelopeName());

        // todo: read sample rate from settings store and set sampleRate(percentThrottled)
        // todo: set flags from settings store and set flags(persistence, latency)
        //envelope.setSeq(this.channelId + ":" + this.seqCounter.incrementAndGet());

        return envelope;
    }
}
