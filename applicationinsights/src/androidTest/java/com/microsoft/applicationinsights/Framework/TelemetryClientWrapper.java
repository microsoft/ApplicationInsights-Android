package com.microsoft.applicationinsights.Framework;

import com.microsoft.applicationinsights.AndroidTelemetryClient;
import com.microsoft.applicationinsights.channel.Sender;
import com.microsoft.applicationinsights.channel.contracts.shared.ITelemetry;

public class TelemetryClientWrapper extends AndroidTelemetryClient {

    public TelemetryChannelWrapper channel;

    /**
     * Constructor of the class TelemetryClient.
     *
     * @param iKey the instrumentation key
     * @param context application telemetryContext from the caller
     */
    public TelemetryClientWrapper(String iKey, android.content.Context context, Sender sender) {
        super(iKey, context);
        this.channel = new TelemetryChannelWrapper(this.config, sender);
    }

    /**
     * send message to the recorder.
     *
     * @param telemetry telemetry object
     * @param itemDataType data type
     * @param itemType item type
     */
    @Override
    protected void track(ITelemetry telemetry, String itemDataType, String itemType) {
        this.channel.send(this.telemetryContext, telemetry, itemDataType, itemType);
    }
}
