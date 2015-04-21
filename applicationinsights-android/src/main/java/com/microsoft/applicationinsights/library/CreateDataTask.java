package com.microsoft.applicationinsights.library;

import android.os.AsyncTask;

import com.microsoft.applicationinsights.contracts.Envelope;
import com.microsoft.applicationinsights.contracts.shared.ITelemetry;

import java.util.Map;

class CreateDataTask extends AsyncTask<Void, Void, Void> {

    protected enum DataType {
        NONE,
        EVENT,
        TRACE,
        METRIC,
        PAGE_VIEW,
        HANDLED_EXCEPTION,
        UNHANDLED_EXCEPTION,
        NEW_SESSION
    };

    private String name;
    private Map<String,String> properties;
    private Map<String, Double> measurements;
    private DataType type;
    private double metric;
    private Throwable exception;
    private ITelemetry telemetry;

    protected CreateDataTask(ITelemetry telemetry){
        this.type = DataType.NONE;
        this.telemetry = telemetry;
    }

    protected CreateDataTask(DataType type){
        this.type = type;
    }

    protected CreateDataTask(DataType type, String metricName, double metric){
        this.type = type;
        this.name = metricName;
        this.metric = metric;
    }

    protected CreateDataTask(DataType type,
                                    String name,
                                    Map<String,String> properties,
                                    Map<String, Double> measurements){
        this.type = type;
        this.name = name;
        this.properties = properties;
        this.measurements = measurements;
    }

    protected CreateDataTask(DataType type,
                                    Throwable exception,
                                    Map<String,String> properties){
        this.type = type;
        this.exception = exception;
        this.properties = properties;
    }

    @Override
    protected Void doInBackground(Void... params) {
        Envelope envelope = null;
        switch (this.type){
            case NONE:
                if(this.telemetry != null){
                    envelope = EnvelopeFactory.INSTANCE.createEnvelope(this.telemetry);
                }
                break;
            case EVENT:
                envelope = EnvelopeFactory.INSTANCE.createEventEnvelope(this.name, this.properties, this.measurements);
                break;
            case PAGE_VIEW:
                envelope = EnvelopeFactory.INSTANCE.createPageViewEnvelope(this.name, this.properties, this.measurements);
                break;
            case TRACE:
                envelope = EnvelopeFactory.INSTANCE.createTraceEnvelope(this.name, this.properties);
                break;
            case METRIC:
                envelope = EnvelopeFactory.INSTANCE.createMetricEnvelope(this.name, this.metric);
                break;
            case NEW_SESSION:
                envelope = EnvelopeFactory.INSTANCE.createNewSessionEnvelope();
                break;
            case HANDLED_EXCEPTION:
            case UNHANDLED_EXCEPTION:
                envelope = EnvelopeFactory.INSTANCE.createExceptionEnvelope(this.exception, this.properties);
                break;
            default:
                break;
        }

        if(envelope != null){
            Channel channel = Channel.getInstance();
            if(type == DataType.UNHANDLED_EXCEPTION){
                channel.processUnhandledException(envelope);
            }else{
                channel.enqueue(envelope);
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        return ;
    }
}