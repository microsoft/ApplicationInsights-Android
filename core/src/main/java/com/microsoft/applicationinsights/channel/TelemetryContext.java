package com.microsoft.applicationinsights.channel;

import com.microsoft.applicationinsights.TelemetryClientConfig;
import com.microsoft.applicationinsights.channel.contracts.Application;
import com.microsoft.applicationinsights.channel.contracts.Device;
import com.microsoft.applicationinsights.channel.contracts.Internal;
import com.microsoft.applicationinsights.channel.contracts.Location;
import com.microsoft.applicationinsights.channel.contracts.Operation;
import com.microsoft.applicationinsights.channel.contracts.Session;
import com.microsoft.applicationinsights.channel.contracts.User;

import java.util.LinkedHashMap;
import java.util.UUID;

/**
 * This class is holding all telemetryContext information.
 */
public class TelemetryContext {

    /**
     * Application telemetryContext.
     */
    protected Application application;

    /**
     * Device telemetryContext.
     */
    protected Device device;

    /**
     * Location telemetryContext.
     */
    protected Location location;

    /**
     * Operation telemetryContext.
     */
    protected Operation operation;

    /**
     * Session telemetryContext.
     */
    protected Session session;

    /**
     * User telemetryContext.
     */
    protected User user;

    /**
     * Internal telemetryContext.
     */
    protected Internal internal;

    /**
     * Constructs a new instance of the Telemetry telemetryContext tag keys
     * @param config the configuration for this telemetryContext
     */
    public TelemetryContext(TelemetryClientConfig config) {
        super();

        this.application = new Application();
        this.device = new Device();
        this.location = new Location();
        this.operation = new Operation();
        this.session = new Session();
        this.user = new User();
        this.internal = new Internal();
    }

    public LinkedHashMap<String, String> getContextTags() {

        this.updateSessionContext();

        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
        this.application.addToHashMap(map);
        this.device.addToHashMap(map);
        this.location.addToHashMap(map);
        this.operation.addToHashMap(map);
        this.session.addToHashMap(map);
        this.user.addToHashMap(map);
        this.internal.addToHashMap(map);
        return map;
    }

    /**
     * Sets the session telemetryContext tags
     */
    private void updateSessionContext() {
        // todo: handle sessions (update expire date every time this is read, renew after 24 hrs)
        this.session.setIsNew("true");
        this.session.setId(UUID.randomUUID().toString());
    }
}
