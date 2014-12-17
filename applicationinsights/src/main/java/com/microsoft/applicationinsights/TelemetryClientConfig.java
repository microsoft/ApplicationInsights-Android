package com.microsoft.applicationinsights;

import android.content.Context;

/**
 * Configuration object when instantiating TelemetryClient
 */
public class TelemetryClientConfig extends AbstractTelemetryClientConfig {


    /**
     * The application telemetryContext for this recorder
     */
    private final Context appContext;

    /**
     * The account id for this telemetryContext
     */
    private String accountId;

    /**
     * The number of milliseconds which must expire before a session is renewed.
     */
    private int sessionRenewalMs;

    /**
     * The number of milliseconds until a session expires.
     */
    private int sessionExpirationMs;

    /**
     * The instrumentation key for this telemetryContext
     */
    public String getInstrumentationKey() {
        return this.instrumentationKey;
    }

    /**
     * The application telemetryContext for this recorder
     */
    public Context getAppContext() {
        return appContext;
    }

    /**
     * The account id for this telemetryContext
     */
    @Override
    public String getAccountId() {
        return accountId;
    }

    /**
     * The number of milliseconds which must expire before a session is renewed.
     */
    @Override
    public int getSessionRenewalMs() {
        return sessionRenewalMs;
    }

    /**
     * The number of milliseconds until a session expires.
     */
    @Override
    public int getSessionExpirationMs() {
        return sessionExpirationMs;
    }

    public TelemetryClientConfig(String iKey, Context context){
        super(iKey);
        this.appContext = context;
    }
}
