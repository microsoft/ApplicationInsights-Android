# Application Insights for Android

This project extends the Application Insights API surface to support Android. [Application Insights](http://azure.microsoft.com/en-us/services/application-insights/) is a service that allows developers to keep their application available, performing and succeeding. This module will allow you to send telemetry of various kinds (event, trace, exception, etc.) to the Application Insights service where they can be visualized in the Azure Portal. 




## Requirements ##
**Install with gradle**
```gradle
dependencies {
    compile project(':applicationinsights')
}
```
**Get an instrumentation key**
>**Note**: an instrumentation key is required before any data can be sent. Please see the "[Getting an Application Insights Instrumentation Key](https://github.com/Microsoft/AppInsights-Home/wiki#getting-an-application-insights-instrumentation-key)" section of the wiki for more information. To try the SDK without an instrumentation key, set the instrumentationKey config value to a non-empty string.




## Usage ##
**Configuration**
```java
import com.microsoft.applicationinsights.TelemetryClient;
```
```java
String instrumentationKey = "2b240a15-4b1c-4c40-a4f0-0e8142116250";
Context context = this.getApplicationContext();
TelemetryClient client = new TelemetryClient(instrumentationKey, context);

```
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