package com.microsoft.applicationinsights.channel.contracts;
/**
 * Enum DependencyKind.
 */
public class DependencyKind
{
    
    private DependencyKind() {
        // hide default constructor
    }
    
    public static final int SQL = 0;
    public static final int Http = 1;
    public static final int Other = 2;
}
