package com.microsoft.applicationinsights.Framework;

import com.microsoft.applicationinsights.channel.Sender;

import junit.framework.Assert;

import java.net.HttpURLConnection;
import java.util.concurrent.CountDownLatch;

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

    @Override
    protected void onResponse(HttpURLConnection connection, int responseCode) {
        this.signal.countDown();
        Assert.assertEquals("Expected response code", this.expectedResponseCode, responseCode);
        super.onResponse(connection, responseCode);
    }
}
