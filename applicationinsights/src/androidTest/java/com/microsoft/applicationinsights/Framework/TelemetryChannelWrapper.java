package com.microsoft.applicationinsights.Framework;

import com.microsoft.applicationinsights.channel.ITelemetryContext;
import com.microsoft.applicationinsights.channel.TelemetryChannel;
import com.microsoft.applicationinsights.channel.TelemetryContext;
import com.microsoft.applicationinsights.channel.IChannelConfig;
import com.microsoft.applicationinsights.channel.Sender;
import com.microsoft.applicationinsights.channel.contracts.shared.ITelemetry;

/**
 * Created by applicationinsights on 12/15/14.
 */
public class TelemetryChannelWrapper extends TelemetryChannel {

    /**
     * Instantiates a new instance of Recorder
     * @param config The configuration for this recorder
     */
    public TelemetryChannelWrapper(IChannelConfig config, Sender sender) {
        super(config, sender);
    }

    /**
     * Records the passed in data.
     *
     * @param telemetryContext The telemetry telemetryContext for this record
     * @param telemetry The telemetry to record
     * @param envelopeName Value to fill Envelope's Content
     * @param baseType Value to fill Envelope's ItemType field
     */
    @Override
    public void send(ITelemetryContext telemetryContext,
                     ITelemetry telemetry,
                     String envelopeName,
                     String baseType) {
        super.send(telemetryContext, telemetry, envelopeName, baseType);
    }
}
