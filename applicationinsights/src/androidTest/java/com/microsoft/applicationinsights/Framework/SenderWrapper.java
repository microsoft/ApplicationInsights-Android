package com.microsoft.applicationinsights.Framework;

import android.util.Log;

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
    public int expectedResponseCode;
    public boolean useFakeWriter;
    public StringWriter writer;

    public SenderWrapper(CountDownLatch signal, int expectedResponseCode) {
        super();
        this.useFakeWriter = false;
        this.signal = signal;
        this.expectedResponseCode = expectedResponseCode;
    }

    public LinkedBlockingQueue<IJsonSerializable> getQueue()
    {
        return this.queue;
    }

    @Override
    protected void onResponse(HttpURLConnection connection, int responseCode) {
        if(!this.useFakeWriter) {
            this.signal.countDown();
            super.onResponse(connection, responseCode);
            Log.i("SenderWrapper.onResponse", "Response code is: " + responseCode);
            Assert.assertEquals("Expected response code", this.expectedResponseCode, responseCode);
        }
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
