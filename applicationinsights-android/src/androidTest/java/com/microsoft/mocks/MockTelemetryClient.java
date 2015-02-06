package com.microsoft.mocks;

import android.app.Activity;

import com.microsoft.applicationinsights.TelemetryClient;
import com.microsoft.applicationinsights.TelemetryClientConfig;
import com.microsoft.commonlogging.channel.contracts.shared.ITelemetry;

import java.util.ArrayList;

public class MockTelemetryClient extends TelemetryClient {
    ArrayList<ITelemetry> messages = new ArrayList<ITelemetry>(10);

    public MockTelemetryClient (Activity activity) {
        super(new TelemetryClientConfig(activity));
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