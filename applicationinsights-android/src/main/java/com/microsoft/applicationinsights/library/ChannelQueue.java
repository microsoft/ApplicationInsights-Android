package com.microsoft.applicationinsights.library;

import android.os.AsyncTask;

import com.microsoft.applicationinsights.contracts.shared.IJsonSerializable;
import com.microsoft.applicationinsights.logging.InternalLogging;

import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * This singleton class sends data to the endpoint
 */
class ChannelQueue {

    /**
     * Logging tag for this class
     */
    private static final String TAG = "TelemetryQueue";

    /**
     * The synchronization LOCK for queueing items
     */
    private static final Object LOCK = new Object();

    /**
     * The singleton INSTANCE
     */
    public static final ChannelQueue INSTANCE = new ChannelQueue();

    /**
     * The configuration for this queue
     */
    protected final QueueConfig config;

    /**
     * The timer for this queue
     */
    protected final Timer timer;

    /**
     * The linked list for this queue
     */
    protected List<IJsonSerializable> list;

    /**
     * If true the app is crashing and data should be persisted instead of sent
     */
    protected volatile boolean isCrashing;

    protected Sender sender; //TODO Remove reference to sender from queue?

    /**
     * All tasks which have been scheduled and not cancelled
     */
    private TimerTask scheduledPersistenceTask;

    /**
     * Prevent external instantiation
     */
    protected ChannelQueue() {
        this.list = new LinkedList<IJsonSerializable>();
        this.timer = new Timer("Application Insights Sender Queue", true);
        this.config = new QueueConfig();
        this.isCrashing = false;
        this.sender = Sender.getInstance();// don't hold reference to this?
    }

    /**
     * Set the isCrashing flag
     *
     * @param isCrashing if true the app is assumed to be crashing and data will be written to disk
     */
    protected void setIsCrashing(Boolean isCrashing) {
        this.isCrashing = isCrashing;
    }

    /**
     * @return The configuration for this sender
     */
    protected QueueConfig getConfig() {
        return config;
    }

    /**
     * Adds an item to the sender queue
     *
     * @param item a telemetry item to enqueue
     * @return true if the item was successfully added to the queue
     */
    protected boolean enqueue(IJsonSerializable item) {
        // prevent invalid argument exception
        if (item == null) {
            return false;
        }

        boolean success;
        synchronized (ChannelQueue.LOCK) {
            // attempt to add the item to the queue
            success = this.list.add(item);

            if (success) {
                if ((this.list.size() >= this.config.getMaxBatchCount()) || isCrashing) {
                    // persisting if the queue is full
                    this.flush();
                } else if (this.list.size() == 1) {
                    // schedule a FlushTask if this is the first item in the queue
                    this.scheduledPersistenceTask = new TriggerPersistTask();
                    this.timer.schedule(this.scheduledPersistenceTask, this.config.getMaxBatchIntervalMs());
                }
            } else {
                InternalLogging.warn(TAG, "Unable to add item to queue");
            }
        }

        return success;
    }

    /**
     * Empties the queue and sends all items to persistence
     */
    protected void flush() {
        // cancel the scheduled persistence task if it exists
        if (this.scheduledPersistenceTask != null) {
            this.scheduledPersistenceTask.cancel();
        }

        IJsonSerializable[] data = null;
        synchronized (ChannelQueue.LOCK) {
            if (!list.isEmpty()) {
                data = new IJsonSerializable[list.size()];
                list.toArray(data);
                list.clear();

                PersistenceTask persistTask = new PersistenceTask(data);
                persistTask.execute();
            }
        }
    }

    /**
     * A task to initiate queue sendPendingData on another thread
     */
    private class TriggerPersistTask extends TimerTask {
        /**
         * The sender INSTANCE is provided to the constructor as a test hook
         *
         */
        public TriggerPersistTask() {}

        @Override
        public void run() {
            flush();
        }
    }

    /**
     * A task to initiate flushing the queue and persisting it's data
     */
    private class PersistenceTask extends AsyncTask<Void, Void, Void> {
        private IJsonSerializable[] data;
        public PersistenceTask(IJsonSerializable[] data) {
            this.data = data;
        }

        @Override
        protected Void doInBackground(Void... params) {
                if (this.data != null) {
                Persistence persistence = Persistence.getInstance();
                if (persistence != null) {
                    persistence.persist(this.data, false);
                }
            }

            return null;
        }
    }
}

