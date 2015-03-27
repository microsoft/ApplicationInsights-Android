package com.microsoft.applicationinsights;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.IBinder;
import android.test.ActivityUnitTestCase;

import com.microsoft.applicationinsights.channel.contracts.SessionState;
import com.microsoft.applicationinsights.channel.contracts.SessionStateData;
import com.microsoft.applicationinsights.channel.contracts.shared.ITelemetry;
import com.microsoft.mocks.MockActivity;
import com.microsoft.mocks.MockApplication;
import com.microsoft.mocks.MockLifeCycleTracking;
import com.microsoft.mocks.MockTelemetryClient;

import junit.framework.Assert;

import java.util.ArrayList;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class LifeCycleTrackingTest extends ActivityUnitTestCase<MockActivity> {
    private Intent intent;
    private MockTelemetryClient telemetryClient;
    private MockApplication mockApplication;
    private MockLifeCycleTracking mockLifeCycleTracking;

    public LifeCycleTrackingTest() {
        super(MockActivity.class);
    }

    public void setUp() throws Exception {
        super.setUp();

        Context context = this.getInstrumentation().getContext();
        this.mockLifeCycleTracking = MockLifeCycleTracking.getInstance(context);
        this.mockLifeCycleTracking.reset();
        this.telemetryClient = this.mockLifeCycleTracking.tc;

        this.mockApplication = new MockApplication(context);
        this.mockApplication.onCreate();
        this.setApplication(this.mockApplication);

        this.intent = new Intent(context, MockActivity.class);

        this.telemetryClient.clearMessages();
    }

    public void tearDown() throws Exception {
        this.mockApplication.unregister();
    }

    public void testPageViewEvent() throws Exception {
        // setup
        MockActivity activity = this.startActivity(this.intent, null, null);

        // test
        getInstrumentation().callActivityOnResume(activity);

        // validation
        ArrayList<ITelemetry> messages = this.mockLifeCycleTracking.tc.getMessages();
        Assert.assertEquals("Received 2 messages", 2, messages.size());
        Assert.assertEquals("Received Session State data", "Microsoft.ApplicationInsights.SessionStateData", messages.get(0).getBaseType());
        Assert.assertEquals("Got the start session", SessionState.Start, ((SessionStateData)messages.get(0)).getState());
        Assert.assertEquals("Received page view", "Microsoft.ApplicationInsights.PageViewData", messages.get(1).getBaseType());
    }

    public void testPageViewEventMultipleActivities() throws Exception {
        MockActivity activity1 = this.startActivity(this.intent, null, null);
        MockActivity activity2 = this.getMockActivity(new Intent(
                this.getInstrumentation().getContext(),
                MockActivity.class), MockActivity.class);
        MockActivity activity3 = this.getMockActivity(new Intent(
                this.getInstrumentation().getContext(),
                MockActivity.class), MockActivity.class);

        // test
        getInstrumentation().callActivityOnResume(activity1);
        getInstrumentation().callActivityOnResume(activity2);
        getInstrumentation().callActivityOnResume(activity3);
        getInstrumentation().callActivityOnResume(activity2);
        getInstrumentation().callActivityOnResume(activity1);

        // validation
        ArrayList<ITelemetry> messages = this.mockLifeCycleTracking.tc.getMessages();
        Assert.assertEquals("Received 6 messages", 6, messages.size());
        Assert.assertEquals("Received Session State data", "Microsoft.ApplicationInsights.SessionStateData", messages.get(0).getBaseType());
        Assert.assertEquals("Got the start session", SessionState.Start, ((SessionStateData)messages.get(0)).getState());
        Assert.assertEquals("Received page view", "Microsoft.ApplicationInsights.PageViewData", messages.get(1).getBaseType());
        Assert.assertEquals("Received page view", "Microsoft.ApplicationInsights.PageViewData", messages.get(2).getBaseType());
        Assert.assertEquals("Received page view", "Microsoft.ApplicationInsights.PageViewData", messages.get(3).getBaseType());
        Assert.assertEquals("Received page view", "Microsoft.ApplicationInsights.PageViewData", messages.get(4).getBaseType());
        Assert.assertEquals("Received page view", "Microsoft.ApplicationInsights.PageViewData", messages.get(5).getBaseType());
    }

    public void testOnStartEvent() throws Exception {
        // setup
        MockActivity activity = this.startActivity(this.intent, null, null);

        // test that on start event fires first time
        getInstrumentation().callActivityOnResume(activity);

        // validation
        ArrayList<ITelemetry> messages = this.mockLifeCycleTracking.tc.getMessages();
        Assert.assertEquals("Received 2 messages", 2, messages.size());
        Assert.assertEquals("Received Session State data", "Microsoft.ApplicationInsights.SessionStateData", messages.get(0).getBaseType());
        Assert.assertEquals("Got the start session", SessionState.Start, ((SessionStateData)messages.get(0)).getState());
        Assert.assertEquals("Received page view", "Microsoft.ApplicationInsights.PageViewData", messages.get(1).getBaseType());

        // test that on start event doesn't fire second time
        getInstrumentation().callActivityOnResume(activity);

        // validation
        messages = this.mockLifeCycleTracking.tc.getMessages();
        Assert.assertEquals("Received 3 message2", 3, messages.size());
        Assert.assertEquals("Received page view", "Microsoft.ApplicationInsights.PageViewData", messages.get(2).getBaseType());
    }

    public void testOnStartEventMultipleActivities() throws Exception {
        // setup
        MockActivity activity1 = this.startActivity(this.intent, null, null);
        MockActivity activity2 = this.getMockActivity(new Intent(
                this.getInstrumentation().getContext(),
                MockActivity.class), MockActivity.class);
        MockActivity activity3 = this.getMockActivity(new Intent(
                this.getInstrumentation().getContext(),
                MockActivity.class), MockActivity.class);

        // test
        getInstrumentation().callActivityOnResume(activity1);
        getInstrumentation().callActivityOnResume(activity2);
        getInstrumentation().callActivityOnResume(activity3);
        getInstrumentation().callActivityOnResume(activity1);

        // validation
        ArrayList<ITelemetry> messages = this.mockLifeCycleTracking.tc.getMessages();
        Assert.assertEquals("Received 5 messages", 5, messages.size());
        Assert.assertEquals("Received Session State data", "Microsoft.ApplicationInsights.SessionStateData", messages.get(0).getBaseType());
        Assert.assertEquals("Got the start session", SessionState.Start, ((SessionStateData)messages.get(0)).getState());
        Assert.assertEquals("Received page view", "Microsoft.ApplicationInsights.PageViewData", messages.get(1).getBaseType());
        Assert.assertEquals("Received page view", "Microsoft.ApplicationInsights.PageViewData", messages.get(2).getBaseType());
        Assert.assertEquals("Received page view", "Microsoft.ApplicationInsights.PageViewData", messages.get(3).getBaseType());
        Assert.assertEquals("Received page view", "Microsoft.ApplicationInsights.PageViewData", messages.get(4).getBaseType());

    }

    public void testSessionTimeout() throws Exception {
        // setup
        MockActivity activity1 = this.startActivity(this.intent, null, null);
        MockActivity activity2 = this.getMockActivity(new Intent(
                this.getInstrumentation().getContext(),
                MockActivity.class), MockActivity.class);

        // test 3 activities starting/stopping then restarting the first
        getInstrumentation().callActivityOnResume(activity1);
        getInstrumentation().callActivityOnResume(activity2);

        this.mockLifeCycleTracking.currentTime += this.telemetryClient.getConfig().getSessionIntervalMs();
        getInstrumentation().callActivityOnResume(activity1);
        getInstrumentation().callActivityOnResume(activity2);

        // validation
        ArrayList<ITelemetry> messages = this.mockLifeCycleTracking.tc.getMessages();
        Assert.assertEquals("Received 6 messages", 6, messages.size());
        Assert.assertEquals("Received Session State data", "Microsoft.ApplicationInsights.SessionStateData", messages.get(0).getBaseType());
        Assert.assertEquals("Got the start session", SessionState.Start, ((SessionStateData)messages.get(0)).getState());
        Assert.assertEquals("Received page view", "Microsoft.ApplicationInsights.PageViewData", messages.get(1).getBaseType());
        Assert.assertEquals("Received page view", "Microsoft.ApplicationInsights.PageViewData", messages.get(2).getBaseType());
        Assert.assertEquals("Received Session State data", "Microsoft.ApplicationInsights.SessionStateData", messages.get(3).getBaseType());
        Assert.assertEquals("Got the start session", SessionState.Start, ((SessionStateData)messages.get(3)).getState());
        Assert.assertEquals("Received page view", "Microsoft.ApplicationInsights.PageViewData", messages.get(4).getBaseType());
        Assert.assertEquals("Received page view", "Microsoft.ApplicationInsights.PageViewData", messages.get(5).getBaseType());
    }

    private MockActivity getMockActivity(Intent intent, Class activityClass) {
        IBinder token = null;
        setApplication(new android.test.mock.MockApplication());
        ComponentName cn = new ComponentName(activityClass.getPackage().getName(),
                activityClass.getName());
        intent.setComponent(cn);
        ActivityInfo info = new ActivityInfo();
        CharSequence title = activityClass.getName();

        String id = null;

        MockActivity activity = null;
        try {
            activity = (MockActivity) getInstrumentation().newActivity(
                    activityClass,
                    this.getInstrumentation().getContext(),
                    token, this.mockApplication, intent, info,
                    title, this.getActivity().getParent(), id, null);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return activity;
    }
}
