package com.microsoft.applicationinsights.internal;

import android.test.InstrumentationTestCase;
import com.microsoft.applicationinsights.contracts.Envelope;

import junit.framework.TestCase;

import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class CreateDataTaskTest extends TestCase implements CreateDataTask.OnCreateDataTaskCompletedListener{

    CountDownLatch signal;

    public void setUp() throws Exception {
        super.setUp();
        signal = new CountDownLatch(1);
    }

    public void testCreateEventWorks() throws InterruptedException {

        String propertyKey = "PropertyKey";
        String propertyValue = "PropertyValue";
        final HashMap<String, String> properties = new HashMap<String, String>();
        properties.put(propertyKey, propertyValue);

        String measurementKey = "MeasurementKey";
        Double measurementValue = new Double(3);
        final HashMap<String, Double> measurements = new HashMap<String, Double>();
        measurements.put(measurementKey, measurementValue);

        // TODO: Mock channel to determine
        new CreateDataTask(CreateDataTask.DataType.EVENT, "Event", properties, measurements, this).execute();
        this.signal.await(100, TimeUnit.SECONDS);
    }

    @Override
    public void onDataCreated(Envelope envelope) {
        assertNotNull(envelope);
        this.signal.countDown();
    }

    @Override
    public void onCreateDataFailed() {
        this.signal.countDown();
    }

}
