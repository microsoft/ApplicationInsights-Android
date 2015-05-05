package com.microsoft.applicationinsights.library;

import java.util.Map;

public class PublicEnvelopeFactory extends EnvelopeFactory{

    protected PublicEnvelopeFactory(TelemetryContext telemetryContext, Map<String, String> commonProperties) {
        super(telemetryContext, commonProperties);
    }
}
