package com.microsoft.applicationinsights.channel;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.telephony.TelephonyManager;

import com.microsoft.applicationinsights.TelemetryClientConfig;
import com.microsoft.applicationinsights.channel.contracts.Application;
import com.microsoft.applicationinsights.channel.contracts.Device;
import com.microsoft.applicationinsights.channel.contracts.Internal;
import com.microsoft.applicationinsights.channel.contracts.Operation;
import com.microsoft.applicationinsights.channel.contracts.Session;
import com.microsoft.applicationinsights.channel.contracts.User;
import com.microsoft.commonlogging.channel.CommonContext;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.UUID;

/**
 * This class is holding all telemetryContext information.
 */
public class TelemetryContext {

    protected static final String SHARED_PREFERENCES_KEY = "APP_INSIGHTS_CONTEXT";
    protected static final String USER_ID_KEY = "USER_ID";
    protected static final String USER_ACQ_KEY = "USER_ACQ";
    protected static final String SDK_VERSION = "1.0-a.2";

    /**
     * Synchronization lock for setting static context
     */
    private static final Object lock = new Object();

    /**
     * The common context already collected by common-logging
     */
    protected static CommonContext commonContext;

    /**
     * Android app telemetryContext.
     */
    private static Context androidAppContext;

    /**
     * Device telemetryContext.
     */
    private static Device device;

    /**
     * Session telemetryContext.
     */
    private static Session session;

    /**
     * User telemetryContext.
     */
    private static User user;

    /**
     * Application telemetryContext.
     */
    private static Application application;

    /**
     * Internal telemetryContext.
     */
    private static Internal internal;

    /**
     * The last session ID
     */
    private static String lastSessionId;

    /**
     * Operation telemetryContext.
     */
    private Operation operation;

    /**
     * Get user telemetryContext.
     */
    public User getUser() {
        return user;
    }

    /**
     * Get device telemetryContext.
     */
    public Device getDevice() {
        return device;
    }

    /**
     * Operation telemetryContext.
     */
    public Operation getOperation() {
        return operation;
    }

    /**
     * Session telemetryContext.
     */
    public Session getSession() {
        return session;
    }

    /**
     * Application telemetryContext.
     */
    public Application getApplication() {
        return application;
    }

    /**
     * Constructs a new instance of the Telemetry telemetryContext tag keys
     * @param config the configuration for this telemetryContext
     */
    public TelemetryContext(TelemetryClientConfig config) {

        if(TelemetryContext.androidAppContext == null) {
            synchronized (TelemetryContext.lock) {

                // get an instance of the shared preferences manager for persistent context fields
                TelemetryContext.commonContext = new CommonContext(config.getAppContext());
                TelemetryContext.androidAppContext = config.getAppContext();

                // initialize static context
                TelemetryContext.device = new Device();
                TelemetryContext.session = new Session();
                TelemetryContext.user = new User();
                TelemetryContext.internal = new Internal();
                TelemetryContext.application = new Application();
                TelemetryContext.lastSessionId = null;

                TelemetryContext.setDeviceContext();
                TelemetryContext.setSessionContext();
                TelemetryContext.setUserContext();
                TelemetryContext.setAppContext();
                TelemetryContext.setInternalContext();
            }
        }

        this.operation = new Operation();
    }

    /**
     * @return a map of the context tags assembled in the required data contract format.
     */
    public LinkedHashMap<String, String> getContextTags() {

        // create a new hash map and add all context to it
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        TelemetryContext.application.addToHashMap(map);
        TelemetryContext.device.addToHashMap(map);
        TelemetryContext.session.addToHashMap(map);
        TelemetryContext.user.addToHashMap(map);
        TelemetryContext.internal.addToHashMap(map);

        this.operation.addToHashMap(map);

        return map;
    }

    /**
     * Renews the session context
     *
     * The session ID is on demand. Additionally, the isFirst flag is set if no data was
     * found in settings and the isNew flag is set each time a new UUID is
     * generated.
     */
    public static void renewSessionId() {
        String newId = UUID.randomUUID().toString();
        TelemetryContext.session.setId(newId);
    }

    /**
     * Sets the session context
     */
    protected static void setSessionContext() {
        if(TelemetryContext.lastSessionId == null) {
            TelemetryContext.renewSessionId();
        } else {
            TelemetryContext.session.setId(TelemetryContext.lastSessionId);
        }
    }

    /**
     * Sets the user context
     */
    protected static void setUserContext() {
        User context = TelemetryContext.user;
        context.setId(TelemetryContext.commonContext.getUserId());
        context.setAccountAcquisitionDate(TelemetryContext.commonContext.getUserAcquisitionDate());
    }

    /**
     * Sets the application telemetryContext tags
     */
    protected static void setAppContext() {
        CommonContext common = TelemetryContext.commonContext;
        String ver = String.format("%s (%S)", common.getAppId(), common.getAppBuild());

        Application context = TelemetryContext.application;
        context.setVer(ver);
    }

    /**
     * Sets the device telemetryContext tags
     */
    protected static void setDeviceContext() {
        Device context = TelemetryContext.device;

        context.setId(TelemetryContext.commonContext.getDeviceId());
        context.setOsVersion(TelemetryContext.commonContext.getDeviceOsVersion());
        context.setOs(TelemetryContext.commonContext.getDeviceOs());
        context.setModel(Build.MODEL);
        context.setOemName(Build.MANUFACTURER);
        context.setLocale(Locale.getDefault().toString());

        // check device type
        final TelephonyManager telephonyManager = (TelephonyManager)
                TelemetryContext.androidAppContext.getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager.getPhoneType() != TelephonyManager.PHONE_TYPE_NONE) {
            context.setType("Phone");
        } else {
            context.setType("Tablet");
        }

        // check network type
        final ConnectivityManager connectivityManager = (ConnectivityManager)
                TelemetryContext.androidAppContext.getSystemService(Context.CONNECTIVITY_SERVICE);
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

        // detect emulator
        if(Build.FINGERPRINT.startsWith("generic")) {
            context.setModel("[Emulator]" + context.getModel());
        }
    }

    /**
     * Sets the internal package context
     */
    protected static void setInternalContext() {
        Internal context = TelemetryContext.internal;
        context.setSdkVersion("Android:" + TelemetryContext.SDK_VERSION);
    }
}
