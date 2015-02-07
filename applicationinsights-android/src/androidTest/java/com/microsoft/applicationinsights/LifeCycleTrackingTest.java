package com.microsoft.applicationinsights;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.test.ActivityUnitTestCase;

import com.microsoft.applicationinsights.channel.contracts.EventData;
import com.microsoft.applicationinsights.channel.contracts.PageViewData;
import com.microsoft.commonlogging.channel.contracts.shared.ITelemetry;
import com.microsoft.mocks.MockActivity;
import com.microsoft.mocks.MockApplication;
import com.microsoft.mocks.MockLifeCycleTracking;

import junit.framework.Assert;

import java.util.ArrayList;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class LifeCycleTrackingTest extends ActivityUnitTestCase<MockActivity> {
    MockApplication testApp;
    Intent intent;

    public LifeCycleTrackingTest() {
        super(MockActivity.class);
    }

    public void setUp() throws Exception {
        super.setUp();
        Context context = this.getInstrumentation().getContext();

        testApp = new MockApplication(getInstrumentation().getContext());
        testApp.onCreate();
        setApplication(testApp);

    }

    public void testOnActivityCreated() throws Exception {
        intent = new Intent(getInstrumentation().getTargetContext(), MockActivity.class);
        MockActivity activity = this.startActivity(intent, null, null);
        ArrayList<ITelemetry> messages = MockLifeCycleTracking.instance.tc.getMessages();

        Assert.assertEquals("Received 1 message", 1, messages.size());
        Assert.assertEquals("Received Event data", "Microsoft.ApplicationInsights.EventData", messages.get(0).getBaseType());
        Assert.assertEquals("Got the start session string", "Session Start Event", ((EventData)messages.get(0)).getName());
        getInstrumentation().callActivityOnDestroy(activity);
    }

    public void testOnActivityStarted() throws Exception {
        intent = new Intent(getInstrumentation().getTargetContext(), MockActivity.class);
        MockActivity activity = this.startActivity(intent, null, null);
        MockLifeCycleTracking.instance.tc.clearMessages();
        getInstrumentation().callActivityOnStart(activity);
        ArrayList<ITelemetry> messages = MockLifeCycleTracking.instance.tc.getMessages();

        Assert.assertEquals("Received 1 message", 1, messages.size());
        Assert.assertEquals("Received Page View data", "Microsoft.ApplicationInsights.PageViewData", messages.get(0).getBaseType());
        Assert.assertEquals("Got the page name", "com.microsoft.mocks.MockActivity", ((PageViewData)messages.get(0)).getName());
        getInstrumentation().callActivityOnDestroy(activity);
    }

    public void testOnActivityPausedAndResumedAfterNewSessionTimeout() throws Exception {
        intent = new Intent(getInstrumentation().getTargetContext(), MockActivity.class);
        MockActivity activity = this.startActivity(intent, null, null);
        getInstrumentation().callActivityOnStart(activity);
        getInstrumentation().callActivityOnPause(activity);

        // increment time by session interval to trigger a new session ID
        MockLifeCycleTracking.instance.currentTime += MockLifeCycleTracking.instance.getSessionInterval() + 1;

        // validate new session ID
        MockLifeCycleTracking.instance.tc.clearMessages();
        getInstrumentation().callActivityOnResume(activity);
        ArrayList<ITelemetry> messages = MockLifeCycleTracking.instance.tc.getMessages();

        Assert.assertEquals("Received 1 message", 1, messages.size());
        Assert.assertEquals("Received Event data", "Microsoft.ApplicationInsights.EventData", messages.get(0).getBaseType());
        Assert.assertEquals("Got the start session string", "Session Start Event", ((EventData)messages.get(0)).getName());
        getInstrumentation().callActivityOnDestroy(activity);
    }

    public void testOnActivityDestroyed() throws Exception {
        intent = new Intent(getInstrumentation().getTargetContext(), MockActivity.class);
        MockActivity activity = this.startActivity(intent, null, null);

        MockLifeCycleTracking.instance.tc.clearMessages();
        getInstrumentation().callActivityOnDestroy(activity);
        ArrayList<ITelemetry> messages = MockLifeCycleTracking.instance.tc.getMessages();

        Assert.assertEquals("Received 1 message", 1, messages.size());
        Assert.assertEquals("Received Event data", "Microsoft.ApplicationInsights.EventData", messages.get(0).getBaseType());
        Assert.assertEquals("Got the start session string", "Session Stop Event", ((EventData)messages.get(0)).getName());
    }
}
