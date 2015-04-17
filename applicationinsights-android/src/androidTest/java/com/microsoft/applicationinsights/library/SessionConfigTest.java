package com.microsoft.applicationinsights.library;

import android.content.Intent;
import android.test.ActivityUnitTestCase;

public class SessionConfigTest extends ActivityUnitTestCase<MockActivity> {

    public SessionConfigTest() {
        super(MockActivity.class);
    }

    public void setUp() throws Exception {
        super.setUp();

        Intent intent = new Intent(getInstrumentation().getTargetContext(), MockActivity.class);
        this.setActivity(this.startActivity(intent, null, null));
    }

    public void testRegister() throws Exception {
        ApplicationInsights.setup(getActivity());
        ApplicationInsights.start();
        assertNotNull("iKey is initialized", ApplicationInsights.getInstrumentationKey());
    }
}