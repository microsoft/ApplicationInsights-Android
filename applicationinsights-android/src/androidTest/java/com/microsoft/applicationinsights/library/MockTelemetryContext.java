package com.microsoft.applicationinsights.library;

import android.content.Context;

public class MockTelemetryContext extends TelemetryContext {

    public MockTelemetryContext(Context context, String instrumentationKey) {
        super(context, instrumentationKey);
    }
}
