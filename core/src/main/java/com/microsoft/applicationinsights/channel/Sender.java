package com.microsoft.applicationinsights.channel;

import com.microsoft.applicationinsights.channel.contracts.shared.IJsonSerializable;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This singleton class sends data to the endpoint
 */
public class Sender extends SenderConfig {
    /**
     * TAG for log cat.
     */
    private static final String TAG = "Sender";

    /**
     * The singleton instance
     */
    public final static Sender instance = new Sender();

    /**
     * The queue for this sender
     */
    protected LinkedBlockingQueue<IJsonSerializable> queue;

    /**
     * The timer for this sender
     */
    private Timer timer;

    /**
     * The timer task for sending data
     */
    private Object lock;

    /**
     * The internal logging adapter for this sender
     */
    private ILoggingInternal log;

    /**
     * Prevent external instantiation
     */
    protected Sender() {
        this.queue = new LinkedBlockingQueue<IJsonSerializable>();
        this.timer = new Timer("Application Insights Sender Queue", false);
        this.lock = new Object();
        this.log = log;
    }

    /**
     * Adds an item to the sender queue
     * @param item a telemetry item to send
     * @return true if the item was successfully added to the queue
     */
    public boolean queue(IJsonSerializable item) {
        // prevent invalid argument exception
        if(item == null)
            return false;

        boolean success = false;
        synchronized (this.lock) {
            // attempt to add the item to the queue
            success = this.queue.add(item);

            if (success) {
                if (this.queue.size() >= Sender.maxBatchCount) {
                    // flush if the queue is full
                    this.flush();
                } else if (this.queue.size() == 1) {
                    // schedule a batch send if this is the first item in the queue
                    this.timer.schedule(this.getSenderTask(), Sender.maxBatchIntervalMs);
                }
            }
        }

        return success;
    }

    /**
     * Empties the queue and sends all items to the endpoint
     */
    public void flush() {
        // schedule a batch send if this is the first item in the queue
        this.timer.schedule(this.getSenderTask(), 0);
    }

    /**
     * Creates a task which will send all items in the queue
     */
    protected TimerTask getSenderTask() {
        return new SendTask(this);
    }

    /**
     * Sends data to the configured URL
     * @param data a collection of serializable data
     */
    protected void send(List<IJsonSerializable> data) {
        Writer writer = null;
        try {
            URL url = new URL(SenderConfig.endpointUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setReadTimeout(10000 /* milliseconds */);
            connection.setConnectTimeout(15000 /* milliseconds */);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            connection.setDoInput(true);

            writer = this.getWriter(connection);
            writer.write('[');

            for (int i = 0; i < data.size(); i++) {
                if (i > 0) {
                    writer.write(',');
                }

                data.get(i).serialize(writer);
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
                responseBuilder.append(message + "\n");
                this.log(Sender.TAG, message);
            }

            // If it isn't the usual success code (200), log the response from the server.
            if (responseCode != 200) {
                InputStream inputStream = connection.getErrorStream();
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

            return response;
        }
    }

    /**
     * Gets a writer from the connection stream (allows for test hooks into the write stream)
     * @param connection the connection to which the stream will be flushed
     * @return a writer for the given connection stream
     * @throws IOException
     */
    protected Writer getWriter(HttpURLConnection connection) throws IOException {
        return new OutputStreamWriter(connection.getOutputStream());
    }

    /**
     * Writes a log to the provided adapter (note: the adapter must be set by the consumer)
     * @param tag the tag for this message
     * @param message the message to be logged
     */
    private void log(String tag, String message) {
        if(this.log != null){
            this.log.warn(tag, message);
        }
    }

    /**
     * Extension of TimerTask for thread-safely flushing the queue
     */
    private class SendTask extends TimerTask {
        private Sender sender;

        /**
         * A sender instance is provided as a test hook
         * @param sender the sender instance which will transmit the contents of the queue
         */
        public SendTask(Sender sender) {
            this.sender = sender;
        }

        @Override
        public void run() {
            // drain the queue
            List<IJsonSerializable> list = new LinkedList<IJsonSerializable>();
            synchronized (sender.lock) {
                sender.queue.drainTo(list);
                sender.timer.purge();
            }

            // send if more than one item is in the queue
            if(list.size() > 0 ) {
                sender.send(list);
            }
        }
    }
}

