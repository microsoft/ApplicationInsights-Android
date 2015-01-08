package com.microsoft.applicationinsights.channel;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import com.microsoft.applicationinsights.TelemetryClientConfig;
import com.microsoft.applicationinsights.channel.contracts.Application;
import com.microsoft.applicationinsights.channel.contracts.Device;

/**
 * This class is holding all telemetryContext information.
 */
public class TelemetryContext extends AbstractTelemetryContext {

    /**
     * Android app telemetryContext.
     */
    private Context androidAppContext;

    /**
     * Constructs a new instance of the Telemetry telemetryContext tag keys
     * @param config the configuration for this telemetryContext
     */
    public TelemetryContext(TelemetryClientConfig config) {
        super(config);
        this.androidAppContext = config.getAppContext(); // todo: use session info

        this.setAppContext();
        this.setDeviceContext();
    }

    /**
     * Sets the application telemetryContext tags
     */
    private void setAppContext() {
        Application context = this.getApplication();
        ContentResolver resolver = this.androidAppContext.getContentResolver();
        String id = Settings.Secure.getString(resolver, Settings.Secure.ANDROID_ID);
        if(id != null) {
            context.setVer(id);
        }
    }

    /**
     * Sets the device telemetryContext tags
     */
    private void setDeviceContext() {
        Device context = this.getDevice();

        // get device ID
        ContentResolver resolver = this.androidAppContext.getContentResolver();
        String deviceIdentifier = Settings.Secure.getString(resolver, Settings.Secure.ANDROID_ID);
        if(deviceIdentifier != null) {
            context.setId(deviceIdentifier);
        }

        // check device type
        final TelephonyManager manager = (TelephonyManager)
                this.androidAppContext.getSystemService(android.content.Context.TELEPHONY_SERVICE);
        if (manager.getPhoneType() != TelephonyManager.PHONE_TYPE_NONE) {
            context.setType("Phone");
        } else {
            context.setType("Tablet");
        }

        context.setOsVersion(Build.VERSION.RELEASE);
        context.setOs("Android");
        context.setOemName(Build.MANUFACTURER);
        context.setModel(Build.MODEL);
        context.setLocale(java.util.Locale.getDefault().toString());
    }
}
