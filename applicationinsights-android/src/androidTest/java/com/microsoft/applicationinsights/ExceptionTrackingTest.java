package com.microsoft.applicationinsights;

import android.content.Context;
import android.content.Intent;
import android.test.ActivityUnitTestCase;

import com.microsoft.applicationinsights.contracts.Envelope;
import com.microsoft.applicationinsights.internal.ChannelQueue;
import com.microsoft.applicationinsights.contracts.CrashData;
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
        Intent intent = new Intent(getInstrumentation().getTargetContext(), com.microsoft.mocks.MockActivity.class);
        this.setActivity(this.startActivity(intent, null, null));
    }

    public void tearDown() throws Exception {
        super.tearDown();
        Thread.setDefaultUncaughtExceptionHandler(originalHandler);
        ChannelQueue.INSTANCE.setIsCrashing(false);
        ChannelQueue.INSTANCE.getConfig().setDeveloperMode(false);
    }

    public void testRegisterExceptionHandler() throws Exception {
        ExceptionTracking.registerExceptionHandler(this.getActivity());
        Thread.UncaughtExceptionHandler handler =
                Thread.getDefaultUncaughtExceptionHandler();
        Assert.assertNotNull("handler is set", handler);
        Assert.assertEquals("handler is of correct type", ExceptionTracking.class, handler.getClass());

        // double register without debug mode
        ChannelQueue.INSTANCE.getConfig().setDeveloperMode(false);
        ExceptionTracking.registerExceptionHandler(this.getActivity());
        Assert.assertTrue("no exception for multiple registration without debug mode", true);

        // double register with debug mode and verify runtime exception
        ChannelQueue.INSTANCE.getConfig().setDeveloperMode(true);
        RuntimeException exception = null;
        try {
            ExceptionTracking.registerExceptionHandler(this.getActivity());
        } catch (RuntimeException e) {
            exception = e;
        }

        Assert.assertNotNull("developer Exception was thrown", exception);
    }
}