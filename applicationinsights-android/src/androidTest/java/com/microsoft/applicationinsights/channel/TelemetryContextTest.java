package com.microsoft.applicationinsights.channel;

import android.content.SharedPreferences;
import android.test.AndroidTestCase;

import com.microsoft.applicationinsights.TelemetryClient;
import com.microsoft.applicationinsights.TelemetryClientConfig;

import junit.framework.Assert;

import java.util.LinkedHashMap;
import java.util.UUID;

public class TelemetryContextTest extends AndroidTestCase {

    private final String userIdKey = "ai.user.id";
    private final String userAcqKey = "ai.user.accountAcquisitionDate";

    private TelemetryClientConfig config;

    public void setUp() throws Exception {
        super.setUp();
        this.config = new TelemetryClientConfig("iKey", this.getContext());

        SharedPreferences.Editor editor = this.getContext().getSharedPreferences(
                TelemetryContext.SHARED_PREFERENCES_KEY, 0).edit();
        editor.putString(TelemetryContext.SESSION_ID_KEY, null);
        editor.putString(TelemetryContext.USER_ID_KEY, null);
        editor.commit();
    }

    public void tearDown() throws Exception {

    }

    public void testUserContextInitialization() {
        TelemetryContext tc = new TelemetryContext(this.config);

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
        editor.putString(TelemetryContext.USER_ACQ_KEY, "test acq");
        editor.commit();

        // this should load context from shared storage to match firstId
        TelemetryContext tc = new TelemetryContext(this.config);
        LinkedHashMap<String, String> tags = tc.getContextTags();
        String newId = tags.get(userIdKey);
        String newAcq = tags.get(userAcqKey);
        Assert.assertEquals("ID persists in local storage", "test value", newId);
        Assert.assertEquals("Acquisition date persists in local storage", "test acq", newAcq);
    }

    public void testSessionContextInitialization() throws Exception {
        TelemetryContext tc = new TelemetryContext(this.config);

        String firstId = checkSessionTags(tc, "initial id", null, "true");
        try {
            java.util.UUID.fromString(firstId);
        } catch (Exception e) {
            Assert.fail("id was not properly initialized by constructor\n" + e.toString());
        }

        // this should load context from shared storage to match firstId
        TelemetryContext newerTc = new TelemetryContext(this.config);
        checkSessionTags(newerTc, "id was loaded from storage", firstId, "false");
    }

    public void testSessionContextRenewal() throws Exception {
        TelemetryContext tc = new TelemetryContext(this.config);
        String firstId = checkSessionTags(tc, "initial id", null, "true");

        // trigger renewal
        tc.renewSessionId();
        String secondId = checkSessionTags(tc, "session id is renewed", null, "false");
        Assert.assertNotSame("session id is renewed", firstId, secondId);

        // check that it doesn't change when accessed a second time
        String thirdId = checkSessionTags(tc, "session id is not renewed", secondId, "false");
        Assert.assertSame("session id is not renewed", secondId, thirdId);
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
}