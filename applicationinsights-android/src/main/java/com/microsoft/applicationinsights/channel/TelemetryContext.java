package com.microsoft.applicationinsights.channel;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import com.microsoft.applicationinsights.TelemetryClientConfig;
import com.microsoft.applicationinsights.channel.contracts.Application;
import com.microsoft.applicationinsights.channel.contracts.Device;
import com.microsoft.applicationinsights.channel.contracts.Internal;
import com.microsoft.applicationinsights.channel.contracts.Location;
import com.microsoft.applicationinsights.channel.contracts.Operation;
import com.microsoft.applicationinsights.channel.contracts.Session;
import com.microsoft.applicationinsights.channel.contracts.User;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.UUID;

/**
 * This class is holding all telemetryContext information.
 */
public class TelemetryContext {

    protected static final String SHARED_PREFERENCES_KEY = "APP_INSIGHTS_CONTEXT";
    protected static final String SESSION_ID_KEY = "SESSION_ID";
    protected static final String USER_ID_KEY = "USER_ID";

    /**
     * The configuration for this context.
     */
    protected TelemetryClientConfig config;

    /**
     * Android app telemetryContext.
     */
    private Context androidAppContext;

    /**
     * The shared preferences reader for this context.
     */
    private SharedPreferences settings;

    /**
     * Application telemetryContext.
     */
    private Application application;

    /**
     * Device telemetryContext.
     */
    private Device device;

    /**
     * Location telemetryContext.
     */
    private Location location;

    /**
     * Operation telemetryContext.
     */
    private Operation operation;

    /**
     * Session telemetryContext.
     */
    private Session session;

    /**
     * User telemetryContext.
     */
    private User user;

    /**
     * Internal telemetryContext.
     */
    private Internal internal;

    /**
     * The last session ID
     */
    private String lastSessionId;

    /**
     * Get user telemetryContext.
     */
    public User getUser() {
        return user;
    }

    /**
     * Set user telemetryContext.
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Get device telemetryContext.
     */
    public Device getDevice() {
        return device;
    }

    /**
     * Set device telemetryContext.
     */
    public void setDevice(Device device) {
        this.device = device;
    }

    /**
     * Get location context
     */
    public Location getLocation() {
        return location;
    }

    /**
     * Set location context
     */
    public void setLocation(Location location) {
        this.location = location;
    }

    /**
     * Operation telemetryContext.
     */
    public Operation getOperation() {
        return operation;
    }

    /**
     * Operation telemetryContext.
     */
    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    /**
     * Session telemetryContext.
     */
    public Session getSession() {
        return session;
    }

    /**
     * Session telemetryContext.
     */
    public void setSession(Session session) {
        this.session = session;
    }

    /**
     * Application telemetryContext.
     */
    public Application getApplication() {
        return application;
    }

    /**
     * Application telemetryContext.
     */
    public void setApplication(Application application) {
        this.application = application;
    }

    /**
     * Constructs a new instance of the Telemetry telemetryContext tag keys
     * @param config the configuration for this telemetryContext
     */
    public TelemetryContext(TelemetryClientConfig config) {

        // initialization
        this.config = config;
        this.application = new Application();
        this.device = new Device();
        this.location = new Location();
        this.operation = new Operation();
        this.session = new Session();
        this.user = new User();
        this.internal = new Internal();
        this.lastSessionId = null;

        // get an instance of the shared preferences manager for persistent context fields
        this.androidAppContext = config.getAppContext();
        this.settings = androidAppContext.getSharedPreferences(
                TelemetryContext.SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);

        // initialize default values for all context objects
        this.setAppContext();
        this.setDeviceContext();
        this.setSessionContext();
        this.setUserContext();
    }

    /**
     * @return a map of the context tags assembled in the required data contract format.
     */
    public LinkedHashMap<String, String> getContextTags() {

        // update session context
        this.setSessionFlags();

        // create a new hash map and add all context to it
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        this.application.addToHashMap(map);
        this.device.addToHashMap(map);
        this.location.addToHashMap(map);
        this.operation.addToHashMap(map);
        this.session.addToHashMap(map);
        this.user.addToHashMap(map);
        this.internal.addToHashMap(map);

        return map;
    }

    /**
     * Renews the session context
     *
     * The session ID is on demand. Additionally, the isFirst flag is set if no data was
     * found in settings and the isNew flag is set each time a new UUID is
     * generated.
     */
    public void renewSessionId() {
        String newId = UUID.randomUUID().toString();

        this.session.setId(newId);

        SharedPreferences.Editor editor = this.settings.edit();
        editor.putString(TelemetryContext.SESSION_ID_KEY, session.getId());
        editor.apply();
    }

    /**
     * Sets the session context
     */
    private void setSessionContext() {
        this.lastSessionId = this.settings.getString(TelemetryContext.SESSION_ID_KEY, null);
        if(this.lastSessionId == null) {
            this.renewSessionId();
        } else {
            this.getSession().setId(this.lastSessionId);
        }
    }

    /**
     * Sets the session context flags based on the previous session ID.
     */
    private void setSessionFlags() {
        String currentId = this.session.getId();

        // default value of lastsessionId in setSessionContext is null, so isFirst is true if null
        boolean isFirst = this.lastSessionId == null;
        boolean isNew = currentId != this.lastSessionId;

        this.lastSessionId = currentId;

        this.session.setIsFirst(isFirst ? "true" : "false");
        this.session.setIsNew(isNew ? "true" : "false");
    }

    /**
     * Sets the user context
     */
    private void setUserContext() {
        String userId = this.settings.getString(TelemetryContext.USER_ID_KEY, null);
        if(userId == null) {
            userId = UUID.randomUUID().toString();
            SharedPreferences.Editor editor = this.settings.edit();
            editor.putString(TelemetryContext.USER_ID_KEY, userId);
            editor.apply();
        }

        User context = this.getUser();
        context.setId(userId);
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
        final TelephonyManager telephonyManager = (TelephonyManager)
                this.androidAppContext.getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager.getPhoneType() != TelephonyManager.PHONE_TYPE_NONE) {
            context.setType("Phone");
        } else {
            context.setType("Tablet");
        }

        // check network type
        final ConnectivityManager connectivityManager = (ConnectivityManager)
                this.androidAppContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        if (activeNetwork != null) {
            int networkType = activeNetwork.getType();
            switch (networkType) {
                case ConnectivityManager.TYPE_WIFI:
                    context.setNetwork("WiFi");
                    break;
                case ConnectivityManager.TYPE_MOBILE:
                    context.setNetwork("Mobile");
            }
        }

        context.setOsVersion(Build.VERSION.RELEASE);
        context.setOs("Android");
        context.setOemName(Build.MANUFACTURER);
        context.setModel(Build.MODEL);
        context.setLocale(Locale.getDefault().toString());
        context.setLanguage(Locale.getDefault().getLanguage());
    }
}
