package com.microsoft.applicationinsights.channel;

public class SenderConfig {
    /**
     * The url to which payloads will be sent
     */
    public static String endpointUrl = "https://dc.services.visualstudio.com/v2/track";

    /**
     * The maximum size of a batch in bytes
     */
    public static int maxBatchCount = 100;

    /**
     * The maximum interval allowed between calls to batchInvoke
     */
    public static int maxBatchIntervalMs = 15 * 1000; // 15 seconds

    /**
     * The master off switch.  Do not send any data if set to TRUE
     */
    public static boolean DisableTelemetry = false;
}