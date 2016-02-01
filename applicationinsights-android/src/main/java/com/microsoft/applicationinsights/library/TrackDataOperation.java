package com.microsoft.applicationinsights.library;

import com.microsoft.applicationinsights.contracts.TelemetryData;
import com.microsoft.telemetry.Data;
import com.microsoft.telemetry.Domain;
import com.microsoft.telemetry.IChannel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

class TrackDataOperation implements Runnable {

    // common
    private final DataType type;
    private String name;
    private Map<String, String> properties;
    private Map<String, Double> measurements;
    // managed exceptions
    private String exceptionMessage;
    private String exceptionStacktrace;
    private boolean handled;
    // metric
    private double metric;
    // unmanaged exceptions
    private Throwable exception;
    // page views
    private long duration;
    // custom
    private TelemetryData telemetry;

    protected TrackDataOperation(TelemetryData telemetry) {
        this.type = DataType.NONE;
        try {
            this.telemetry = (TelemetryData) deepCopy(telemetry);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected TrackDataOperation(DataType type, String name) {
        this.type = type;
        try {
            this.name = (String) deepCopy(name);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected TrackDataOperation(DataType type) {
        this.type = type; // no need to copy as enum is pass by value
    }

    protected TrackDataOperation(DataType type, String metricName, double metric, Map<String, String> properties) {
        this(type, metricName, properties, null);

        this.metric = metric;  // no need to copy as enum is pass by value
    }

    protected TrackDataOperation(DataType type,
                                 String name,
                                 Map<String, String> properties,
                                 Map<String, Double> measurements) {
        this.type = type; // no need to copy as enum is pass by value
        try {
            this.name = (String) deepCopy(name);
            if (properties != null) {
                this.properties = new HashMap<String, String>(properties);
            }
            if (measurements != null) {
                this.measurements = new HashMap<String, Double>(measurements);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    protected TrackDataOperation(DataType type,
                                 String name,
                                 long duration,
                                 Map<String, String> properties,
                                 Map<String, Double> measurements) {
        this(type, name, properties, measurements);
        this.duration = duration;
    }

    protected TrackDataOperation(DataType type,
                                 Throwable exception,
                                 Map<String, String> properties,
                                 Map<String, Double> measurements) {
        this(type, "", properties, measurements);
        try {
            this.exception = (Throwable) deepCopy(exception);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected TrackDataOperation(DataType type,
                                 String name,
                                 String message,
                                 String stacktrace,
                                 boolean handled) {
        this.type = type; // no need to copy as enum is pass by value
        try {
            this.name = (String) deepCopy(name);
            this.exceptionMessage = (String) deepCopy(message);
            this.exceptionStacktrace = (String) deepCopy(stacktrace);
            this.handled = handled;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        boolean highPrioItem = (type == DataType.MANAGED_EXCEPTION && !handled);
        if (!Persistence.getInstance().isFreeSpaceAvailable(highPrioItem)) {
            return;
        }

        Data<Domain> telemetry = getTelemetry();
        if (telemetry != null) {
            IChannel channel = ChannelManager.getInstance().getChannel();
            if (highPrioItem) {
                ((Channel) Channel.getInstance()).processException(telemetry);
            } else {
                telemetry.getBaseData().QualifiedName = telemetry.getBaseType();
                Map<String, String> tags = EnvelopeFactory.getInstance().getContext().getContextTags();
                if (this.type == DataType.NEW_SESSION) {
                    //updating IsNew tag from session context doesn't work because editing shared prefs
                    //doesn't happen timely enough so we can be sure isNew is true for all cases
                    //so we set it to true explicitly
                    tags.put("ai.session.isNew", "true");
                }
                channel.log(telemetry, tags);
            }
        }
    }

    private Data<Domain> getTelemetry() {
        Data<Domain> telemetry = null;
        if ((this.type == DataType.MANAGED_EXCEPTION)) {
            telemetry = EnvelopeFactory.getInstance().createExceptionData(this.name, this.exceptionMessage, this.exceptionStacktrace, this.handled);
        } else {
            switch (this.type) {
                case NONE:
                    if (this.telemetry != null) {
                        telemetry = EnvelopeFactory.getInstance().createData(this.telemetry);
                    }
                    break;
                case EVENT:
                    telemetry = EnvelopeFactory.getInstance().createEventData(this.name, this.properties, this.measurements);
                    break;
                case PAGE_VIEW:
                    telemetry = EnvelopeFactory.getInstance().createPageViewData(this.name, this.duration, this.properties, this.measurements);
                    break;
                case TRACE:
                    telemetry = EnvelopeFactory.getInstance().createTraceData(this.name, this.properties);
                    break;
                case METRIC:
                    telemetry = EnvelopeFactory.getInstance().createMetricData(this.name, this.metric, this.properties);
                    break;
                case NEW_SESSION:
                    telemetry = EnvelopeFactory.getInstance().createNewSessionData();
                    break;
                case HANDLED_EXCEPTION:
                    telemetry = EnvelopeFactory.getInstance().createExceptionData(this.exception, this.properties, this.measurements);
                    break;
                default:
                    break;
            }
        }
        return telemetry;
    }

    private Object deepCopy(Object serializableObject) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        new ObjectOutputStream(outputStream).writeObject(serializableObject);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

        return new ObjectInputStream(inputStream).readObject();
    }

    protected enum DataType {
        NONE,
        EVENT,
        TRACE,
        METRIC,
        PAGE_VIEW,
        HANDLED_EXCEPTION,
        MANAGED_EXCEPTION,
        NEW_SESSION
    }
}
