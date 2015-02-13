package com.microsoft.commonlogging.channel;

import android.test.AndroidTestCase;

import junit.framework.Assert;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class SenderPersistenceTest extends AndroidTestCase {
    public void testOn500Response() throws Exception {
        Persistence persist = Persistence.getInstance();
        persist.setPersistenceContext(this.getContext());
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
        sender.serializedData = expected;
        sender.onResponse(conn, 501);

        String data = persist.getData();
        Assert.assertEquals("Data was retrieved from persistence file", expected, data);
    }
}
