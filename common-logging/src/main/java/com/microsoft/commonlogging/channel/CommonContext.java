package com.microsoft.commonlogging.channel;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;

import java.util.Date;
import java.util.UUID;

/**
 * This class collects context which is used by the common logging library
 */
public class CommonContext {

    protected static final String SHARED_PREFERENCES_KEY = "APP_INSIGHTS_CONTEXT";
    protected static final String USER_ID_KEY = "USER_ID";
    protected static final String USER_ACQ_KEY = "USER_ACQ";

    private Context androidAppContext;
    private SharedPreferences settings;

    private String appVersion;
    private String appId;
    private String appBuild;
    private String deviceOsVersion;
    private String deviceOs;
    private String deviceId;
    private String userId;
    private String userAcquisitionDate;

    public String getAppVersion() {
        return appVersion;
    }

    public String getAppId() {
        return appId;
    }

    public String getAppBuild() {
        return appBuild;
    }

    public String getDeviceOsVersion() {
        return deviceOsVersion;
    }

    public String getDeviceOs() {
        return deviceOs;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserAcquisitionDate() {
        return userAcquisitionDate;
    }

    /**
     * Constructs a new instance of the CommonContext
     * @param androidContext the android context to extract values from
     */
    public CommonContext(Context androidContext) {
        // get an instance of the shared preferences manager for persistent context fields
        this.androidAppContext = androidContext;
        this.settings = androidAppContext.getSharedPreferences(
                CommonContext.SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);

        // initialize default values for all context objects
        this.setAppContext();
        this.setDeviceContext();
        this.setUserContext();
    }

    /**
     * Sets the application telemetryContext tags
     */
    private void setAppContext() {
        this.appId = ""; // packageName
        this.appVersion = ""; // versionName
        this.appBuild = ""; // versionCode

        try {
            final PackageManager manager = this.androidAppContext.getPackageManager();
            final PackageInfo info = manager
                    .getPackageInfo(this.androidAppContext.getPackageName(), 0);

            if(info.packageName != null) {
                this.appId = info.packageName;
            }

            if(info.versionName != null) {
                this.appVersion = info.versionName;
            }

            this.appBuild = Integer.toString(info.versionCode);
        } catch (PackageManager.NameNotFoundException e) {
            InternalLogging._warn("TelemetryContext", "Could not collect application context");
        }
    }

    /**
     * Sets the device telemetryContext tags
     */
    private void setDeviceContext() {

        this.deviceOsVersion = Build.VERSION.RELEASE;
        this.deviceOs = "Android";

        // get device ID
        ContentResolver resolver = this.androidAppContext.getContentResolver();
        String deviceIdentifier = Settings.Secure.getString(resolver, Settings.Secure.ANDROID_ID);
        if(deviceIdentifier != null) {
            this.deviceId = Util.tryHashStringSha256(deviceIdentifier);
        }
    }

    /**
     * Sets the user context
     */
    private void setUserContext() {
        String userId = this.settings.getString(CommonContext.USER_ID_KEY, null);
        String userAcq = this.settings.getString(CommonContext.USER_ACQ_KEY, null);

        if(userId == null || userAcq == null) {
            userId = UUID.randomUUID().toString();
            userAcq = Util.dateToISO8601(new Date());

            SharedPreferences.Editor editor = this.settings.edit();
            editor.putString(CommonContext.USER_ID_KEY, userId);
            editor.putString(CommonContext.USER_ACQ_KEY, userAcq);
            editor.apply();
        }

        this.userId = userId;
        this.userAcquisitionDate = userAcq;
    }
}
