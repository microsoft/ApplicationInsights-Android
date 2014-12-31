package com.microsoft.applicationinsights.channel;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import com.microsoft.applicationinsights.AndroidConfig;
import com.microsoft.applicationinsights.channel.contracts.Application;
import com.microsoft.applicationinsights.channel.contracts.Device;
import com.microsoft.applicationinsights.channel.contracts.Internal;
import com.microsoft.applicationinsights.channel.contracts.Location;
import com.microsoft.applicationinsights.channel.contracts.Operation;
import com.microsoft.applicationinsights.channel.contracts.Session;
import com.microsoft.applicationinsights.channel.contracts.User;

/**
 * This class is holding all telemetryContext information.
 */
public class AndroidTelemetryContext extends TelemetryContext {

    /**
     * Android app telemetryContext.
     */
    private Context androidAppContext;

    /**
     * Constructs a new instance of the Telemetry telemetryContext tag keys
     * @param config the configuration for this telemetryContext
     */
    public AndroidTelemetryContext(AndroidConfig config) {
        super(config);
        this.androidAppContext = config.getAppContext(); // todo: use session info

        this.application = new Application();
        this.device = new Device();
        this.location = new Location();
        this.operation = new Operation();
        this.session = new Session();
        this.user = new User();
        this.internal = new Internal();

        this.setAppContext();
        this.setDeviceContext();
    }

    /**
     * Sets the application telemetryContext tags
     */
    private void setAppContext() {
        ContentResolver resolver = this.androidAppContext.getContentResolver();
        String id = Settings.Secure.getString(resolver, Settings.Secure.ANDROID_ID);
        if(id != null) {
            this.application.setVer(id);
        }
    }

    /**
     * Sets the device telemetryContext tags
     */
    private void setDeviceContext() {

        // get device ID
        ContentResolver resolver = this.androidAppContext.getContentResolver();
        String deviceIdentifier = Settings.Secure.getString(resolver, Settings.Secure.ANDROID_ID);
        if(deviceIdentifier != null) {
            this.device.setId(deviceIdentifier);
        }

        // check device type
        final TelephonyManager manager = (TelephonyManager)
                this.androidAppContext.getSystemService(android.content.Context.TELEPHONY_SERVICE);
        if (manager.getPhoneType() != TelephonyManager.PHONE_TYPE_NONE) {
            this.device.setType("Phone");
        } else {
            this.device.setType("Tablet");
        }

        this.device.setOsVersion(Build.VERSION.RELEASE);
        this.device.setOs("Android");
        this.device.setOemName(Build.MANUFACTURER);
        this.device.setModel(Build.MODEL);
        this.device.setLocale(java.util.Locale.getDefault().toString());
    }
}
