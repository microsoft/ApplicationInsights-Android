package com.microsoft.applicationinsights.channel.contracts;
/**
 * Enum DataPointType.
 */
public class DataPointType
{
    
    private DataPointType() {
        // hide default constructor
    }
    
    public static final int Measurement = 0;
    public static final int Aggregation = 1;
}
