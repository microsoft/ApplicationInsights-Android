package com.microsoft.applicationinsights.library;

import android.content.Context;

import java.util.HashMap;

public class PublicTelemetryContext extends TelemetryContext {
    public PublicTelemetryContext(Context appContext, String instrumentationKey, String userId){
        super(appContext,instrumentationKey, userId);
    }
}
