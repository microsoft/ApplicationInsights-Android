package com.microsoft.applicationinsights.channel;

import com.microsoft.applicationinsights.channel.contracts.shared.IJsonSerializable;
import com.microsoft.applicationinsights.channel.logging.InternalLogging;

import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * This singleton class sends data to the endpoint
 */
public class TelemetryQueue {

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
    public static final TelemetryQueue INSTANCE = new TelemetryQueue();

    /**
     * The configuration for this queue
     */
    protected final TelemetryQueueConfig config;

    /**
     * The timer for this queue
     */
    protected final Timer timer;

    /**
     * The linked list for this queue
     */
    protected List<IJsonSerializable> list;

    /**
     * The sender for this queue
     */
    protected Sender sender;

    /**
     * If true the app is crashing and data should be persisted instead of sent
     */
    protected volatile boolean isCrashing;

    /**
     * All tasks which have been scheduled and not cancelled
     */
    private TimerTask sendTask;

    /**
     * Prevent external instantiation
     */
    protected TelemetryQueue() {
        this.list = new LinkedList<IJsonSerializable>();
        this.timer = new Timer("Application Insights Sender Queue", true);
        this.config = new TelemetryQueueConfig();
        this.sender = new Sender(this.config);
        this.isCrashing = false;
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
    public TelemetryQueueConfig getConfig() {
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
        if (item == null || this.config.isTelemetryDisabled()) //TODO what about crashes?!
            return false;

        boolean success;
        synchronized (TelemetryQueue.LOCK) {
            // attempt to add the item to the queue
            success = this.list.add(item);

            if (success) {
                if ((this.list.size() >= this.config.getMaxBatchCount()) || isCrashing) {
                    // flush if the queue is full
                    this.flush();
                } else if (this.list.size() == 1) {
                    // schedule a FlushTask if this is the first item in the queue
                    this.sendTask = new FlushTask(this);
                    this.timer.schedule(this.sendTask, this.config.getMaxBatchIntervalMs());
                }
            } else {
                InternalLogging.warn(TAG, "Unable to add item to queue");
            }
        }

        return success;
    }

    /**
     * Empties the queue and sends all items to the endpoint
     */
    public void flush() {

        // asynchronously flush the queue with a non-daemon thread
        // if this was called from the timer task it would inherit the daemon status
        // since this thread does I/O it must not be a daemon thread
        Thread flushThread = new Thread(new SendTask(this, this.sender));
        flushThread.setDaemon(false);
        flushThread.start();

        // cancel the scheduled enqueue task if it exists
        if (this.sendTask != null) {
            this.sendTask.cancel();
        }
    }

    /**
     * A task to initiate queue flush on another thread
     */
    private class FlushTask extends TimerTask {
        private TelemetryQueue queue;

        /**
         * The sender INSTANCE is provided to the constructor as a test hook
         *
         * @param queue the sender INSTANCE which will be flushed
         */
        public FlushTask(TelemetryQueue queue) {
            this.queue = queue;
        }

        @Override
        public void run() {
            this.queue.flush();
        }
    }

    /**
     * A task to initiate network I/O on another thread
     */
    private class SendTask extends TimerTask {
        private TelemetryQueue queue;
        private Sender sender;

        /**
         * The sender INSTANCE is provided to the constructor as a test hook
         *
         * @param queue  the queue for this task
         * @param sender the sender for this task
         */
        public SendTask(TelemetryQueue queue, Sender sender) {
            this.queue = queue;
            this.sender = sender;
        }

        @Override
        public void run() {
            IJsonSerializable[] data = null;
            synchronized (TelemetryQueue.LOCK) {
                // enqueue if more than one item is in the queue
                if (!this.queue.list.isEmpty()) {
                    data = new IJsonSerializable[this.queue.list.size()];
                    this.queue.list.toArray(data);
                    this.queue.list.clear();
                }
            }

            if (data != null) {
                if (this.queue.isCrashing) {
                    // persist the queue if the app is crashing
                    Persistence persistence = Persistence.getInstance();
                    if (persistence != null) {
                        persistence.persist(data);
                    }
                } else {
                    // otherwise enqueue data
                    this.sender.send(data);
                }
            }
        }
    }
}

