package com.microsoft.applicationinsights.channel;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import com.microsoft.applicationinsights.TelemetryClientConfig;
import com.microsoft.applicationinsights.channel.contracts.Application;
import com.microsoft.applicationinsights.channel.contracts.Device;
import com.microsoft.applicationinsights.channel.contracts.Session;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.UUID;

/**
 * This class is holding all telemetryContext information.
 */
public class TelemetryContext extends AbstractTelemetryContext {

    protected static final String SHARED_PREFERENCES_KEY = "APPINSIGHTS_CONTEXT";
    protected static final String SESSION_ID_KEY = "SESSION_ID";
    protected static final String SESSION_ACQUISITION_KEY = "SESSION_ACQUISITION";

    /**
     * Android app telemetryContext.
     */
    private Context androidAppContext;

    /**
     * The session acquisition date.
     */
    private long acquisitionMs;

    /**
     * The session renewal date.
     */
    private long renewalMs;

    /**
     * The shared preferences reader for this context.
     */
    private SharedPreferences settings;

    /**
     * Constructs a new instance of the Telemetry telemetryContext tag keys
     * @param config the configuration for this telemetryContext
     */
    public TelemetryContext(TelemetryClientConfig config) {
        super(config);

        this.androidAppContext = config.getAppContext();
        this.settings = androidAppContext.getSharedPreferences(
                TelemetryContext.SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);

        this.setAppContext();
        this.setDeviceContext();
        this.setSessionContext();
    }

    /**
     * Update the session context and
     * @return a map of the context tags assembled in the required data contract format.
     */
    @Override
    public LinkedHashMap<String, String> getContextTags() {
        this.updateSessionContext();
        return super.getContextTags();
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

    /**
     * Sets the session context tags
     */
    private void setSessionContext() {

        // read session info from persistent storage
        this.acquisitionMs = this.settings.getLong(TelemetryContext.SESSION_ACQUISITION_KEY, 0);
        this.renewalMs = this.acquisitionMs;
        String sessionId = this.settings.getString(TelemetryContext.SESSION_ID_KEY, "");
        this.getSession().setId(sessionId);
    }

    /**
     * Updates the session context
     *
     * The session ID is renewed after 30min of inactivity or 24 hours of
     * continuous use. Additionally, the isFirst flag is set if no data was
     * found in settings and the isNew flag is set each time a new UUID is
     * generated.
     */
    private void updateSessionContext() {
        long now = this.getTime();

        // check if this is the first known session (default value of 0 is assigned by setSessionContext)
        boolean isFirst = this.acquisitionMs == 0 || this.renewalMs == 0;

        // check if the session has expired
        boolean acqExpired = (now - this.acquisitionMs) > this.config.getSessionExpirationMs();
        boolean renewalExpired = (now - this.renewalMs) > this.config.getSessionRenewalMs();

        // renew if this is the first update or if acquisitionSpan/renewalSpan have elapsed
        Session session = this.getSession();
        session.setIsFirst(isFirst ? "true" : "false");
        if (isFirst || acqExpired || renewalExpired) {
            session.setId(UUID.randomUUID().toString());
            session.setIsNew("true");

            this.renewalMs = now;
            this.acquisitionMs = now;

            SharedPreferences.Editor editor = this.settings.edit();
            editor.putString(TelemetryContext.SESSION_ID_KEY, session.getId());
            editor.putLong(TelemetryContext.SESSION_ACQUISITION_KEY, this.acquisitionMs);
            editor.apply();
        } else {
            this.renewalMs = now;
            session.setIsNew("false");
        }
    }

    /**
     * Gets the current time in milliseconds (also allows a test hook for session logic).
     * @return the current time in milliseconds
     */
    protected long getTime() {
        return new Date().getTime();
    }
}
