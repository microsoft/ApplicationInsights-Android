

## 1.0-beta.8

* added CHANGELOG.md
* Deprecate `track`-Methods of `TelemetryClient`that have a `duration`-parameter
* The `TelemetryContext`object is now completely exposed for customization – this includes the `User` object.
* Common Properties can be changed after calling `ApplicationInsights.start()`
* Some more cleanup
* `AutoCollection` is now initialized by `TelemetryClient`
* Improved threat-safety for `AutoCollection`
* Added spec to run very simple automated UI tests on an Appium device grid (requires additional configuration in case you want to use it)
* Rename `ApplicationInsightsConfig`to `Configuration` – the former class has been deprecated.

## 1.0-beta.7

* [BUGFIX] Fixed bug where new user was created instead of loaded existing user from preferences.
* [BUGFIX] Fixed `NotSerializableException` when using the `track(ITelemetry)`-method.
* Updated contract files
* Deprecated track(..)-methods to align our API with other SDKs.
* Removed previously deprecated methods to set a custom `userID`
* Small cleanups
* Previously deprecated method to set custom `userID` have been removed, use `ApplicationInsights.setCustomUserContext(User user)`instead.

## 1.0-beta.6
* Integrated support for CLL channel
* Improvements related to our new Xamarin SDK
* Improved handling for user properties
* [BUGFIX] Fixed bug for session management when starting an activity
* Data will be now sent to the server using json-x-streaming
* Small cleanups
* Previously deprecated `LifecycleTracking` has been removed, use `AutoCollection` instead.

## 1.0-beta.5

* The SDK is now built using the Android Tools Gradle plugin 1.2.3
* Fix a null pointer exception in LifecycleTracking#43
* Refactored Autocollection – LifecycleTrackinghas been deprecated #51
* Fix for null pointer exceptions when trying to serialize null #45
* Fix for Concurrent Modification Exception in case the same Telemetry-Object was after it was modified #44
* Fix for ClassNotFoundException when running the SDK on an Android 2.3 device #48
* Fix a bug that was introduced in 1.0-beta.4 that caused crashes not to be sent under some circumstances #52 & e3b51e7927f238cc123c50b654fbeab448ba6df6
* Two previously deprecated setup-methods for `ApplicationInsights` have been removed.
* ```LifecycleTracking```has been deprecated, use ```AutoCollection```instead. 

## 1.0-beta.4
* Improvements regarding threat safety
* Improved unit tests (now using Mockito)
* Simplified threading model (still deferring work to background tasks)
* Bugfix for sending logic (number of running operations wasn't decremented when we don't have a connection)
* Fix for potential memory leaks
* Updated code in sample app
* Data is now persisted when the user sends the app into the background (requires API level 14)
* Data is now persisted when the device is low on memory
* Two setup-methods for ```ApplicationInsights```have been deprecated and will be removed in the next beta

## 1.0-beta.3

* Configuration of the Application Insights SDK is now done using ```ApplicationInsightsConfig```. The previous config-classes have been removed

## 1.0-beta.2

* To enable automatic lifecycle-tracking, Application Insights has to be set up with an instance of Application (see [Life-cycle tracking] (#2)), otherwise, lifecycle-tracking is disabled.

## 1.0-beta.1
* Renamed umbrella class for setting up and starting the SDK to ApplicationInsights
* Developer Mode for improved logging and shorter default interval and batch size for sending telemetry
* Exception tracking and telemetry are now enabled by default
* Source compatibility with Java 6
* Performance improvements and bug fixes
* Setup and start of the Application Insights SDK are now done using the new umbrella class `ApplicationInsights` instead of `AppInsights `