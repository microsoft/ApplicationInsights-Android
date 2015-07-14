package com.microsoft.applicationinsights.library;

import junit.framework.Assert;
import junit.framework.TestCase;

import java.util.Date;

public class UtilTest extends TestCase {

    public void setUp() throws Exception {
        super.setUp();

    }

    public void testDateToISO8601() throws Exception {
        Date now = new Date();

        String pattern = "[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}.[0-9]{3}Z";
        Assert.assertTrue("now format", Util.dateToISO8601(now).matches(pattern));
    }

    public void testMsToTimeSpan() throws Exception {
        this.testMsToTimeSpanHelper(0, "00:00:00.000", "zero");
        this.testMsToTimeSpanHelper(1, "00:00:00.001", "milliseconds digit 1");
        this.testMsToTimeSpanHelper(10, "00:00:00.010", "milliseconds digit 2");
        this.testMsToTimeSpanHelper(100, "00:00:00.100", "milliseconds digit 3");
        this.testMsToTimeSpanHelper(1000, "00:00:01.000", "seconds digit 1");
        this.testMsToTimeSpanHelper(10 * 1000, "00:00:10.000", "seconds digit 2");
        this.testMsToTimeSpanHelper(60 * 1000, "00:01:00.000", "minutes digit 1");
        this.testMsToTimeSpanHelper(10 * 60 * 1000, "00:10:00.000", "minutes digit 2");
        this.testMsToTimeSpanHelper(60 * 60 * 1000, "01:00:00.000", "hours digit 1");
        this.testMsToTimeSpanHelper(10 * 60 * 60 * 1000, "10:00:00.000", "hours digit 2");
        this.testMsToTimeSpanHelper(24 * 60 * 60 * 1000, "1.00:00:00.000", "hours overflow");
        this.testMsToTimeSpanHelper(24 * 61 * 61 * 1010, "1.01:03:17.040", "hours overflow+");
        this.testMsToTimeSpanHelper((24 * 11 + 11)  * 3600000 + 11 * 60000 + 11111,
                "11.11:11:11.111", "all digits");

        this.testMsToTimeSpanHelper(-1, "00:00:00.000", "invalid input");
        this.testMsToTimeSpanHelper(0xffffffff, "00:00:00.000", "invalid input");
    }

    private void testMsToTimeSpanHelper(long durationMs, String expected, String message) {
        String actual = Util.msToTimeSpan(durationMs);
        Assert.assertEquals(message, expected, actual);
    }
}