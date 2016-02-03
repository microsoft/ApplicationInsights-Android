package com.microsoft.applicationinsights.library;

import android.annotation.SuppressLint;
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
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * This class is holding all telemetryContext information.
 */
public class TelemetryContext {

    private static final String SHARED_PREFERENCES_KEY = "APP_INSIGHTS_CONTEXT";
    private static final String USER_ID_KEY = "USER_ID";
    private static final String USER_ACQ_KEY = "USER_ACQ";
    private static final String USER_ACCOUNT_ID_KEY = "USER_ACCOUNT_ID";
    private static final String USER_AUTH_USER_ID_KEY = "USER_AUTH_USER_ID";
    private static final String USER_ANON_ACQ_DATE_KEY = "USER_ANON_ACQ_DATE";
    private static final String USER_AUTH_ACQ_DATE_KEY = "USER_AUTH_ACQ_DATE";
    private static final String SESSION_IS_FIRST_KEY = "SESSION_IS_FIRST";
    private static final String TAG = "TelemetryContext";

    /**
     * Volatile boolean for double checked synchronize block
     */
    private static volatile boolean isTelemetryContextLoaded = false;

    /**
     * The shared TelemetryContext instance.
     */
    private static TelemetryContext instance;

    /**
     * Synchronization LOCK for setting static context
     */
    private static final Object LOCK = new Object();


    private final Object IKEY_LOCK = new Object();

    /**
     * Synchronization LOCK for setting static context
     */
    private final Object INSTANCE_LOCK = new Object();

    /**
     * The shared preferences INSTANCE for reading persistent context
     */
    private SharedPreferences settings;

    /**
     * Device telemetryContext.
     */
    private String instrumentationKey;

    /**
     * Device telemetryContext.
     */
    private final Device device;

    /**
     * Session telemetryContext.
     */
    private final Session session;

    /**
     * User telemetryContext.
     */
    private final User user;

    /**
     * Application telemetryContext.
     */
    private final Application application;

    /**
     * Internal telemetryContext.
     */
    private final Internal internal;

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
    private final Operation operation;

    private TelemetryContext() {
        this.operation = new Operation();
        this.device = new Device();
        this.session = new Session();
        this.user = new User();
        this.internal = new Internal();
        this.application = new Application();
    }

    /**
     * @return the INSTANCE of persistence or null if not yet initialized
     */
    public static TelemetryContext newInstance() {
        TelemetryContext context = null;
        if (TelemetryContext.instance == null) {
            InternalLogging.error(TAG, "newInstance was called before calling ApplicationInsights.setup()");
        } else {
            context = new TelemetryContext();
            context.resetContext();
        }
        return context;
    }

    public void resetContext() {

        // Reset device context
        setDeviceId(instance.getDeviceId());
        setDeviceModel(instance.getDeviceModel());
        setDeviceOemName(instance.getDeviceOemName());
        setDeviceType(instance.getDeviceType());
        setOsName(instance.getOsName());
        setOsVersion(instance.getOsVersion());
        setNetworkType(instance.getNetworkType());

        // Reset session context
        setSessionId(instance.getSessionId());

        // Reset user context
        setUserAcqusitionDate(instance.getUserAcqusitionDate());
        setUserId(instance.getUserId());
        setAccountId(instance.getAccountId());

        // Reset internal context
        setSdkVersion(instance.getSdkVersion());

        // Reset applicationContext
        setAppVersion(instance.getAppVersion());

        // Reset other
        setInstrumentationKey(instance.getInstrumentationKey());
    }

    /**
     * Constructs a new INSTANCE of the Telemetry telemetryContext tag keys
     *
     * @param context            the context for this telemetryContext
     * @param instrumentationKey the instrumentationkey for this application
     * @param user               a custom user object that will be assiciated with the telemetry data
     */
    protected TelemetryContext(Context context, String instrumentationKey, User user) {
        this();
        this.settings = context.getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);

        configDeviceContext(context);
        configSessionContext();
        configUserContext(user);
        configInternalContext(context);
        configAppContext(context);

