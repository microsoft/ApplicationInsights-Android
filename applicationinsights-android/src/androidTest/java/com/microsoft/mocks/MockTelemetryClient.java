package com.microsoft.mocks;

import android.content.Context;

import com.microsoft.applicationinsights.TelemetryClient;
import com.microsoft.applicationinsights.TelemetryClientConfig;
import com.microsoft.applicationinsights.channel.contracts.shared.ITelemetry;

import java.util.ArrayList;

public class MockTelemetryClient extends TelemetryClient {
    ArrayList<ITelemetry> messages = new ArrayList<ITelemetry>(10);

    public MockTelemetryClient (Context context, String iKey) {
        super(new TelemetryClientConfig(iKey, context));
    }

    @Override
    public void track(ITelemetry telemetry) {
        messages.add(telemetry);
    }

    public ArrayList<ITelemetry> getMessages()
    {
        return messages;
    }

    public void clearMessages()
    {
        messages.clear();
    }
}