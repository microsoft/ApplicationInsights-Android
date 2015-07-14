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
//        // Test
//        Data<Domain> testItem1 = new Data<Domain>();
//        sut.log(testItem1, null);
//        Data<ITelemetry> testItem2 = new Data<ITelemetry>();
//        sut.log(testItem2, null);
//
//        // Verify
//        verify(mockQueue, times(1)).enqueue(testItem1);
//        verify(mockQueue, times(1)).enqueue(testItem2);
    }

    public void testProcessUnhandledExceptionIsPersistedDirectly(){
//        // Test
//        Data<Domain> testItem1 = new Data<Domain>();
//        sut.processException(testItem1);
//
//        // Verify
//        verify(mockQueue, times(0)).enqueue(testItem1);
//        verify(mockPersistence, times(1)).persist(any(IJsonSerializable[].class), eq(true));
    }

    public void testQueueFlushesWhenProcessingCrash(){
//        // Setup
//        Data<Domain> testItem1 = new Data<Domain>();
//
//        // Test
//        sut.processException(testItem1);
//
//        // Verify
//        verify(mockQueue, times(0)).enqueue(testItem1);
//        verify(mockQueue, times(1)).flush();
    }
}
