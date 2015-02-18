# Application Insights for Android

This project provides an Android SDK for Application Insights. [Application Insights](http://azure.microsoft.com/en-us/services/application-insights/) is a service that allows developers to keep their applications available, performing, and succeeding. This module allows you to send telemetry of various kinds (events, traces, exceptions, etc.) to the Application Insights service where your data can be visualized in the Azure Portal.




## Setup ##


**Add the repository and compile dependency**

Top-level build file
```java
allprojects {
    repositories {
        jcenter()
        maven {
            url 'https://dl.bintray.com/appinsights-android/maven'
        }
    }
}
```

Per-module
```java
dependencies {
    compile 'com.microsoft.azure:applicationinsights-android:+'
}
```

**Configure the instrumentation key and add permissions**

>Please see the "[Getting an Application Insights Instrumentation Key](https://github.com/Microsoft/AppInsights-Home/wiki#getting-an-application-insights-instrumentation-key)" section of the wiki for more information on acquiring a key.

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




## Usage ##

```java
import com.microsoft.applicationinsights.TelemetryClient;
```
```java
public class MyActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //... other initialization code ...//

        TelemetryClient client = TelemetryClient.getInstance(this);
        client.trackTrace("example trace");
        client.trackEvent("example event");
        client.trackException(new Exception("example error"));
        client.trackMetric("example metric", 1);
    }
}
```

## Automatic collection of life-cycle events ##

> Note: this only works in Android SDK version 15 and up (Ice Cream Sandwich+)

**Extend Application and register for life cycle callbacks**

MyApplication.java
```java
import com.microsoft.applicationinsights.ApplicationLifeCycleEventTracking;
```
```java
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            registerActivityLifecycleCallbacks(LifeCycleTracking.getInstance());
        }
    }
}
```
AndroidManifest.xml
```xml
<manifest>
    <application android:name="MyApplication"></application>
</manifest>
```


## Documentation ##

http://microsoft.github.io/AppInsights-Android/



## Contributing ##


**Development environment**

* Install <a href="http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html" target="_blank">JDK 1.8</a>
* Install <a href="http://developer.android.com/sdk/index.html" target="_blank">Android studio</a>
* [Get an instrumentation key](/Microsoft/AppInsights-Home/wiki#getting-an-application-insights-instrumentation-key) and set it in the manifest
* Run tests from android studio
