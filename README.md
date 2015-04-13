[ ![Download](https://api.bintray.com/packages/appinsights-android/maven/AppInsights-Android/images/download.svg) ](https://bintray.com/appinsights-android/maven/AppInsights-Android/_latestVersion)

# Application Insights for Android

This project provides an Android SDK for Application Insights. [Application Insights](http://azure.microsoft.com/en-us/services/application-insights/) is a service that allows developers to keep their applications available, performing, and succeeding. This module allows you to send telemetry of various kinds (events, traces, exceptions, etc.) to the Application Insights service where your data can be visualized in the Azure Portal.

## Setup ##
	
**Add a compile dependency for the SDK**

Per-module

```java
dependencies {
    compile 'com.microsoft.azure:applicationinsights-android:+'
}
```

**Configure the instrumentation key and add permissions**

Please see the "[Getting an Application Insights Instrumentation Key](https://github.com/Microsoft/AppInsights-Home/wiki#getting-an-application-insights-instrumentation-key)" section of the wiki for more information on acquiring a key.

AndroidManifest.xml

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

Top-level build file

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

It is also possible to set the instrumentation key of your app in code. This will override the one you might have set in your gradle or manifest file. Setting the instrumentation key programmatically can be done while setting up AppInsights:

```java
AppInsights.setup(this, "<YOUR-IKEY-GOES-HERE>");
AppInsights.start();
```

## Usage ##

```java
import com.microsoft.applicationinsights.TelemetryClient;
```
```java
public class MyActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        

        AppInsights.setup(this);
        //... other initialization code ...//
        AppInsights.start();
        
        // track telemetry data
        HashMap<String, String> properties = new HashMap<String, String>();
        properties.put("property1", "my custom property");
        client.trackEvent("custom event", properties);
        
        client.trackMetric("custom metric", 3);
    }
}
```

## Automatic collection of life-cycle events ##

> Note: this only works in Android SDK version 15 and up (Ice Cream Sandwich+)

**Register for life cycle callbacks**

In order to register for lifecycle events, you only have to do the following call

```java
	// track activity lifecycle / session states
    AppInsights.enableActivityTracking(this.getApplication());
```

Please note, that this will only work after `AppInsights.start()`has been called. Furthermore, the telemetry feature must not be disabled. 

## Documentation ##

http://microsoft.github.io/AppInsights-Android/

## Contributing ##

**Development environment**

* Install <a href="http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html" target="_blank">JDK 1.8</a>
* Install <a href="http://developer.android.com/sdk/index.html" target="_blank">Android studio</a>
* [Get an instrumentation key](/Microsoft/AppInsights-Home/wiki#getting-an-application-insights-instrumentation-key) and set it in the manifest
* Run tests from android studio
