package com.microsoft.applicationinsights.library.config;

import android.test.AndroidTestCase;

import com.microsoft.applicationinsights.library.ApplicationInsights;

public class ApplicationInsightsConfigTest extends AndroidTestCase {

    private ApplicationInsightsConfig sut;
    public void setUp() throws Exception {
        super.setUp();
        sut = new ApplicationInsightsConfig();
        ApplicationInsights.setDeveloperMode(false);
    }

    public void testInitializesCorrectly() throws Exception {
        assertEquals(ApplicationInsightsConfig.DEFAULT_MAX_BATCH_COUNT, sut.getMaxBatchCount());
        assertEquals(ApplicationInsightsConfig.DEFAULT_MAX_BATCH_INTERVAL_MS, sut.getMaxBatchIntervalMs());
        assertEquals(ApplicationInsightsConfig.DEFAULT_ENDPOINT_URL, sut.getEndpointUrl());
        assertEquals(ApplicationInsightsConfig.DEFAULT_SENDER_READ_TIMEOUT, sut.getSenderReadTimeout());
        assertEquals(ApplicationInsightsConfig.DEFAULT_SENDER_CONNECT_TIMEOUT, sut.getSenderConnectTimeout());
        assertEquals(ApplicationInsightsConfig.DEFAULT_SESSION_INTERVAL, sut.getSessionIntervalMs());
    }

    public void testReturnsDebugValuesInDevMode() throws Exception {
        int testBatchCount = 111;
        int testBatchInterval = 11111;
        sut.setMaxBatchIntervalMs(testBatchInterval);
        sut.setMaxBatchCount(testBatchCount);

        // if dev mode enabled, actual values should be ignored
        ApplicationInsights.setDeveloperMode(true);
        assertEquals(ApplicationInsightsConfig.DEBUG_MAX_BATCH_COUNT, sut.getMaxBatchCount());
        assertEquals(ApplicationInsightsConfig.DEBUG_MAX_BATCH_INTERVAL_MS, sut.getMaxBatchIntervalMs());

        // if dev mode disabled, actual values should be used
        ApplicationInsights.setDeveloperMode(false);
        assertEquals(testBatchCount, sut.getMaxBatchCount());
        assertEquals(testBatchInterval, sut.getMaxBatchIntervalMs());
    }
}
