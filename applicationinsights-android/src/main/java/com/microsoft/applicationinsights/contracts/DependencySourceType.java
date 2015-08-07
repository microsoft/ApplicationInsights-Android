/*
 * Generated from AppInsightsTypes.bond (https://github.com/Microsoft/bond)
*/
package com.microsoft.applicationinsights.contracts;

/**
 * Enum DependencySourceType.
 */
public enum DependencySourceType {
    UNDEFINED(0), AIC(1), APMC(2);

    private final int value;

    private DependencySourceType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
