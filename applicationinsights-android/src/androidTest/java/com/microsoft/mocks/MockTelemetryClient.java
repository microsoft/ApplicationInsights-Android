package com.microsoft.mocks;

import android.content.Context;

import com.microsoft.applicationinsights.TelemetryClient;
import com.microsoft.applicationinsights.SessionConfig;
import com.microsoft.applicationinsights.contracts.Envelope;
import com.microsoft.applicationinsights.internal.EnvelopeFactory;
import com.microsoft.applicationinsights.internal.TelemetryContext;
import java.util.ArrayList;
import java.util.Map;

public class MockTelemetryClient extends TelemetryClient {
    public ArrayList<Envelope> messages;
    public boolean mockTrackMethod;

    public MockTelemetryClient (Context context) {
        this(new SessionConfig(context), context);
        this.messages = new ArrayList<Envelope>(10);
        this.mockTrackMethod = true;
    }

    protected MockTelemetryClient(SessionConfig config, Context context) {
        super(config, new TelemetryContext(context, config.getInstrumentationKey()), new MockChannel());
        ((MockChannel)this.channel).setQueue(new MockQueue(1));
    }

    @Override
    public void trackEvent(
            String eventName,
            Map<String, String> properties,
            Map<String, Double> measurements) {
        if(this.mockTrackMethod) {
            messages.add(EnvelopeFactory.INSTANCE.createEventEnvelope(eventName, properties, measurements));
        }else{
            super.trackEvent(eventName, properties, measurements);
        }
    }

    @Override
    public void trackTrace(String message, Map<String, String> properties) {
        if(this.mockTrackMethod) {
            messages.add(EnvelopeFactory.INSTANCE.createTraceEnvelope(message, properties));
        }else{
            super.trackTrace(message, properties);
        }
    }

    @Override
    public void trackMetric(String name, double value) {
        if(this.mockTrackMethod) {
            messages.add(EnvelopeFactory.INSTANCE.createMetricEnvelope(name, value));
        }else{
            super.trackMetric(name, value);
        }
    }

    @Override
    public void trackHandledException(Throwable handledException, Map<String, String> properties) {
        if(this.mockTrackMethod) {
            messages.add(EnvelopeFactory.INSTANCE.createExceptionEnvelope(handledException, properties));
        }else{
            super.trackHandledException(handledException, properties);
        }
    }

    @Override
    public void trackUnhandledException(Throwable unhandledException, Map<String, String> properties) {
        if(this.mockTrackMethod) {
            messages.add(EnvelopeFactory.INSTANCE.createExceptionEnvelope(unhandledException, properties));
        }else{
            super.trackUnhandledException(unhandledException, properties);
        }
    }

    @Override
    public void trackPageView(
            String pageName,
            Map<String, String> properties,
            Map<String, Double> measurements) {
        if(this.mockTrackMethod) {
            messages.add(EnvelopeFactory.INSTANCE.createPageViewEnvelope(pageName, properties, measurements));
        }else{
            super.trackPageView(pageName, properties, measurements);
        }
    }

    @Override
    public void trackNewSession() {
        if(this.mockTrackMethod) {
            messages.add(EnvelopeFactory.INSTANCE.createNewSessionEnvelope());
        }else{
            super.trackNewSession();
        }
    }

    public ArrayList<Envelope> getMessages()
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
