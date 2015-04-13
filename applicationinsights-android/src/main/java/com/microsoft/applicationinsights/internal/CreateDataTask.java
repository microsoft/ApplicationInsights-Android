package com.microsoft.applicationinsights.internal;

import android.os.AsyncTask;

import com.microsoft.applicationinsights.contracts.Envelope;
import com.microsoft.applicationinsights.internal.Channel;
import com.microsoft.applicationinsights.internal.EnvelopeFactory;

import java.util.Map;

public class CreateDataTask extends AsyncTask<Void, Envelope, Envelope> {

    public interface OnCreateDataTaskCompletedListener{
        void onDataCreated(Envelope envelope);
        void onCreateDataFailed();
    }

    public enum DataType {
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
    private OnCreateDataTaskCompletedListener listener;

    public CreateDataTask(DataType type){
        this.type = type;
    }

    public CreateDataTask(DataType type, String metricName, double metric){
        this(type, metricName, metric, null);
    }

    public CreateDataTask(DataType type, String metricName, double metric, OnCreateDataTaskCompletedListener listener){
        this.type = type;
        this.name = metricName;
        this.metric = metric;
        this.listener = listener;
    }

    public CreateDataTask(DataType type,
                          String name,
                          Map<String,String> properties,
                          Map<String, Double> measurements){
        this(type, name, properties, measurements, null);
    }

    public CreateDataTask(DataType type,
                          String name,
                          Map<String,String> properties,
                          Map<String, Double> measurements,
                          OnCreateDataTaskCompletedListener listener){
        this.type = type;
        this.name = name;
        this.properties = properties;
        this.measurements = measurements;
        this.listener = listener;
    }

    public CreateDataTask(DataType type,
                                    Throwable exception,
                                    Map<String,String> properties){
        this(type, exception, properties, null);
    }

    public CreateDataTask(DataType type,
                          Throwable exception,
                          Map<String,String> properties,
                          OnCreateDataTaskCompletedListener listener){
        this.type = type;
        this.exception = exception;
        this.properties = properties;
        this.listener = listener;
    }

    @Override
    protected Envelope doInBackground(Void... params) {

        Envelope envelope = null;
        switch (this.type){
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
        return envelope;
    }

    @Override
    protected void onPostExecute(Envelope result) {
        if(this.listener != null){
            if(result != null){
                this.listener.onDataCreated(result);
            }else{
                this.listener.onCreateDataFailed();
            }
        }
    }
}