package com.microsoft.applicationinsights.library;

import com.microsoft.telemetry.Data;
import com.microsoft.telemetry.Domain;

import java.util.ArrayList;
import java.util.Map;

public class MockTelemetryClient extends TelemetryClient {
    public ArrayList<Data<Domain>> messages;
    public boolean mockTrackMethod;

    /**
     * Restrict access to the default constructor
     *
     * @param telemetryEnabled YES if tracking telemetry data manually should be enabled
     */
    protected MockTelemetryClient(boolean telemetryEnabled) {
        super(telemetryEnabled);
    }

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
        MockTelemetryClient mClient = new MockTelemetryClient(true);
        mClient.messages = new ArrayList<Data<Domain>>();
        return mClient;
    }


    //TODO fix unit tests

    @Override
    public void trackEvent(
            String eventName,
            Map<String, String> properties,
            Map<String, Double> measurements) {
        if(this.mockTrackMethod) {
            messages.add(EnvelopeFactory.getInstance().createEventData(eventName, properties, measurements));
        }else{
            super.trackEvent(eventName, properties, measurements);
        }
    }

    @Override
    public void trackTrace(String message, Map<String, String> properties) {
        if(this.mockTrackMethod) {
            messages.add(EnvelopeFactory.getInstance().createTraceData(message, properties));
        }else{
            super.trackTrace(message, properties);
        }
    }

    @Override
    public void trackMetric(String name, double value) {
        if(this.mockTrackMethod) {
            messages.add(EnvelopeFactory.getInstance().createMetricData(name, value, null));
        }else{
            super.trackMetric(name, value);
        }
    }

    @Override
    public void trackHandledException(Throwable handledException, Map<String, String> properties) {
        if(this.mockTrackMethod) {
            messages.add(EnvelopeFactory.getInstance().createExceptionData(handledException, properties));
        }else{
            super.trackHandledException(handledException, properties);
        }
    }

    @Override
    public void trackPageView(
            String pageName,
            String duration,
            Map<String, String> properties,
            Map<String, Double> measurements) {
        if(this.mockTrackMethod) {
            messages.add(EnvelopeFactory.getInstance().createPageViewData(pageName, duration, properties, measurements));
        }else{
            super.trackPageView(pageName, duration, properties, measurements);
        }
    }

    @Override
    public void trackNewSession() {
        if(this.mockTrackMethod) {
            messages.add(EnvelopeFactory.getInstance().createNewSessionData());
        }else{
            super.trackNewSession();
        }
    }

    public ArrayList<Data<Domain>> getMessages()
    {
        return messages;
    }

    public void clearMessages()
    {
        messages.clear();
    }
}
