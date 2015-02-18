package com.microsoft.applicationinsights;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.IBinder;
import android.test.ActivityUnitTestCase;

import com.microsoft.applicationinsights.channel.contracts.EventData;
import com.microsoft.commonlogging.channel.contracts.shared.ITelemetry;
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
        Assert.assertEquals("Received Event data", "Microsoft.ApplicationInsights.EventData", messages.get(0).getBaseType());
        Assert.assertEquals("Got the start session string", "Session Start Event", ((EventData)messages.get(0)).getName());
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
        Assert.assertEquals("Received Event data", "Microsoft.ApplicationInsights.EventData", messages.get(0).getBaseType());
        Assert.assertEquals("Got the start session string", "Session Start Event", ((EventData)messages.get(0)).getName());
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
        Assert.assertEquals("Received Event data", "Microsoft.ApplicationInsights.EventData", messages.get(0).getBaseType());
        Assert.assertEquals("Got the start session string", "Session Start Event", ((EventData)messages.get(0)).getName());
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
        getInstrumentation().callActivityOnPause(activity3);
        getInstrumentation().callActivityOnPause(activity2);
        getInstrumentation().callActivityOnPause(activity1);
        getInstrumentation().callActivityOnResume(activity1);

        // validation
        ArrayList<ITelemetry> messages = this.mockLifeCycleTracking.tc.getMessages();
        Assert.assertEquals("Received 7 messages", 7, messages.size());
        Assert.assertEquals("Received Event data", "Microsoft.ApplicationInsights.EventData", messages.get(5).getBaseType());
        Assert.assertEquals("Got the start session string", "Session Start Event", ((EventData)messages.get(5)).getName());
        Assert.assertEquals("Received page view", "Microsoft.ApplicationInsights.PageViewData", messages.get(6).getBaseType());
    }

    public void testOnStopEvent() throws Exception {
        // setup
        MockActivity activity = this.startActivity(this.intent, null, null);

        // test that on start event fires first time
        getInstrumentation().callActivityOnResume(activity);
        getInstrumentation().callActivityOnPause(activity);

        // validation
        ArrayList<ITelemetry> messages = this.mockLifeCycleTracking.tc.getMessages();
        Assert.assertEquals("Received 3 messages", 3, messages.size());
        Assert.assertEquals("Received Event data", "Microsoft.ApplicationInsights.EventData", messages.get(0).getBaseType());
        Assert.assertEquals("Got the start session string", "Session Start Event", ((EventData)messages.get(0)).getName());
        Assert.assertEquals("Received page view", "Microsoft.ApplicationInsights.PageViewData", messages.get(1).getBaseType());
        Assert.assertEquals("Received Event data", "Microsoft.ApplicationInsights.EventData", messages.get(2).getBaseType());
        Assert.assertEquals("Got the stop session string", "Session Stop Event", ((EventData)messages.get(2)).getName());
    }

    public void testOnStopEventMultipleActivities() throws Exception {
        // setup
        MockActivity activity1 = this.startActivity(this.intent, null, null);
        MockActivity activity2 = this.getMockActivity(new Intent(
                this.getInstrumentation().getContext(),
                MockActivity.class), MockActivity.class);
        MockActivity activity3 = this.getMockActivity(new Intent(
                this.getInstrumentation().getContext(),
                MockActivity.class), MockActivity.class);

        // test 3 activities starting/stopping then restarting the first
        getInstrumentation().callActivityOnResume(activity1);
        getInstrumentation().callActivityOnResume(activity2);
        getInstrumentation().callActivityOnResume(activity3);
        getInstrumentation().callActivityOnPause(activity3);
        getInstrumentation().callActivityOnPause(activity2);
        getInstrumentation().callActivityOnPause(activity1);
        getInstrumentation().callActivityOnResume(activity1);

        // validation
        ArrayList<ITelemetry> messages = this.mockLifeCycleTracking.tc.getMessages();
        Assert.assertEquals("Received 7 messages", 7, messages.size());
        Assert.assertEquals("Received Event data", "Microsoft.ApplicationInsights.EventData", messages.get(4).getBaseType());
        Assert.assertEquals("Got the stop session string", "Session Stop Event", ((EventData)messages.get(4)).getName());
        Assert.assertEquals("Received Event data", "Microsoft.ApplicationInsights.EventData", messages.get(5).getBaseType());
        Assert.assertEquals("Got the start session string", "Session Start Event", ((EventData)messages.get(5)).getName());
        Assert.assertEquals("Received page view", "Microsoft.ApplicationInsights.PageViewData", messages.get(6).getBaseType());
    }

    public void testSessionTimeout() throws Exception {
        // setup
        MockActivity activity1 = this.startActivity(this.intent, null, null);
        MockActivity activity2 = this.getMockActivity(new Intent(
                this.getInstrumentation().getContext(),
                MockActivity.class), MockActivity.class);
        MockActivity activity3 = this.getMockActivity(new Intent(
                this.getInstrumentation().getContext(),
                MockActivity.class), MockActivity.class);

        // test 3 activities starting/stopping then restarting the first
        getInstrumentation().callActivityOnResume(activity1);

        this.mockLifeCycleTracking.currentTime += this.mockLifeCycleTracking.getSessionInterval();
        getInstrumentation().callActivityOnResume(activity2);
        getInstrumentation().callActivityOnResume(activity3);
        getInstrumentation().callActivityOnPause(activity3);
        getInstrumentation().callActivityOnPause(activity2);

        // return immediately (should trigger new session since all activities were stopped)
        getInstrumentation().callActivityOnPause(activity1);
        getInstrumentation().callActivityOnResume(activity1);

        // validation
        ArrayList<ITelemetry> messages = this.mockLifeCycleTracking.tc.getMessages();
        Assert.assertEquals("Received 8 messages", 8, messages.size());
        Assert.assertEquals("Got the start session string", "Session Start Event", ((EventData)messages.get(2)).getName());
        Assert.assertEquals("Received page view", "Microsoft.ApplicationInsights.EventData", messages.get(2).getBaseType());
        Assert.assertEquals("Received page view", "Microsoft.ApplicationInsights.PageViewData", messages.get(3).getBaseType());
        Assert.assertEquals("Received page view", "Microsoft.ApplicationInsights.PageViewData", messages.get(4).getBaseType());
        Assert.assertEquals("Received Event data", "Microsoft.ApplicationInsights.EventData", messages.get(5).getBaseType());
        Assert.assertEquals("Got the stop session string", "Session Stop Event", ((EventData)messages.get(5)).getName());
        Assert.assertEquals("Received Event data", "Microsoft.ApplicationInsights.EventData", messages.get(6).getBaseType());
        Assert.assertEquals("Got the start session string", "Session Start Event", ((EventData)messages.get(6)).getName());
        Assert.assertEquals("Received page view", "Microsoft.ApplicationInsights.PageViewData", messages.get(7).getBaseType());
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
