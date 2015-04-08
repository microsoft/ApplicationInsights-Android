package com.microsoft.applicationinsights.internal;

import android.os.AsyncTask;

import com.microsoft.applicationinsights.contracts.shared.IJsonSerializable;
import com.microsoft.applicationinsights.internal.logging.InternalLogging;

import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * This singleton class sends data to the endpoint
 */
public class ChannelQueue {

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
    protected final TelemetryConfig config;

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
        this.config = new TelemetryConfig();
        this.isCrashing = false;


        Sender.initialize(getConfig());
        this.sender = Sender.getInstance();
    }

    /**
     * Set the isCrashing flag
     *
     * @param isCrashing if true the app is assumed to be crashing and data will be written to disk
     */
    public void setIsCrashing(Boolean isCrashing) {
        this.isCrashing = isCrashing;
    }

    /**
     * @return The configuration for this sender
     */
    public TelemetryConfig getConfig() {
        return config;
    }

    /**
     * Adds an item to the sender queue
     *
     * @param item a telemetry item to enqueue
     * @return true if the item was successfully added to the queue
     */
    public boolean enqueue(IJsonSerializable item) {
        // prevent invalid argument exception
        if (item == null || this.config.isTelemetryDisabled()) {
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
                    this.scheduledPersistenceTask = new TriggerPersistTask(this);
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
    public void flush() {
        PersistenceTask persistTask = new PersistenceTask(this);
        persistTask.execute();

        // asynchronously sendPendingData the queue with a non-daemon thread
        // if this was called from the timer task it would inherit the daemon status
        // since this thread does I/O it must not be a daemon thread
        //Thread flushThread = new Thread(new TimedPersistenceTask(this));
        //flushThread.setDaemon(false);
        //flushThread.start();


        // cancel the scheduled persistence task if it exists
        if (this.scheduledPersistenceTask != null) {
            this.scheduledPersistenceTask.cancel();
        }
    }

    /**
     * A task to initiate queue sendPendingData on another thread
     */
    private class TriggerPersistTask extends TimerTask {
        private ChannelQueue queue;

        /**
         * The sender INSTANCE is provided to the constructor as a test hook
         *
         * @param queue the sender INSTANCE which willbe flushed
         */
        public TriggerPersistTask(ChannelQueue queue) {
            this.queue = queue;
        }

        @Override
        public void run() {
            this.queue.flush();
        }
    }

    /**
     * A task to initiate flushing the queue and persisting it's data
     */
    //private class PersistTask extends TimerTask {

    private class PersistenceTask extends AsyncTask<Void, Void, Void> {

        private ChannelQueue queue;

        /**
         *
         * @param queue  the queue for this task
         */
        public PersistenceTask(ChannelQueue queue) {
            this.queue = queue;
        }

        @Override
         //     public void run() {
        protected Void doInBackground(Void... params) {

            IJsonSerializable[] data = null;
            synchronized (ChannelQueue.LOCK) {
                // enqueue if more than one item is in the queue
                if (!this.queue.list.isEmpty()) {
                    data = new IJsonSerializable[this.queue.list.size()];
                    this.queue.list.toArray(data);
                    this.queue.list.clear();
                }
            }

            if (data != null) {
                // flush the queue
                Persistence persistence = Persistence.getInstance();
                if (persistence != null) {
                    persistence.persist(data, false);
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            return ;
        } //TODO do we want do something here?!

    }


}

