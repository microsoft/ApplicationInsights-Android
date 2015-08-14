package com.microsoft.applicationinsights.library;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.content.ComponentCallbacks2;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;

import com.microsoft.applicationinsights.library.config.ISessionConfig;
import com.microsoft.applicationinsights.logging.InternalLogging;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
class AutoCollection implements Application.ActivityLifecycleCallbacks, ComponentCallbacks2 {
    /**
     * The activity counter
     */
    protected final AtomicInteger activityCount;

    /**
     * The configuration for tracking sessions
     */
    protected ISessionConfig config;

    /**
     * The timestamp of the last activity
     */
    protected final AtomicLong lastBackground;

    /**
     * The telemetryContext which is needed to renew a session
     */
    protected TelemetryContext telemetryContext;

    /**
     * Volatile boolean for double checked synchronize block
     */
    private static volatile boolean isLoaded = false;

    /**
     * Synchronization LOCK for setting static context
     */
    private static final Object LOCK = new Object();

    /**
     * The singleton INSTANCE of this class
     */
    private static AutoCollection instance;

    /**
     * The tag for logging
     */
    private static final String TAG = "AutoCollection";

    /**
     * A flag which determines whether auto page view tracking has been enabled or not.
     */
    private static boolean autoPageViewsEnabled;

    /**
     * A flag which determines whether session management has been enabled or not.
     */
    private static boolean autoSessionManagementEnabled;

    protected static boolean isAutoAppearanceTrackingEnabled() {
        return autoAppearanceTrackingEnabled;
    }

    protected static boolean isHasRegisteredComponentCallbacks() {
        return hasRegisteredComponentCallbacks;
    }

    protected static boolean isHasRegisteredLifecycleCallbacks() {
        return hasRegisteredLifecycleCallbacks;
    }

    protected static boolean isAutoPageViewsEnabled() {
        return autoPageViewsEnabled;
    }

    protected static boolean isAutoSessionManagementEnabled() {
        return autoSessionManagementEnabled;
    }

    /**
     * A flag that determines whether we want to auto-track events for foregrounding backgrounding
     */
    private static boolean autoAppearanceTrackingEnabled;

    /**
     * A flag that indicates if componentcallbacks have been registered
     */
    private static boolean hasRegisteredComponentCallbacks;

    /**
     * A flag that indicates if lifecyclecallbacks have been already registered
     */
    private static boolean hasRegisteredLifecycleCallbacks;

    /**
     * Create a new INSTANCE of the autocollection event tracking
     *
     * @param config           the session configuration for session tracking
     * @param telemetryContext the context, which is needed to renew sessions
     */
    protected AutoCollection(ISessionConfig config, TelemetryContext telemetryContext) {
        this.activityCount = new AtomicInteger(0);
        this.lastBackground = new AtomicLong(this.getTime());
        this.config = config;
        this.telemetryContext = telemetryContext;
    }

    /**
     * Initialize the INSTANCE of Autocollection event tracking.
     *
     * @param telemetryContext the context, which is needed to renew sessions
     * @param config           the session configuration for session tracking
     */
    protected static void initialize(TelemetryContext telemetryContext, ISessionConfig config) {
        // note: isLoaded must be volatile for the double-checked LOCK to work
        if (!AutoCollection.isLoaded) {
            synchronized (AutoCollection.LOCK) {
                if (!AutoCollection.isLoaded) {
                    AutoCollection.isLoaded = true;
                    AutoCollection.hasRegisteredComponentCallbacks = false;
                    AutoCollection.hasRegisteredLifecycleCallbacks = false;
                    AutoCollection.instance = new AutoCollection(config, telemetryContext);
                }
            }
        }
    }

    /**
     * @return the INSTANCE of autocollection event tracking or null if not yet initialized
     */
    protected static AutoCollection getInstance() {
        if (AutoCollection.instance == null) {
            InternalLogging.error(TAG, "getSharedInstance was called before initialization");
        }

        return AutoCollection.instance;
    }

    /**
     * Enables lifecycle event tracking for the provided application
     *
     * @param application the application object
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private static void registerActivityLifecycleCallbacks(Application application) {
        if (!hasRegisteredLifecycleCallbacks) {
            if ((application != null) && Util.isLifecycleTrackingAvailable()) {
                application.registerActivityLifecycleCallbacks(AutoCollection.getInstance());
                hasRegisteredLifecycleCallbacks = true;
                InternalLogging.info(TAG, "Registered activity lifecycle callbacks");
            }
        }
    }

    /**
     * Register for component callbacks to enable persisting when backgrounding on devices with API-level 14+
     * and persisting when receiving onMemoryLow() on devices with API-level 1+
     *
     * @param application the application object
     */
    private static void registerForComponentCallbacks(Application application) {
        if (!hasRegisteredComponentCallbacks) {
            if ((application != null) && Util.isLifecycleTrackingAvailable()) {
                application.registerComponentCallbacks(AutoCollection.getInstance());
                hasRegisteredComponentCallbacks = true;
                InternalLogging.info(TAG, "Registered component callbacks");
            }
        }
    }

