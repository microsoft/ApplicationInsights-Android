package com.microsoft.applicationinsights.internal;

import com.microsoft.applicationinsights.contracts.Envelope;
import com.microsoft.applicationinsights.contracts.shared.IJsonSerializable;
import com.microsoft.applicationinsights.internal.logging.InternalLogging;

/**
 * This class records telemetry for application insights.
 */
public class Channel {
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

    private static void initialize() {
        // note: isPersistenceLoaded must be volatile for the double-checked LOCK to work
        if (!Channel.isChannelLoaded) {
            synchronized (Channel.LOCK) {
                if (!Channel.isChannelLoaded) {
                    Channel.isChannelLoaded = true;
                    Channel.instance = new Channel();
                }
            }
        }
        queue = ChannelQueue.INSTANCE;
    }

    /**
     * @return the INSTANCE of persistence or null if not yet initialized
     */
    public static Channel getInstance() {
        initialize();
        if (Channel.instance == null) {
            InternalLogging.error(TAG, "getInstance was called before initialization");
        }

        return Channel.instance;
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
