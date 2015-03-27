package com.microsoft.applicationinsights.channel.contracts;
/**
 * Enum SeverityLevel.
 */
public class SeverityLevel
{
    
    private SeverityLevel() {
        // hide default constructor
    }
    
    public static final int Verbose = 0;
    public static final int Information = 1;
    public static final int Warning = 2;
    public static final int Error = 3;
    public static final int Critical = 4;
}
