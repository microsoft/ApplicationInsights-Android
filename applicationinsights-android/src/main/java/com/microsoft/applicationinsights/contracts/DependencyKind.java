/*
 * Generated from DependencyKind.bond (https://github.com/Microsoft/bond)
*/
package com.microsoft.applicationinsights.contracts;

/**
 * Enum DependencyKind.
 */
public enum DependencyKind {
    SQL(0), HTTP(1), OTHER(2);

    private final int value;

    private DependencyKind(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
