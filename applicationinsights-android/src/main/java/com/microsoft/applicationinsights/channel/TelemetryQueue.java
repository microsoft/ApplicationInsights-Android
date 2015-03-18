package com.microsoft.applicationinsights.channel;

import com.microsoft.applicationinsights.channel.contracts.shared.IJsonSerializable;

import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * This singleton class sends data to the endpoint
 */
public class TelemetryQueue {

    /**
     * The singleton instance
     */
    public final static TelemetryQueue instance = new TelemetryQueue();

    /**
     * The synchronization lock for queueing items
     */
    private static final Object lock = new Object();

    /**
     * The linked list for this queue
     */
    protected LinkedList<IJsonSerializable> linkedList;

    /**
     * The configuration for this queue
     */
    protected final TelemetryQueueConfig config;

    /**
     * The timer for this queue
     */
    protected final Timer timer;

    /**
     * The sender for this queue
     */
    protected Sender sender;

    /**
     * All tasks which have been scheduled and not cancelled
     */
    private TimerTask sendTask;

    /**
     * Prevent external instantiation
     */
    protected TelemetryQueue() {
        this.linkedList = new LinkedList<IJsonSerializable>();
        this.timer = new Timer("Application Insights Sender Queue", true);
        this.config = new TelemetryQueueConfig();
        this.sender = new Sender(this.config);
    }

    /**
     * @return The configuration for this sender
     */
    public TelemetryQueueConfig getConfig() {
        return config;
    }

    /**
     * Adds an item to the sender queue
     * @param item a telemetry item to send
     * @return true if the item was successfully added to the queue
     */
    public boolean enqueue(IJsonSerializable item) {
        // prevent invalid argument exception
        if(item == null || this.config.isTelemetryDisabled())
            return false;

        boolean success;
        synchronized (TelemetryQueue.lock) {
            // attempt to add the item to the queue
            success = this.linkedList.add(item);

            if (success) {
                if (this.linkedList.size() >= this.config.getMaxBatchCount()) {
                    // flush if the queue is full
                    this.flush();
                } else if (this.linkedList.size() == 1) {
                    // schedule a FlushTask if this is the first item in the queue
                    this.sendTask = new FlushTask(this);
                    this.timer.schedule(this.sendTask, this.config.getMaxBatchIntervalMs());
                }
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

        // cancel the scheduled send task if it exists
        if(this.sendTask != null) {
            this.sendTask.cancel();
        }
    }

    /**
     * A task to initiate queue flush on another thread
     */
    private class FlushTask extends TimerTask {
        private TelemetryQueue queue;

        /**
         * The sender instance is provided to the constructor as a test hook
         * @param queue the sender instance which will be flushed
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
         * The sender instance is provided to the constructor as a test hook
         * @param queue the queue for this task
         * @param sender the sender for this task
         */
        public SendTask(TelemetryQueue queue, Sender sender) {
            this.queue = queue;
            this.sender = sender;
        }

        @Override
        public void run() {
            IJsonSerializable[] data = null;
            synchronized (TelemetryQueue.lock) {
                // send if more than one item is in the queue
                if(this.queue.linkedList.size() > 0 ) {
                    data = new IJsonSerializable[this.queue.linkedList.size()];
                    this.queue.linkedList.toArray(data);
                    this.queue.linkedList.clear();
                }
            }

            if(data != null) {
                this.sender.send(data);
            }
        }
    }
}

