package com.microsoft.applicationinsights.logging;

/**
 * This exception is only thrown if {@code TelemetryQueueConfig.developerMode} is true. If this is
 * thrown, the SDK is likely not being used as intended.
 */
public class UserActionableSDKException extends RuntimeException {
    public UserActionableSDKException(String message){
        super(message);
    }
}
