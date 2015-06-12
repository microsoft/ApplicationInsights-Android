package com.microsoft.applicationinsights.library;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.Intent;
import android.os.Build;
import android.test.ActivityUnitTestCase;
import android.test.UiThreadTest;

import com.microsoft.applicationinsights.contracts.Device;
import com.microsoft.applicationinsights.contracts.User;
import com.microsoft.applicationinsights.library.config.ApplicationInsightsConfig;

import junit.framework.Assert;

import java.util.HashMap;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class AutoCollectionTests extends ActivityUnitTestCase<MockActivity> {

    private Intent intent;
    private MockTelemetryClient telemetryClient;
    private Application mockApplication;
    private MockActivity mockActivity;
    private TelemetryContext mockTelemetryContext;
    private ApplicationInsightsConfig mockConfig;

    private static final String MOCK_APP_ID = "appId";
    private static final String MOCK_APP_VER = "appVer";
    private static final String MOCK_IKEY = "iKey";
    private static final String MOCK_USER_ID = "userId";
    private static final String MOCK_DEVICE_ID = "deviceId";
    private static final String MOCK_OS_VER = "osVer";
    private static final String MOCK_OS = "os";
    private static final String MOCK_TAGS_KEY = "tagsKey";
    private static final String MOCK_TAGS_VALUE = "tagsValue";

    public AutoCollectionTests() {
        super(MockActivity.class);
    }


    public void setUp() throws Exception {
        super.setUp();
        System.setProperty("dexmaker.dexcache", getInstrumentation().getTargetContext().getCacheDir().getPath());

        mockTelemetryContext = getMockContext();
        mockConfig = mock(ApplicationInsightsConfig.class);
        mockApplication = mock(Application.class);
        this.setApplication(mockApplication);

        intent = new Intent(getInstrumentation().getTargetContext(), MockActivity.class);


        AutoCollection.initialize(mockTelemetryContext, mockConfig);
    }

    public void tearDown() throws Exception {
        //this.mockApplication.unregister();
    }

    public void testInitialisationWorks() {
        Assert.assertNotNull(AutoCollection.getInstance());
    }


    public void testReturnsSameAutoCollection() {
        AutoCollection autoCollection1 = AutoCollection.getInstance();
        AutoCollection autoCollection2 = AutoCollection.getInstance();

        Assert.assertSame(autoCollection1, autoCollection2);
    }

    public void testEnablingCallbacksCanBeEnabled() {
        AutoCollection.getInstance().enableAutoPageViews(mockApplication);
        Assert.assertTrue(AutoCollection.getInstance().isAutoPageViewsEnabled());

        AutoCollection.getInstance().enableAutoSessionManagement(mockApplication);
        Assert.assertTrue(AutoCollection.getInstance().isAutoSessionManagementEnabled());

        AutoCollection.getInstance().enableAutoAppearanceTracking(mockApplication);
        Assert.assertTrue(AutoCollection.getInstance().isAutoAppearanceTrackingEnabled());
    }

    //TODO throws a NPE and I haven't managed to solve it
    public void testPageViewEvent() throws Exception {
        // setup
        MockActivity activity = this.startActivity(this.intent, null, null);

        // test
        getInstrumentation().callActivityOnCreate(activity, null);
    }

//
//    @UiThreadTest
//    public void testCallbacksGetCalled() {
//        // setup
//        //MockActivity activity = this.startActivity(intent, null, null);
//        getInstrumentation().runOnMainSync(new Runnable() {
//            @Override
//            public void run() {
//                mockActivity = startActivity(intent, null, null);
//            }
//        });
//        //MockActivity activity;
//        // test
//        //getInstrumentation().callActivityOnResume(mockActivity);
//
//    }


//    public void testPageViewEvent() throws Exception {
//        // setup
//        MockActivity activity = this.startActivity(this.intent, null, null);
//        setActivity(activity);
//
//        // test
//        getInstrumentation().callActivityOnResume(getActivity());
//
//
//        // validation
//        ArrayList<Envelope> messages = MockAutoCollection.getInstance().tc.getMessages();
//
//        Assert.assertEquals("Received 2 messages", 2, messages.size());
//        SessionStateData sessionData = (SessionStateData)((Data<ITelemetryData>) messages.get(0).getData()).getBaseData();
//        Assert.assertEquals("Received Session State data", "Microsoft.ApplicationInsights.SessionStateData", sessionData.getBaseType());
//        Assert.assertEquals("Got the start session", SessionState.Start, sessionData.getState());
//        Assert.assertEquals("Received page view", "Microsoft.ApplicationInsights.PageViewData", messages.get(1).getData().getBaseType());
//    }

//    public void testPageViewEventMultipleActivities() throws Exception {
//        MockActivity activity1 = this.startActivity(this.intent, null, null);
//        MockActivity activity2 = this.getMockActivity(new Intent(
//              this.getInstrumentation().getContext(),
//              MockActivity.class), MockActivity.class);
//        MockActivity activity3 = this.getMockActivity(new Intent(
//              this.getInstrumentation().getContext(),
//              MockActivity.class), MockActivity.class);
//
//        // test
//        getInstrumentation().callActivityOnResume(activity1);
//        getInstrumentation().callActivityOnResume(activity2);
//        getInstrumentation().callActivityOnResume(activity3);
//        getInstrumentation().callActivityOnResume(activity2);
//        getInstrumentation().callActivityOnResume(activity1);
//
//        // validation
//        ArrayList<Envelope> messages = this.mockAutoCollection.tc.getMessages();
//        Assert.assertEquals("Received 6 messages", 6, messages.size());
//        Assert.assertEquals("Received Session State data", "Microsoft.ApplicationInsights.SessionStateData", messages.get(0).getData().getBaseType());
//
//        SessionStateData sessionData = (SessionStateData) ((Data<ITelemetryData>) messages.get(0).getData()).getBaseData();
//        Assert.assertEquals("Got the start session", SessionState.Start, sessionData.getState());
//        Assert.assertEquals("Received page view", "Microsoft.ApplicationInsights.PageViewData", messages.get(1).getData().getBaseType());
//        Assert.assertEquals("Received page view", "Microsoft.ApplicationInsights.PageViewData", messages.get(2).getData().getBaseType());
//        Assert.assertEquals("Received page view", "Microsoft.ApplicationInsights.PageViewData", messages.get(3).getData().getBaseType());
//        Assert.assertEquals("Received page view", "Microsoft.ApplicationInsights.PageViewData", messages.get(4).getData().getBaseType());
//        Assert.assertEquals("Received page view", "Microsoft.ApplicationInsights.PageViewData", messages.get(5).getData().getBaseType());
//    }

//    public void testOnStartEvent() throws Exception {
//        // setup
//        MockActivity activity = this.startActivity(this.intent, null, null);
//
//        // test that on start event fires first time
//        getInstrumentation().callActivityOnResume(activity);
//
//        // validation
//        ArrayList<Envelope> messages = this.mockAutoCollection.tc.getMessages();
//        Assert.assertEquals("Received 2 messages", 2, messages.size());
//        Assert.assertEquals("Received Session State data", "Microsoft.ApplicationInsights.SessionStateData", messages.get(0).getData().getBaseType());
//
//        SessionStateData sessionData = (SessionStateData) ((Data<ITelemetryData>) messages.get(0).getData()).getBaseData();
//        Assert.assertEquals("Got the start session", SessionState.Start, sessionData.getState());
//        Assert.assertEquals("Received page view", "Microsoft.ApplicationInsights.PageViewData", messages.get(1).getData().getBaseType());
//
//        // test that on start event doesn't fire second time
//        getInstrumentation().callActivityOnResume(activity);
//
//        // validation
//        messages = this.mockAutoCollection.tc.getMessages();
//        Assert.assertEquals("Received 3 message2", 3, messages.size());
//        Assert.assertEquals("Received page view", "Microsoft.ApplicationInsights.PageViewData", messages.get(2).getData().getBaseType());
//    }
//
//    public void testOnStartEventMultipleActivities() throws Exception {
//        // setup
//        MockActivity activity1 = this.startActivity(this.intent, null, null);
//        MockActivity activity2 = this.getMockActivity(new Intent(
//              this.getInstrumentation().getContext(),
//              MockActivity.class), MockActivity.class);
//        MockActivity activity3 = this.getMockActivity(new Intent(
//              this.getInstrumentation().getContext(),
//              MockActivity.class), MockActivity.class);
//
//        // test
//        getInstrumentation().callActivityOnResume(activity1);
//        getInstrumentation().callActivityOnResume(activity2);
//        getInstrumentation().callActivityOnResume(activity3);
//        getInstrumentation().callActivityOnResume(activity1);
//
//        // validation
//        ArrayList<Envelope> messages = this.mockAutoCollection.tc.getMessages();
//        Assert.assertEquals("Received 5 messages", 5, messages.size());
//        Assert.assertEquals("Received Session State data", "Microsoft.ApplicationInsights.SessionStateData", messages.get(0).getData().getBaseType());
//
//        SessionStateData sessionData = (SessionStateData) ((Data<ITelemetryData>) messages.get(0).getData()).getBaseData();
//        Assert.assertEquals("Got the start session", SessionState.Start, sessionData.getState());
//        Assert.assertEquals("Received page view", "Microsoft.ApplicationInsights.PageViewData", messages.get(1).getData().getBaseType());
//        Assert.assertEquals("Received page view", "Microsoft.ApplicationInsights.PageViewData", messages.get(2).getData().getBaseType());
//        Assert.assertEquals("Received page view", "Microsoft.ApplicationInsights.PageViewData", messages.get(3).getData().getBaseType());
//        Assert.assertEquals("Received page view", "Microsoft.ApplicationInsights.PageViewData", messages.get(4).getData().getBaseType());
//
//    }
//
//    public void testSessionTimeout() throws Exception {
//        // setup
//        MockActivity activity1 = this.startActivity(this.intent, null, null);
//        MockActivity activity2 = this.getMockActivity(new Intent(
//              this.getInstrumentation().getContext(),
//              MockActivity.class), MockActivity.class);
//
//        // test 3 activities starting/stopping then restarting the first
//        getInstrumentation().callActivityOnResume(activity1);
//        getInstrumentation().callActivityOnResume(activity2);
//
//        this.mockAutoCollection.currentTime += ApplicationInsights.getConfig().getSessionIntervalMs();
//        getInstrumentation().callActivityOnResume(activity1);
//        getInstrumentation().callActivityOnResume(activity2);
//
//        // validation
//        ArrayList<Envelope> messages = this.mockAutoCollection.tc.getMessages();
//        Assert.assertEquals("Received 6 messages", 6, messages.size());
//        Assert.assertEquals("Received Session State data", "Microsoft.ApplicationInsights.SessionStateData", messages.get(0).getData().getBaseType());
//
//        SessionStateData sessionData = (SessionStateData) ((Data<ITelemetryData>) messages.get(0).getData()).getBaseData();
//        Assert.assertEquals("Got the start session", SessionState.Start, sessionData.getState());
//        Assert.assertEquals("Received page view", "Microsoft.ApplicationInsights.PageViewData", messages.get(1).getData().getBaseType());
//        Assert.assertEquals("Received page view", "Microsoft.ApplicationInsights.PageViewData", messages.get(2).getData().getBaseType());
//        Assert.assertEquals("Received Session State data", "Microsoft.ApplicationInsights.SessionStateData", messages.get(3).getData().getBaseType());
//
//        sessionData = (SessionStateData) ((Data<ITelemetryData>) messages.get(3).getData()).getBaseData();
//        Assert.assertEquals("Got the start session", SessionState.Start, sessionData.getState());
//        Assert.assertEquals("Received page view", "Microsoft.ApplicationInsights.PageViewData", messages.get(4).getData().getBaseType());
//        Assert.assertEquals("Received page view", "Microsoft.ApplicationInsights.PageViewData", messages.get(5).getData().getBaseType());
//    }
//
//    private MockActivity getMockActivity(Intent intent, Class activityClass) {
//        IBinder token = null;
//        setApplication(new android.test.mock.MockApplication());
//        ComponentName cn = new ComponentName(activityClass.getPackage().getName(),
//              activityClass.getName());
//        intent.setComponent(cn);
//        ActivityInfo info = new ActivityInfo();
//        CharSequence title = activityClass.getName();
//
//        String id = null;
//
//        MockActivity activity = null;
//        try {
//            activity = (MockActivity) getInstrumentation().newActivity(
//                  activityClass,
//                  this.getInstrumentation().getContext(),
//                  token, this.mockApplication, intent, info,
//                  title, this.getActivity().getParent(), id, null);
//        } catch (InstantiationException e) {
//            e.printStackTrace();
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        }
//
//        return activity;
//    }

    private static PublicTelemetryContext getMockContext() {
        HashMap<String, String> tags = new HashMap<String, String>();
        tags.put(MOCK_TAGS_KEY, MOCK_TAGS_VALUE);

        com.microsoft.applicationinsights.contracts.Application mockApplication = mock(com.microsoft.applicationinsights.contracts.Application.class);
        when(mockApplication.getVer()).thenReturn(MOCK_APP_VER);

        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn(MOCK_USER_ID);

        Device mockDevice = mock(Device.class);
        when(mockDevice.getId()).thenReturn(MOCK_DEVICE_ID);
        when(mockDevice.getOsVersion()).thenReturn(MOCK_OS_VER);
        when(mockDevice.getOs()).thenReturn(MOCK_OS);

        PublicTelemetryContext mockContext = mock(PublicTelemetryContext.class);
        when(mockContext.getPackageName()).thenReturn(MOCK_APP_ID);
        when(mockContext.getContextTags()).thenReturn(tags);
        when(mockContext.getApplication()).thenReturn(mockApplication);
        when(mockContext.getInstrumentationKey()).thenReturn(MOCK_IKEY);
        when(mockContext.getDevice()).thenReturn(mockDevice);
        when(mockContext.getUser()).thenReturn(mockUser);

        return mockContext;
    }
}
