package com.microsoft.applicationinsights;

import android.content.Intent;
import android.test.ActivityUnitTestCase;

import com.microsoft.mocks.MockActivity;

public class SessionConfigTest extends ActivityUnitTestCase<MockActivity> {

    public SessionConfigTest() {
        super(MockActivity.class);
    }

    public void setUp() throws Exception {
        super.setUp();

        Intent intent = new Intent(getInstrumentation().getTargetContext(), MockActivity.class);
        this.setActivity(this.startActivity(intent, null, null));
    }

    //TODO move to AppInsights-Tests
//    public void testRegister() throws Exception {
//        TelemetryClient client = TelemetryClient.getInstance();
//        assertNotNull("iKey is initialized", client.config.getInstrumentationKey());
//    }
}