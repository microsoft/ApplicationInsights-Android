package com.microsoft.applicationinsights.channel.contracts;
/**
 * Enum DependencySourceType.
 */
public class DependencySourceType
{
    
    private DependencySourceType() {
        // hide default constructor
    }
    
    public static final int Undefined = 0;
    public static final int Aic = 1;
    public static final int Apmc = 2;
}
