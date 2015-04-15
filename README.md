[ ![Download](https://api.bintray.com/packages/appinsights-android/maven/AppInsights-Android/images/download.svg) ](https://bintray.com/appinsights-android/maven/AppInsights-Android/_latestVersion)

# Application Insights for Android (1.0-Beta.1)

This project provides an Android SDK for Application Insights. [Application Insights](http://azure.microsoft.com/en-us/services/application-insights/) is a service that allows developers to keep their applications available, performing, and succeeding. This module allows you to send telemetry of various kinds (events, traces, exceptions, etc.) to the Application Insights service where your data can be visualized in the Azure Portal.

The minimum SDK to use the Application Insights SDK in your app is 9.

Automatic collection of lifecycle-events requires API level 15 and up (Ice Cream Sandwich+).


**Release Notes**

* Renamed umbrella class for setting up and starting the SDK to ```ApplicationInsights```
* Developer Mode for improved logging and shorter default interval and batch size for sending telemetry
* Exception tracking and telemetry are now enabled by default
* Source compatibility with Java 6
* Performance improvements and bug fixes 


**Breaking Changes**

* **[1.0-Beta.1]** Setup and start of the Application Insights SDK are now done using the new umbrella class `ApplicationInsights` instead of `AppInsights `
* **[1.0-Alpha.5]** Setup and start of the Application Insights SDK are now done using the new umbrella class `AppInsights` instead of `TelemetryClient`

## Setup ##
	
**Add a compile dependency for the SDK**

Per-module

```java
dependencies {
    compile 'com.microsoft.azure:applicationinsights-android:+'
}
```

**Configure the instrumentation key and add permissions**

Please see the "[Getting an Application Insights Instrumentation Key](https://github.com/Microsoft/ApplicationInsights-Home/wiki#getting-an-application-insights-instrumentation-key)" section of the wiki for more information on acquiring a key.

Plase add the two permissions for `INTERNET` and `ACCESS_NETWORK_STATE` into your app's `AndroidManifest.xml` as well as the property for your instrumentation key as follows. Replace `${AI_INSTRUMENTATION_KEY}` with your instrumentation key or the variable leave it and use gradle.properties to set it. 

```xml
<manifest>
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    
    <application>
        <meta-data
            android:name="com.microsoft.applicationinsights.instrumentationKey"
            android:value="${AI_INSTRUMENTATION_KEY}" />
    </application>
</manifest>
```

**Optional: load instrumentation key from gradle**

~/.gradle/gradle.properties

```java
ai_instrumentation_key=<KEY_PLACEHOLDER>
```

Top-level gradle build file

```java
android {
    buildTypes {
        all {
            manifestPlaceholders = [AI_INSTRUMENTATION_KEY: ai_instrumentation_key]
        }
    }
}
```

**Optional: set instrumentation key in code**

It is also possible to set the instrumentation key of your app in code. This will override the one you might have set in your gradle or manifest file. Setting the instrumentation key programmatically can be done while setting up Application Insights:

```java
ApplicationInsights.setup(this, "<YOUR-IKEY-GOES-HERE>");
ApplicationInsights.start();
```

## Usage ##

Add the following import to your apps root activity

```java
import com.microsoft.applicationinsights.TelemetryClient;
```

And add 
```java
ApplicationInsights.setup(this);
ApplicationInsights.start();
```

in the activity's `onCreate`-callback.

A typicall onCreate-method looks like this.

```java
public class MyActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        

        ApplicationInsights.setup(this);
        //... other initialization code ...//
        ApplicationInsights.start();
        
        // track telemetry data
        TelemetryClient client = TelemetryClient.getInstance();
        HashMap<String, String> properties = new HashMap<String, String>();
        properties.put("property1", "my custom property");
        client.trackEvent("sample event", properties);
        client.trackTrace("sample trace");
        client.trackMetric("sample metric", 3);
    }
}
```

## Automatic collection of life-cycle events ##

This only works in Android SDK version 15 and up (Ice Cream Sandwich+) and is enabled by default.
If you want to **Disable** automatic collection of life-cycle events call ```setAutoCollectionDisabled``` inbetween setup and start of Application Insights. 

```java
	ApplicationInsights.setup(this); //setup

	ApplicationInsights.setAutoCollectionDisabled(true); //disable the auto-collection
	
	ApplicationInsights.start(); //start
```

## Documentation ##

[http://microsoft.github.io/ApplicationInsights-Android/]()

## Contributing ##

**Development environment**

* Install <a href="http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html" target="_blank">JDK 1.8</a>
* Install <a href="http://developer.android.com/sdk/index.html" target="_blank">Android studio</a>
* [Get an instrumentation key](/Microsoft/ApplicationInsights-Home/wiki#getting-an-application-insights-instrumentation-key) and set it in the manifest
* Run tests from android studio
