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
    ;

    /**
     * A flag that deterines whether we want to auto-track events for foregrounding backgrounding
     */
    private static boolean autoAppearanceTrackingEnabled;


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
            InternalLogging.error(TAG, "getInstance was called before initialization");
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
        if (!autoPageViewsEnabled && !autoSessionManagementEnabled && !autoAppearanceTrackingEnabled) {
            if (Util.isLifecycleTrackingAvailable()) {
                application.registerActivityLifecycleCallbacks(AutoCollection.getInstance());
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
        if (application != null && Util.isLifecycleTrackingAvailable()) {
            application.registerComponentCallbacks(AutoCollection.getInstance());
            InternalLogging.info(TAG, "Registered component callbacks");
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
        int count = this.activityCount.getAndIncrement();
        synchronized (AutoCollection.LOCK) {
            if (count == 0) {
                if (autoSessionManagementEnabled) {
                    TrackDataOperation sessionOp = new TrackDataOperation(TrackDataOperation.DataType.NEW_SESSION);
                    new Thread(sessionOp).start();
                }
                if(autoAppearanceTrackingEnabled) {
                    //TODO track cold start as soon as it's available in new Schema.
                }
            }
        }
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
        // check if the session should be renewed
        long now = this.getTime();
        long then = this.lastBackground.getAndSet(this.getTime());
        boolean shouldRenew = (now - then) >= this.config.getSessionIntervalMs();

        //TODO check what happens when an app is in foreground for more than the session interval (videoplay) and we then start a new activity

        synchronized (AutoCollection.LOCK) {
            if (autoSessionManagementEnabled && shouldRenew) {
                this.telemetryContext.renewSessionId();
                TrackDataOperation sessionOp = new TrackDataOperation(TrackDataOperation.DataType.NEW_SESSION);
                new Thread(sessionOp).start();
            }

            if (autoPageViewsEnabled) {
                TrackDataOperation pageViewOp = new TrackDataOperation(TrackDataOperation.DataType.PAGE_VIEW, activity.getClass().getName(), null, null);
                new Thread(pageViewOp).start();
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
    public void onTrimMemory(int level) {//TODO Move syncing to seperate class
        if (level == TRIM_MEMORY_UI_HIDDEN) {
            InternalLogging.info(TAG, "UI of the app is hidden");
            InternalLogging.info(TAG, "Setting background time");
            this.lastBackground.set(this.getTime());
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // unused but required to implement ComponentCallbacks
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
