package com.microsoft.applicationinsights.library;

import com.microsoft.applicationinsights.contracts.Envelope;
import com.microsoft.applicationinsights.contracts.Internal;
import com.microsoft.applicationinsights.library.config.IQueueConfig;
import com.microsoft.applicationinsights.logging.InternalLogging;

import java.io.IOException;
import java.io.StringWriter;

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
    protected void synchronize() {
        this.queue.flush();
        if(Sender.getInstance() != null) {
            Sender.getInstance().sendNextFile();
        }
    }

    /**
     * Records the passed in data.
     *
     * @param envelope the envelope object to record
     */
    protected void enqueue(Envelope envelope) {
        String serializedData = this.serializeEnvelope(envelope);

        // enqueue to queue
        queue.enqueue(serializedData);

        InternalLogging.info(TAG, "enqueued telemetry", envelope.getName());
    }

    protected String serializeEnvelope(Envelope envelope) {
        try {
            if (envelope != null) {
                StringWriter stringWriter = new StringWriter();
                envelope.serialize(stringWriter);
                return stringWriter.toString();
            }
            InternalLogging.warn(TAG, "Envelop wasn't empty but failed to serialize anything, returning null");
            return null;
        } catch (IOException e) {
            InternalLogging.warn(TAG, "Failed to save data with exception: " + e.toString());
            return null;
        }
    }

    protected void processUnhandledException(Envelope envelope) {
        queue.isCrashing = true;
        queue.flush();

        String[] data = new String[1];
        data[0] = serializeEnvelope(envelope);

        if (this.persistence != null) {
            InternalLogging.info(TAG, "persisting crash", envelope.toString());
            this.persistence.persist(data, true);
        } else {
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
