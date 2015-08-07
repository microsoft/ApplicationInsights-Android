/*
 * Generated from AppInsightsTypes.bond (https://github.com/Microsoft/bond)
*/
package com.microsoft.applicationinsights.contracts;
/**
 * Enum TestResult.
 */
public enum TestResult
{
    PASS(0), FAIL(1);
    private final int value;
    private TestResult(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
