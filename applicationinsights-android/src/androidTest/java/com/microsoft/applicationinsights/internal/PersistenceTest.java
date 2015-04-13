package com.microsoft.applicationinsights.internal;

import android.test.AndroidTestCase;

import junit.framework.Assert;

public class PersistenceTest extends AndroidTestCase {

    public void setUp() throws Exception {
        super.setUp();
        Persistence.initialize(this.getContext());
    }

    public void testGetInstance() throws Exception {
        Persistence persistence = Persistence.getInstance();
        Assert.assertNotNull(persistence);
    }

    public void testSaveAndGetData() throws Exception {
        Persistence persistence = Persistence.getInstance();

        String data = "SAVE THIS DATA";
        persistence.persist(data, false);

        Assert.assertEquals("Data retrieved from file is equal to data saved", data, persistence.nextAvailableFile());
    }
}