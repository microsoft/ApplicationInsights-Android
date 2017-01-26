[ ![Download](https://api.bintray.com/packages/appinsights-android/maven/ApplicationInsights-Android/images/download.svg) ](https://bintray.com/appinsights-android/maven/ApplicationInsights-Android/_latestVersion)

# Application Insights for Android (1.0-beta.10) (DEPRECATED)

This project provides an Android SDK for Application Insights. [Application Insights](http://azure.microsoft.com/services/application-insights/) is a service for monitoring the performance and usage of your apps. This module allows you to send telemetry of various kinds (events, traces, etc.) to the Application Insights service where your data can be visualized in the Azure Portal.

The minimum SDK to use the Application Insights SDK in your app is 9.

Automatic collection of lifecycle-events requires API level 15 and up (Ice Cream Sandwich+).

### Breaking Changes!

Version 1.0-beta.9 of the Application Insights for Android SDK came with two major changes: 

Crash Reporting and the API to send handled exceptions have been removed from the SDK. 
In addition, the Application Insights for Android SDK is now **deprecated**.

The reason for this is that [HockeyApp](http://hockeyapp.net) is now our major offering for mobile and cross-plattform crash reporting, beta distribution and user feedback. We are focusing all our efforts on enhancing the HockeySDK and adding telemetry features to make HockeyApp the best platform to build awesome apps.
We've launched [HockeyApp Preseason](http://hockeyapp.net/blog/2016/02/02/introducing-preseason.html) so you can try all the new bits yourself, including **User Metrics**.

We apologize for any inconvenience and please feel free to [contact us](http://support.hockeyapp.net/) at any time.

## Content
1. [Release Notes](#1)
2. [Setup](#2)
3. [Advanced Setup](#3)
4. [Developer Mode](#4)
5. [Basic Usage](#5)
6. [Automatic Collection of Lifecycle Events](#6)
7. [Additional configuration](#7)
8. [Documentation](#8)
9. [Contributing](#9) 
10. [Contact](#10)

## <a name="1"></a> 1. Release Notes

* Fix for critical bug that prevented telemetry from being sent when developerMode was not enabled

See [here](https://github.com/Microsoft/ApplicationInsights-Android/releases) for release notes of previous versions or our [changelog](https://github.com/Microsoft/ApplicationInsights-Android/blob/master/CHANGELOG.md).

##<a name="2"></a> 2. Setup

This is the recommended way to setup Application Insights for your Android app. For other ways to setup the SDK, see [Advanced Setup](#4).

We're assuming you are using Android Studio and gradle to build your Android application.

### 2.1 **Add a compile dependency for the SDK**

In your module's ```build.gradle```add a dependency for Application Insights 

```groovy
dependencies {
    compile 'com.microsoft.azure:applicationinsights-android:1.0-beta.10'
}
```

### 2.2 Get the Instrumentation Key of an Application Insights resource

To view your telemetry, you'll need an Application Insights resource in the [Microsoft Azure Portal](https://portal.azure.com). You can either:

* [Create a new resource](https://azure.microsoft.com/documentation/articles/app-insights-create-new-resource/); or
* Use the same resource that you created to monitor your web service [on ASP.NET](https://azure.microsoft.com/documentation/articles/app-insights-asp-net/) or [on J2EE](https://azure.microsoft.com/documentation/articles/app-insights-java-get-started/).

Open your resource, open the Essentials drop-down. You'll need to copy the Instrumentation Key shortly.

### 2.3 Add permissions

Add the two permissions for `INTERNET` and `ACCESS_NETWORK_STATE` to your app's `AndroidManifest.xml`

```xml
<manifest>
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
</manifest>
```

### 2.4 Add your instrumentation key to manifest

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

### 2.5 Add code to setup and start Application Insights

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

##<a name="3"></a> 3. Advanced Setup

### 3.0 Download files manually

We recommend integrating our SDK as described above, if, however, you want to add our SDK to your project manually, press on the button below to download our SDK from Bintray.

[ ![Download](https://api.bintray.com/packages/appinsights-android/maven/ApplicationInsights-Android/images/download.svg) ](https://bintray.com/appinsights-android/maven/ApplicationInsights-Android/1.0-beta.6/view#files)


### 3.1 Set the instrumentation key in your gradle.properties

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

### 3.2 Set instrumentation key in code

It is also possible to set the instrumentation key of your app in code. This will override the one you might have set in your gradle or manifest file. Setting the instrumentation key programmatically can be done while setting up Application Insights:

```java
ApplicationInsights.setup(this.getApplicationContext(), getApplication(), "<YOUR-INSTRUMENTATION-KEY>");
ApplicationInsights.start();
```

## <a name="4"></a> 4. Developer Mode

The **developer mode** is enabled automatically in case the debugger is attached or if the app is running in the emulator. This will enable the console logging and decrease the number of telemetry items per batch (5 items) as well as the sending interval (3 seconds). If you don't want this behavior, disable the **developer mode**.

You can explicitly enable/disable the developer mode like this:

```java
//do this after ApplicationInsights.setup(this.getApplicationContext(), this.getApplication())
//and before ApplicationInsights.start()

ApplicationInsights.setDeveloperMode(false);

```

## <a name="5"></a> 5. Basic Usage  ##

The ```TelemetryClient```-instance provides various methods to track events, traces, metrics page views, and handled exceptions. To view metrics and event counts on the portal, use [Metric Explorer](https://azure.microsoft.com/documentation/articles/app-insights-metrics-explorer/). To inspect events, traces and exceptions, use [Search](https://azure.microsoft.com/documentation/articles/app-insights-diagnostic-search/).

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

//Setup a custom property
HashMap<String, String> properties = new HashMap<String, String>();
properties.put("property1", "my custom property");

//track an event with custom properties
client.trackEvent("sample event", properties);

```

See the [Guide to the API](https://azure.microsoft.com/documentation/articles/app-insights-api-custom-events-metrics/) for more about using the API and viewing the results.

## <a name="6"></a>6. Automatic collection of life-cycle events (Sessions & Page Views)

This only works in Android SDK version 14 and up (Ice Cream Sandwich+) and is **enabled by default**. Don't forget to provide an Application instance when setting up Application Insights (otherwise auto collection will be disabled):

```java
ApplicationInsights.setup(this.getApplicationContext(), this.getApplication());
```

If you want to explicitly **Disable** automatic collection of life-cycle events (auto session tracking and auto page view tracking), call ```setAutoCollectionDisabled``` inbetween setup and start of Application Insights. 

```java
ApplicationInsights.setup(this.getApplicationContext());
ApplicationInsights.disableAutoCollection(); //disable the auto-collection
ApplicationInsights.start();
```

After `ApplicationInsights.start()` was called, you can enable or disable those features at any point, even if you have disabled it between setup and start of the Application Insights SDK:

```java
// Disable automatic session renewal & tracking
ApplicationInsights.disableAutoSessionManagement();

// Enable automatic page view tracking
ApplicationInsights.enableAutoPageViewTracking();
```

###Note
Automatic collection of pageviews logs the name of the activity class. If you need a more descriptive name for your pageviews, turn of autocollection of pageviews 

```java
ApplicationInsights.disableAutoPageViewTracking();
```
and log pageviews yourself using one of the three methods provided by ```TelemetryClient```.

```java
TelemetryClient.getInstance().trackPageView("Page 1");
 
//Setup a custom property
HashMap<String, String> properties = new HashMap<String, String>();
properties.put("property1", "my custom property");
TelemetryClient.getInstance().trackPageView("Page 2", properties);

HashMap<String, Double> measurements = new HashMap<String, Double>();
measurements.put("measurement1", 2);
TelemetryClient.getInstance().trackPageView("Page 3", properties, measurements);
```

## <a name="7"></a>7. Additional Configuration

To configure Application Insights according to your needs, first, call

```java
ApplicationInsights.setup(this.getApplicationContext(), this.getApplication());
```

After that you can use `Configuration` to customize the behavior and values of the SDK.

```java
ApplicationInsightsConfig config = ApplicationInsights.getConfig();
//do the different configurations
```

After all custom configurations have been made, just start `ApplicationInsights`:

```java
ApplicationInsights.start();
```

### 7.1 Set User Session Time

The default time the users entering the app counts as a new session is 20s. If you want to set it to a different value, do the following:

```java
config.setSessionIntervalMs(30000); //set intercal to 30s (30,000ms)
```

### 7.2 Batch Size for a Bundle of Telemetry

Regular telemetry data is send out in batches or after a specified interval.

[**NOTE**] The [developer mode](#4) will automatically set the batching interval to 3s and the size of a batch to 5 items.

The default interval until a batch of telemetry is sent to the server is 15s. The following code will change it to 5s:

```java
config.setMaxBatchIntervalMs(5000); //set the interval to e.g. 5s (5,000ms)
```

To set the maxBatchSize to a different value (default is 100) like this:
```java
config.setMaxBatchCount(20); //set batch size to 20.
```

### 7.3 Set Different Endpoint

You can also configure a different server endpoint for the SDK if needed:

```java
config.setEndpointUrl("https://myserver.com/v2/track");
```

### 7.4 Override sessionID and user fields

Application Insights manages the ID of a session for you. If you want to override the generated ID with your own, it can be done like this:

```java
ApplicationInsights.renewSession("New session ID");
```
[**NOTE**] If you want to manage sessions manually, please disable [Automatic Collection of Lifecycle Events](#7).

It's also possible to make a lot of custom settings on the `TelemetryContext`of application insights, e.g. to set custom value for the `authUserId`. We're assuming that you know what you do if you customize the user object. Most user's won't need to customize the user object.

To get the TelemetryContext, call:
```java
ApplicationInsights.getTelemetryContext();
```

The telemetryContext has a lot of setters, e.g. to customize the user object.

```java
ApplicationInsights.getTelemetryContext().setAccountId("someId");
```

### 7.5 Other

For all available configarion options, see our [Javadoc](http://microsoft.github.io/ApplicationInsights-Android/) for ```Configuration```

##<a name="8"></a> 8. Documentation

Our Javadoc can be found at [http://microsoft.github.io/ApplicationInsights-Android/](http://microsoft.github.io/ApplicationInsights-Android/)

##<a name="9"></a> 9. Contributing

**Development environment**

* Install a Java Development Kit (JDK). The SDK can be compiled using JDK 1.6, though we recoommend installing <a href="http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html" target="_blank">JDK 1.8</a>
* Install <a href="http://developer.android.com/sdk/index.html" target="_blank">Android studio</a>
* [Get an instrumentation key](https://github.com/Microsoft/ApplicationInsights-Home/wiki#getting-an-application-insights-instrumentation-key) and set it in the manifest
* Run tests from Android Studio

<a id="codeofconduct"></a>
### 9.1 Code of Conduct

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/). For more information see the [Code of Conduct FAQ](https://opensource.microsoft.com/codeofconduct/faq/) or contact [opencode@microsoft.com](mailto:opencode@microsoft.com) with any additional questions or comments.

<a id="contributorlicense"></a>
### 9.2 Contributor License

You must sign a [Contributor License Agreement](https://cla.microsoft.com/) before submitting your pull request. To complete the Contributor License Agreement (CLA), you will need to submit a request via the [form](https://cla.microsoft.com/) and then electronically sign the CLA when you receive the email containing the link to the document. You need to sign the CLA only once to cover submission to any Microsoft OSS project. 

##<a name="10"></a> 10. Contact

If you have further questions or are running into trouble that cannot be resolved by any of the steps here, feel free to contact us at [AppInsights-Android@microsoft.com](mailto:AppInsights-Android@microsoft.com)

