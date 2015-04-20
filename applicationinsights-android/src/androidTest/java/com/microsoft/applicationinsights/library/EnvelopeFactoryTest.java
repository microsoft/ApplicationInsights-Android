package com.microsoft.applicationinsights.library;

import android.content.Intent;
import android.test.ActivityUnitTestCase;

import com.microsoft.applicationinsights.contracts.Data;
import com.microsoft.applicationinsights.contracts.Envelope;
import com.microsoft.applicationinsights.contracts.EventData;
import com.microsoft.applicationinsights.contracts.shared.ITelemetryData;

import junit.framework.Assert;

public class EnvelopeFactoryTest extends ActivityUnitTestCase<MockActivity> {

    public EnvelopeFactoryTest() {
        super(MockActivity.class);
    }

    public void setUp() throws Exception {
        super.setUp();
        Intent intent = new Intent(getInstrumentation().getTargetContext(), MockActivity.class);
        this.setActivity(this.startActivity(intent, null, null));
        TelemetryContext mockContext = new MockTelemetryContext(this.getActivity(), "testIKey", "1234");
        EnvelopeFactory.INSTANCE.configure(mockContext);
    }

    public void testCreatedEnvelopeIsInitialized() {
        Envelope envelope = EnvelopeFactory.INSTANCE.createEnvelope();
        validateEnvelopeTemplate(envelope);
    }

    public void testCreatedEnvelopeContainsTelemetryData() {

        // Set up test event
        EventData testTelemetryData = new EventData();
        testTelemetryData.setName("myEvent");

        // Create expectation
        Data<ITelemetryData> testData = new Data<ITelemetryData>();
        testData.setBaseData(testTelemetryData);
        testData.setBaseType(testTelemetryData.getBaseType());

        // Test
        Envelope envelope = EnvelopeFactory.INSTANCE.createEnvelope(testTelemetryData);

        // Validate
        validateEnvelopeTemplate(envelope);
        Assert.assertNotNull("Envelope data is not null.", envelope.getData());

        String eventName = ((EventData)((Data<ITelemetryData>)envelope.getData()).getBaseData()).getName();
        Assert.assertEquals("Envelope data has correct event name.", eventName,  testTelemetryData.getName());

        String baseType = envelope.getData().getBaseType();
        Assert.assertEquals("Envelope data has correct base type.", baseType,  testTelemetryData.getBaseType());
    }

    // TestHelper

    private void validateEnvelopeTemplate(Envelope envelope){
        Assert.assertNotNull("Envelope is not null.", envelope);
        Assert.assertNotNull("AppId is not null.", envelope.getAppId());
        Assert.assertNotNull("AppVer is not null.", envelope.getAppVer());
        Assert.assertNotNull("Time is not null.", envelope.getTime());
        Assert.assertNotNull("Ikey is not null.", envelope.getIKey());
        Assert.assertNotNull("UserId is not null.", envelope.getUserId());
        Assert.assertNotNull("DeviceId is not null.", envelope.getDeviceId());
        Assert.assertNotNull("OsVer is not null.", envelope.getOsVer());
        Assert.assertNotNull("Os is not null.", envelope.getOs());
        Assert.assertNotNull("Tags is not null.", envelope.getTags());
    }

    //TODO write more tests
}
