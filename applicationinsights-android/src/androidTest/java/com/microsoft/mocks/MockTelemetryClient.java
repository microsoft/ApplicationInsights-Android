package com.microsoft.mocks;

import android.content.Context;

import com.microsoft.applicationinsights.TelemetryClient;
import com.microsoft.applicationinsights.TelemetryClientConfig;
import com.microsoft.applicationinsights.channel.TelemetryContext;
import com.microsoft.applicationinsights.channel.contracts.shared.ITelemetry;

import java.util.ArrayList;

public class MockTelemetryClient extends TelemetryClient {
    public ArrayList<ITelemetry> messages;
    public boolean mockTrackMethod;

    public MockTelemetryClient (Context context) {
        this(new TelemetryClientConfig(context), context);
        this.messages = new ArrayList<ITelemetry>(10);
        this.mockTrackMethod = true;
    }

    protected MockTelemetryClient(TelemetryClientConfig config, Context context) {
        super(config, new TelemetryContext(context), new MockChannel(config, context));
        ((MockChannel)this.channel).setQueue(new MockQueue(1));
    }

    @Override
    public void track(ITelemetry telemetry) {
        if(this.mockTrackMethod) {
            messages.add(telemetry);
        } else {
            super.track(telemetry);
        }
    }

    public ArrayList<ITelemetry> getMessages()
    {
        return messages;
    }

    public void clearMessages()
    {
        messages.clear();
    }

    public MockChannel getChannel() {
        return (MockChannel)this.channel;
    }
}
