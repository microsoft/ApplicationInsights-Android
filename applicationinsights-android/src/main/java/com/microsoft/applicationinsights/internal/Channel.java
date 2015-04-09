package com.microsoft.applicationinsights.internal;

import com.microsoft.applicationinsights.contracts.Envelope;
import com.microsoft.applicationinsights.contracts.shared.IJsonSerializable;
import com.microsoft.applicationinsights.internal.logging.InternalLogging;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class records telemetry for application insights.
 */
public enum Channel {
    INSTANCE;

    private static final String TAG = "Channel";

    /**
     * The id for this channel
     */
    private final long channelId;

    /**
     * The sequence counter for this channel
     */
    private final AtomicInteger seqCounter;

    /**
     * Test hook to the sender
     */
    private ChannelQueue queue;

    private Sender sender;

    /**
     * Instantiates a new INSTANCE of Sender
     */
    public Channel() {
        this.queue = ChannelQueue.INSTANCE;
        Random random = new Random();
        this.channelId = Math.abs(random.nextLong());
        this.seqCounter = new AtomicInteger(0);
    }

    public void synchronize() {
        getQueue().flush();
    }

    /**
     * @return the sender for this channel.
     */
    public ChannelQueue getQueue() {
        return this.queue;
    }

    /**
     * Test hook to set the queue for this channel
     *
     * @param queue the queue to use for this channel
     */
    protected void setQueue(ChannelQueue queue) {
        this.queue = queue;
    }

    /**
     * Records the passed in data.
     *
     * @param envelope the envelope object to record
     */
    public void enqueue(Envelope envelope) {
        this.queue.isCrashing = false;

        // enqueue to queue
        this.queue.enqueue(envelope);

        InternalLogging.info(TAG, "enqueued telemetry", envelope.getName());
    }

    public void processUnhandledException(Envelope envelope) {
        this.queue.isCrashing = true;
        this.queue.flush();

        IJsonSerializable[] data = new IJsonSerializable[1];
        data[0] = envelope;

        Persistence persistence = Persistence.getInstance();
        if (persistence != null) {
            persistence.persist(data, true);
        }
        else {
            InternalLogging.info(TAG, "error persisting crash", envelope.toString());
        }

    }

}
