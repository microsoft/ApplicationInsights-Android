package com.microsoft.applicationinsights.library;

import com.microsoft.applicationinsights.contracts.Envelope;
import com.microsoft.applicationinsights.contracts.shared.IJsonSerializable;
import com.microsoft.applicationinsights.library.config.IQueueConfig;
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
    protected ChannelQueue queue;

    /**
     * The singleton INSTANCE of this class
     */
    private static Channel instance;

    /**
     * Persistence used for saving unhandled exceptions.
     */
    private Persistence persistence;

    /**
     * Instantiates a new INSTANCE of Sender
     */
    protected Channel() {
        this.persistence = Persistence.getInstance();
    }

    protected static void initialize(IQueueConfig config) {
        // note: isPersistenceLoaded must be volatile for the double-checked LOCK to work
        if (!isChannelLoaded) {
            synchronized (Channel.LOCK) {
                if (!isChannelLoaded) {
                    isChannelLoaded = true;
                    instance = new Channel();
                    instance.setQueue(new ChannelQueue(config));
                }
            }
        }
    }

    /**
     * @return the INSTANCE of Channel or null if not yet initialized
     */
    protected static Channel getInstance() {
        if (Channel.instance == null) {
            InternalLogging.error(TAG, "getInstance was called before initialization");
        }

        return Channel.instance;
    }

    /**
     * Persist all pending items.
     */
    protected void synchronize(Boolean shouldSend) {
        InternalLogging.warn(TAG, "Flubber");
        this.queue.flush(shouldSend);
    }

    /**
     * Records the passed in data.
     *
     * @param envelope the envelope object to record
     */
    protected void enqueue(Envelope envelope) {
        // enqueue to queue
        queue.enqueue(envelope);

        InternalLogging.info(TAG, "enqueued telemetry", envelope.getName());
    }

    protected void processUnhandledException(Envelope envelope) {
        queue.isCrashing = true;
        queue.flush(true);

        IJsonSerializable[] data = new IJsonSerializable[1];
        data[0] = envelope;

        if (this.persistence != null) {
            this.persistence.persist(data, true , false);
        }
        else {
            InternalLogging.info(TAG, "error persisting crash", envelope.toString());
        }

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
     * Set the persistence instance used to save unhandled exceptions.
     *
     * @param persistence the persitence instance which should be used
     */
    protected void setPersistence(Persistence persistence) {
        this.persistence = persistence;
    }

}
