package com.microsoft.applicationinsights.channel;

import android.test.AndroidTestCase;

import junit.framework.Assert;
import junit.framework.TestCase;

public class PersistenceTest extends AndroidTestCase {



    public void setUp() throws Exception {
        super.setUp();

    }

    public void testGetInstance() throws Exception {
        Persistence persist1 = Persistence.getInstance();
        String expectedFilePath = "THIS_PATH";
        persist1.setFilePath(expectedFilePath);

        Persistence persist2 = Persistence.getInstance();
        Assert.assertEquals("validating file paths are equal", expectedFilePath, persist2.getfilePath());
    }

    public void testSaveAndGetData() throws Exception {
        Persistence persist = Persistence.getInstance();
        persist.setPersistenceContext(this.getContext());

        String data = "SAVE THIS DATA";
        persist.saveData(data);

        Persistence persist2 = Persistence.getInstance();
        Assert.assertEquals("Data retrieved from file is equal to data saved", data, persist2.getData());
    }


}