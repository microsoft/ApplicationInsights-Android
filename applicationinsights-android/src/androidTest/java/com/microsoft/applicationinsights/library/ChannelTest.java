package com.microsoft.applicationinsights.library;

import android.test.InstrumentationTestCase;

import com.microsoft.applicationinsights.contracts.Data;
import com.microsoft.applicationinsights.contracts.shared.IJsonSerializable;
import com.microsoft.applicationinsights.contracts.shared.ITelemetryData;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
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
        // Test
        Data<ITelemetryData> testItem1 = new Data<ITelemetryData>();
        sut.log(testItem1);
        Data<ITelemetryData> testItem2 = new Data<ITelemetryData>();
        sut.log(testItem2);

        // Verify
        verify(mockQueue, times(1)).enqueue(testItem1);
        verify(mockQueue, times(1)).enqueue(testItem2);
    }

    public void testProcessUnhandledExceptionIsPersistedDirectly(){
        // Test
        Data<ITelemetryData> testItem1 = new Data<ITelemetryData>();
        sut.processUnhandledException(testItem1);

        // Verify
        verify(mockQueue, times(0)).enqueue(testItem1);
        verify(mockPersistence, times(1)).persist(any(IJsonSerializable[].class), eq(true));
    }

    public void testQueueFlushesWhenProcessingCrash(){
        // Setup
        Data<ITelemetryData> testItem1 = new Data<ITelemetryData>();

        // Test
        sut.processUnhandledException(testItem1);

        // Verify
        verify(mockQueue, times(0)).enqueue(testItem1);
        verify(mockQueue, times(1)).flush();
    }
}
