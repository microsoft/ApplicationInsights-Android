package com.microsoft.applicationinsights;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.test.ActivityTestCase;
import android.test.ActivityUnitTestCase;

import com.microsoft.applicationinsights.channel.contracts.shared.ITelemetry;
import com.microsoft.mocks.MockActivity;
import com.microsoft.mocks.MockApplication;
import com.microsoft.mocks.MockTelemetryClient;

import junit.framework.Assert;
import junit.framework.TestCase;

import java.util.ArrayList;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class ApplicationLifeCycleEventTrackingTest extends ActivityUnitTestCase<MockActivity> {
    MockApplication testApp;
    MockTelemetryClient tc;
    Intent intent;

    public ApplicationLifeCycleEventTrackingTest() {
        super(MockActivity.class);

    }

    public void setUp() throws Exception {
        super.setUp();
        Context context = this.getInstrumentation().getContext();
        tc =  new MockTelemetryClient(context, "TEST_IKEY");
        testApp = new MockApplication(getInstrumentation().getContext());
        setApplication(testApp);
        intent = new Intent(getInstrumentation().getTargetContext(), MockActivity.class);
    }

    public void testOnActivityCreated() throws Exception {
        this.startActivity(intent, null, null);
        ArrayList<ITelemetry> messages = tc.getMessages();
        for (ITelemetry item: messages)
        {
            Assert.assertTrue("message: " + item.getBaseType(), true);
        }

        Assert.assertEquals("messages has something", 1, messages.size());
        //Assert.assertNotSame("instrumentation not null", this.getInstrumentation(), null);
    }
/*
    public void testOnActivityStarted() throws Exception {

    }

    public void testOnActivityResumed() throws Exception {

    }

    public void testOnActivityPaused() throws Exception {

    }

    public void testOnActivityDestroyed() throws Exception {

    }
*/

}
