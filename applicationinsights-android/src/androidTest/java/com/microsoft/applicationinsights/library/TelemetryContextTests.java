package com.microsoft.applicationinsights.library;

import android.test.AndroidTestCase;
import com.microsoft.applicationinsights.contracts.User;
import junit.framework.Assert;

public class TelemetryContextTests extends AndroidTestCase {

    private TelemetryContext sut;

    public void setUp() throws Exception {
        super.setUp();
        TelemetryContext.initialize(getContext(), "iKey", null);
        sut = TelemetryContext.getSharedInstance();
    }

    public void testNewUserContext(){

        // validate
        Assert.assertNull(sut.getAccountId());
        Assert.assertNull(sut.getUserAcqusitionDate());
        Assert.assertNotNull(sut.getUserId());
    }

    public void testLoadOldUserContextIfNotSet(){

        // prepare
        String accountId = "accountId";
        String acquisitionDateString = "acqusitionDate";
        String userId = "userId";

        // simulate existing user info
        sut.saveUserInfo(userId, acquisitionDateString, accountId);

        // verify
        Assert.assertEquals(accountId, sut.getAccountId());
        Assert.assertEquals(acquisitionDateString, sut.getUserAcqusitionDate());
        Assert.assertEquals(userId, sut.getUserId());
    }

    public void testNewInstanceGetsSetupWithSharedInstance(){

        // setup
        TelemetryContext newInstance = TelemetryContext.newInstance();

        // verify
        Assert.assertEquals(sut.getDeviceModel(), newInstance.getDeviceModel());
    }

    public void testNewInstanceWillNotChangeSharedInstance(){

        TelemetryContext newInstance = TelemetryContext.newInstance();
        newInstance.setDeviceModel("myDeviceModel");
        Assert.assertNotSame(sut.getDeviceModel(), newInstance.getDeviceModel());
    }

    public void testSharedInstanceWillChangeNewInstance(){

        TelemetryContext newInstance = TelemetryContext.newInstance();
        sut.setDeviceModel("myDeviceModel");
        Assert.assertEquals(sut.getDeviceModel(), newInstance.getDeviceModel());
    }

    protected void tearDown (){

        // reset saved user context
        sut.saveUserInfo(null, null, null);
    }
}
