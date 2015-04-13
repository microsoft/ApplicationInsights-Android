package com.microsoft.mocks;

import android.content.Context;

import com.microsoft.applicationinsights.TelemetryClient;
import com.microsoft.applicationinsights.SessionConfig;
import com.microsoft.applicationinsights.contracts.Envelope;
import com.microsoft.applicationinsights.internal.EnvelopeFactory;
import com.microsoft.applicationinsights.internal.logging.InternalLogging;

import java.util.ArrayList;
import java.util.Map;

public class MockTelemetryClient extends TelemetryClient {
    public ArrayList<Envelope> messages;
    public boolean mockTrackMethod;

//    private static MockTelemetryClient instance;
//
//    /**
//     * Volatile boolean for double checked synchronize block
//     */
//    private static volatile boolean isTelemetryClientLoaded = false;
//
//    /**
//     * Synchronization LOCK for setting static context
//     */
//    private static final Object LOCK = new Object();
//
//    /**
//     * Restrict access to the default constructor
//     */
//    protected MockTelemetryClient() {
//    }
//
//    /**
//     * Initialize the INSTANCE of the telemetryclient
//     */
//    protected static void initialize() {
//        // note: isPersistenceLoaded must be volatile for the double-checked LOCK to work
//        if (!MockTelemetryClient.isTelemetryClientLoaded) {
//            synchronized (MockTelemetryClient.LOCK) {
//                if (!MockTelemetryClient.isTelemetryClientLoaded) {
//                    MockTelemetryClient.isTelemetryClientLoaded = true;
//                    MockTelemetryClient.instance = new MockTelemetryClient();
//                }
//            }
//        }
//    }

    /**
     * @return the INSTANCE of persistence or null if not yet initialized
     */
    public static MockTelemetryClient getInstance() {
//        initialize();
//        if (MockTelemetryClient.instance == null) {
//            InternalLogging.error(TAG, "getInstance was called before initialization");
//        }
//
//        return MockTelemetryClient.instance;

        return (MockTelemetryClient)TelemetryClient.getInstance();
    }


    //TODO fix unit tests

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
}
