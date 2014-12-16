package com.microsoft.applicationinsights.Framework;

import com.microsoft.applicationinsights.channel.Channel;
import com.microsoft.applicationinsights.channel.Context;
import com.microsoft.applicationinsights.channel.IChannelConfig;
import com.microsoft.applicationinsights.channel.Sender;
import com.microsoft.applicationinsights.channel.contracts.shared.ITelemetry;

/**
 * Created by applicationinsights on 12/15/14.
 */
public class ChannelWrapper extends Channel {

    public SenderWrapper sender;

    /**
     * Instantiates a new instance of Recorder
     * @param config The configuration for this recorder
     */
    public ChannelWrapper(IChannelConfig config, Sender sender) {
        super(config, sender);
    }

    /**
     * Records the passed in data.
     *
     * @param context The telemetry context for this record
     * @param telemetry The telemetry to record
     * @param envelopeName Value to fill Envelope's Content
     * @param baseType Value to fill Envelope's ItemType field
     */
    @Override
    public void send(Context context,
                     ITelemetry telemetry,
                     String envelopeName,
                     String baseType) {
        super.send(context, telemetry, envelopeName, baseType);
    }
}
