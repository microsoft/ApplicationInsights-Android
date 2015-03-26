package com.microsoft.applicationinsights.channel;

import android.util.Log;

import com.microsoft.applicationinsights.channel.contracts.shared.IJsonSerializable;

import java.io.IOException;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * This singleton class sends data to the endpoint
 */
public class TelemetryQueue {

    private static final String TAG = "TelemetryQueue";

    /**
     * The singleton instance
     */
    public final static TelemetryQueue instance = new TelemetryQueue();

    /**
     * The synchronization lock for queueing items
     */
    private static final Object lock = new Object();

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
    protected LinkedList<IJsonSerializable> linkedList;

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
        this.linkedList = new LinkedList<IJsonSerializable>();
        this.timer = new Timer("Application Insights Sender Queue", true);
        this.config = new TelemetryQueueConfig();
        this.sender = new Sender(this.config);
        this.isCrashing = false;
    }

    /**
     * Set the isCrashing flag
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
     * Saves the queue to disk. This will be necessary in case we have an (unhandled) exception and
     * the app crashes
     */
    public void persist() {
        if(this.linkedList.size() > 0 ) {
            IJsonSerializable[] data = new IJsonSerializable[this.linkedList.size()];
            this.linkedList.toArray(data);
            this.linkedList.clear();

            StringBuilder buffer = new StringBuilder();

            try {
                buffer.append('[');
                for (int i = 0; i < data.length; i++) {
                    if (i > 0) {
                        buffer.append(',');
                    }
                    StringWriter stringWriter = new StringWriter();
                    data[i].serialize(stringWriter);
                    buffer.append(stringWriter.toString());
                }

                buffer.append(']');
                String serializedData = buffer.toString();
                Log.v(TAG, serializedData);

                Persistence persistence = Persistence.getInstance();
                //persistence.saveData(serializedData);
            } catch (IOException e) {
                InternalLogging._error(TAG, e.toString());
            }

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
                if(this.queue.isCrashing) {
                    // persist the queue if the app is crashing
                    Persistence persistence = Persistence.getInstance();
                    if(persistence != null) {
                        persistence.persist(data);
                    }
                } else {
                    // otherwise send data
                    this.sender.send(data);
                }
            }
        }
    }
}

