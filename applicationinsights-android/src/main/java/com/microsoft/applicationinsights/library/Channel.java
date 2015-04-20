package com.microsoft.applicationinsights.library;

import com.microsoft.applicationinsights.contracts.Envelope;
import com.microsoft.applicationinsights.contracts.shared.IJsonSerializable;
import com.microsoft.applicationinsights.library.config.QueueConfig;
import com.microsoft.applicationinsights.logging.InternalLogging;

/**
 * This class records telemetry for application insights.
 */
class Channel {
    private static final String TAG = "Channel";

    /**
     * Volatile boolean for double checked synchronize block
     */
    private static volatile boolean isChannelLoaded = false;

    /**
     * Synchronization LOCK for setting static context
     */
    private static final Object LOCK = new Object();

    /**
     * Test hook to the sender
     */
    private static ChannelQueue queue;

    /**
     * The singleton INSTANCE of this class
     */
    private static Channel instance;


    /**
     * Instantiates a new INSTANCE of Sender
     */
    protected Channel() {
    }

    protected static void initialize(QueueConfig queueConfig) {
        // note: isPersistenceLoaded must be volatile for the double-checked LOCK to work
        if (!isChannelLoaded) {
            synchronized (Channel.LOCK) {
                if (!isChannelLoaded) {
                    isChannelLoaded = true;
                    instance = new Channel();
                    instance.setQueue(new ChannelQueue(queueConfig));
                }
            }
        }
    }

    /**
     * @return the INSTANCE of persistence or null if not yet initialized
     */
    protected static Channel getInstance() {
        if (Channel.instance == null) {
            InternalLogging.error(TAG, "getInstance was called before initialization");
        }

        return Channel.instance;
    }

    protected void synchronize() {
        getQueue().flush();
    }

    /**
     * @return the sender for this channel.
     */
    protected ChannelQueue getQueue() {
        return queue;
    }

    /**
     * Test hook to set the queue for this channel
     *
     * @param queue the queue to use for this channel
     */
    protected void setQueue(ChannelQueue queue) {
        Channel.queue = queue;
    }

    /**
     * Records the passed in data.
     *
     * @param envelope the envelope object to record
     */
    protected void enqueue(Envelope envelope) {
        queue.isCrashing = false;

        // enqueue to queue
        queue.enqueue(envelope);

        InternalLogging.info(TAG, "enqueued telemetry", envelope.getName());
    }

    protected void processUnhandledException(Envelope envelope) {
        queue.isCrashing = true;
        queue.flush();

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
