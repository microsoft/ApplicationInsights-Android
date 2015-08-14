package com.microsoft.applicationinsights.library.config;

import android.test.AndroidTestCase;

import com.microsoft.applicationinsights.library.ApplicationInsights;

public class ConfigurationTest extends AndroidTestCase {

    private Configuration sut;
    public void setUp() throws Exception {
        super.setUp();
        sut = new Configuration();
        ApplicationInsights.setDeveloperMode(false);
    }

    public void testInitializesCorrectly() throws Exception {
        assertEquals(Configuration.DEFAULT_MAX_BATCH_COUNT, sut.getMaxBatchCount());
        assertEquals(Configuration.DEFAULT_MAX_BATCH_INTERVAL_MS, sut.getMaxBatchIntervalMs());
        assertEquals(Configuration.DEFAULT_ENDPOINT_URL, sut.getEndpointUrl());
        assertEquals(Configuration.DEFAULT_SENDER_READ_TIMEOUT, sut.getSenderReadTimeout());
        assertEquals(Configuration.DEFAULT_SENDER_CONNECT_TIMEOUT, sut.getSenderConnectTimeout());
        assertEquals(Configuration.DEFAULT_SESSION_INTERVAL, sut.getSessionIntervalMs());
    }

    public void testReturnsDebugValuesInDevMode() throws Exception {
        int testBatchCount = 111;
        int testBatchInterval = 11111;
        sut.setMaxBatchIntervalMs(testBatchInterval);
        sut.setMaxBatchCount(testBatchCount);

        // if dev mode enabled, actual values should be ignored
        ApplicationInsights.setDeveloperMode(true);
        assertEquals(Configuration.DEBUG_MAX_BATCH_COUNT, sut.getMaxBatchCount());
        assertEquals(Configuration.DEBUG_MAX_BATCH_INTERVAL_MS, sut.getMaxBatchIntervalMs());

        // if dev mode disabled, actual values should be used
        ApplicationInsights.setDeveloperMode(false);
        assertEquals(testBatchCount, sut.getMaxBatchCount());
        assertEquals(testBatchInterval, sut.getMaxBatchIntervalMs());
    }
}
