package com.microsoft.applicationinsights.library;

import com.microsoft.telemetry.Data;
import com.microsoft.telemetry.Domain;
import com.microsoft.telemetry.IChannel;
import com.microsoft.telemetry.ITelemetry;

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
        Data<Domain> telemetry = null;
        if ((this.type == DataType.UNHANDLED_EXCEPTION) && Persistence.getInstance().isFreeSpaceAvailable(true)) {
            telemetry = EnvelopeFactory.getInstance().createExceptionData(this.exception, this.properties);
        } else if (Persistence.getInstance().isFreeSpaceAvailable(false)) {
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
                    telemetry = EnvelopeFactory.getInstance().createPageViewData(this.name, this.properties, this.measurements);
                    break;
                case TRACE:
                    telemetry = EnvelopeFactory.getInstance().createTraceData(this.name, this.properties);
                    break;
                case METRIC:
                    telemetry = EnvelopeFactory.getInstance().createMetricData(this.name, this.metric);
                    break;
                case NEW_SESSION:
                    telemetry = EnvelopeFactory.getInstance().createNewSessionData();
                    break;
                case HANDLED_EXCEPTION:
                case UNHANDLED_EXCEPTION:
                    break;
                default:
                    break;
            }
        }

        if (telemetry != null) {
            IChannel channel = ChannelManager.getInstance().getChannel();
            if (type == DataType.UNHANDLED_EXCEPTION) {
                ((Channel)Channel.getInstance()).processUnhandledException(telemetry);
            } else {
                telemetry.getBaseData().QualifiedName = telemetry.getBaseType();
                Map<String,String> tags = EnvelopeFactory.getInstance().getContext().getContextTags();
                channel.log(telemetry, tags);
            }
        }
    }
}
