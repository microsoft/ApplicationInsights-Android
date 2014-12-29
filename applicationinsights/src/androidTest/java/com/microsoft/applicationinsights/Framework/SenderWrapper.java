package com.microsoft.applicationinsights.Framework;


import com.microsoft.applicationinsights.EndToEnd.TelemetryClientTest;
import com.microsoft.applicationinsights.channel.Sender;
import com.microsoft.applicationinsights.channel.contracts.shared.IJsonSerializable;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;

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

    public LinkedList<IJsonSerializable> getQueue()
    {
        return this.queue;
    }

    @Override
    protected String onResponse(HttpURLConnection connection, int responseCode) {
        String response = null;
        this.signal.countDown();
        if(!this.useFakeWriter) {
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
