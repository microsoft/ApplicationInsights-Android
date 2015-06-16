package com.microsoft.applicationinsights.library;

import android.test.InstrumentationTestCase;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class ChannelTest extends InstrumentationTestCase {

    private Channel sut;
    private PublicChannelQueue mockQueue;
    private PublicPersistence mockPersistence;

    public void setUp() throws Exception {
        super.setUp();
        System.setProperty("dexmaker.dexcache",getInstrumentation().getTargetContext().getCacheDir().getPath());
        sut = new Channel();
        mockQueue = mock(PublicChannelQueue.class);
        sut.setQueue(mockQueue);
        mockPersistence = mock(PublicPersistence.class);
        sut.setPersistence(mockPersistence);
    }

    public void testSynchronizeFlushesQueue(){
        // Test
        sut.synchronize();

        // Verify
        verify(mockQueue, times(1)).flush();
    }

    public void testEnqueuedItemIsAddedToQueue(){
        // todo: EnvelopeFactory can't be instantiated, fix this
//        // Test
//        Data<ITelemetryData> testItem1 = new Data<ITelemetryData>();
//        Envelope envelope1 = EnvelopeFactory.getInstance().createEnvelope((Data) testItem1);
//        String serializedString1 = sut.serializeEnvelope(envelope1);
//        sut.log(testItem1);
//        Data<ITelemetryData> testItem2 = new Data<ITelemetryData>();
//        Envelope envelope2 = EnvelopeFactory.getInstance().createEnvelope((Data) testItem1);
//        String serializedString2 = sut.serializeEnvelope(envelope2);
//        sut.log(testItem2);
//
//        // Verify
//        verify(mockQueue, times(1)).enqueue(serializedString1);
//        verify(mockQueue, times(1)).enqueue(serializedString2);
    }

    public void testProcessUnhandledExceptionIsPersistedDirectly(){
//        // Test
//        Data<ITelemetryData> testItem1 = new Data<ITelemetryData>();
//        sut.processUnhandledException(testItem1);
//
//        // Verify
//        verify(mockQueue, times(0)).enqueue(new String());
//        verify(mockPersistence, times(1)).persist(any(String[].class), eq(true));
    }

    public void testQueueFlushesWhenProcessingCrash(){
        // todo: EnvelopeFactory can't be instantiated, fix this
//        // Setup
//        Data<ITelemetryData> testItem1 = new Data<ITelemetryData>();
//        Envelope envelope = EnvelopeFactory.getInstance().createEnvelope((Data) testItem1);
//        String serializedString = sut.serializeEnvelope(envelope);
//
//        // Test
//        sut.processUnhandledException(testItem1);
//
//        // Verify
//        verify(mockQueue, times(0)).enqueue(serializedString);
//        verify(mockQueue, times(1)).flush();
    }
}
