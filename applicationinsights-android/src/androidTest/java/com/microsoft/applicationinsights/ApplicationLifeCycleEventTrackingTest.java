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

import junit.framework.Assert;
import junit.framework.TestCase;

import java.util.ArrayList;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class ApplicationLifeCycleEventTrackingTest extends ActivityUnitTestCase<TestActivity> {
    TestApplication testApp;
    TestActivity testActivity;
    MockTelemetryClient tc;
    Intent intent;

    public ApplicationLifeCycleEventTrackingTest() {
        super(TestActivity.class);

    }

    public void setUp() throws Exception {
        super.setUp();
        tc =  new MockTelemetryClient(this.getInstrumentation().getContext(), "TEST_IKEY");
        testApp = new TestApplication(getInstrumentation().getContext());
        setApplication(testApp);
        testActivity = getActivity();
        intent = new Intent(getInstrumentation().getTargetContext(), TestActivity.class);
    }

    public void testOnActivityCreated() throws Exception {
        startActivity(intent, null, null);
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


    private class MockTelemetryClient extends TelemetryClient {
        ArrayList<ITelemetry> messages = new ArrayList<ITelemetry>(10);

        public MockTelemetryClient (Context context, String iKey) {
            super(new TelemetryClientConfig(iKey, context));
        }

        @Override
        public void track(ITelemetry telemetry) {
            messages.add(telemetry);
        }

        public ArrayList<ITelemetry> getMessages()
        {
            return messages;
        }

        public void clearMessages()
        {
            messages.clear();
        }
    }

}

class TestApplication extends Application {
    Context context;
    public TestApplication(Context ctx) {
        this.context = ctx;
    }

    @Override
    public Context getApplicationContext() {
        return this.context;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ApplicationLifeCycleEventTracking tracking = new ApplicationLifeCycleEventTracking();
        registerActivityLifecycleCallbacks(tracking.getApplicationLifeCycleEventTracking());
    }
}

class TestActivity extends Activity {
    public Context context;
    public Application app;
    public TestActivity(Application app) {
        this.context = app.getApplicationContext();
        this.app = app;
        Application application = this.getApplication();
        application = this.app;
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