package com.microsoft.applicationinsights.library;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.microsoft.applicationinsights.contracts.Application;
import com.microsoft.applicationinsights.contracts.Device;
import com.microsoft.applicationinsights.contracts.Internal;
import com.microsoft.applicationinsights.contracts.Operation;
import com.microsoft.applicationinsights.contracts.Session;
import com.microsoft.applicationinsights.contracts.User;
import com.microsoft.applicationinsights.logging.InternalLogging;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * This class is holding all telemetryContext information.
 */
class TelemetryContext {

    protected static final String SHARED_PREFERENCES_KEY = "APP_INSIGHTS_CONTEXT";
    protected static final String USER_ID_KEY = "USER_ID";
    protected static final String USER_ACQ_KEY = "USER_ACQ";
    private static final String TAG = "TelemetryContext";

    /**
     * Synchronization LOCK for setting static context
     */
    private static final Object LOCK = new Object();

    /**
     * Volatile boolean for double checked synchronize block
     */
    private static volatile boolean isContextLoaded = false;

    /**
     * The shared preferences INSTANCE for reading persistent context
     */
    private static SharedPreferences settings;

    /**
     * Content for tags field of an envelope
     */
    private static Map<String, String> cachedTags;

    /**
     * Device telemetryContext.
     */
    private static String instrumentationKey;

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
     * The App ID for the envelope (defined as PackageInfo.packageName by CLL team)
     */
    private static String appIdForEnvelope;

    /**
     * Operation telemetryContext.
     */
    private Operation operation;

    /**
     * Constructs a new INSTANCE of the Telemetry telemetryContext tag keys
     *
     * @param appContext the context for this telemetryContext
     */
    protected TelemetryContext(Context appContext, String instrumentationKey) {

        this.operation = new Operation();

        // note: isContextLoaded must be volatile for the double-checked LOCK to work
        if (!TelemetryContext.isContextLoaded && appContext != null) {
            synchronized (TelemetryContext.LOCK) {
                if (!TelemetryContext.isContextLoaded) {
                    TelemetryContext.isContextLoaded = true;

                    // get an INSTANCE of the shared preferences manager for persistent context fields
                    TelemetryContext.settings = appContext.getSharedPreferences(
                            TelemetryContext.SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);

                    // initialize static context
                    TelemetryContext.device = new Device();
                    TelemetryContext.session = new Session();
                    TelemetryContext.user = new User();
                    TelemetryContext.internal = new Internal();
                    TelemetryContext.application = new Application();
                    TelemetryContext.lastSessionId = null;
                    TelemetryContext.instrumentationKey = instrumentationKey;
                    TelemetryContext.cachedTags = getCachedTags();
                    TelemetryContext.setDeviceContext(appContext);
                    TelemetryContext.setSessionContext();
                    TelemetryContext.setUserContext();
                    TelemetryContext.setAppContext(appContext);
                    TelemetryContext.setInternalContext(appContext);
                }
            }
        }
    }

    /**
     * Get user the instrumentationKey.
     * @return the instrumentation key
     */
    protected String getInstrumentationKey() {
        return instrumentationKey;
    }


    /**
     * Get user telemetryContext.
     * @return the user object
     */
    protected User getUser() {
        return user;
    }

    /**
     * Get device telemetryContext.
     * @return the device object
     */
    protected Device getDevice() {
        return device;
    }

    /**
     * Operation telemetryContext.
     * @return the operation
     */
    protected Operation getOperation() {
        return operation;
    }

    /**
     * Session telemetryContext.
     * @return the session
     */
    protected Session getSession() {
        return session;
    }

    /**
     * Application telemetryContext.
     * @return the application
     */
    protected Application getApplication() {
        return application;
    }

    /**
     * The package name
     *
     * @see TelemetryContext#appIdForEnvelope
     */
    protected String getPackageName() {
        return appIdForEnvelope;
    }

    /**
     * @return a map of the context tags assembled in the required data contract format.
     */
    private Map<String, String> getCachedTags() {

        if(cachedTags == null) {
            // create a new hash map and add all context to it
            cachedTags = new LinkedHashMap<String, String>();
            TelemetryContext.application.addToHashMap(cachedTags);
            TelemetryContext.internal.addToHashMap(cachedTags);
            this.operation.addToHashMap(cachedTags);
        }
        return cachedTags;
    }

    protected Map<String, String> getContextTags() {
        Map<String, String> contextTags = new LinkedHashMap<String, String>();
        TelemetryContext.cachedTags.putAll(contextTags);
        TelemetryContext.device.addToHashMap(contextTags);
        TelemetryContext.application.addToHashMap(contextTags);
        TelemetryContext.session.addToHashMap(contextTags);
        TelemetryContext.user.addToHashMap(contextTags);
        TelemetryContext.internal.addToHashMap(contextTags);

        return contextTags;
    }

