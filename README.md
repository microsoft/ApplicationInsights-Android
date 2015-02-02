# Application Insights for Android

This project provides an Android SDK for Application Insights. [Application Insights](http://azure.microsoft.com/en-us/services/application-insights/) is a service that allows developers to keep their applications available, performing, and succeeding. This module allows you to send telemetry of various kinds (events, traces, exceptions, etc.) to the Application Insights service where your data can be visualized in the Azure Portal.




## Setup ##


**Add the repository and compile dependency**

Top-level build file:
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

Per-module:
```java
dependencies {
    compile 'com.microsoft.azure:applicationinsights-android:+'
}
```

**Configure the instrumentation key**

Please see the "[Getting an Application Insights Instrumentation Key](https://github.com/Microsoft/AppInsights-Home/wiki#getting-an-application-insights-instrumentation-key)" section of the wiki for more information. To try the SDK without an instrumentation key, set the instrumentationKey config value to a non-empty string.
>**Note**: an instrumentation key is required before any data can be viewed in the Azure portal.

Set the instrumentation key as a string resource _(somewhere under /res/values/*.xml)_
```xml
<resources>
    <item name="ai_instrumentationKey" type="string">Instrumentation_Key_Goes_Here</item>
</resources>
```

**Allow the following permissions in your AndroidManifest.xml**

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```





## Usage ##


**Initialization**
```java
import com.microsoft.applicationinsights.TelemetryClient;
```
```java
public class MyActivity extends Activity {

    private TelemetryClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //... other initialization code ...//

        client = TelemetryClient.getInstance(this);
        client.trackEvent("onCreate");
    }
}
```

**Track events/metrics/traces/exceptions**

```java
client.trackTrace("example trace");
client.trackEvent("example event");
client.trackException(new Error("example error"), "handledAt");
client.trackMetric("example metric", 1);
```

**Track page views and user sessions**
```java
@Override
public void onStart() {
    super.onStart();
    client.trackPageView("page name");
}
```
**Documentation**

http://microsoft.github.io/AppInsights-Android/



## Contributing ##


**Development environment**

* Install <a href="http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html" target="_blank">JDK 1.8</a>
* Install <a href="http://developer.android.com/sdk/index.html" target="_blank">Android studio</a>
* Run tests from android studio
