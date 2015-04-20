package com.microsoft.applicationinsights.library;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.microsoft.applicationinsights.contracts.Application;
import com.microsoft.applicationinsights.contracts.Device;
import com.microsoft.applicationinsights.contracts.Internal;
import com.microsoft.applicationinsights.contracts.Operation;
import com.microsoft.applicationinsights.contracts.Session;
import com.microsoft.applicationinsights.contracts.User;
import com.microsoft.applicationinsights.logging.InternalLogging;

import java.lang.reflect.Method;
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
     * The shared preferences INSTANCE for reading persistent context
     */
    private SharedPreferences settings;

    /**
     * Content for tags field of an envelope
     */
    private Map<String, String> cachedTags;

    /**
     * Device telemetryContext.
     */
    private String instrumentationKey;

    /**
     * Device telemetryContext.
     */
    private Device device;

    /**
     * Session telemetryContext.
     */
    private Session session;

    /**
     * User telemetryContext.
     */
    private User user;

    /**
     * Application telemetryContext.
     */
    private Application application;

    /**
     * Internal telemetryContext.
     */
    private Internal internal;

    /**
     * The last session ID
     */
    private String lastSessionId;

    /**
     * The App ID for the envelope (defined as PackageInfo.packageName by CLL team)
     */
    private String appIdForEnvelope;

    /**
     * Operation telemetryContext.
     */
    private Operation operation;

    /**
     * Constructs a new INSTANCE of the Telemetry telemetryContext tag keys
     *
     * @param appContext the context for this telemetryContext
     */
    public TelemetryContext(Context appContext, String instrumentationKey, String userId) {

        // TODO: Why does everything in here have to be static? Constructor is used to create new instance rather than setting static fields
        this.operation = new Operation();

        // get an INSTANCE of the shared preferences manager for persistent context fields
        this.settings = appContext.getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);
        this.operation = new Operation();
        this.device = new Device();
        configDeviceContext(appContext);
        this.session = new Session();
        configSessionContext();
        this.user = new User();
        configUserContext(userId);
        this.internal = new Internal();
        configInternalContext(appContext);
        this.application = new Application();
        configAppContext(appContext);

        this.lastSessionId = null;
        this.instrumentationKey = instrumentationKey;
        this.cachedTags = getCachedTags();
    }

    /**
     * Get user the instrumentationKey.
     *
     * @return the instrumentation key
     */
    protected String getInstrumentationKey() {
        return instrumentationKey;
    }

    /**
     * Get user telemetryContext.
     *
     * @return the user object
     */
    protected User getUser() {
        return user;
    }

    /**
     * Get device telemetryContext.
     *
     * @return the device object
     */
    protected Device getDevice() {
        return device;
    }

    /**
     * Operation telemetryContext.
     *
     * @return the operation
     */
    protected Operation getOperation() {
        return operation;
    }

    /**
     * Session telemetryContext.
     *
     * @return the session
     */
    protected Session getSession() {
        return session;
    }

    /**
     * Application telemetryContext.
     *
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
        if (this.cachedTags == null) {
            // create a new hash map and add all context to it
            this.cachedTags = new LinkedHashMap<String, String>();
            this.application.addToHashMap(cachedTags);
            this.internal.addToHashMap(cachedTags);
            this.operation.addToHashMap(cachedTags);
        }
        return this.cachedTags;
    }

    protected Map<String, String> getContextTags() {
        Map<String, String> contextTags = new LinkedHashMap<String, String>();
        contextTags.putAll(getCachedTags());
        this.device.addToHashMap(contextTags);
        this.session.addToHashMap(contextTags);
        this.user.addToHashMap(contextTags);

        return contextTags;
    }

    // TODO: Synchronize session renewal

    /**
     * Renews the session context
     * <p/>
     * The session ID is on demand. Additionally, the isFirst flag is set if no data was
     * found in settings and the isNew flag is set each time a new UUID is
     * generated.
     */
    protected void renewSessionId() {
        String newId = UUID.randomUUID().toString();
        this.session.setId(newId);
    }

    /**
     * Renews the session context with a custom session ID.
     *
     * @param sessionId a custom session ID
     */
    public void renewSessionId(String sessionId) {
        this.session.setId(sessionId);
    }

    /**
     * Sets the session context
     */
    protected void configSessionContext() {
        if (this.lastSessionId == null) {
            renewSessionId();
        } else {
            this.session.setId(this.lastSessionId);
        }
    }

    /**
     * Sets the application telemetryContext tags
     *
     * @param appContext the android context
     */
    protected void configAppContext(Context appContext) {
        String version = "unknown";
        this.appIdForEnvelope = "";

        try {
            final PackageManager manager = appContext.getPackageManager();
            final PackageInfo info = manager
                  .getPackageInfo(appContext.getPackageName(), 0);

            if (info.packageName != null) {
                this.appIdForEnvelope = info.packageName;
            }

            String appBuild = Integer.toString(info.versionCode);
            version = String.format("%s (%S)", this.appIdForEnvelope, appBuild);
        } catch (PackageManager.NameNotFoundException e) {
            InternalLogging.warn(TAG, "Could not collect application context");
        } finally {
            this.application.setVer(version);
        }
    }

    /**
     * Sets the user context
     */
    public void configUserContext(String userId) {
        String userAcq;

        if (userId == null) {
            // No custom user Id is given, so get this info from settings
            userId = this.settings.getString(TelemetryContext.USER_ID_KEY, null);
            userAcq = this.settings.getString(TelemetryContext.USER_ACQ_KEY, null);

            if (userId == null || userAcq == null) {
                // No settings available, generate new user info
                userId = UUID.randomUUID().toString();
                userAcq = Util.dateToISO8601(new Date());
                saveUserInfo(userId, userAcq);
            }
        } else {
            // UserId provided by the User, generate date
            userAcq = Util.dateToISO8601(new Date());
            saveUserInfo(userId, userAcq);
        }

        this.user.setId(userId);
        this.user.setAccountAcquisitionDate(userAcq);
    }

    /**
     * Write user information to shared preferences.
     *
     * @param userId        the user ID
     * @param acqDateString the date of the acquisition as string
     */
    private void saveUserInfo(String userId, String acqDateString) {
        SharedPreferences.Editor editor = this.settings.edit();
        editor.putString(TelemetryContext.USER_ID_KEY, userId);
        editor.putString(TelemetryContext.USER_ACQ_KEY, acqDateString);
        editor.apply();
    }

    /**
     * Sets the device telemetryContext tags
     *
     * @param appContext the android Context
     */
    protected void configDeviceContext(Context appContext) {
        this.device.setOsVersion(Build.VERSION.RELEASE);
        this.device.setOs("Android");
        this.device.setModel(Build.MODEL);
        this.device.setOemName(Build.MANUFACTURER);
        this.device.setLocale(Locale.getDefault().toString());
        updateScreenResolution(appContext);
        // get device ID
        ContentResolver resolver = appContext.getContentResolver();
        String deviceIdentifier = Settings.Secure.getString(resolver, Settings.Secure.ANDROID_ID);
        if (deviceIdentifier != null) {
            this.device.setId(Util.tryHashStringSha256(deviceIdentifier));
        }

        // check device type
        final TelephonyManager telephonyManager = (TelephonyManager)
              appContext.getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager.getPhoneType() != TelephonyManager.PHONE_TYPE_NONE) {
            this.device.setType("Phone");
        } else {
            this.device.setType("Tablet");
        }

        // check network type
        final ConnectivityManager connectivityManager = (ConnectivityManager)
              appContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        if (activeNetwork != null) {
            int networkType = activeNetwork.getType();
            String networkString;
            switch (networkType) {
                case ConnectivityManager.TYPE_WIFI:
                    networkString = "WiFi";
                    break;
                case ConnectivityManager.TYPE_MOBILE:
                    networkString = "Mobile";
                    break;
                default:
                    networkString = "Unknown";
                    InternalLogging.warn(TAG, "Unknown network type:" + networkType);
                    break;
            }
            this.device.setNetwork(networkString);
        }

        // detect emulator
        if (Util.isEmulator()) {
            this.device.setModel("[Emulator]" + device.getModel());
        }
    }

    // TODO: Synchronize resolution update
    protected void updateScreenResolution(Context context) {
        String resolutionString;
        int width;
        int height;

        WindowManager wm = (WindowManager) context.getSystemService(
              Context.WINDOW_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Point size = new Point();
            wm.getDefaultDisplay().getRealSize(size);
            width = size.x;
            height = size.y;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            try {
                //We have to use undocumented API here. Android 4.0 introduced soft buttons for
                //back, home and menu, but there's no API present to get the real display size
                //all available methods only return the size of the contentview.
                Method mGetRawW = Display.class.getMethod("getRawWidth");
                Method mGetRawH = Display.class.getMethod("getRawHeight");
                Display display = wm.getDefaultDisplay();
                width = (Integer) mGetRawW.invoke(display);
                height = (Integer) mGetRawH.invoke(display);
            } catch (Exception ex) {
                Point size = new Point();
                wm.getDefaultDisplay().getSize(size);
                width = size.x;
                height = size.y;
                InternalLogging.error(TAG, ex.toString());
            }

        } else {
            //Use old, and now deprecated API to get width and height of the display
            Display d = wm.getDefaultDisplay();
            width = d.getWidth();
            height = d.getHeight();
        }

        resolutionString = String.valueOf(height) + "x" + String.valueOf(width);

        this.device.setScreenResolution(resolutionString);
    }

    /**
     * Sets the internal package context
     */
    protected void configInternalContext(Context appContext) {
        String sdkVersionString = "";
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
        this.internal.setSdkVersion("android:" + sdkVersionString);
    }
}
