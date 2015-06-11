package com.microsoft.applicationinsights.library;

import com.microsoft.applicationinsights.contracts.Envelope;
import com.microsoft.applicationinsights.contracts.shared.ITelemetry;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
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
        try {
            this.telemetry = (ITelemetry)deepCopy(telemetry);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected TrackDataOperation(DataType type) {
        this.type = type; // no need to copy as enum is pass by value
    }

    protected TrackDataOperation(DataType type, String metricName, double metric) {
        this.type = type; // no need to copy as enum is pass by value
        this.metric = metric;  // no need to copy as enum is pass by value
        try {
            this.name = (String) deepCopy(metricName);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected TrackDataOperation(DataType type,
                                 String name,
                                 Map<String, String> properties,
                                 Map<String, Double> measurements) {
        this.type = type; // no need to copy as enum is pass by value
        try {
            this.name = (String) deepCopy(name);
            if(properties != null) {
                this.properties = new HashMap<String, String>(properties);
            }
            if(measurements != null) {
                this.measurements = new HashMap<String, Double>(measurements);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    protected TrackDataOperation(DataType type,
                                 Throwable exception,
                                 Map<String, String> properties) {
        this.type = type; // no need to copy as enum is pass by value
        try {
            this.exception = (Throwable) deepCopy(exception);
            if(properties != null) {
                this.properties = new HashMap<String, String>(properties);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

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

    private Object deepCopy(Object serializableObject) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        new ObjectOutputStream(outputStream).writeObject(serializableObject);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

        return new ObjectInputStream(inputStream).readObject();
    }
}
