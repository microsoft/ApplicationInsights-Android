package com.microsoft.applicationinsights.channel;

import android.content.SharedPreferences;
import android.test.AndroidTestCase;

import com.microsoft.applicationinsights.TelemetryClientConfig;

import junit.framework.Assert;

import java.util.LinkedHashMap;

public class TelemetryContextTest extends AndroidTestCase {

    private final String idKey = "id";
    private final String isFirstKey = "isFirst";
    private final String isNewKey = "isNew";

    private final int renewalTime = 50;
    private final int expireTime = 250;

    private TelemetryClientConfig config;

    public void setUp() throws Exception {
        super.setUp();
        this.config = new TelemetryClientConfig("iKey", this.getContext());
        config.setSessionRenewalMs(renewalTime);
        config.setSessionExpirationMs(expireTime);

        SharedPreferences.Editor editor = this.getContext().getSharedPreferences(
                TelemetryContext.SHARED_PREFERENCES_KEY, 0).edit();
        editor.putString(TelemetryContext.SESSION_ID_KEY, "");
        editor.putLong(TelemetryContext.SESSION_ACQUISITION_KEY, 0);
        editor.commit();
    }

    public void tearDown() throws Exception {

    }

    public void testSessionContextInitialization() throws Exception {
        TestContext tc = new TestContext(this.config);

        String firstId = checkSessionTags(tc, "initial id", null, "true", "true");
        try {
            java.util.UUID.fromString(firstId);
        } catch (Exception e) {
            Assert.fail("id was not properly initialized by constructor\n" + e.toString());
        }

        // this should load context from shared storage to match firstId
        TestContext newerTc = new TestContext(this.config);
        checkSessionTags(newerTc, "id was loaded from storage", firstId, "false", "false");
    }

    public void testSessionContextRenewal() throws Exception {
        TestContext tc = new TestContext(this.config);
        String firstId = checkSessionTags(tc, "initial id", null, "true", "true");

        // wait half renewal time
        tc.incrementTime(renewalTime / 2);
        checkSessionTags(tc, "session id persists", firstId, "false", "false");

        // trigger renewal
        tc.incrementTime(renewalTime + 1);
        String secondId = checkSessionTags(tc, "session id is renewed", null, "false", "true");
        Assert.assertNotSame("session id is renewed", firstId, secondId);
    }

    public void testSessionContextExpiration() throws Exception {
        TestContext tc = new TestContext(this.config);
        String firstId = checkSessionTags(tc, "initial id", null, "true", "true");

        // trigger expiration
        int duration = 0;
        int interval = renewalTime / 2;
        while(duration < expireTime) {
            duration += interval;
            tc.incrementTime(interval);
            checkSessionTags(tc, "session id persists loop", firstId, "false", "false");
        }

        tc.incrementTime(interval);
        String secondId = checkSessionTags(tc, "session id is renewed after expire", null, "false", "true");
        Assert.assertNotSame("session id is renewed after expire", firstId, secondId);
    }

    private String checkSessionTags(TelemetryContext tc, String message, String id, String isFirst, String isNew) {
        LinkedHashMap<String, String> tags = tc.getContextTags();
        String _id = tags.get(idKey);
        String _isFirst = tags.get(isFirstKey);
        String _isNew = tags.get(isNewKey);

        if(id != null) {
            assertEquals(message + " - id", id, _id);
        }

        assertEquals(message + " - isFirst", isFirst, _isFirst);
        assertEquals(message + " - isNew", isNew, _isNew);

        return _id;
    }

    private class TestContext extends TelemetryContext {

        private long timeMs;

        public TestContext(TelemetryClientConfig config) {
            super(config);
            this.timeMs = super.getTime();
        }

        public void incrementTime(long timeMs) {
            this.timeMs += timeMs;
        }

        protected long getTime() {
            return this.timeMs;
        }
    }
}