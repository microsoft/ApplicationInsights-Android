package com.microsoft.applicationinsights.library;

import android.content.Context;

public class MockExceptionTracking extends ExceptionTracking {

    public int processKillCount;

    public MockExceptionTracking(Context context,
                                 Thread.UncaughtExceptionHandler preexistingExceptionHandler,
                                 boolean ignoreDefaultHandler) {
        super(preexistingExceptionHandler, ignoreDefaultHandler);
        this.processKillCount = 0;
    }

    public void setUncaughtExceptionHandler(Thread.UncaughtExceptionHandler handler) {
        super.preexistingExceptionHandler = handler;
    }

    @Override
    public void killProcess() {
        // no-op but count that this was called
        this.processKillCount++;
    }
}
