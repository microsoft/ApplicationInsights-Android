package com.microsoft.applicationinsights.Framework;

import com.microsoft.applicationinsights.channel.Sender;
import com.microsoft.applicationinsights.channel.contracts.shared.IJsonSerializable;

import junit.framework.Assert;

import java.net.HttpURLConnection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by applicationinsights on 12/15/14.
 */
public class SenderWrapper extends Sender {

    public CountDownLatch signal;
    public int expectedResponseCode;

    public SenderWrapper(CountDownLatch signal, int expectedResponseCode) {
        super();
        this.signal = signal;
        this.expectedResponseCode = expectedResponseCode;
    }

    public LinkedBlockingQueue<IJsonSerializable> getQueue()
    {
        return this.queue;
    }

    @Override
    protected void onResponse(HttpURLConnection connection, int responseCode) {
        this.signal.countDown();
        super.onResponse(connection, responseCode);
        Assert.assertEquals("Expected response code", this.expectedResponseCode, responseCode);
    }
}
