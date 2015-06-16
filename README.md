[ ![Download](https://api.bintray.com/packages/appinsights-android/maven/ApplicationInsights-Android/images/download.svg) ](https://bintray.com/appinsights-android/maven/ApplicationInsights-Android/_latestVersion)

# Application Insights for Android (1.0-beta.5)

This project provides an Android SDK for Application Insights. [Application Insights](http://azure.microsoft.com/en-us/services/application-insights/) is a service that allows developers to keep their applications available, performing, and succeeding. This module allows you to send telemetry of various kinds (events, traces, exceptions, etc.) to the Application Insights service where your data can be visualized in the Azure Portal.

The minimum SDK to use the Application Insights SDK in your app is 9.

Automatic collection of lifecycle-events requires API level 15 and up (Ice Cream Sandwich+).

## Content

1. [Release Notes](#1)
2. [Breaking Changes & Deprecations](#2)
3. [Setup](#3)
4. [Advanced Setup](#4)
5. [Developer Mode](#5)
6. [Basic Usage](#6)
7. [Automatic Collection of Lifecycle Events](#7)
8. [Exception Handling (Crashes)](#8)
9. [Additional configuration](#9)
10. [Documentation](#10)
11. [Contributing](#11) 

## <a name="1"></a> 1. Release Notes

* The SDK is now built using the Android Tools Gradle plugin 1.2.3
* Fix a null pointer exception in ```LifecycleTracking```[#43](https://github.com/Microsoft/ApplicationInsights-Android/pull/43)
* Refactored Autocollection – ```LifecycleTracking```has been deprecated [#51](https://github.com/Microsoft/ApplicationInsights-Android/pull/51)
* Fix for null pointer exceptions when trying to serialize ```null``` [#45](https://github.com/Microsoft/ApplicationInsights-Android/pull/45)
* Fix for ```Concurrent Modification Exception``` in case the same Telemetry-Object was after it was modified [#44](https://github.com/Microsoft/ApplicationInsights-Android/pull/44)
* **Fix for ```ClassNotFoundException``` when running the SDK on an Android 2.3 device** [#48](https://github.com/Microsoft/ApplicationInsights-Android/pull/48)
* **Fix a bug that was introduced in 1.0-beta.4 that caused crashes not to be sent under some circumstances** [#52](https://github.com/Microsoft/ApplicationInsights-Android/pull/52) & [e3b51e7927f238cc123c50b654fbeab448ba6df6](https://github.com/Microsoft/ApplicationInsights-Android/commit/e3b51e7927f238cc123c50b654fbeab448ba6df6)


##<a name="2"></a> 2. Breaking Changes & deprecations

Starting with 1.0-beta.5, breaking changes will be announced 1 release in advance. Once a method has been deprecated, the next release of the SDK will remove the API.

**[1.0-beta.5]**

* Two previously deprecated setup-methods for ```ApplicationInsights```have been removed.
* ```LifecycleTracking```has been deprecated, use ```AutoCollection```instead. 

**[1.0-beta.4]**

* Two setup-methods for ```ApplicationInsights```have been deprecated and will be removed in the next beta

**[1.0-beta.3]**

Configuration of the Application Insights SDK is now done using ```ApplicationInsightsConfig```. The previous config-classes have been removed

**[1.0-beta.2]**

To enable automatic lifecycle-tracking, Application Insights has to be set up with an instance of Application (see [Life-cycle tracking] (#2)), otherwise, lifecycle-tracking is disabled.

**[1.0-beta.1]**

Setup and start of the Application Insights SDK are now done using the new umbrella class `ApplicationInsights` instead of `AppInsights `

**[1.0-Alpha.5]**

Setup and start of the Application Insights SDK are now done using the new umbrella class `AppInsights` instead of `TelemetryClient`

##<a name="3"></a> 3. Setup

This is the recommended way to setup Application Insights for your Android app. For other ways to setup the SDK, see [Advanced Setup](#4).

We're assuming you are using Android Studio and gradle to build your Android application.

### 3.1 **Add a compile dependency for the SDK**

In your module's ```build.gradle```add a dependency for Application Insights 

```groovy
dependencies {
    compile 'com.microsoft.azure:applicationinsights-android:1.0-beta.5'
}
```

### 3.2 Configure the instrumentation key

Please see the "[Getting an Application Insights Instrumentation Key](https://github.com/Microsoft/ApplicationInsights-Home/wiki#getting-an-application-insights-instrumentation-key)" section of the wiki for more information on acquiring a key.

### 3.3 Add permissions

Add the two permissions for `INTERNET` and `ACCESS_NETWORK_STATE` to your app's `AndroidManifest.xml`

```xml
<manifest>
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
</manifest>
```

### 3.4 Add your instrumentation key to manifest

Add the _instrumentation key_ for your app to your Android Manifest as follows. Replace `${AI_INSTRUMENTATION_KEY}` with your instrumentation key. You can leave the variable as is if you want to use your ```gradle.properties``` to set it (see [Advanced Setup](#4)). 

```xml
<manifest>
    <application>
        <meta-data
            android:name="com.microsoft.applicationinsights.instrumentationKey"
            android:value="${AI_INSTRUMENTATION_KEY}" />
    </application>
</manifest>
```

### 3.5 Add code to setup and start Application Insights

Add the following import to your app's root activity

```java
import com.microsoft.applicationinsights.library.ApplicationInsights;
```

and add 

```java
ApplicationInsights.setup(this.getApplicationContext(), this.getApplication());
ApplicationInsights.start();
```

in the activity's `onCreate`-callback.

**Congratulation, now you're all set to use Application Insights! See [Usage](#6) on how to use Application Insights.**

##<a name="4"></a> 4. Advanced Setup

### 4.1 Set the instrumentation key in your gradle.properties

Add the ```<meta-data>```to your Android Manifest as described above but leave the variable ```${AI_INSTRUMENTATION_KEY}```as is. In your global ```gradle.properties```, add

```java
ai_instrumentation_key=<KEY_PLACEHOLDER>
```

and replace ```<KEY_PLACEHOLDER>``` with your instrumentation key.
After that, open your top-level ```build.gradle```and add the mainfest placeholder as follows:

```groovy
android {
    buildTypes {
        all {
            manifestPlaceholders = [AI_INSTRUMENTATION_KEY: ai_instrumentation_key]
        }
    }
}
```

### 4.2 Set instrumentation key in code

It is also possible to set the instrumentation key of your app in code. This will override the one you might have set in your gradle or manifest file. Setting the instrumentation key programmatically can be done while setting up Application Insights:

```java
ApplicationInsights.setup(this.getApplicationContext(), getApplication(), "<YOUR-INSTRUMENTATION-KEY>");
ApplicationInsights.start();
```

## <a name="5"></a> 5. Developer Mode

The **developer mode** is enabled automatically in case the debugger is attached or if the app is running in the emulator. This will enable the console logging and decrease the number of telemetry items per batch (5 items) as well as the sending interval (3 seconds). If you don't want this behavior, disable the **developer mode**.

You can explicitly enable/disable the developer mode like this:

```java
//do this after ApplicationInsights.setup(this.getApplicationContext(), this.getApplication())
//and before ApplicationInsights.start()

ApplicationInsights.setDeveloperMode(false);

```

## <a name="6"></a> 6. Basic Usage  ##

The ```TelemetryClient```-instance provides various methods to track events, traces, metrics page views, and handled exceptions.

```java
	
//somewhere in your app, e.g. in the callback of a button
//or in the onCreate-callback of an activity
	
//Get the instance of TelemetryClient
TelemetryClient client = TelemetryClient.getInstance();

//track an event
client.trackEvent("sample event");

//track a trace
client.trackTrace("sample trace");

//track a metric
client.trackMetric("sample metric", 3);

//track handled exceptions
ArrayList<Object> myList = new ArrayList<Object>();
try{
	Object test = myList.get(2);
}catch(Exception e){
	TelemetryClient.getInstance().trackHandledException(e);
}
```

Some data types allow for custom properties.

```java

//Get the instance of TelemetryClient
TelemetryClient client = TelemetryClient.getInstance();

Setup a custom property
HashMap<String, String> properties = new HashMap<String, String>();
properties.put("property1", "my custom property");

//track an event with custom properties
client.trackEvent("sample event", properties);

```

## <a name="7"></a>7. Automatic collection of life-cycle events (Sessions & Page Views)

This only works in Android SDK version 14 and up (Ice Cream Sandwich+) and is **enabled by default**. Don't forget to provide an Application instance when setting up Application Insights (otherwise auto collection will be disabled):

```java
ApplicationInsights.setup(this.getApplicationContext(), this.getApplication());
```

If you want to explicitly **Disable** automatic collection of life-cycle events (auto session tracking and auto page view tracking), call ```setAutoCollectionDisabled``` inbetween setup and start of Application Insights. 

```java
ApplicationInsights.setup(this.getApplicationContext());
ApplicationInsights.setAutoCollectionDisabled(true); //disable the auto-collection
ApplicationInsights.start();
```

After `ApplicationInsights.start()` was called, you can enable or disable those features at any point, even if you have disabled it between setup and start of the Application Insights SDK:

```java
// Disable automatic session renewal & tracking
ApplicationInsights.disableAutoSessionManagement();

// Enable automatic page view tracking
ApplicationInsights.enableAutoPageViewTracking();
```

## <a name="8"></a>8.  Exception Handling (Crashes)

The Application Insights SDK enables crash reporting **per default**. Unhandled exceptions (aka crashes) will be immediately sent to the server if a connection is available.

This feature can be disabled as follows:

```java
 ApplicationInsights.setExceptionTrackingDisabled(true);

```

## <a name="9"></a>9. Additional Configuration

To configure Application Insights according to your needs, first, call

```java
ApplicationInsights.setup(this.getApplicationContext(), this.getApplication());
```

After that you can use `ApplicationInsightsConfig` to customize the behavior and values of the SDK.

```java
ApplicationInsightsConfig config = ApplicationInsights.getConfig();
//do the different configurations
```

After all custom configurations have been made, just start `ApplicationInsights`:

```java
ApplicationInsights.start();
```

### 9.1 Set User Session Time

The default time the users entering the app counts as a new session is 20s. If you want to set it to a different value, do the following:

```java
config.setSessionIntervalMs(30000); //set intercal to 30s (30,000ms)
```

### 9.2 Batch Size for a Bundle of Telemetry

Unhandled exceptions (aka ”your app is crashing!“) are sent out immediately, while regular telemetry data is send out in batches or after a specified interval.

[**NOTE**] The [developer mode](#4) will automatically set the batching interval to 3s and the size of a batch to 5 items.

The default interval until a batch of telemetry is sent to the server is 15s. The following code will change it to 5s:

```java
config.setMaxBatchIntervalMs(5000); //set the interval to e.g. 5s (5,000ms)
```

To set the maxBatchSize to a different value (default is 100) like this:
```java
config.setMaxBatchCount(20); //set batch size to 20.
```

### 9.3 Set Different Endpoint

You can also configure a different server endpoint for the SDK if needed:

```java
config.setEndpointUrl("https://myserver.com/v2/track");
```

### 9.4 Override sessionID and userID

Application Insights manages IDs for a session and for individual users for you. If you want to override the generated IDs with your own, it can be done like this:

```java
ApplicationInsights.setUserId("New user ID");
ApplicationInsights.renewSession("New session ID");
```
[**NOTE**] If you want to manage sessions manually, please disable [Automatic Collection of Lifecycle Events](#7).

### 9.5 Other

For all available configarion options, see our [Javadoc](http://microsoft.github.io/ApplicationInsights-Android/) for ```ApplicationInsightsConfig```

##<a name="10"></a> 10. Documentation

Our Javadoc can be found at [http://microsoft.github.io/ApplicationInsights-Android/](http://microsoft.github.io/ApplicationInsights-Android/)

##<a name="11"></a> 11 Contributing

**Development environment**

* Install a Java Development Kit (JDK). The SDK can be compiled using JDK 1.6, though we recoommend installing <a href="http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html" target="_blank">JDK 1.8</a>
* Install <a href="http://developer.android.com/sdk/index.html" target="_blank">Android studio</a>
* [Get an instrumentation key](/Microsoft/ApplicationInsights-Home/wiki#getting-an-application-insights-instrumentation-key) and set it in the manifest
* Run tests from Android Studio

<a id="contact"></a>
## 12. Contact

If you have further questions or are running into trouble that cannot be resolved by any of the steps here, feel free to contact us at [AppInsights-Android@microsoft.com](mailto:AppInsights-Android@microsoft.com)

