package com.microsoft.applicationinsights.channel;

import android.util.Log;

import com.microsoft.applicationinsights.channel.contracts.shared.IJsonSerializable;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.xml.transform.stream.StreamResult;

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
    private LinkedBlockingQueue<IJsonSerializable> queue;

    /**
     * The timer for this sender
     */
    private Timer timer;

    /**
     * The timer task for sending data
     */

    /**
     * Prevent external instantiation
     */
    protected Sender() {
        this.queue = new LinkedBlockingQueue<IJsonSerializable>();
        this.timer = new Timer("Application Insights Sender Queue", false);
        this.timer.scheduleAtFixedRate(this.getSenderTask(), new Date(), Sender.maxBatchIntervalMs);
    }

    /**
     * Adds an item to the sender queue
     * @param item a telemetry item to send
     * @return true if the item was successfully added to the queue
     */
    public boolean queue(IJsonSerializable item){
        // prevent invalid argument exception
        if(item == null)
            return false;

        // attempt to add the item to the queue
        boolean success = this.queue.add(item);

        // flush if the queue is full
        if(this.queue.size() >= Sender.maxBatchCount) {
            this.flush();
        }

        return success;
    }

    /**
     * Empties the queue and sends all items to the endpoint
     */
    public void flush() {
        this.timer.schedule(this.getSenderTask(), 1);
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

            writer = this.GetWriter(connection);
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
            Logging.Warn(TAG, e.toString());
        } catch (ProtocolException e) {
            Logging.Warn(TAG, e.toString());
        } catch (JSONException e) {
            Logging.Warn(TAG, e.toString());
        } catch (IOException e) {
            Logging.Warn(TAG, e.toString());
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    Logging.Warn(TAG, e.toString());
                }
            }
        }
    }

    protected TimerTask getSenderTask() {
        return new TimerTask() {

            @Override
            public void run() {
                // drain the queue
                List<IJsonSerializable> list = new LinkedList<IJsonSerializable>();
                Sender.instance.queue.drainTo(list);

                // send if more than one item is in the queue
                if(list.size() > 0 ) {
                    Sender.instance.send(list);
                }
            }
        };
    }

    protected void onResponse(HttpURLConnection connection, int responseCode) {
        BufferedReader reader = null;
        try {

            if ((responseCode < 200)
                    || (responseCode >= 300 && responseCode < 400)
                    || (responseCode > 500 && responseCode != 529)) {
                String message = String.format("Unexpected response code: %d", responseCode);
                Logging.Warn(Sender.TAG, message);
            }

            // If it isn't the usual success code (200), log the response from the server.
            if (responseCode != 200) {
                InputStream inputStream = connection.getErrorStream();
                InputStreamReader streamReader = new InputStreamReader(inputStream, "UTF-8");
                reader = new BufferedReader(streamReader);
                String responseLine = reader.readLine();
                Logging.Warn(TAG, "Error response:");
                while (responseLine != null) {
                    Logging.Warn(TAG, responseLine);
                    responseLine = reader.readLine();
                }
            }
        } catch (IOException e) {
            Logging.Warn(TAG, e.toString());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Logging.Warn(TAG, e.toString());
                }
            }
        }
    }
    
    protected Writer GetWriter(HttpURLConnection connection) throws IOException {
        return new OutputStreamWriter(connection.getOutputStream());
    }
}
