# Application Insights for Android

This project extends the Application Insights API surface to support Android. [Application Insights](http://azure.microsoft.com/en-us/services/application-insights/) is a service that allows developers to keep their application available, performing and succeeding. This module will allow you to send telemetry of various kinds (event, trace, exception, etc.) to the Application Insights service where they can be visualized in the Azure Portal. 




## Setup ##




>_****temporary - until hosted publicly****_

>- clone and build locally
- copy android-sdk.aar and common.jar into your libs folder
- reference the libs folder from gradle and add to dependencies

```gradle
repositories {
    flatDir {
        dirs 'libs'
    }
}

compile(name: 'android-sdk-debug', ext: 'aar')
```



**Get an instrumentation key**

Please see the "[Getting an Application Insights Instrumentation Key](https://github.com/Microsoft/AppInsights-Home/wiki#getting-an-application-insights-instrumentation-key)" section of the wiki for more information. To try the SDK without an instrumentation key, set the instrumentationKey config value to a non-empty string.
>**Note**: an instrumentation key is required before any data can be viewed in the Azure portal.

**Allow the following permissions in your AndroidManifest.xml**

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```





## Usage ##
**Track an activity**

From an activity in your app, import and instantiate a telemetry client
```java
import com.microsoft.applicationinsights.TelemetryClient;
```
```java
String instrumentationKey = "<INSTRUMENTATION_KEY>";
Context context = this.getApplicationContext();
TelemetryClient client = new TelemetryClient(instrumentationKey, context);
```
Override the onStart and onStop methods and track events
**Track events/metrics/traces/exceptions**
```java
client.trackTrace("example trace");
client.trackEvent("example event");
client.trackException(new Error("example error"), "handledAt");
client.trackMetric("example metric", 1);
```





## Contributing ##
**Development environment**

* Install [Android studio](http://developer.android.com/sdk/index.html)
* Install [JDK 1.8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
* Run tests from android studio