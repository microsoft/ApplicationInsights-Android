package com.microsoft.applicationinsights.channel;

import java.util.UUID;

public class TelemetryContext extends AbstractTelemetryContext {

    /**
     * Constructs a new instance of the TelemetryContext.
     * @param config the context configuration
     */
    public TelemetryContext(IContextConfig config) {
        super(config);
    }

    /**
     * Sets the session telemetryContext tags
     */
    private void updateSessionContext() {
        // todo: handle sessions (update expire date every time this is read, renew after 24 hrs)
        this.getSession().setIsNew("true");
        this.getSession().setId(UUID.randomUUID().toString());
    }
}
