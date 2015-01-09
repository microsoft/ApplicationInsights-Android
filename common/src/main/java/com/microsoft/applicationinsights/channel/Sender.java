package com.microsoft.applicationinsights.channel;

import com.microsoft.applicationinsights.channel.contracts.shared.IJsonSerializable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
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
     * Tag for log messages
     */
    private static final String TAG = "Sender";

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
     * Prevent external instantiation
     */
    protected Sender() {
        this.queue = new LinkedList<IJsonSerializable>();
        this.timer = new Timer("Application Insights Sender Queue", true);
        this.config = new SenderConfig();
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
        if(item == null)
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
        Writer writer = null;
        try {
            URL url = new URL(this.config.getEndpointUrl());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setReadTimeout(10000 /* milliseconds */); // todo: move to config
            connection.setConnectTimeout(15000 /* milliseconds */);
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setUseCaches(false);

            writer = this.getWriter(connection);
            writer.write('[');

            for (int i = 0; i < data.length; i++) {
                if (i > 0) {
                    writer.write(',');
                }

                data[i].serialize(writer);
            }

            writer.write(']');
            writer.flush();

            // Starts the query
            connection.connect();
            int responseCode = connection.getResponseCode();
            this.onResponse(connection, responseCode);

        } catch (MalformedURLException e) {
            this.log(TAG, e.toString());
        } catch (ProtocolException e) {
            this.log(TAG, e.toString());
        } catch (IOException e) {
            this.log(TAG, e.toString());
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    this.log(TAG, e.toString());
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
                this.log(Sender.TAG, message);
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
                    this.log(TAG, "Error response:");
                    while (responseLine != null) {
                        this.log(TAG, responseLine);
                        responseBuilder.append(responseLine);
                        responseLine = reader.readLine();
                    }

                    response = responseBuilder.toString();
                } else {
                    response = connection.getResponseMessage();
                }
            }
        } catch (IOException e) {
            this.log(TAG, e.toString());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    this.log(TAG, e.toString());
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
        connection.addRequestProperty("Content-Encoding", "gzip");
        connection.setRequestProperty("Content-Type", "application/json");
        GZIPOutputStream gzip = new GZIPOutputStream(connection.getOutputStream(), true);
        return new OutputStreamWriter(gzip);
    }

    /**
     * Writes a log to the provided adapter (note: the adapter must be set by the consumer)
     * @param tag the tag for this message
     * @param message the message to be logged
     */
    private void log(String tag, String message) {
        ILoggingInternal logger = this.config.getInternalLogger();
        if(logger != null){
            logger.warn(tag, message);
        }
    }

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

