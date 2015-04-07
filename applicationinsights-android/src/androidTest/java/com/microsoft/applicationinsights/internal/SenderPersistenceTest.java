package com.microsoft.applicationinsights.internal;

import android.test.AndroidTestCase;

import junit.framework.Assert;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class SenderPersistenceTest extends AndroidTestCase {

    public void setUp() throws Exception {
        super.setUp();
        Persistence.initialize(this.getContext());
    }

    public void testOn500Response() throws Exception {
        Persistence persist = Persistence.getInstance();
        Sender sender = new Sender(new TelemetryQueueConfig());
        URL url = new URL("http://www.android.com/");
        HttpURLConnection conn = new HttpURLConnection(url) {
            @Override
            public void disconnect() {

            }

            @Override
            public boolean usingProxy() {
                return false;
            }

            @Override
            public void connect() throws IOException {

            }
        };
        String expected = "THIS IS A TEST";
        sender.onResponse(conn, 501, expected);

        File file = persist.nextAvailableFile();
        String data = persist.load(file);
        Assert.assertEquals("Data was retrieved from persistence file", expected, data);
    }
}