        this.lastSessionId = null;
        setInstrumentationKey(instrumentationKey);
    }

    /**
     * Initialize the INSTANCE of the telemetryContext
     *
     * @param context            the context for this telemetryContext
     * @param instrumentationKey the instrumentationkey for this application
     * @param user               a custom user object that will be assiciated with the telemetry data
     */
    protected static void initialize(Context context, String instrumentationKey, User user) {
        if (!TelemetryContext.isTelemetryContextLoaded) {
            synchronized (TelemetryContext.LOCK) {
                if (!TelemetryContext.isTelemetryContextLoaded) {
                    TelemetryContext.isTelemetryContextLoaded = true;
                    TelemetryContext.instance = new TelemetryContext(context, instrumentationKey, user);
                }
            }
        }
    }

    /**
     * @return the INSTANCE of persistence or null if not yet initialized
     */
    protected static TelemetryContext getSharedInstance() {
        if (TelemetryContext.instance == null) {
            InternalLogging.error(TAG, "getSharedInstance was called before initialization");
        }
        return TelemetryContext.instance;
    }

    /**
     * Renews the session context
     * The session ID is on demand. Additionally, the isFirst flag is set if no data was
     * found in settings and the isNew flag is set each time a new UUID is
     * generated.
     */
    protected void renewSessionId() {
        String newId = UUID.randomUUID().toString();
        this.renewSessionId(newId);
    }

    /**
     * Renews the session context with a custom session ID.
     *
     * @param sessionId a custom session ID
     */
    protected void renewSessionId(String sessionId) {
        setSessionId(sessionId);
        //normally, this should also be saved to SharedPrefs like isFirst.
        //The problem is that there are cases when committing the changes is too slow and we get
        //the wrong value. As isNew is only "true" when we start a new session, it is set in
        //TrackDataOperation directly before enqueueing the session event.
        setIsNewSession("false");

        SharedPreferences.Editor editor = this.settings.edit();
        if (!this.settings.getBoolean(SESSION_IS_FIRST_KEY, false)) {
            editor.putBoolean(SESSION_IS_FIRST_KEY, true);
            editor.apply();
            setIsFirstSession("true");
        } else {
            setIsFirstSession("false");
        }
    }

    /**
     * Sets the session context
     */
    protected void configSessionContext() {
        if (this.lastSessionId == null) {
            renewSessionId();
        } else {
            setSessionId(this.lastSessionId);
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
            version = String.format("%s (%S)", info.versionName, appBuild);
        } catch (PackageManager.NameNotFoundException e) {
            InternalLogging.warn(TAG, "Could not collect application context");
        } finally {
            setAppVersion(version);
        }
    }

    /**
     * Sets the user Id. This method has been made protected to make sure it's not accessed from outside the SDK
     *
     * @param userId custom user id
     */
    protected void configUserContext(String userId) {
        if (userId == null) {
            // No custom user Id is given, so get this info from settings
            userId = this.settings.getString(TelemetryContext.USER_ID_KEY, null);
            if (userId == null) {
                // No settings available, generate new user info
                userId = UUID.randomUUID().toString();

            }
        }

        setUserId(userId);
        saveUserInfo();
    }

    /**
     * set the user for the user context associated with telemetry data.
     *
     * @param user The user object
     *             In case the user object that is passed is null, a new user object will be generated.
     *             If the user is missing a property, they will be generated, too.
     */
    protected void configUserContext(User user) {
        if (user == null) {
            loadUserInfo();
        }

        if (user != null && user.getId() == null) {
            setUserId(UUID.randomUUID().toString());
        }
        saveUserInfo();
    }

    /**
     * Write user information to shared preferences.
     */
    protected void saveUserInfo() {
        SharedPreferences.Editor editor = this.settings.edit();
        editor.putString(TelemetryContext.USER_ID_KEY, getUserId());
        editor.putString(TelemetryContext.USER_ACQ_KEY, getUserAcqusitionDate());
        editor.putString(TelemetryContext.USER_ACCOUNT_ID_KEY, getAccountId());
        editor.putString(TelemetryContext.USER_AUTH_USER_ID_KEY, getAuthenticatedUserId());
        editor.putString(TelemetryContext.USER_AUTH_ACQ_DATE_KEY, getAuthenticatedUserAcquisitionDate());
        editor.putString(TelemetryContext.USER_ANON_ACQ_DATE_KEY, getAnonymousUserAcquisitionDate());
        editor.apply();
    }

    /**
     * Load user information to shared preferences.
     *
     */
    protected void loadUserInfo() {
        User user = new User();

        String userId = this.settings.getString(TelemetryContext.USER_ID_KEY, null);
        setUserId(userId);

        String acquisitionDateString = this.settings.getString(TelemetryContext.USER_ACQ_KEY, null);
        setUserAcqusitionDate(acquisitionDateString);

        String accountId = this.settings.getString(TelemetryContext.USER_ACCOUNT_ID_KEY, null);
        setAccountId(accountId);

        String authorizedUserId = this.settings.getString(TelemetryContext.USER_AUTH_USER_ID_KEY, null);
        user.setAuthUserId(authorizedUserId);

        String authUserAcqDate = this.settings.getString(TelemetryContext.USER_AUTH_ACQ_DATE_KEY, null);
        user.setAuthUserAcquisitionDate(authUserAcqDate);

        String anonUserAcqDate = this.settings.getString(TelemetryContext.USER_ANON_ACQ_DATE_KEY, null);
        user.setAnonUserAcquisitionDate(anonUserAcqDate);
    }

    /**
     * Sets the device telemetryContext tags
     *
     * @param appContext the android Context
     */
    protected void configDeviceContext(Context appContext) {
        setOsVersion(Build.VERSION.RELEASE);
        setOsName("Android"); //used by the AI extension in Azure Portal to build stack traces
        setDeviceModel(Build.MODEL);
        setDeviceOemName(Build.MANUFACTURER);
        setOsLocale(Locale.getDefault().toString());
        updateScreenResolution(appContext);
        // get device ID
        ContentResolver resolver = appContext.getContentResolver();
        String deviceIdentifier = Settings.Secure.getString(resolver, Settings.Secure.ANDROID_ID);
        if (deviceIdentifier != null) {
            setDeviceId(Util.tryHashStringSha256(deviceIdentifier));
        }

        // check device type
        final TelephonyManager telephonyManager = (TelephonyManager)
              appContext.getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager.getPhoneType() != TelephonyManager.PHONE_TYPE_NONE) {
            setDeviceType("Phone");
        } else {
            setDeviceType("Tablet");
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
            setNetworkType(networkString);
        }

        // detect emulator
        if (Util.isEmulator()) {
            setDeviceModel("[Emulator]" + device.getModel());
        }
    }

    @SuppressLint({"NewApi", "Deprecation"})
    protected void updateScreenResolution(Context context) {
        String resolutionString;
        int width;
        int height;

        if (context != null) {
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
                    InternalLogging.warn(TAG, "Couldn't determine screen resolution: " + ex.toString());
                }

            } else {
                //Use old, and now deprecated API to get width and height of the display
                Display d = wm.getDefaultDisplay();
                width = d.getWidth();
                height = d.getHeight();
            }

            resolutionString = String.valueOf(height) + "x" + String.valueOf(width);

            setScreenResolution(resolutionString);
        }
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
                    sdkVersionString = bundle.getString("com.microsoft.applicationinsights.library.sdkVersion");
                } else {
                    InternalLogging.warn(TAG, "Could not load sdk version from gradle.properties or manifest");
                }
            } catch (PackageManager.NameNotFoundException exception) {
                InternalLogging.warn(TAG, "Error loading SDK version from manifest");
                Log.v(TAG, exception.toString());
            }
        }
        setSdkVersion("android:" + sdkVersionString);
    }

    /**
     * The package name
     *
     * @see TelemetryContext#appIdForEnvelope
     */
    protected String getPackageName() {
        return appIdForEnvelope;
    }

    protected Map<String, String> getContextTags() {
        Map<String, String> contextTags = new LinkedHashMap<String, String>();

        synchronized (this.application){
            this.application.addToHashMap(contextTags);
        }
        synchronized (this.internal){
            this.internal.addToHashMap(contextTags);
        }
        synchronized (this.operation){
            this.operation.addToHashMap(contextTags);
        }
        synchronized (this.device){
            this.device.addToHashMap(contextTags);
        }
        synchronized (this.session){
            this.session.addToHashMap(contextTags);
        }
        synchronized (this.user) {
            this.user.addToHashMap(contextTags);
        }

        return contextTags;
    }

    public String getInstrumentationKey() {
        synchronized(IKEY_LOCK){
            return this.instrumentationKey;
        }
    }

    public synchronized void setInstrumentationKey(String instrumentationKey) {
        synchronized(IKEY_LOCK){
            this.instrumentationKey = instrumentationKey;
        }
    }

    public String getScreenResolution() {
        synchronized (this.application) {
            return this.device.getScreenResolution();
        }
    }

    public void setScreenResolution(String screenResolution) {
        synchronized (this.application) {
            this.device.setScreenResolution(screenResolution);
        }
    }

    public String getAppVersion() {
        synchronized (this.application) {
            return this.application.getVer();
        }
    }

    public void setAppVersion(String appVersion) {
        synchronized (this.application){
            this.application.setVer(appVersion);
        }
    }

    public String getUserId() {
        synchronized (this.user) {
            return this.user.getId();
        }
    }

    public void setUserId(String userId) {
        synchronized (this.user){
            this.user.setId(userId);
            if (this == instance) {
                saveUserInfo();
            }
        }
    }

    public String getUserAcqusitionDate() {
        synchronized (this.user){
            return this.user.getAccountAcquisitionDate();
        }
    }

    public void setUserAcqusitionDate(String userAcqusitionDate) {
        synchronized (this.user){
            this.user.setAccountAcquisitionDate(userAcqusitionDate);
            if (this == instance) {
                saveUserInfo();
            }
        }
    }

    public String getAccountId() {
        synchronized (this.user){
            return this.user.getAccountId();
        }
    }

    public void setAccountId(String accountId) {
        synchronized (this.user){
            this.user.setAccountId(accountId);
            if (this == instance) {
                saveUserInfo();
            }
        }
    }

    public String getAuthenticatedUserId() {
        synchronized (this.user){
            return this.user.getAuthUserId();
        }
    }

    public void setAuthenticatedUserId(String authenticatedUserId) {
        synchronized (this.user){
            this.user.setAuthUserId(authenticatedUserId);
            if (this == instance) {
                saveUserInfo();
            }
        }
    }

    public String getAuthenticatedUserAcquisitionDate() {
        synchronized (this.user){
            return this.user.getAuthUserAcquisitionDate();
        }
    }

    public void setAuthenticatedUserAcquisitionDate(String authenticatedUserAcquisitionDate) {
        synchronized (this.user){
            this.user.setAuthUserAcquisitionDate(authenticatedUserAcquisitionDate);
            if (this == instance) {
                saveUserInfo();
            }
        }
    }

    public String getAnonymousUserAcquisitionDate() {
        synchronized (this.user){
            return this.user.getAnonUserAcquisitionDate();
        }
    }

    public void setAnonymousUserAcquisitionDate(String anonymousUserAcquisitionDate) {
        synchronized (this.user){
            this.user.setAnonUserAcquisitionDate(anonymousUserAcquisitionDate);
            if (this == instance) {
                saveUserInfo();
            }
        }
    }

    public String getSdkVersion() {
        synchronized (this.internal){
            return this.internal.getSdkVersion();
        }
    }

    public void setSdkVersion(String sdkVersion) {
        synchronized (this.internal){
            this.internal.setSdkVersion(sdkVersion);
        }
    }

    public String getSessionId() {
        synchronized (this.session){
            return this.session.getId();
        }
    }

    public void setSessionId(String sessionId) {
        synchronized (this.session){
            this.session.setId(sessionId);
        }
    }

    public String getIsFirstSession() {
        synchronized (this.session){
            return this.session.getIsFirst();
        }
    }

    public void setIsFirstSession(String isFirst) {
        synchronized (this.session){
            this.session.setIsFirst(isFirst);
        }
    }

    public String getIsNewSession() {
        synchronized (this.session){
            return this.session.getIsNew();
        }
    }

    public void setIsNewSession(String isFirst) {
        synchronized (this.session){
            this.session.setIsNew(isFirst);
        }
    }

    public String getOsVersion() {
        synchronized (this.device) {
            return this.device.getOsVersion();
        }
    }

    public void setOsVersion(String osVersion) {
        synchronized (this.device) {
            this.device.setOsVersion(osVersion);
        }
    }

    public String getOsName() {
        synchronized (this.device) {
            return this.device.getOs();
        }
    }

    public void setOsName(String osName) {
        synchronized (this.device) {
            this.device.setOs(osName);
        }
    }

    public String getDeviceModel() {
        synchronized (this.device){
            return this.device.getModel();
        }
    }

    public void setDeviceModel(String deviceModel) {
        synchronized (this.device){
            this.device.setModel(deviceModel);
        }
    }

    public String getDeviceOemName() {
        synchronized (this.device) {
            return this.device.getOemName();
        }
    }

    public void setDeviceOemName(String deviceOemName) {
        synchronized (this.device) {
            this.device.setOemName(deviceOemName);
        }
    }

    public String getOsLocale() {
        synchronized (this.device){
            return this.device.getLocale();
        }
    }

    public void setOsLocale(String osLocale) {
        synchronized (this.device){
            this.device.setLocale(osLocale);
        }
    }

    public String getDeviceId() {
        return this.device.getId();
    }

    public void setDeviceId(String deviceId) {
        synchronized (this.device){
            this.device.setId(deviceId);
        }
    }

    public String getDeviceType() {
        return this.device.getType();
    }

    public void setDeviceType(String deviceType) {
        synchronized (this.device){
            this.device.setType(deviceType);
        }
    }

    public String getNetworkType() {
        return this.device.getNetwork();
    }

    public void setNetworkType(String networkType) {
        synchronized (this.device) {
            this.device.setNetwork(networkType);
        }
    }
}