    /**
     * Renews the session context
     * <p>
     * The session ID is on demand. Additionally, the isFirst flag is set if no data was
     * found in settings and the isNew flag is set each time a new UUID is
     * generated.
     */
    protected static void renewSessionId() {
        String newId = UUID.randomUUID().toString();
        TelemetryContext.session.setId(newId);
    }

    /**
     * Sets the session context
     */
    protected static void setSessionContext() {
        if (TelemetryContext.lastSessionId == null) {
            TelemetryContext.renewSessionId();
        } else {
            TelemetryContext.session.setId(TelemetryContext.lastSessionId);
        }
    }

    /**
     * Sets the application telemetryContext tags
     * @param appContext the android context
     */
    protected static void setAppContext(Context appContext) {
        String version = "unknown";
        TelemetryContext.appIdForEnvelope = "";

        try {
            final PackageManager manager = appContext.getPackageManager();
            final PackageInfo info = manager
                    .getPackageInfo(appContext.getPackageName(), 0);

            if (info.packageName != null) {
                TelemetryContext.appIdForEnvelope = info.packageName;
            }

            String appBuild = Integer.toString(info.versionCode);
            version = String.format("%s (%S)", TelemetryContext.appIdForEnvelope, appBuild);
        } catch (PackageManager.NameNotFoundException e) {
            InternalLogging.warn("TelemetryContext", "Could not collect application context");
        } finally {
            Application context = TelemetryContext.application;
            context.setVer(version);
        }
    }

    /**
     * Sets the user context
     */
    protected static void setUserContext() {
        String userId = TelemetryContext.settings.getString(TelemetryContext.USER_ID_KEY, null);
        String userAcq = TelemetryContext.settings.getString(TelemetryContext.USER_ACQ_KEY, null);

        if (userId == null || userAcq == null) {
            userId = UUID.randomUUID().toString();
            userAcq = Util.dateToISO8601(new Date());

            SharedPreferences.Editor editor = TelemetryContext.settings.edit();
            editor.putString(TelemetryContext.USER_ID_KEY, userId);
            editor.putString(TelemetryContext.USER_ACQ_KEY, userAcq);
            editor.apply();
        }

        User context = TelemetryContext.user;
        context.setId(userId);
        context.setAccountAcquisitionDate(userAcq);

    }

    /**
     * Sets the device telemetryContext tags
     * @param appContext the android Context
     */
    protected static void setDeviceContext(Context appContext) {
        Device context = TelemetryContext.device;

        context.setOsVersion(Build.VERSION.RELEASE);
        context.setOs("Android");
        context.setModel(Build.MODEL);
        context.setOemName(Build.MANUFACTURER);
        context.setLocale(Locale.getDefault().toString());

        // get device ID
        ContentResolver resolver = appContext.getContentResolver();
        String deviceIdentifier = Settings.Secure.getString(resolver, Settings.Secure.ANDROID_ID);
        if (deviceIdentifier != null) {
            context.setId(Util.tryHashStringSha256(deviceIdentifier));
        }

        // check device type
        final TelephonyManager telephonyManager = (TelephonyManager)
                appContext.getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager.getPhoneType() != TelephonyManager.PHONE_TYPE_NONE) {
            context.setType("Phone");
        } else {
            context.setType("Tablet");
        }

        // check network type
        final ConnectivityManager connectivityManager = (ConnectivityManager)
                appContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        if (activeNetwork != null) {
            int networkType = activeNetwork.getType();
            switch (networkType) {
                case ConnectivityManager.TYPE_WIFI:
                    context.setNetwork("WiFi");
                    break;
                case ConnectivityManager.TYPE_MOBILE:
                    context.setNetwork("Mobile");
                    break;
                default:
                    context.setNetwork("Unknown");
                    InternalLogging.warn(TAG, "Unknown network type:" + networkType);
                    break;
            }
        }

        // detect emulator
        if (Build.FINGERPRINT.startsWith("generic")) {
            context.setModel("[Emulator]" + context.getModel());
        }
    }

    /**
     * Sets the internal package context
     */
    protected static void setInternalContext(Context appContext) {
        Internal context = TelemetryContext.internal;

        String sdkVersionString = "";
        String packageName = appContext.getPackageName();
        if (appContext != null) {
            try {
                Bundle bundle = appContext.getPackageManager()
                      .getApplicationInfo(appContext.getPackageName(), PackageManager.GET_META_DATA)
                      .metaData;
                if (bundle != null) {
                    sdkVersionString = bundle.getString("com.microsoft.applicationinsights.internal.sdkVersion");
                } else {
                   InternalLogging.warn(TAG, "Could not load sdk version from gradle.properties or manifest");
                }
            } catch (PackageManager.NameNotFoundException exception) {
                InternalLogging.warn(TAG, "Error loading SDK version from manifest");
                Log.v(TAG, exception.toString());
            }
        }
        context.setSdkVersion("android:" + sdkVersionString);
    }
}
