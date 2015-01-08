package com.microsoft.applicationinsights.channel;

import com.microsoft.applicationinsights.channel.contracts.Application;
import com.microsoft.applicationinsights.channel.contracts.Device;
import com.microsoft.applicationinsights.channel.contracts.Internal;
import com.microsoft.applicationinsights.channel.contracts.Location;
import com.microsoft.applicationinsights.channel.contracts.Operation;
import com.microsoft.applicationinsights.channel.contracts.Session;
import com.microsoft.applicationinsights.channel.contracts.User;
import com.microsoft.applicationinsights.common.AbstractTelemetryClientConfig;

import java.util.LinkedHashMap;
import java.util.UUID;

/**
 * This class is holding all telemetryContext information.
 */
public abstract class AbstractTelemetryContext {

    /**
     * Application telemetryContext.
     */
    private Application application;

    /**
     * Device telemetryContext.
     */
    private Device device;

    /**
     * Location telemetryContext.
     */
    private Location location;

    /**
     * Operation telemetryContext.
     */
    private Operation operation;

    /**
     * Session telemetryContext.
     */
    private Session session;

    /**
     * User telemetryContext.
     */
    private User user;

    /**
     * Internal telemetryContext.
     */
    private Internal internal;


    /**
     * User telemetryContext.
     */
    public User getUser() {
        return user;
    }

    /**
     * User telemetryContext.
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Device telemetryContext.
     */
    public Device getDevice() {
        return device;
    }

    /**
     * Device telemetryContext.
     */
    public void setDevice(Device device) {
        this.device = device;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    /**
     * Operation telemetryContext.
     */
    public Operation getOperation() {
        return operation;
    }

    /**
     * Operation telemetryContext.
     */
    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    /**
     * Session telemetryContext.
     */
    public Session getSession() {
        return session;
    }

    /**
     * Session telemetryContext.
     */
    public void setSession(Session session) {
        this.session = session;
    }

    /**
     * Application telemetryContext.
     */
    public Application getApplication() {
        return application;
    }

    /**
     * Application telemetryContext.
     */
    public void setApplication(Application application) {
        this.application = application;
    }

    /**
     * Constructs a new instance of the Telemetry telemetryContext tag keys
     * @param config the configuration for this telemetryContext
     */
    protected AbstractTelemetryContext(IContextConfig config) {
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
}
