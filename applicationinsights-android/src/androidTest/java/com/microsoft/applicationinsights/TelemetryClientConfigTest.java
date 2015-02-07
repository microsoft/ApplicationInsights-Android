package com.microsoft.applicationinsights;

import android.test.ActivityTestCase;

import junit.framework.Assert;

public class TelemetryClientConfigTest extends ActivityTestCase {

    public void setUp() throws Exception {
        super.setUp();

    }

    public void testRegister() throws Exception {
        TelemetryClient client = TelemetryClient.getInstance(this.getActivity());
        Assert.assertNotNull("static registration returns non-null client", client);
    }
}