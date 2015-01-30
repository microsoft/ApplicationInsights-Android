package com.microsoft.applicationinsights.channel;

import android.content.SharedPreferences;
import android.test.AndroidTestCase;

import com.microsoft.applicationinsights.TelemetryClientConfig;

import junit.framework.Assert;

import java.util.LinkedHashMap;
import java.util.UUID;

public class TelemetryContextTest extends AndroidTestCase {

    private final String userIdKey = "ai.user.id";

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
        editor.putString(TelemetryContext.USER_ID_KEY, null);
        editor.commit();
    }

    public void tearDown() throws Exception {

    }

    public void testUserContextInitialization() {
        TestContext tc = new TestContext(this.config);

        String id = tc.getContextTags().get(userIdKey);
        try {
            UUID guidId = UUID.fromString(id);
            Assert.assertNotNull("generated ID is a valid GUID", guidId);
        } catch (Exception e) {
            Assert.fail("id was not properly initialized by constructor\n" + e.toString());
        }
    }

    public void testUserContextPersistence() {
        SharedPreferences.Editor editor = this.getContext().getSharedPreferences(
                TelemetryContext.SHARED_PREFERENCES_KEY, 0).edit();
        editor.putString(TelemetryContext.USER_ID_KEY, "test value");
        editor.commit();

        // this should load context from shared storage to match firstId
        TestContext tc = new TestContext(this.config);
        String newId = tc.getContextTags().get(userIdKey);
        Assert.assertEquals("ID persists in local storage", "test value", newId);
    }

    public void testSessionContextInitialization() throws Exception {
        TestContext tc = new TestContext(this.config);

        String firstId = checkSessionTags(tc, "initial id", null, "true");
        try {
            java.util.UUID.fromString(firstId);
        } catch (Exception e) {
            Assert.fail("id was not properly initialized by constructor\n" + e.toString());
        }

        // this should load context from shared storage to match firstId
        TestContext newerTc = new TestContext(this.config);
        checkSessionTags(newerTc, "id was loaded from storage", firstId, "false");
    }

    public void testSessionContextRenewal() throws Exception {
        TestContext tc = new TestContext(this.config);
        String firstId = checkSessionTags(tc, "initial id", null, "true");

        // trigger renewal
        tc.renewSessionContext(true);
        String secondId = checkSessionTags(tc, "session id is renewed", null, "false");
        Assert.assertNotSame("session id is renewed", firstId, secondId);
    }

    public void testSessionContextRenewalWithoutUUIDRenewal() throws Exception {
        TestContext tc = new TestContext(this.config);
        String firstId = checkSessionTags(tc, "initial id", null, "true");

        // trigger renewal
        tc.renewSessionContext(false);
        String secondId = checkSessionTags(tc, "session id is not renewed", null, "false");
        Assert.assertEquals("session id should not be renewed", firstId, secondId);
    }

    private String checkSessionTags(TelemetryContext tc, String message, String id, String isFirst) {
        LinkedHashMap<String, String> tags = tc.getContextTags();
        String sessionIdKey = "ai.session.id";
        String _id = tags.get(sessionIdKey);
        String sessionIsFirstKey = "ai.session.isFirst";
        String _isFirst = tags.get(sessionIsFirstKey);

        if(id != null) {
            assertEquals(message + " - id", id, _id);
        }

        assertEquals(message + " - isFirst", isFirst, _isFirst);

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