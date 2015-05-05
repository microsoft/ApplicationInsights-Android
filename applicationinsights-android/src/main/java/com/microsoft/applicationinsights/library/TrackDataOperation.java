package com.microsoft.applicationinsights.library;

import com.microsoft.applicationinsights.contracts.Envelope;
import com.microsoft.applicationinsights.contracts.shared.ITelemetry;

import java.util.Map;

class TrackDataOperation implements Runnable {

    protected enum DataType {
        NONE,
        EVENT,
        TRACE,
        METRIC,
        PAGE_VIEW,
        HANDLED_EXCEPTION,
        UNHANDLED_EXCEPTION,
        NEW_SESSION
    }

    private String name;
    private Map<String, String> properties;
    private Map<String, Double> measurements;
    private final DataType type;
    private double metric;
    private Throwable exception;
    private ITelemetry telemetry;

    protected TrackDataOperation(ITelemetry telemetry) {
        this.type = DataType.NONE;
        this.telemetry = telemetry;
    }

    protected TrackDataOperation(DataType type) {
        this.type = type;
    }

    protected TrackDataOperation(DataType type, String metricName, double metric) {
        this.type = type;
        this.name = metricName;
        this.metric = metric;
    }

    protected TrackDataOperation(DataType type,
                                 String name,
                                 Map<String, String> properties,
                                 Map<String, Double> measurements) {
        this.type = type;
        this.name = name;
        this.properties = properties;
        this.measurements = measurements;
    }

    protected TrackDataOperation(DataType type,
                                 Throwable exception,
                                 Map<String, String> properties) {
        this.type = type;
        this.exception = exception;
        this.properties = properties;
    }

    @Override
    public void run() {
        Envelope envelope = null;
        if ((this.type == DataType.UNHANDLED_EXCEPTION) && Persistence.getInstance().isFreeSpaceAvailable(true)) {
            envelope = EnvelopeFactory.getInstance().createExceptionEnvelope(this.exception, this.properties);
        } else if (Persistence.getInstance().isFreeSpaceAvailable(false)) {
            switch (this.type) {
                case NONE:
                    if (this.telemetry != null) {
                        envelope = EnvelopeFactory.getInstance().createEnvelope(this.telemetry);
                    }
                    break;
                case EVENT:
                    envelope = EnvelopeFactory.getInstance().createEventEnvelope(this.name, this.properties, this.measurements);
                    break;
                case PAGE_VIEW:
                    envelope = EnvelopeFactory.getInstance().createPageViewEnvelope(this.name, this.properties, this.measurements);
                    break;
                case TRACE:
                    envelope = EnvelopeFactory.getInstance().createTraceEnvelope(this.name, this.properties);
                    break;
                case METRIC:
                    envelope = EnvelopeFactory.getInstance().createMetricEnvelope(this.name, this.metric);
                    break;
                case NEW_SESSION:
                    envelope = EnvelopeFactory.getInstance().createNewSessionEnvelope();
                    break;
                case HANDLED_EXCEPTION:
                case UNHANDLED_EXCEPTION:
                    break;
                default:
                    break;
            }
        }

        if (envelope != null) {
            Channel channel = Channel.getInstance();
            if (type == DataType.UNHANDLED_EXCEPTION) {
                channel.processUnhandledException(envelope);
            } else {
                channel.enqueue(envelope);
            }
        }
    }
}
