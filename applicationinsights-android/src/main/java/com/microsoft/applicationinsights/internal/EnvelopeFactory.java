package com.microsoft.applicationinsights.internal;


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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public enum EnvelopeFactory {
    INSTANCE;

    public static final int CONTRACT_VERSION = 2;

    /**
     * The context for this recorder
     */
    private TelemetryContext context;

    /**
     * Map of properties, which should be set for each envelope
     */
    private Map<String,String> commonProperties;

    /**
     * Configures the shared instance with a telemetry context, which is needed to create envelops.
     * Warning: Method should be called before creating envelops.
     *
     * @param context the telemetry context, which is used to create envelops with proper context information.
     */
    public void configureWithTelemetryContext(TelemetryContext context){
        this.configureWithTelemetryContext(context, null);
    }

    /**
     * Configures the shared instance with a telemetry context, which is needed to create envelops.
     * Warning: Method should be called before creating envelops.
     *
     * @param context the telemetry context, which is used to create envelops with proper context information.
     */
    public void configureWithTelemetryContext(TelemetryContext context, Map<String,String>commonProperties){
        this.context = context;
        this.commonProperties = commonProperties;
    }

    /**
     * Create an envelope template
     */
    public Envelope createEnvelope() {
        Envelope envelope = new Envelope();
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
     */
    public Envelope createEnvelope(ITelemetry telemetryData){
        addCommonProperties(telemetryData);

        Data<ITelemetryData> data = new Data<>();
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
     *
     * @return an Envelope object, which contains an event
     */
    public Envelope createEventEnvelope(String eventName,
                                  Map<String, String> properties,
                                  Map<String, Double> measurements) {
        EventData telemetry = new EventData();
        telemetry.setName(ensureNotNull(eventName));
        telemetry.setProperties(properties);
        telemetry.setMeasurements(measurements);

        Envelope envelope = createEnvelope(telemetry);
        return envelope;
    }

    /**
     * Creates tracing information for Application Insights. This method gets called by a
     * CreateTelemetryDataTask in order to create and forward data on a background thread.
     *
     * @param message    The message associated with this trace
     * @param properties Custom properties associated with the event
     *
     * @return an Envelope object, which contains a trace
     */
    public Envelope createTraceEnvelope(String message, Map<String, String> properties) {
        MessageData telemetry = new MessageData();
        telemetry.setMessage(this.ensureNotNull(message));
        telemetry.setProperties(properties);

        Envelope envelope = createEnvelope(telemetry);
        return envelope;
    }

    /**
     * Creates information about an aggregated metric for Application Insights. This method gets
     * called by a CreateTelemetryDataTask in order to create and forward data on a background thread.
     *
     * @param name  The name of the metric
     * @param value The value of the metric
     *
     * @return an Envelope object, which contains a metric
     */
    public Envelope createMetricEnvelope(String name, double value) {
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

        Envelope envelope = createEnvelope(telemetry);
        return envelope;
    }

    /**
     * Creates information about an handled or unhandled exception to Application Insights. This
     * method gets called by a CreateTelemetryDataTask in order to create and forward data on a
     * background thread.
     *
     * @param exception  The exception to track
     * @param properties Custom properties associated with the event
     *
     * @return an Envelope object, which contains a handled or unhandled exception
     */
    public Envelope createExceptionEnvelope(Throwable exception, Map<String, String> properties) {
        CrashData telemetry = this.getCrashData(exception, properties);

        Envelope envelope = createEnvelope(telemetry);
        return envelope;
    }

    /**
     * Creates information about a page view for Application Insights. This method gets called by a
     * CreateTelemetryDataTask in order to create and forward data on a background thread.
     *
     * @param pageName     The name of the page
     * @param properties   Custom properties associated with the event
     * @param measurements Custom measurements associated with the event
     *
     * @return an Envelope object, which contains a page view
     */
    public Envelope createPageViewEnvelope(
            String pageName,
            Map<String, String> properties,
            Map<String, Double> measurements) {
        PageViewData telemetry = new PageViewData();
        telemetry.setName(ensureNotNull(pageName));
        telemetry.setUrl(null);
        telemetry.setProperties(properties);
        telemetry.setMeasurements(measurements);

        Envelope envelope = createEnvelope(telemetry);
        return envelope;
    }

    /**
     * Creates information about a new session view for Application Insights. This method gets
     * called by a CreateTelemetryDataTask in order to create and forward data on a background thread.
     *
     * @return an Envelope object, which contains a session
     */
    public Envelope createNewSessionEnvelope() {
        SessionStateData telemetry = new SessionStateData();
        telemetry.setState(SessionState.Start);

        Envelope envelope = createEnvelope(telemetry);
        return envelope;
    }

    /**
     * Adds common properties to the given telemetry data.
     *
     * @param telemetry The telemetry data
     */
    private void addCommonProperties(ITelemetry telemetry){
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
    public void setCommonProperties(Map<String, String> commonProperties) {
        this.commonProperties = commonProperties;
    }


    /**
     * Parse an exception and it's stack trace and create the CrashData object
     * @param exception the throwable object we want to create a crashdata from
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
        List<CrashDataThreadFrame> stackFrames = new ArrayList<>();
        StackTraceElement[] stack = localException.getStackTrace();
        for (int i = stack.length - 1; i >= 0; i--) {
            StackTraceElement rawFrame = stack[i];
            CrashDataThreadFrame frame = new CrashDataThreadFrame();
            frame.setSymbol(rawFrame.toString());
            stackFrames.add(frame);
            frame.setAddress("");
        }

        CrashDataThread crashDataThread = new CrashDataThread();
        crashDataThread.setFrames(stackFrames);
        List<CrashDataThread> threads = new ArrayList<>(1);
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
}
