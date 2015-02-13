package com.microsoft.commonlogging.channel;

import android.os.Build;
import android.util.Log;

import com.microsoft.commonlogging.channel.contracts.shared.IJsonSerializable;


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
     * The configuration for this sender
     */
    protected final TelemetryQueueConfig config;

    /**
     * Saves data to disk if there is a protocol error
     */
    private Persistence persist;

    /**
     * Prevent external instantiation
     */
    public Sender(TelemetryQueueConfig config) {
        this.config = config;
        this.persist = Persistence.getInstance();
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
                this.info("adding persisted data", persistedData);
                sendRequestWithPayload(persistedData);
                this.persist.clearData();
            }

            // Send the new data
            String serializedData = buffer.toString();
            sendRequestWithPayload(serializedData);
        } catch (IOException e) {
            this.warn(e.toString());
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
            this.info("writing payload", payload);
            writer = this.getWriter(connection);
            writer.write(payload);
            writer.flush();

            // Starts the query
            connection.connect();
            int responseCode = connection.getResponseCode();
            this.info("response code", Integer.toString(responseCode));
            this.onResponse(connection, responseCode, payload);
        } catch (IOException e){
            this.persist.saveData(payload);
            this.warn(e.toString());
        } finally {
            if(writer != null)
            {
                try {
                    writer.close();
                } catch (IOException e) {
                    this.warn(e.toString());
                }
            }
        }
    }

    /**
     * Handler for the http response from the sender
     * @param connection a connection containing a response
     * @param responseCode the response code from the connection
     * @param payload the payload which generated this response
     * @return null if the request was successful, the server response otherwise
     */
    protected String onResponse(HttpURLConnection connection, int responseCode, String payload) {
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
                this.warn(message);
            }

            //If there was a server issue, persist the data
            if(responseCode >= 500 && responseCode != 529)
            {
                this.info("Server error, persisting data", payload);
                persist.saveData(payload);
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
                    while (responseLine != null) {
                        responseBuilder.append(responseLine);
                        responseLine = reader.readLine();
                    }

                    response = responseBuilder.toString();
                } else {
                    response = connection.getResponseMessage();
                }

                this.info("Non-200 response", response);
            }
        } catch (IOException e) {
            this.warn(e.toString());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    this.warn(e.toString());
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
     * @param message the message to log
     */
    private void warn(String message) {
        InternalLogging._warn("Sender", message);
    }

    /**
     * Writes info to the verbose logging channel
     * @param message the message to log
     */
    private void info(String message, String payload) {
        InternalLogging._info("Sender", message, payload);
    }
}

