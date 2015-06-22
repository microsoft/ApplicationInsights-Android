package com.microsoft.applicationinsights.library;

import android.content.Context;

import com.microsoft.applicationinsights.contracts.User;

public class PublicTelemetryContext extends TelemetryContext {
    public PublicTelemetryContext(Context appContext, String instrumentationKey, String userId){
        super(appContext,instrumentationKey, userId);
    }

    public PublicTelemetryContext(Context appContext, String instrumentationKey, User user) {
        super(appContext, instrumentationKey, user);
    }
}
