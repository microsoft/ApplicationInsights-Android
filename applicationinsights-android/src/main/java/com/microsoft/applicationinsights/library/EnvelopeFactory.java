package com.microsoft.applicationinsights.library;


import com.microsoft.applicationinsights.ApplicationInsights;
import com.microsoft.applicationinsights.contracts.CrashData;
import com.microsoft.applicationinsights.contracts.CrashDataHeaders;
import com.microsoft.applicationinsights.contracts.CrashDataThread;
import com.microsoft.applicationinsights.contracts.CrashDataThreadFrame;
import com.microsoft.applicationinsights.contracts.Data;
import com.microsoft.applicationinsights.contracts.DataPoint;
import com.microsoft.applicationinsights.contracts.DataPointType;
import com.microsoft.applicationinsights.contracts.Envelope;
import com.microsoft.applicationinsights.contracts.EventData;
import com.microsoft.applicationinsights.contracts.MessageData;
import com.microsoft.applicationinsights.contracts.MetricData;
import com.microsoft.applicationinsights.contracts.PageViewData;
import com.microsoft.applicationinsights.contracts.SessionState;
import com.microsoft.applicationinsights.contracts.SessionStateData;
import com.microsoft.applicationinsights.contracts.shared.ITelemetry;
import com.microsoft.applicationinsights.contracts.shared.ITelemetryData;
import com.microsoft.applicationinsights.logging.InternalLogging;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

enum EnvelopeFactory {
    INSTANCE;

    protected static final int CONTRACT_VERSION = 2;

    /**
     * The tag for logging
     */
    private static final String TAG = "EnvelopeManager";

    /**
     * Flag which determines, if the EnvelopeManager has been configured, yet
     */
    private boolean configured;

    /**
     * The context for this recorder
     */
    private TelemetryContext context;

    /**
     * Map of properties, which should be set for each envelope
     */
    private Map<String, String> commonProperties;

    /**
     * Configures the shared instance with a telemetry context, which is needed to create envelops.
     * Warning: Method should be called before creating envelops.
     *
     * @param context the telemetry context, which is used to create envelops with proper context information.
     */
    protected void configure(TelemetryContext context) {
        this.configure(context, null);
    }

    /**
     * Configures the shared instance with a telemetry context, which is needed to create envelops.
     * Warning: Method should be called before creating envelops.
     *
     * @param context          the telemetry context, which is used to create envelops with proper context information.
     * @param commonProperties Map of properties
     */
    protected void configure(TelemetryContext context, Map<String, String> commonProperties) {
        this.context = context;
        this.commonProperties = commonProperties;
        this.configured = true;
    }

    /**
     * Create an envelope template
     *
     * @return the envelope used for telemetry
     */
    protected Envelope createEnvelope() {
        Envelope envelope = new Envelope();
        this.context.setScreenResolution(ApplicationInsights.INSTANCE.getContext());
        envelope.setAppId(this.context.getPackageName());
        envelope.setAppVer(this.context.getApplication().getVer());
        envelope.setTime(Util.dateToISO8601(new Date()));
        envelope.setIKey(this.context.getInstrumentationKey());
        envelope.setUserId(this.context.getUser().getId());
        envelope.setDeviceId(this.context.getDevice().getId());
        envelope.setOsVer(this.context.getDevice().getOsVersion());
        envelope.setOs(this.context.getDevice().getOs());

        Map<String, String> tags = this.context.getContextTags();
        if (tags != null) {
            envelope.setTags(tags);
        }
        return envelope;
    }

    /**
     * Create an envelope with the given object as its base data
     *
     * @param telemetryData The telemetry we want to wrap inside an Enevelope and send to the server
     * @return the envelope that includes the telemetry data
     */
    protected Envelope createEnvelope(ITelemetry telemetryData) {
        addCommonProperties(telemetryData);

        Data<ITelemetryData> data = new Data<ITelemetryData>();
        data.setBaseData(telemetryData);
        data.setBaseType(telemetryData.getBaseType());

        Envelope envelope = createEnvelope();
        envelope.setData(data);
        envelope.setName(telemetryData.getEnvelopeName());

        // todo: read sample rate from settings store and set sampleRate(percentThrottled)
        // todo: set flags from settings store and set flags(persistence, latency)
        //envelope.setSeq(this.channelId + ":" + this.seqCounter.incrementAndGet());

        return envelope;
    }

    /**
     * Creates information about an event for Application Insights. This method gets called by a
     * CreateTelemetryDataTask in order to create and forward data on a background thread.
     *
     * @param eventName    The name of the event
     * @param properties   Custom properties associated with the event
     * @param measurements Custom measurements associated with the event
     * @return an Envelope object, which contains an event
     */
    protected Envelope createEventEnvelope(String eventName,
                                        Map<String, String> properties,
                                        Map<String, Double> measurements) {
        Envelope envelope = null;
        if (isConfigured()) {
            EventData telemetry = new EventData();
            telemetry.setName(ensureNotNull(eventName));
            telemetry.setProperties(properties);
            telemetry.setMeasurements(measurements);

            envelope = createEnvelope(telemetry);
        }
        return envelope;
    }

    /**
     * Creates tracing information for Application Insights. This method gets called by a
     * CreateTelemetryDataTask in order to create and forward data on a background thread.
     *
     * @param message    The message associated with this trace
     * @param properties Custom properties associated with the event
     * @return an Envelope object, which contains a trace
     */
    protected Envelope createTraceEnvelope(String message, Map<String, String> properties) {
        Envelope envelope = null;
        if (isConfigured()) {
            MessageData telemetry = new MessageData();
            telemetry.setMessage(this.ensureNotNull(message));
            telemetry.setProperties(properties);

            envelope = createEnvelope(telemetry);
        }
        return envelope;
    }

