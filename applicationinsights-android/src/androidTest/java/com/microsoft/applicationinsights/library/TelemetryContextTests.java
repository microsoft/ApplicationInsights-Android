package com.microsoft.applicationinsights.library;

import android.test.AndroidTestCase;
import com.microsoft.applicationinsights.contracts.User;
import junit.framework.Assert;

public class TelemetryContextTests extends AndroidTestCase {

    private TelemetryContext sut;

    public void setUp() throws Exception {
        super.setUp();
        sut = new TelemetryContext(this.getContext(), "ikey", null);
    }

    public void testNewUserContext(){

        // prepare
        String userId = "userId";

        // test
        User user = sut.getUser();

        // validate
        Assert.assertNull(user.getAccountId());
        Assert.assertNull(user.getAccountAcquisitionDate());
        Assert.assertNotSame(userId, user.getId());
    }

    public void testLoadOldUserContextIfNotSet(){

        // prepare
        String accountId = "accountId";
        String acquisitionDateString = "acqusitionDate";
        String userId = "userId";

        // simulate existing user info
        sut.saveUserInfo(userId, acquisitionDateString, accountId);

        // test
        sut = new TelemetryContext(this.getContext(), "ikey", null);

        // verify
        User loadedUser = sut.getUser();
        Assert.assertEquals(accountId, loadedUser.getAccountId());
        Assert.assertEquals(acquisitionDateString, loadedUser.getAccountAcquisitionDate());
        Assert.assertEquals(userId, loadedUser.getId());
    }

    protected void tearDown (){

        // reset saved user context
        sut.saveUserInfo(null, null, null);
    }
}
