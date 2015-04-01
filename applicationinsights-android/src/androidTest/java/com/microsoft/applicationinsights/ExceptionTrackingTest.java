package com.microsoft.applicationinsights;

import android.test.ActivityUnitTestCase;

import com.microsoft.applicationinsights.internal.TelemetryQueue;
import com.microsoft.applicationinsights.contracts.CrashData;
import com.microsoft.applicationinsights.contracts.shared.ITelemetry;
import com.microsoft.mocks.MockActivity;
import com.microsoft.mocks.MockExceptionTracking;
import com.microsoft.mocks.MockTelemetryClient;

import junit.framework.Assert;

public class ExceptionTrackingTest extends ActivityUnitTestCase<MockActivity> {

    public Thread.UncaughtExceptionHandler originalHandler;
    public ExceptionTrackingTest() {
        super(MockActivity.class);
    }

    public void setUp() throws Exception {
        super.setUp();
        originalHandler = Thread.getDefaultUncaughtExceptionHandler();
    }

    public void tearDown() throws Exception {
        super.tearDown();
        Thread.setDefaultUncaughtExceptionHandler(originalHandler);
        TelemetryQueue.INSTANCE.setIsCrashing(false);
        TelemetryQueue.INSTANCE.getConfig().setDeveloperMode(false);
    }

    public void testRegisterExceptionHandler() throws Exception {
        ExceptionTracking.registerExceptionHandler(this.getActivity());
        Thread.UncaughtExceptionHandler handler =
                Thread.getDefaultUncaughtExceptionHandler();
        Assert.assertNotNull("handler is set", handler);
        Assert.assertEquals("handler is of correct type", ExceptionTracking.class, handler.getClass());

        // double register without debug mode
        TelemetryQueue.INSTANCE.getConfig().setDeveloperMode(false);
        ExceptionTracking.registerExceptionHandler(this.getActivity());
        Assert.assertTrue("no exception for multiple registration without debug mode", true);

        // double register with debug mode and verify runtime exception
        TelemetryQueue.INSTANCE.getConfig().setDeveloperMode(true);
        RuntimeException exception = null;
        try {
            ExceptionTracking.registerExceptionHandler(this.getActivity());
        } catch (RuntimeException e) {
            exception = e;
        }

        Assert.assertNotNull("developer Exception was thrown", exception);
    }

    public void testUncaughtException() throws Exception {

        // setup
        MockExceptionTracking tracker = new MockExceptionTracking(this.getActivity(), null, false);
        MockTelemetryClient client = new MockTelemetryClient(this.getActivity());
        tracker.setTelemetryClient(client);
        String testMessage = "test exception message";

        // test
        tracker.uncaughtException(Thread.currentThread(), new Exception(testMessage));

        // validation
        ITelemetry message = client.getMessages().get(0);
        Assert.assertNotNull("crash was caught", message);
        Assert.assertEquals("crash is of the correct type", CrashData.class, message.getClass());
        Assert.assertEquals("kill process was called", 1, tracker.processKillCount);
    }
}