package com.microsoft.applicationinsights.library;

import android.test.InstrumentationTestCase;

import com.microsoft.applicationinsights.contracts.Application;
import com.microsoft.applicationinsights.contracts.Data;
import com.microsoft.applicationinsights.contracts.DataPoint;
import com.microsoft.applicationinsights.contracts.Device;
import com.microsoft.applicationinsights.contracts.Domain;
import com.microsoft.applicationinsights.contracts.Envelope;
import com.microsoft.applicationinsights.contracts.EventData;
import com.microsoft.applicationinsights.contracts.MessageData;
import com.microsoft.applicationinsights.contracts.MetricData;
import com.microsoft.applicationinsights.contracts.PageViewData;
import com.microsoft.applicationinsights.contracts.SessionState;
import com.microsoft.applicationinsights.contracts.SessionStateData;
import com.microsoft.applicationinsights.contracts.User;
import com.microsoft.applicationinsights.contracts.shared.ITelemetryData;

import junit.framework.Assert;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EnvelopeFactoryTest extends InstrumentationTestCase {

    private EnvelopeFactory sut;

    public void setUp() throws Exception {
        super.setUp();
        System.setProperty("dexmaker.dexcache",getInstrumentation().getTargetContext().getCacheDir().getPath());
        PublicTelemetryContext telemetryContext = getMockContext();
        HashMap<String,String> commonProperties = getCommonProperties();
        sut = new EnvelopeFactory(telemetryContext, commonProperties);
    }

    public void testCreateEnvelopeWorks() {
        Envelope envelope = sut.createEnvelope();
        validateEnvelopeTemplate(envelope);
    }


    public void testCreateEventEnvelope() {
        String expectedName = "EVENT";
        Envelope envelope = sut.createEnvelope(sut.createEventData(expectedName, getCustomProperties(), getMeasurements()));

        // Validate
        validateEnvelopeTemplate(envelope);
        Assert.assertNotNull(envelope.getData());
        validateEnvelopeProperties(envelope);
        validateEnvelopeMeasurements(envelope);

        String actualName = ((EventData)((Data<ITelemetryData>)envelope.getData()).getBaseData()).getName();
        Assert.assertEquals(expectedName, actualName);

        String actualBaseType = envelope.getData().getBaseType();
        Assert.assertEquals(new EventData().getBaseType(), actualBaseType);
    }

    public void testTraceEnvelope() {
        String expectedName = "TRACE";
        Envelope envelope = sut.createEnvelope(sut.createTraceData(expectedName, getCustomProperties()));

        // Validate
        validateEnvelopeTemplate(envelope);
        Assert.assertNotNull(envelope.getData());
        validateEnvelopeProperties(envelope);

        String actualName = ((MessageData)((Data<ITelemetryData>)envelope.getData()).getBaseData()).getMessage();
        Assert.assertEquals(expectedName, actualName);

        String actualBaseType = envelope.getData().getBaseType();
        Assert.assertEquals(new MessageData().getBaseType(), actualBaseType);
    }

    public void testPageViewEnvelope() {
        String expectedName = "PAGEVIEW";
        Envelope envelope = sut.createEnvelope(sut.createPageViewData(expectedName, getCustomProperties(), getMeasurements()));

        // Validate
        validateEnvelopeTemplate(envelope);
        Assert.assertNotNull(envelope.getData());
        validateEnvelopeProperties(envelope);
        validateEnvelopeMeasurements(envelope);

        String actualName = ((PageViewData)((Data<ITelemetryData>)envelope.getData()).getBaseData()).getName();
        Assert.assertEquals(expectedName, actualName);

        String actualBaseType = envelope.getData().getBaseType();
        Assert.assertEquals(new PageViewData().getBaseType(), actualBaseType);
    }

    public void testCreateSessionEnvelope() {
        Envelope envelope = sut.createEnvelope(sut.createNewSessionData());

        // Validate
        validateEnvelopeTemplate(envelope);
        Assert.assertNotNull(envelope.getData());

        int actualState = ((SessionStateData)((Data<ITelemetryData>)envelope.getData()).getBaseData()).getState();
        Assert.assertEquals(SessionState.Start, actualState);

        String actualBaseType = envelope.getData().getBaseType();
        Assert.assertEquals(new SessionStateData().getBaseType(), actualBaseType);
    }

    public void testCreateITelemetryEnvelope() {
        String expectedName = "EVENT";
        EventData eventData = new EventData();
        eventData.setName(expectedName);
        eventData.setMeasurements(getMeasurements());
        eventData.setProperties(getCustomProperties());

        Envelope envelope = sut.createEnvelope(sut.createData(eventData));

        // Validate
        validateEnvelopeTemplate(envelope);
        Assert.assertNotNull(envelope.getData());
        validateEnvelopeProperties(envelope);
        validateEnvelopeMeasurements(envelope);

        String actualName = ((EventData)((Data<ITelemetryData>)envelope.getData()).getBaseData()).getName();
        Assert.assertEquals(expectedName, actualName);

        String actualBaseType = envelope.getData().getBaseType();
        Assert.assertEquals(new EventData().getBaseType(), actualBaseType);
    }

    public void testCreateMetricEnvelope() {
        String expectedName = "METRIC";
        double expectedValue = 2.0;
        Envelope envelope = sut.createEnvelope(sut.createMetricData(expectedName, expectedValue));

        // Validate
        validateEnvelopeTemplate(envelope);
        Assert.assertNotNull(envelope.getData());

        MetricData metricData = ((MetricData)((Data<ITelemetryData>)envelope.getData()).getBaseData());
        List<DataPoint> actualMetrics = metricData.getMetrics();
        Assert.assertEquals(1, actualMetrics.size());
        Assert.assertEquals(expectedName, actualMetrics.get(0).getName());
        Assert.assertEquals(expectedValue, actualMetrics.get(0).getMax());

        String actualBaseType = envelope.getData().getBaseType();
        Assert.assertEquals(new MetricData().getBaseType(), actualBaseType);
    }

    public void testExceptionEnvelope() {
        // TODO: Add test
    }

    // TestHelper

    private void validateEnvelopeProperties(Envelope envelope){
        Map<String,String> actualProperties = null;
        Domain baseData = (Domain)((Data<ITelemetryData>)envelope.getData()).getBaseData();

        if(baseData instanceof EventData){
            actualProperties = ((EventData)baseData).getProperties();
        }else if (baseData instanceof MessageData){
            actualProperties = ((MessageData)baseData).getProperties();
        }

        Assert.assertEquals(2, actualProperties.size());
        Assert.assertTrue(actualProperties.containsKey(MOCK_PROPERTY_KEY));
        Assert.assertTrue(actualProperties.containsKey(MOCK_COMMON_PROPERTY_KEY));
    }

    private void validateEnvelopeMeasurements(Envelope envelope){
        Map<String,Double> actualMeasurements = null;
        Domain baseData = (Domain)((Data<ITelemetryData>)envelope.getData()).getBaseData();

        if(baseData instanceof EventData){
            actualMeasurements = ((EventData)baseData).getMeasurements();
        }else if (baseData instanceof PageViewData){
            actualMeasurements = ((PageViewData)baseData).getMeasurements();
        }

        Assert.assertEquals(1, actualMeasurements.size());
        Assert.assertTrue(actualMeasurements.containsKey(MOCK_MEASUREMENTS_KEY));
    }

    private void validateEnvelopeTemplate(Envelope envelope){
        Assert.assertNotNull(envelope);
        Assert.assertEquals(MOCK_APP_ID, envelope.getAppId());
        Assert.assertEquals(MOCK_APP_VER, envelope.getAppVer());
        Assert.assertNotNull(envelope.getTime());
        Assert.assertEquals(MOCK_IKEY, envelope.getIKey());
        Assert.assertEquals(MOCK_USER_ID, envelope.getUserId());
        Assert.assertEquals(MOCK_DEVICE_ID, envelope.getDeviceId());
        Assert.assertEquals(MOCK_OS_VER, envelope.getOsVer());
        Assert.assertEquals(MOCK_OS, envelope.getOs());
        Assert.assertNotNull(envelope.getTags());
        Assert.assertEquals(1, envelope.getTags().size());
        Assert.assertTrue(envelope.getTags().containsKey(MOCK_TAGS_KEY));
        Assert.assertEquals(MOCK_TAGS_VALUE, envelope.getTags().get(MOCK_TAGS_KEY));
    }

    private static final String MOCK_APP_ID = "appId";
    private static final String MOCK_APP_VER = "appVer";
    private static final String MOCK_IKEY = "iKey";
    private static final String MOCK_USER_ID = "userId";
    private static final String MOCK_DEVICE_ID = "deviceId";
    private static final String MOCK_OS_VER = "osVer";
    private static final String MOCK_OS = "os";
    private static final String MOCK_TAGS_KEY = "tagsKey";
    private static final String MOCK_TAGS_VALUE = "tagsValue";

    private static PublicTelemetryContext getMockContext(){
        HashMap<String,String> tags = new HashMap<String,String>();
        tags.put(MOCK_TAGS_KEY, MOCK_TAGS_VALUE);

        Application mockApplication = mock(Application.class);
        when(mockApplication.getVer()).thenReturn(MOCK_APP_VER);

        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn(MOCK_USER_ID);

        Device mockDevice = mock(Device.class);
        when(mockDevice.getId()).thenReturn(MOCK_DEVICE_ID);
        when(mockDevice.getOsVersion()).thenReturn(MOCK_OS_VER);
        when(mockDevice.getOs()).thenReturn(MOCK_OS);

        PublicTelemetryContext mockContext = mock(PublicTelemetryContext.class);
        when(mockContext.getPackageName()).thenReturn(MOCK_APP_ID);
        when(mockContext.getContextTags()).thenReturn(tags);
        when(mockContext.getApplication()).thenReturn(mockApplication);
        when(mockContext.getInstrumentationKey()).thenReturn(MOCK_IKEY);
        when(mockContext.getDevice()).thenReturn(mockDevice);
        when(mockContext.getUser()).thenReturn(mockUser);

        return mockContext;
    }

    private static final String MOCK_PROPERTY_KEY = "propertyKey";
    private static final String MOCK_PROPERTY_VALUE = "propertyValue";

    private static HashMap<String,String> getCustomProperties(){
        HashMap<String,String> properties = new HashMap<String,String>();
        properties.put(MOCK_PROPERTY_KEY, MOCK_PROPERTY_VALUE);
        return properties;
    }

    private static final String MOCK_COMMON_PROPERTY_KEY = "commonPropertyKey";
    private static final String MOCK_COMMON_PROPERTY_VALUE = "commonPropertyValue";

    private static HashMap<String,String> getCommonProperties(){
        HashMap<String,String> properties = new HashMap<String,String>();
        properties.put(MOCK_COMMON_PROPERTY_KEY, MOCK_COMMON_PROPERTY_VALUE);
        return properties;
    }

    private static final String MOCK_MEASUREMENTS_KEY = "measurementsKey";
    private static final Double MOCK_MEASUREMENTS_VALUE = new Double(11);

    private static HashMap<String,Double> getMeasurements(){
        HashMap<String,Double> measurements = new HashMap<String,Double>();
        measurements.put(MOCK_MEASUREMENTS_KEY, MOCK_MEASUREMENTS_VALUE);
        return measurements;
    }
}