    /**
     * Creates information about an aggregated metric for Application Insights. This method gets
     * called by a CreateTelemetryDataTask in order to create and forward data on a background thread.
     *
     * @param name  The name of the metric
     * @param value The value of the metric
     * @return an Envelope object, which contains a metric
     */
    protected Envelope createMetricEnvelope(String name, double value) {
        Envelope envelope = null;
        if (isConfigured()) {
            MetricData telemetry = new MetricData();
            DataPoint data = new DataPoint();
            data.setCount(1);
            data.setKind(DataPointType.Measurement);
            data.setMax(value);
            data.setMax(value);
            data.setName(ensureNotNull(name));
            data.setValue(value);
            List<DataPoint> metricsList = new ArrayList<DataPoint>();
            metricsList.add(data);
            telemetry.setMetrics(metricsList);

            envelope = createEnvelope(telemetry);
        }
        return envelope;
    }

    /**
     * Creates information about an handled or unhandled exception to Application Insights. This
     * method gets called by a CreateTelemetryDataTask in order to create and forward data on a
     * background thread.
     *
     * @param exception  The exception to track
     * @param properties Custom properties associated with the event
     * @return an Envelope object, which contains a handled or unhandled exception
     */
    protected Envelope createExceptionEnvelope(Throwable exception, Map<String, String> properties) {
        Envelope envelope = null;
        if (isConfigured()) {
            CrashData telemetry = this.getCrashData(exception, properties);

            envelope = createEnvelope(telemetry);
        }
        return envelope;
    }

    /**
     * Creates information about a page view for Application Insights. This method gets called by a
     * CreateTelemetryDataTask in order to create and forward data on a background thread.
     *
     * @param pageName     The name of the page
     * @param properties   Custom properties associated with the event
     * @param measurements Custom measurements associated with the event
     * @return an Envelope object, which contains a page view
     */
    protected Envelope createPageViewEnvelope(
          String pageName,
          Map<String, String> properties,
          Map<String, Double> measurements) {
        Envelope envelope = null;
        if (isConfigured()) {
            PageViewData telemetry = new PageViewData();
            telemetry.setName(ensureNotNull(pageName));
            telemetry.setUrl(null);
            telemetry.setProperties(properties);
            telemetry.setMeasurements(measurements);

            envelope = createEnvelope(telemetry);
        }
        return envelope;
    }

    /**
     * Creates information about a new session view for Application Insights. This method gets
     * called by a CreateTelemetryDataTask in order to create and forward data on a background thread.
     *
     * @return an Envelope object, which contains a session
     */
    protected Envelope createNewSessionEnvelope() {
        Envelope envelope = null;
        if (isConfigured()) {
            SessionStateData telemetry = new SessionStateData();
            telemetry.setState(SessionState.Start);

            envelope = createEnvelope(telemetry);
        }
        return envelope;
    }

    /**
     * Adds common properties to the given telemetry data.
     *
     * @param telemetry The telemetry data
     */
    protected void addCommonProperties(ITelemetry telemetry) {
        telemetry.setVer(CONTRACT_VERSION);
        if (this.commonProperties != null) {
            Map<String, String> map = telemetry.getProperties();
            if (map != null) {
                map.putAll(this.commonProperties);
            }
            telemetry.setProperties(map);
        }
    }

    /**
     * Ensures required string values are non-null
     */
    private String ensureNotNull(String input) {
        if (input == null) {
            return "";
        } else {
            return input;
        }
    }

    /**
     * Set properties, which should be set for each envelope.
     *
     * @param commonProperties a map with properties, which should be set for each envelope
     */
    protected void setCommonProperties(Map<String, String> commonProperties) {
        this.commonProperties = commonProperties;
    }


    /**
     * Parse an exception and it's stack trace and create the CrashData object
     *
     * @param exception  the throwable object we want to create a crashdata from
     * @param properties properties used foor the CrashData
     * @return a CrashData object that contains the stacktrace and context info
     */
    private CrashData getCrashData(Throwable exception, Map<String, String> properties) {
        Throwable localException = exception;
        if (localException == null) {
            localException = new Exception();
        }

        // TODO: set handledAt - Is of relevance in future releases, not at the moment
        // read stack frames
        List<CrashDataThreadFrame> stackFrames = new ArrayList<CrashDataThreadFrame>();
        StackTraceElement[] stack = localException.getStackTrace();
        for (int i = 0; i < stack.length - 1; i++) {
            StackTraceElement rawFrame = stack[i];
            CrashDataThreadFrame frame = new CrashDataThreadFrame();
            frame.setSymbol(rawFrame.toString());
            stackFrames.add(frame);
            frame.setAddress("");
        }

        CrashDataThread crashDataThread = new CrashDataThread();
        crashDataThread.setFrames(stackFrames);
        List<CrashDataThread> threads = new ArrayList<CrashDataThread>(1);
        threads.add(crashDataThread);

        CrashDataHeaders crashDataHeaders = new CrashDataHeaders();
        crashDataHeaders.setId(UUID.randomUUID().toString());

        String message = localException.getMessage();
        crashDataHeaders.setExceptionReason(ensureNotNull(message));
        crashDataHeaders.setExceptionType(localException.getClass().getName());
        crashDataHeaders.setApplicationPath(this.context.getPackageName());

        CrashData crashData = new CrashData();
        crashData.setThreads(threads);
        crashData.setHeaders(crashDataHeaders);
        crashData.setProperties(properties);

        return crashData;
    }

    protected boolean isConfigured() {
        if (!configured) {
            InternalLogging.warn(TAG, "Could not create telemetry data. You have to setup & start ApplicationInsights first.");
        }
        return configured;
    }
}
