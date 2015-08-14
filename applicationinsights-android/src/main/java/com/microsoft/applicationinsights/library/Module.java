package com.microsoft.applicationinsights.library;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import com.microsoft.applicationinsights.contracts.User;
import com.microsoft.applicationinsights.library.config.Configuration;
import com.microsoft.applicationinsights.logging.InternalLogging;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class Module {
    /**
     * The tag for logging.
     */
    private static final String TAG = "ApplicationInsights";

    /**
     * A flag which determines, if developer mode (logging) should be enabled.
     */
    private static AtomicBoolean DEVELOPER_MODE = new AtomicBoolean(Util.isEmulator() || Util.isDebuggerAttached());

    /**
     * The configuration of the SDK.
     */
    private Configuration configuration;

    /**
     * A flag, which determines if sending telemetry data should be disabled. Default is false.
     */
    private boolean telemetryDisabled;

    /**
     * A flag, which determines if crash reporting should be disabled. Default is false.
     */
    private boolean exceptionTrackingDisabled;

    /**
     * A flag, which determines if auto page views should be disabled from the start.
     * Default is false.
     */
    private boolean autoPageViewsDisabled;

    /**
     * A flag, which determines if auto session management should be disabled from the start.
     * Default is false.
     */
    private boolean autoSessionManagementDisabled;

    /**
     * A flag, which determines if auto appearance should be disabled from the start.
     * Default is false.
     */
    private boolean autoAppearanceDisabled;

    /**
     * The instrumentation key associated with the app.
     */
    private String instrumentationKey;

    /**
     * The weakContext which contains additional information for the telemetry data sent out.
     */
    private TelemetryContext telemetryContext;

    /**
     * A custom user object for sending telemetry data. Replaces
     * userId as we allow more configuration of the user object
     */
    private User user;

    /**
     * The weakContext associated with Application Insights.
     */
    private WeakReference<Context> weakContext;

    /**
     * The application needed for auto collecting telemetry data
     */
    private WeakReference<Application> weakApplication;

    /**
     * Properties associated with this telemetryContext.
     */
    private Map<String, String> commonProperties = Collections.synchronizedMap(new HashMap<String, String>());

    /**
     * Flag that indicates that the user has called a setup-method before
     */
    private static boolean isConfigured;

    /**
     * Flag that indicates that the pipeline (Channel, Persistence, etc.) have been setup
     */
    private static boolean isSetupAndRunning;

    /**
     * The type of channel to use for logging
     */
    private ChannelType channelType;


    private TelemetryClient telemetryClient;

    private ExceptionTracking exceptionTracking;

    private String moduleName;


    /**
     * Configure Application Insights Module
     * Note: This should be called before start
     *
     * @param moduleName  the name of the module, used for system preferences
     * @param context     the application context associated with Application Insights
     * @param application the application needed for auto collecting telemetry data
     */
    public Module(String moduleName, Context context, Application application) {
        setup(moduleName, context, application, null);
    }

    /**
     * Configure Application Insights
     * Note: This should be called before start
     *
     * @param moduleName         the name of the module, used for system preferences
     * @param context            the application context associated with Application Insights
     * @param application        the application needed for auto collecting telemetry data
     * @param instrumentationKey the instrumentation key associated with the app
     */
    public Module(String moduleName, Context context, Application application, String instrumentationKey) {
        setup(moduleName, context, application, instrumentationKey);
    }

    /**
     * Configure Application Insights
     * Note: This should be called before start
     *
     * @param moduleName         the name of the module, used for system preferences
     * @param context            the application context associated with Application Insights
     * @param application        the application needed for auto collecting telemetry data
     * @param instrumentationKey the instrumentation key associated with the app
     */
    private void setup(String moduleName, Context context, Application application, String instrumentationKey) {
        if (!isConfigured) {
            if (moduleName != null && (moduleName.length()) > 0 && context != null) {
                this.moduleName = moduleName;

                this.channelType = ChannelType.Default;
                this.configuration = new Configuration();

                this.weakContext = new WeakReference<Context>(context);
                this.weakApplication = new WeakReference<Application>(application);
                isConfigured = true;
                this.instrumentationKey = instrumentationKey;

                if (this.instrumentationKey == null) {
                    //TODO: what if multiple modules and no ikey was provided?
                    this.instrumentationKey = readInstrumentationKey(context, moduleName);
                }

                if (this.user == null) {
                    //in case the dev uses deprecated method to set the user's ID
                    this.user = new User();
                }
                TelemetryContext.initialize(context, this.instrumentationKey, this.user);
                this.telemetryContext = TelemetryContext.getSharedInstance();
                InternalLogging.info(TAG, "ApplicationInsights has been setup correctly.", null);
            } else {
                InternalLogging.warn(TAG, "ApplicationInsights could not be setup correctly " +
                      "because the given moduleName was null or empty or the weakContext was null");
            }
        }
    }

    /**
     * Reads the instrumentation key from AndroidManifest.xml if it is available
     *
     * @param context the application weakContext to check the manifest from
     * @return the instrumentation key configured for the application
     */
    private String readInstrumentationKey(Context context, String moduleName) {
        String iKey = "";
        if (context != null) {
            try {
                Bundle bundle = context
                      .getPackageManager()
                      .getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA)
                      .metaData;
                if (bundle != null) {
                    //TODO: multiple modules?
                    iKey = bundle.getString("com.microsoft.applicationinsights.instrumentationKey." + moduleName);
                } else {
                    logInstrumentationInstructions();
                }
            } catch (PackageManager.NameNotFoundException exception) {
                logInstrumentationInstructions();
                Log.v(TAG, exception.toString());
            }
        }

        return iKey;
    }

    /* Writes instructions on how to configure the instrumentation key.
        */
    private void logInstrumentationInstructions() { //TODO: change log info if multiple modules
        String instructions = "No instrumentation key found.\n" +
              "Set the instrumentation key in AndroidManifest.xml";
        String manifestSnippet = "<meta-data\n" +
              "android:name=\"com.microsoft.applicationinsights.instrumentationKey\"" +
              this.moduleName + "\n" + "android:value=\"${AI_INSTRUMENTATION_KEY}\" />";
        InternalLogging.error("MissingInstrumentationkey", instructions + "\n" + manifestSnippet);
    }

    /**
     * Start the module
     * Note: This should be called after {@link #isConfigured}
     */
    public void start() {
        startInstance();
    }

    /**
     * Start the module
     * Note: This should be called after {@link #isConfigured}
     */
    private void startInstance() {
        if (!isConfigured) {
            InternalLogging.warn(TAG, "Could not start Application Insights since it has not been " +
                  "setup correctly.");
            return;
        }
        if (!isSetupAndRunning) {
            Context context = this.weakContext.get();

            initializePipeline(context);
            startCrashReporting();//TODO does this work with several modules?

            Sender.getInstance().sendDataOnAppStart();
            InternalLogging.info(TAG, "ApplicationInsights has been started.", "");
            isSetupAndRunning = true;
        }
    }

    private void startCrashReporting() {
        // Start crash reporting
        if (!this.exceptionTrackingDisabled) {
            ExceptionTracking.registerExceptionHandler();
        }
    }

    /**
     * Makes sure Persistence, Sender, ChannelManager, TelemetryClient and AutoCollection are initialized
     * Call this before starting AutoCollection
     *
     * @param context application context
     */
    private void initializePipeline(Context context) {
        EnvelopeFactory.initialize(telemetryContext, this.commonProperties); //TODO un-singleton EnvelopeFactory

        Persistence.initialize(context); //TODO un-singleton Persistence
        Sender.initialize(this.configuration); //TODO un-singleton Sender
        ChannelManager.initialize(channelType); //TODO un-singleton ChannelManager

        // Initialize Telemetry
        Application application = null;
        if (this.weakApplication != null) {
            application = this.weakApplication.get();
        }
        TelemetryClient.initialize(!this.telemetryDisabled, application); //TODO unsingleton TelemetryClient
        TelemetryClient.startAutoCollection(this.telemetryContext, this.configuration, !this.autoAppearanceDisabled, !this.autoPageViewsDisabled, !this.autoSessionManagementDisabled);
    }
}
