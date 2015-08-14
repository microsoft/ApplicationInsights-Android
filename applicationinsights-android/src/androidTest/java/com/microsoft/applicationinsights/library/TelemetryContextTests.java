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
        resetUserContext();

    }

    public void testNewUserContext(){
        // validate
        Assert.assertNull(sut.getAccountId());
        Assert.assertNull(sut.getUserAcqusitionDate());
        Assert.assertNull(sut.getUserId());
        Assert.assertNull(sut.getAuthenticatedUserId());
        Assert.assertNull(sut.getAuthenticatedUserAcquisitionDate());
        Assert.assertNull(sut.getAnonymousUserAcquisitionDate());
    }

    public void testSavingAndLoadingUserContextWorks(){

        // prepare
        String accountId = "accountId";
        String acquisitionDateString = "acqusitionDate";
        String userId = "userId";
        String authenticatedUserId = "authenticatedUserId";
        String authenticatedUserAcqDate = "authenticatedUserAcqDate";
        String anonUserAcquDate = "anonUserAcquDate";

        //save user context
        sut.setAccountId(accountId);
        sut.setUserAcqusitionDate(acquisitionDateString);
        sut.setUserId(userId);
        sut.setAuthenticatedUserId(authenticatedUserId);
        sut.setAuthenticatedUserAcquisitionDate(authenticatedUserAcqDate);
        sut.setAnonymousUserAcquisitionDate(anonUserAcquDate);

        // simulate existing user info
        User user = null;
        sut.configUserContext(user);

        // verify
        Assert.assertEquals(accountId, sut.getAccountId());
        Assert.assertEquals(acquisitionDateString, sut.getUserAcqusitionDate());
        Assert.assertEquals(userId, sut.getUserId());
        Assert.assertEquals(authenticatedUserId, sut.getAuthenticatedUserId());
        Assert.assertEquals(authenticatedUserAcqDate, sut.getAuthenticatedUserAcquisitionDate());
        Assert.assertEquals(anonUserAcquDate, sut.getAnonymousUserAcquisitionDate());
    }

    public void testNewInstanceGetsSetupWithSharedInstance(){

        // setup
        TelemetryContext newInstance = TelemetryContext.newInstance();

        // verify
        Assert.assertEquals(sut.getDeviceModel(), newInstance.getDeviceModel());
        Assert.assertEquals(sut.getAccountId(), newInstance.getAccountId());
        Assert.assertEquals(sut.getUserAcqusitionDate(), newInstance.getUserAcqusitionDate());
        Assert.assertEquals(sut.getUserId(), newInstance.getUserId());
        Assert.assertEquals(sut.getAuthenticatedUserId(), newInstance.getAuthenticatedUserId());
        Assert.assertEquals(sut.getAuthenticatedUserAcquisitionDate(), newInstance.getAuthenticatedUserAcquisitionDate());
        Assert.assertEquals(sut.getAnonymousUserAcquisitionDate(), newInstance.getAnonymousUserAcquisitionDate());
    }

    public void testNewInstanceWillNotChangeSharedInstance(){
        TelemetryContext newInstance = TelemetryContext.newInstance();
        newInstance.setDeviceModel("myDeviceModel");
        Assert.assertNotSame(sut.getDeviceModel(), newInstance.getDeviceModel());
    }

    public void testSharedInstanceWillNotChangeNewInstance(){
        TelemetryContext newInstance = TelemetryContext.newInstance();
        sut.setDeviceModel("myDeviceModel");
        Assert.assertNotSame(sut.getDeviceModel(), newInstance.getDeviceModel());
    }

    protected void tearDown (){
        // reset saved user context
        resetUserContext();
    }

    // helper

    protected void resetUserContext(){
        sut.setUserId(null);
        sut.setAccountId(null);
        sut.setUserAcqusitionDate(null);
        sut.setAuthenticatedUserId(null);
        sut.setAuthenticatedUserAcquisitionDate(null);
        sut.setAnonymousUserAcquisitionDate(null);
        User user = null;
        sut.configUserContext(user);
    }
}
