package com.microsoft.applicationinsights.core;

import junit.framework.Assert;
import junit.framework.TestCase;

public class TelemetryClientConfigTest extends TestCase {

    TelemetryClientConfig config;

    public void setUp() throws Exception {
        super.setUp();
        this.config = new TelemetryClientConfig("ikey");
    }

    public void tearDown() throws Exception {

    }

    public void testGetInstrumentationKey() throws Exception {
        Assert.assertEquals("Ikey is set", "ikey", this.config.instrumentationKey);
    }

    public void testGetAccountId() throws Exception {
        Assert.assertEquals("Account ID is set", null, this.config.accountId);
    }

    public void testGetSessionRenewalMs() throws Exception {
        Assert.assertEquals("SessionRenewal is set", TelemetryClientConfig.defaultSessionRenewalMs,
                this.config.sessionRenewalMs);
    }

    public void testGetSessionExpirationMs() throws Exception {
        Assert.assertEquals("SessionExpiry is set", TelemetryClientConfig.defaultSessionExpirationMs,
                this.config.sessionExpirationMs);
    }

    public void testGetSenderConfig() throws Exception {
        Assert.assertNotNull("Sender config is not null", this.config.getSenderConfig());
    }
}