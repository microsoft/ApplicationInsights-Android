//package com.microsoft.applicationinsights.library;
//
//import android.test.InstrumentationTestCase;
//
//import com.microsoft.applicationinsights.contracts.Envelope;
//import com.microsoft.applicationinsights.contracts.EventData;
//import com.microsoft.applicationinsights.contracts.shared.ITelemetry;
//
//import junit.framework.Assert;
//
//import java.util.HashMap;
//
//import static org.mockito.Mockito.*;
//
//import static org.mockito.Matchers.any;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.timeout;
//import static org.mockito.Mockito.times;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.verifyNoMoreInteractions;
//import static org.mockito.Mockito.when;
//
//public class TrackDataTaskTest  extends InstrumentationTestCase {
//
//    private PublicEnvelopeFactory mockEnvelopeFactory;
//    private PublicChannel mockChannel;
//
//    public void setUp() throws Exception {
//        super.setUp();
//        System.setProperty("dexmaker.dexcache",getInstrumentation().getTargetContext().getCacheDir().getPath());
//
//        mockEnvelopeFactory = mock(PublicEnvelopeFactory.class);
//        mockChannel = mock(PublicChannel.class);
//    }
//
//    public void testInitialisationWorks(){
//
//        //Setup
//        Channel.initialize(null);
//        EnvelopeFactory.initialize(null, null);
//
//        String name = "NAME";
//        double metric = 2.0;
//        TrackDataTask.DataType type = TrackDataTask.DataType.EVENT;
//        Throwable exception = new Exception();
//        HashMap<String,String> properties = new HashMap<String,String>();
//        HashMap<String,Double> measurements = new HashMap<String,Double>();
//        EventData eventData = new EventData();
//
//        // Test & Verify
//        TrackDataTask sut = new TrackDataTask(type, name, properties, measurements);
//        Assert.assertEquals(type, sut.type);
//        Assert.assertEquals(name, sut.name);
//        Assert.assertEquals(properties, sut.properties);
//        Assert.assertEquals(measurements, sut.measurements);
//        Assert.assertNotNull(sut.channel);
//        Assert.assertNotNull(sut.envelopeFactory);
//
//        sut = new TrackDataTask(type, exception, properties);
//        Assert.assertEquals(type, sut.type);
//        Assert.assertEquals(exception, sut.exception);
//        Assert.assertEquals(properties, sut.properties);
//        Assert.assertNotNull(sut.channel);
//        Assert.assertNotNull(sut.envelopeFactory);
//
//        sut = new TrackDataTask(type, name, metric);
//        Assert.assertEquals(type, sut.type);
//        Assert.assertEquals(name, sut.name);
//        Assert.assertEquals(metric, sut.metric);
//        Assert.assertNotNull(sut.channel);
//        Assert.assertNotNull(sut.envelopeFactory);
//
//        sut = new TrackDataTask(eventData);
//        Assert.assertEquals(TrackDataTask.DataType.NONE, sut.type);
//        Assert.assertEquals(eventData, sut.telemetry);
//        Assert.assertNotNull(sut.channel);
//        Assert.assertNotNull(sut.envelopeFactory);
//
//        sut = new TrackDataTask(type);
//        Assert.assertEquals(type, sut.type);
//        Assert.assertNotNull(sut.channel);
//        Assert.assertNotNull(sut.envelopeFactory);
//    }
//
//    public void testTrackEventWorks(){
//
//        //Setup
//        String name = "NAME";
//        TrackDataTask.DataType type = TrackDataTask.DataType.EVENT;
//        HashMap<String,String> properties = new HashMap<String,String>();
//        HashMap<String,Double> measurements = new HashMap<String,Double>();
//
//        // Test
//        TrackDataTask sut = new TrackDataTask(type, name, properties, measurements);
//        setupTask(sut);
//        sut.trackEnvelope();
//
//        // Verify
//        verify(mockEnvelopeFactory, times(1)).createEventEnvelope(name, properties, measurements);
//        verify(mockChannel, times(1)).log(any(Envelope.class));
//        verifyNoMoreInteractions(mockEnvelopeFactory);
//        verifyNoMoreInteractions(mockChannel);
//    }
//
//    public void testTrackPageViewWorks(){
//
//        //Setup
//        String name = "NAME";
//        TrackDataTask.DataType type = TrackDataTask.DataType.PAGE_VIEW;
//        HashMap<String,String> properties = new HashMap<String,String>();
//        HashMap<String,Double> measurements = new HashMap<String,Double>();
//
//        // Test
//        TrackDataTask sut = new TrackDataTask(type, name, properties, measurements);
//        setupTask(sut);
//        sut.trackEnvelope();
//
//        // Verify
//        verify(mockEnvelopeFactory, times(1)).createPageViewEnvelope(name, properties, measurements);
//        verify(mockChannel, times(1)).log(any(Envelope.class));
//        verifyNoMoreInteractions(mockEnvelopeFactory);
//        verifyNoMoreInteractions(mockChannel);
//    }
//
//    public void testTrackMetricWorks(){
//
//        //Setup
//        String name = "NAME";
//        TrackDataTask.DataType type = TrackDataTask.DataType.METRIC;
//        double metric = 2.0;
//
//        // Test
//        TrackDataTask sut = new TrackDataTask(type, name, metric);
//        setupTask(sut);
//        sut.trackEnvelope();
//
//        // Verify
//        verify(mockEnvelopeFactory, times(1)).createMetricEnvelope(name, metric);
//        verify(mockChannel, times(1)).log(any(Envelope.class));
//        verifyNoMoreInteractions(mockEnvelopeFactory);
//        verifyNoMoreInteractions(mockChannel);
//    }
//
//    public void testTrackSessionWorks(){
//
//        //Setup
//        TrackDataTask.DataType type = TrackDataTask.DataType.NEW_SESSION;
//
//        // Test
//        TrackDataTask sut = new TrackDataTask(type);
//        setupTask(sut);
//        sut.trackEnvelope();
//
//        // Verify
//        verify(mockEnvelopeFactory, times(1)).createNewSessionEnvelope();
//        verify(mockChannel, times(1)).log(any(Envelope.class));
//        verifyNoMoreInteractions(mockEnvelopeFactory);
//        verifyNoMoreInteractions(mockChannel);
//    }
//
//    public void testTrackTraceWorks(){
//
//        //Setup
//        String name = "NAME";
//        TrackDataTask.DataType type = TrackDataTask.DataType.TRACE;
//        HashMap<String,String> properties = new HashMap<String,String>();
//
//        // Test
//        TrackDataTask sut = new TrackDataTask(type, name, properties, null);
//        setupTask(sut);
//        sut.trackEnvelope();
//
//        // Verify
//        verify(mockEnvelopeFactory, times(1)).createTraceEnvelope(name, properties);
//        verify(mockChannel, times(1)).log(any(Envelope.class));
//        verifyNoMoreInteractions(mockEnvelopeFactory);
//        verifyNoMoreInteractions(mockChannel);
//
//    }
//
//    public void testTrackHandledExceptionWorks(){
//
//        //Setup
//        Throwable exception = new Exception();
//        TrackDataTask.DataType type = TrackDataTask.DataType.HANDLED_EXCEPTION;
//        HashMap<String,String> properties = new HashMap<String,String>();
//
//        // Test
//        TrackDataTask sut = new TrackDataTask(type, exception, properties);
//        setupTask(sut);
//        sut.trackEnvelope();
//
//        // Verify
//        timeout(100);
//        verify(mockEnvelopeFactory, times(1)).createExceptionEnvelope(exception, properties);
//        verify(mockChannel, times(1)).log(any(Envelope.class));
//        verifyNoMoreInteractions(mockEnvelopeFactory);
//        verifyNoMoreInteractions(mockChannel);
//    }
//
//    public void testTrackUnandledExceptionWorks(){
//
//        //Setup
//        Throwable exception = new Exception();
//        TrackDataTask.DataType type = TrackDataTask.DataType.UNHANDLED_EXCEPTION;
//        HashMap<String,String> properties = new HashMap<String,String>();
//
//        // Test
//        TrackDataTask sut = new TrackDataTask(type, exception, properties);
//        setupTask(sut);
//        sut.trackEnvelope();
//
//        // Verify
//        timeout(100);
//        verify(mockEnvelopeFactory, times(1)).createExceptionEnvelope(exception, properties);
//        verify(mockChannel, times(1)).processException(any(Envelope.class));
//        verifyNoMoreInteractions(mockEnvelopeFactory);
//        verifyNoMoreInteractions(mockChannel);
//    }
//
//    public void testDropItemIfMembersAreNull(){
//
//        for (TrackDataTask.DataType type : TrackDataTask.DataType.values()) {
//
//            if(type != TrackDataTask.DataType.NEW_SESSION){
//                // Test
//                TrackDataTask sut = new TrackDataTask(type, null, null, null);
//                setupTask(sut);
//                sut.trackEnvelope();
//
//
//                // Verify
//                verifyNoMoreInteractions(mockEnvelopeFactory);
//                verifyNoMoreInteractions(mockChannel);
//            }
//        }
//    }
//
//    // Helper
//
//    private void setupTask(TrackDataTask task){
//        task.setChannel(mockChannel);
//        when(mockEnvelopeFactory.createEnvelope(any(ITelemetry.class))).thenReturn(new Envelope());
//        when(mockEnvelopeFactory.createEventEnvelope(any(String.class), any(HashMap.class), any(HashMap.class))).thenReturn(new Envelope());
//        when(mockEnvelopeFactory.createPageViewEnvelope(any(String.class), any(HashMap.class), any(HashMap.class))).thenReturn(new Envelope());
//        when(mockEnvelopeFactory.createMetricEnvelope(any(String.class), anyDouble())).thenReturn(new Envelope());
//        when(mockEnvelopeFactory.createTraceEnvelope(any(String.class),any(HashMap.class))).thenReturn(new Envelope());
//        when(mockEnvelopeFactory.createNewSessionEnvelope()).thenReturn(new Envelope());
//        when(mockEnvelopeFactory.createExceptionEnvelope(any(Throwable.class), any(HashMap.class))).thenReturn(new Envelope());
//        task.setEnvelopeFactory(mockEnvelopeFactory);
//    }
//}
