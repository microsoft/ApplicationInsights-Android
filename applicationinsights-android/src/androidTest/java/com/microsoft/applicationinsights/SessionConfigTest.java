package com.microsoft.applicationinsights;

import android.content.Intent;
import android.test.ActivityUnitTestCase;

import com.microsoft.applicationinsights.library.ApplicationInsights;
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

    public void testRegister() throws Exception {
        ApplicationInsights.setup(getActivity());
        ApplicationInsights.start();
        assertNotNull("iKey is initialized", ApplicationInsights.getConfig().getInstrumentationKey());
    }
}