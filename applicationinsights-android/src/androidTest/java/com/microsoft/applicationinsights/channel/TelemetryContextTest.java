package com.microsoft.applicationinsights.channel;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.test.ActivityUnitTestCase;

import com.microsoft.mocks.MockActivity;
import com.microsoft.mocks.MockTelemetryContext;

import junit.framework.Assert;

import java.util.LinkedHashMap;
import java.util.UUID;

public class TelemetryContextTest extends ActivityUnitTestCase<MockActivity> {

    public TelemetryContextTest() {
        super(com.microsoft.mocks.MockActivity.class);
    }

    private final String userIdKey = "ai.user.id";
    private final String userAcqKey = "ai.user.accountAcquisitionDate";

    private Context context;

    public void setUp() throws Exception {
        super.setUp();

        Intent intent = new Intent(getInstrumentation().getTargetContext(), com.microsoft.mocks.MockActivity.class);
        this.setActivity(this.startActivity(intent, null, null));
        this.context = this.getActivity();

        SharedPreferences.Editor editor = this.getActivity().getApplicationContext()
                .getSharedPreferences(TelemetryContext.SHARED_PREFERENCES_KEY, 0).edit();
        editor.putString(TelemetryContext.USER_ID_KEY, null);
        editor.commit();
    }

    public void tearDown() throws Exception {

    }

    public void testInitialization() {
        TelemetryContext telemetryContext = new TelemetryContext(this.context);

        Assert.assertNotNull("app", telemetryContext.getApplication());
        Assert.assertNotNull("appVer", telemetryContext.getApplication().getVer());
        Assert.assertNotNull("appPackageName", telemetryContext.getPackageName());
        Assert.assertNotNull("device", telemetryContext.getDevice());
        Assert.assertNotNull("deviceId", telemetryContext.getDevice().getId());
        Assert.assertNotNull("deviceOs", telemetryContext.getDevice().getOs());
        Assert.assertNotNull("user", telemetryContext.getUser());
        Assert.assertNotNull("userId", telemetryContext.getUser().getId());
        Assert.assertNotNull("userAcquisition", telemetryContext.getUser().getAccountAcquisitionDate());
    }

    public void testUserContextInitialization() {
        TelemetryContext tc = new MockTelemetryContext(this.context);

        String id = tc.getContextTags().get(userIdKey);
        try {
            UUID guidId = UUID.fromString(id);
            Assert.assertNotNull("generated ID is a valid GUID", guidId);
        } catch (Exception e) {
            Assert.fail("id was not properly initialized by constructor\n" + e.toString());
        }
    }

    public void testUserContextPersistence() {
        SharedPreferences.Editor editor = this.getActivity().getApplicationContext()
                .getSharedPreferences(TelemetryContext.SHARED_PREFERENCES_KEY, 0).edit();
        editor.putString(TelemetryContext.USER_ID_KEY, "test value");
        editor.putString(TelemetryContext.USER_ACQ_KEY, "test acq");
        editor.commit();

        // this should load context from shared storage to match firstId
        TelemetryContext tc = new MockTelemetryContext(this.context);
        LinkedHashMap<String, String> tags = tc.getContextTags();
        String newId = tags.get(userIdKey);
        String newAcq = tags.get(userAcqKey);
        Assert.assertEquals("ID persists in local storage", "test value", newId);
        Assert.assertEquals("Acquisition date persists in local storage", "test acq", newAcq);
    }

    public void testSessionContextInitialization() throws Exception {
        TelemetryContext tc = new MockTelemetryContext(this.context);

        String firstId = checkSessionTags(tc);
        try {
            java.util.UUID.fromString(firstId);
        } catch (Exception e) {
            Assert.fail("id was not properly initialized by constructor\n" + e.toString());
        }

        // this should load context from shared storage to match firstId
        TelemetryContext newerTc = new MockTelemetryContext(this.context);
        checkSessionTags(newerTc);
    }

    public void testSessionContextRenewal() throws Exception {
        TelemetryContext tc = new MockTelemetryContext(this.context);
        String firstId = checkSessionTags(tc);

        // trigger renewal
        tc.renewSessionId();
        String secondId = checkSessionTags(tc);
        Assert.assertNotSame("session id is renewed", firstId, secondId);

        // check that it doesn't change when accessed a second time
        String thirdId = checkSessionTags(tc);
        Assert.assertSame("session id is not renewed", secondId, thirdId);
    }

    private String checkSessionTags(TelemetryContext tc) {
        LinkedHashMap<String, String> tags = tc.getContextTags();
        String sessionIdKey = "ai.session.id";
        String _id = tags.get(sessionIdKey);
        return _id;
    }

    private class MockActivity extends Activity {
        public Context context;
        public MockActivity(Context context) {
            this.context = context;
        }

        @Override
        public Resources getResources() {
            return this.context.getResources();
        }

        @Override
        public Context getApplicationContext() {
            return this.context;
        }

        @Override
        public String getPackageName() {
            return "com.microsoft.applicationinsights.test";
        }
    }
}