package com.microsoft.applicationinsights.Framework;

import android.util.Log;

import com.microsoft.applicationinsights.EndToEnd.TelemetryClientTest;
import com.microsoft.applicationinsights.channel.Sender;
import com.microsoft.applicationinsights.channel.contracts.shared.IJsonSerializable;

import junit.framework.Assert;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by applicationinsights on 12/15/14.
 */
public class SenderWrapper extends Sender {

    public CountDownLatch signal;
    public boolean useFakeWriter;
    public StringWriter writer;

    public SenderWrapper(CountDownLatch signal) {
        super();
        this.useFakeWriter = false;
        this.signal = signal;
    }

    public LinkedBlockingQueue<IJsonSerializable> getQueue()
    {
        return this.queue;
    }

    @Override
    protected String onResponse(HttpURLConnection connection, int responseCode) {
        String response = null;
        if(!this.useFakeWriter) {
            this.signal.countDown();
            response = super.onResponse(connection, responseCode);
            TelemetryClientTest.validateResponse(responseCode, response);
        }

        return response;
    }

    @Override
    protected Writer getWriter(HttpURLConnection connection) throws IOException {
        if(this.useFakeWriter){
            this.writer = new StringWriter();
            return this.writer;
        } else {
            return new OutputStreamWriter(connection.getOutputStream());
        }
    }
}
