package com.microsoft.applicationinsights.channel;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.test.AndroidTestCase;

import junit.framework.Assert;

public class CommonContextTest extends AndroidTestCase {

    private Context context;

    @SuppressLint("CommitPrefEdits")
    public void setUp() throws Exception {
        super.setUp();
        this.context = this.getContext();

        SharedPreferences.Editor editor = this.context.getSharedPreferences(
                CommonContext.SHARED_PREFERENCES_KEY, 0).edit();
        editor.putString(CommonContext.USER_ACQ_KEY, null);
        editor.putString(CommonContext.USER_ID_KEY, null);
        editor.commit();
    }

    public void tearDown() throws Exception {

    }

    public void testInitialization() {
        CommonContext commonContext = new CommonContext(this.context);

        Assert.assertNotNull("appId", commonContext.getAppId());
        Assert.assertNotNull("appVer", commonContext.getAppVersion());
        Assert.assertNotNull("appBuild", commonContext.getAppBuild());
        Assert.assertNotNull("deviceId", commonContext.getDeviceId());
        Assert.assertNotNull("deviceOs", commonContext.getDeviceOs());
        Assert.assertNotNull("deviceOsVersion", commonContext.getDeviceOsVersion());
        Assert.assertNotNull("deviceUserId", commonContext.getUserId());
        Assert.assertNotNull("deviceUserAcquisition", commonContext.getUserAcquisitionDate());
    }

    @SuppressLint("CommitPrefEdits")
    public void testPersistedData() {
        SharedPreferences.Editor editor = this.context.getSharedPreferences(
                CommonContext.SHARED_PREFERENCES_KEY, 0).edit();
        editor.putString(CommonContext.USER_ACQ_KEY, "u_acq");
        editor.putString(CommonContext.USER_ID_KEY, "u_id");
        editor.commit();

        CommonContext commonContext = new CommonContext(this.context);
        Assert.assertNotNull("deviceUserId", commonContext.getUserId());
        Assert.assertNotNull("deviceUserAcquisition", commonContext.getUserAcquisitionDate());
    }
}