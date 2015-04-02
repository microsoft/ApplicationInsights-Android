package com.microsoft.mocks;

import android.content.Context;

import com.microsoft.applicationinsights.ExceptionUtil;
import com.microsoft.applicationinsights.TelemetryClient;
import com.microsoft.applicationinsights.TelemetryClientConfig;
import com.microsoft.applicationinsights.internal.TelemetryContext;
import com.microsoft.applicationinsights.contracts.CrashData;
import com.microsoft.applicationinsights.contracts.shared.ITelemetry;

import java.util.ArrayList;
import java.util.Map;

public class MockTelemetryClient extends TelemetryClient {
    public ArrayList<ITelemetry> messages;
    public boolean mockTrackMethod;

    public MockTelemetryClient (Context context) {
        this(new TelemetryClientConfig(context), context);
        this.messages = new ArrayList<ITelemetry>(10);
        this.mockTrackMethod = true;
    }

    protected MockTelemetryClient(TelemetryClientConfig config, Context context) {
        super(config, new TelemetryContext(context, config.getInstrumentationKey()), new MockChannel());
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

    @Override
    public void trackUnhandledException(Throwable unhandledException, Map<String, String> properties) {
        if(this.mockTrackMethod) {
            CrashData data = ExceptionUtil.getCrashData(unhandledException, properties, context.getPackageName()); //TODO mock this one for real
            messages.add(data);
        }
        else {
            super.trackUnhandledException(unhandledException, properties);
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