    /**
     * Enables page view event tracking for the provided application
     *
     * @param application the application object
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    protected static void enableAutoPageViews(Application application) {
        if (application != null && Util.isLifecycleTrackingAvailable()) {
            synchronized (AutoCollection.LOCK) {
                registerActivityLifecycleCallbacks(application);
                autoPageViewsEnabled = true;
            }
        }
    }

    /**
     * Disables page view event tracking for the provided application*
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    protected static void disableAutoPageViews() {
        if (Util.isLifecycleTrackingAvailable()) {
            synchronized (AutoCollection.LOCK) {
                autoPageViewsEnabled = false;
            }
        }
    }

    /**
     * Enables session event tracking for the provided application
     *
     * @param application the application object
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    protected static void enableAutoSessionManagement(Application application) {
        if (application != null && Util.isLifecycleTrackingAvailable()) {
            synchronized (AutoCollection.LOCK) {
                registerForComponentCallbacks(application);
                registerActivityLifecycleCallbacks(application);
                autoSessionManagementEnabled = true;
            }
        }
    }

    /**
     * Disables session event tracking for the provided application
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    protected static void disableAutoSessionManagement() {
        if (Util.isLifecycleTrackingAvailable()) {
            synchronized (AutoCollection.LOCK) {
                autoSessionManagementEnabled = false;
            }
        }
    }

    /**
     * Enables auto appearance event tracking for the provided application
     *
     * @param application the application object
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    protected static void enableAutoAppearanceTracking(Application application) {
        if (application != null && Util.isLifecycleTrackingAvailable()) {
            synchronized (AutoCollection.LOCK) {
                registerForComponentCallbacks(application);
                registerActivityLifecycleCallbacks(application);
                autoAppearanceTrackingEnabled = true;
            }
        }
    }


    /**
     * Disables auto appearance event tracking for the provided application
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    protected static void disableAutoAppearanceTracking() {
        if (Util.isLifecycleTrackingAvailable()) {
            synchronized (AutoCollection.LOCK) {
                autoAppearanceTrackingEnabled = false;
            }
        }
    }

    /**
     * This is called each time an activity is created.
     *
     * @param activity           the Android Activity that's created
     * @param savedInstanceState the bundle
     */
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        // unused but required to implement ActivityLifecycleCallbacks
        //NOTE:
        //We first implemented Session management here. This callback doesn't work for the starting
        //activity of the app because the SDK will be setup and initialized in the onCreate, so
        //we don't get the very first call to an app activity's onCreate.
        //This is why the logic was moved to onActivityResumed below
    }

    /**
     * This is called each time an activity becomes visible
     *
     * @param activity the activity which entered the foreground
     */
    public void onActivityStarted(Activity activity) {
        // unused but required to implement ActivityLifecycleCallbacks
    }

    /**
     * This is called each time an activity has been started or was resumed after pause
     *
     * @param activity the activity which left the foreground
     */
    public void onActivityResumed(Activity activity) {
        synchronized (AutoCollection.LOCK) {
            sessionManagement();
            sendPagewView(activity);
        }
    }

    private void sendPagewView(Activity activity) {
        if (autoPageViewsEnabled) {
            InternalLogging.info(TAG, "New Pageview");
            TrackDataOperation pageViewOp = new TrackDataOperation(TrackDataOperation.DataType.PAGE_VIEW, activity.getClass().getName());
            new Thread(pageViewOp).start();
        }
    }

    private void sessionManagement() {
        int count = this.activityCount.getAndIncrement();
        if (count == 0) {
            if (autoSessionManagementEnabled) {
                InternalLogging.info(TAG, "Starting & tracking session");
                TrackDataOperation sessionOp = new TrackDataOperation(TrackDataOperation.DataType.NEW_SESSION);
                new Thread(sessionOp).start();
            } else {
                InternalLogging.info(TAG, "Session management disabled by the developer");
            }
            if (autoAppearanceTrackingEnabled) {
                //TODO track cold start as soon as it's available in new Schema.
            }
        } else {
            //we should already have a session now
            //check if the session should be renewed
            long now = this.getTime();
            long then = this.lastBackground.getAndSet(this.getTime());
            boolean shouldRenew = ((now - then) >= this.config.getSessionIntervalMs());
            InternalLogging.info(TAG, "Checking if we have to renew a session, time difference is: " + (now-then));

            if (autoSessionManagementEnabled && shouldRenew) {
                InternalLogging.info(TAG, "Renewing session");
                this.telemetryContext.renewSessionId();
                TrackDataOperation sessionOp = new TrackDataOperation(TrackDataOperation.DataType.NEW_SESSION);
                new Thread(sessionOp).start();
            }
        }
    }

    /**
     * This is called each time an activity leaves the foreground
     *
     * @param activity the activity which was paused
     */
    public void onActivityPaused(Activity activity) {
        //set backgrounding in onTrimMemory
    }

    public void onActivityStopped(Activity activity) {
        // unused but required to implement ActivityLifecycleCallbacks
    }

    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        // unused but required to implement ActivityLifecycleCallbacks
    }

    public void onActivityDestroyed(Activity activity) {
        // unused but required to implement ActivityLifecycleCallbacks
    }

    @Override
    public void onTrimMemory(int level) {
        if (level == TRIM_MEMORY_UI_HIDDEN) {
            InternalLogging.info(TAG, "UI of the app is hidden");
            InternalLogging.info(TAG, "Setting background time");
            this.lastBackground.set(this.getTime());
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        switch (newConfig.orientation) {
            case Configuration.ORIENTATION_PORTRAIT:
                InternalLogging.info(TAG, "Device Orientation is portrait");
                break;
            case Configuration.ORIENTATION_LANDSCAPE:
                InternalLogging.info(TAG, "Device Orientation is landscape");
                break;
            case Configuration.ORIENTATION_UNDEFINED:
                InternalLogging.info(TAG, "Device Orientation is undefinded");
                break;
            default:
                break;
        }
    }

    @Override
    public void onLowMemory() {
        // unused but required to implement ComponentCallbacks
    }

    /**
     * Test hook to get the current time
     *
     * @return the current time in milliseconds
     */
    protected long getTime() {
        return new Date().getTime();
    }
}
