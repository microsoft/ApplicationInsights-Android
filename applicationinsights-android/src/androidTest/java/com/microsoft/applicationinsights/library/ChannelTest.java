package com.microsoft.applicationinsights.library;

import android.test.InstrumentationTestCase;

import com.microsoft.applicationinsights.contracts.Envelope;
import com.microsoft.applicationinsights.contracts.shared.IJsonSerializable;

import static org.mockito.Mockito.*;

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
        // Test
        Envelope testItem1 = new Envelope();
        testItem1.setDeviceId("Test");
        String serialized1 = sut.serializeEnvelope(testItem1);
        sut.enqueue(testItem1);
        Envelope testItem2 = new Envelope();
        testItem2.setDeviceId("Test1");
        String serialized2 = sut.serializeEnvelope(testItem2);
        sut.enqueue(testItem2);

        // Verify
        verify(mockQueue, times(1)).enqueue(serialized1);
        verify(mockQueue, times(1)).enqueue(serialized2);
    }

    public void testProcessUnhandledExceptionIsPersistedDirectly(){
        // Test
        Envelope testItem1 = new Envelope();
        sut.processUnhandledException(testItem1);

        // Verify
        verify(mockQueue, times(0)).enqueue(new String());
        verify(mockPersistence, times(1)).persist(any(String[].class), eq(true));
    }

    public void testQueueFlushesWhenProcessingCrash(){
        // Setup
        Envelope testItem1 = new Envelope();
        String serializedString = sut.serializeEnvelope(testItem1);

        // Test
        sut.processUnhandledException(testItem1);

        // Verify
        verify(mockQueue, times(0)).enqueue(serializedString);
        verify(mockQueue, times(1)).flush();
    }
}
