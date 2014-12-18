package com.microsoft.applicationinsights;

import android.text.TextUtils;

import com.microsoft.applicationinsights.channel.TelemetryChannel;
import com.microsoft.applicationinsights.channel.TelemetryContext;
import com.microsoft.applicationinsights.channel.contracts.DataPoint;
import com.microsoft.applicationinsights.channel.contracts.EventData;
import com.microsoft.applicationinsights.channel.contracts.ExceptionData;
import com.microsoft.applicationinsights.channel.contracts.MessageData;
import com.microsoft.applicationinsights.channel.contracts.MetricData;
import com.microsoft.applicationinsights.channel.contracts.PageViewData;
import com.microsoft.applicationinsights.channel.contracts.shared.ITelemetry;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * The public API for recording application insights telemetry.
 * Users would call TelemetryClient.track*
 */
public class TelemetryClient extends CoreTelemetryClient {

    /**
     * Constructor of the class TelemetryClient.
     * 
     * @param iKey the instrumentation key
     * @param context application telemetryContext from the caller
     */
    public TelemetryClient(String iKey, android.content.Context context) {
        super(new TelemetryClientConfig(iKey, context));
        this.telemetryContext = new TelemetryContext((TelemetryClientConfig)this.config);
        this.channel = new TelemetryChannel(this.config);
    }

    /**
     * Sends information about the page viewed in the application to Application
     * Insights.
     *
     * @param pageName Name of the page
     */
    public void trackPageView(String pageName) {
        this.trackPageView(pageName, null);
    }

    /**
     * Sends information about the page viewed in the application to Application
     * Insights.
     *
     * @param pageName Name of the page
     * @param properties exception properties
     */
    public void trackPageView(String pageName, HashMap<String, String> properties) {
        String localPageName = pageName;
        if (TextUtils.isEmpty(localPageName)) {
            localPageName = "";
        }

        PageViewData telemetry = new PageViewData();
        telemetry.setName(localPageName);
        telemetry.setProperties(properties);

        track(telemetry, PageViewData.EnvelopeName, PageViewData.BaseType);
    }
}
