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
        sut.synchronize(true);

        // Verify
        verify(mockQueue, times(1)).flush(true);
    }

    public void testEnqueuedItemIsAddedToQueue(){
        // Test
        Envelope testItem1 = new Envelope();
        sut.enqueue(testItem1);
        Envelope testItem2 = new Envelope();
        sut.enqueue(testItem2);

        // Verify
        verify(mockQueue, times(1)).enqueue(testItem1);
        verify(mockQueue, times(1)).enqueue(testItem2);
    }

    public void testProcessUnhandledExceptionIsPersistedDirectly(){
        // Test
        Envelope testItem1 = new Envelope();
        sut.processUnhandledException(testItem1);

        // Verify
        verify(mockQueue, times(0)).enqueue(testItem1);
        verify(mockPersistence, times(1)).persist(any(IJsonSerializable[].class), eq(true), anyBoolean());
    }

    public void testQueueFlushesWhenProcessingCrash(){
        // Setup
        Envelope testItem1 = new Envelope();

        // Test
        sut.processUnhandledException(testItem1);

        // Verify
        verify(mockQueue, times(0)).enqueue(testItem1);
        verify(mockQueue, times(1)).flush(true);
    }
}
