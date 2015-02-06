package com.microsoft.applicationinsights.channel;

import android.os.Build;
import android.util.Log;

import com.microsoft.applicationinsights.channel.contracts.shared.IJsonSerializable;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.zip.GZIPOutputStream;

/**
 * This singleton class sends data to the endpoint
 */
public class Sender {

    /**
     * The singleton instance
     */
    public final static Sender instance = new Sender();

    /**
     * The synchronization lock for sending
     */
    private static final Object lock = new Object();

    /**
     * The queue for this sender
     */
    protected LinkedList<IJsonSerializable> queue;

    /**
     * The configuration for this sender
     */
    protected final SenderConfig config;

    /**
     * The timer for this sender
     */
    protected final Timer timer;

    /**
     * All tasks which have been scheduled and not cancelled
     */
    private TimerTask sendTask;

    /**
     * Saves data to disk if there is a protocol error
     */
    private Persistence persist;

    /**
     * Saves data to disk if there is a protocol error
     */
    protected String serializedData;

    /**
     * Prevent external instantiation
     */
    protected Sender() {
        this.queue = new LinkedList<IJsonSerializable>();
        this.timer = new Timer("Application Insights Sender Queue", true);
        this.config = new SenderConfig();
        this.persist = Persistence.getInstance();
    }

    /**
     * @return The configuration for this sender
     */
    public SenderConfig getConfig() {
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
        synchronized (Sender.lock) {
            // attempt to add the item to the queue
            success = this.queue.add(item);

            if (success) {
                if (this.queue.size() >= this.config.getMaxBatchCount()) {
                    // flush if the queue is full
                    this.flush();
                } else if (this.queue.size() == 1) {
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
        Thread flushThread = new Thread(new SendTask(this));
        flushThread.setDaemon(false);
        flushThread.start();

        // cancel the scheduled send task if it exists
        if(this.sendTask != null) {
            this.sendTask.cancel();
        }
    }

    /**
     * Sends data to the configured URL
     * @param data a collection of serializable data
     */
    protected void send(IJsonSerializable[] data) {
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

            // Send the persisted data
            String persistedData = this.persist.getData();
            if (persistedData != "")
            {
                sendRequestWithPayload(persistedData);
                this.persist.clearData();
            }

            // Send the new data
            serializedData = buffer.toString();
            sendRequestWithPayload(serializedData);
        } catch (IOException e) {
            this.log(e.toString());
        }
    }

    private void sendRequestWithPayload(String payload) throws IOException {
        Writer writer = null;
        URL url = new URL(this.config.getEndpointUrl());
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setReadTimeout(10000 /* milliseconds */); // todo: move to config
        connection.setConnectTimeout(15000 /* milliseconds */);
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setUseCaches(false);

        try {
            writer = this.getWriter(connection);
            writer.write(payload);
            writer.flush();

            // Starts the query
            connection.connect();
            int responseCode = connection.getResponseCode();
            this.onResponse(connection, responseCode);
        } catch (IOException e){
            this.persist.saveData(this.serializedData);
        } finally {
            if(writer != null)
            {
                try {
                    writer.close();
                } catch (IOException e) {
                    this.log(e.toString());
                }

            }
        }
    }

    /**
     * Handler for the http response from the sender
     * @param connection a connection containing a response
     * @param responseCode the response code from the connection
     * @return null if the request was successful, the server response otherwise
     */
    protected String onResponse(HttpURLConnection connection, int responseCode) {
        BufferedReader reader = null;
        String response = null;
        try {

            StringBuilder responseBuilder = new StringBuilder();

            if ((responseCode < 200)
                    || (responseCode >= 300 && responseCode < 400)
                    || (responseCode > 500 && responseCode != 529)) {
                String message = String.format("Unexpected response code: %d", responseCode);
                responseBuilder.append(message);
                responseBuilder.append("\n");
                this.log(message);
            }

            //If there was a server issue, persist the data
            if(responseCode >= 500 && responseCode != 529)
            {
                persist.saveData(this.serializedData);
            }

            // If it isn't the usual success code (200), log the response from the server.
            if (responseCode != 200) {
                InputStream inputStream = connection.getErrorStream();
                if(inputStream == null) {
                    inputStream = connection.getInputStream();
                }

                if(inputStream != null) {
                    InputStreamReader streamReader = new InputStreamReader(inputStream, "UTF-8");
                    reader = new BufferedReader(streamReader);
                    String responseLine = reader.readLine();
                    this.log("Error response:");
                    while (responseLine != null) {
                        this.log(responseLine);
                        responseBuilder.append(responseLine);
                        responseLine = reader.readLine();
                    }

                    response = responseBuilder.toString();
                } else {
                    response = connection.getResponseMessage();
                }
            }
        } catch (IOException e) {
            this.log(e.toString());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    this.log(e.toString());
                }
            }
        }

        return response;
    }

    /**
     * Gets a writer from the connection stream (allows for test hooks into the write stream)
     * @param connection the connection to which the stream will be flushed
     * @return a writer for the given connection stream
     * @throws java.io.IOException
     */
    protected Writer getWriter(HttpURLConnection connection) throws IOException {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // GZIP if we are running SDK 19 or higher
            connection.addRequestProperty("Content-Encoding", "gzip");
            connection.setRequestProperty("Content-Type", "application/json");
            GZIPOutputStream gzip = new GZIPOutputStream(connection.getOutputStream(), true);
            return new OutputStreamWriter(gzip);
        } else {
            // no GZIP for older devices
            return new OutputStreamWriter(connection.getOutputStream());
        }
    }

    /**
     * Writes a log to the provided adapter (note: the adapter must be set by the consumer)
     * @param message the message to be logged
     */
    private void log(String message) {
        InternalLogging._warn("Sender", message);
    }

    /**
     * A task to initiate queue flush on another thread
     */
    private class FlushTask extends TimerTask {
        private Sender sender;

        /**
         * The sender instance is provided to the constructor as a test hook
         * @param sender the sender instance which will be flushed
         */
        public FlushTask(Sender sender) {
            this.sender = sender;
        }

        @Override
        public void run() {
            this.sender.flush();
        }
    }

    /**
     * A task to initiate network I/O on another thread
     */
    private class SendTask extends TimerTask {
        private Sender sender;

        /**
         * The sender instance is provided to the constructor as a test hook
         * @param sender the sender instance which will transmit the contents of the queue
         */
        public SendTask(Sender sender) {
            this.sender = sender;
        }

        @Override
        public void run() {
            IJsonSerializable[] data = null;
            synchronized (Sender.lock) {
                // send if more than one item is in the queue
                if(sender.queue.size() > 0 ) {
                    data = new IJsonSerializable[sender.queue.size()];
                    sender.queue.toArray(data);
                    sender.queue.clear();
                }
            }

            if(data != null) {
                sender.send(data);
            }
        }
    }
}

