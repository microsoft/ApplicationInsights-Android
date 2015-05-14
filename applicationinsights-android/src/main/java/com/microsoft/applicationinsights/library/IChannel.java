package com.microsoft.applicationinsights.library;

import com.microsoft.applicationinsights.contracts.Base;

public interface IChannel {
    void log(final Base telemetry);
}
