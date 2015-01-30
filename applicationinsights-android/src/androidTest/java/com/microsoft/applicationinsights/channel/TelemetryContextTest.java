package com.microsoft.applicationinsights.channel;

import android.content.SharedPreferences;
import android.test.AndroidTestCase;

import com.microsoft.applicationinsights.TelemetryClientConfig;

import junit.framework.Assert;

import java.util.LinkedHashMap;
import java.util.UUID;

public class TelemetryContextTest extends AndroidTestCase {

    private final String userIdKey = "ai.user.id";
    private final String sessionIdKey = "ai.session.id";

    private TelemetryClientConfig config;

    public void setUp() throws Exception {
        super.setUp();
        this.config = new TelemetryClientConfig("iKey", this.getContext());

        SharedPreferences.Editor editor = this.getContext().getSharedPreferences(
                TelemetryContext.SHARED_PREFERENCES_KEY, 0).edit();
        editor.putString(TelemetryContext.SESSION_ID_KEY, "");
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

        String firstId = tc.getContextTags().get(sessionIdKey);
        try {
            java.util.UUID.fromString(firstId);
        } catch (Exception e) {
            Assert.fail("id was not properly initialized by constructor\n" + e.toString());
        }
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