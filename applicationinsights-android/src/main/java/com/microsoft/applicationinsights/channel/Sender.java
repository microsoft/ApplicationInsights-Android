package com.microsoft.applicationinsights.channel;

import android.annotation.TargetApi;
import android.os.Build;

import com.microsoft.applicationinsights.channel.contracts.shared.IJsonSerializable;
import com.microsoft.applicationinsights.channel.logging.InternalLogging;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;
import java.util.zip.GZIPOutputStream;

/**
 * This singleton class sends data to the endpoint
 */
public class Sender {

    private static final String TAG = "Sender";
    /**
     * The configuration for this sender
     */
    protected final TelemetryQueueConfig config;

    /**
     * Prevent external instantiation
     */
    public Sender(TelemetryQueueConfig config) {
        this.config = config;
    }

    /**
     * Sends data to the configured URL
     *
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
            Persistence persistence = Persistence.getInstance();
            if (persistence != null) {
                String persistedData = persistence.getNextItemFromDisk();
                if (!persistedData.isEmpty()) {
                    InternalLogging.info(TAG, "adding persisted data", persistedData);
                    sendRequestWithPayload(persistedData);
                }
            }

            // Send the new data
            String serializedData = buffer.toString();
            sendRequestWithPayload(serializedData);
        } catch (IOException e) {
            InternalLogging.error(TAG, e.toString());
        }
    }

    private void sendRequestWithPayload(String payload) throws IOException {
        Writer writer = null;
        URL url = new URL(this.config.getEndpointUrl());
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setReadTimeout(this.config.getSenderReadTimeout());
        connection.setConnectTimeout(this.config.getSenderConnectTimeout());
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setUseCaches(false);

        try {
            InternalLogging.info(TAG, "writing payload", payload);
            writer = this.getWriter(connection);
            writer.write(payload);
            writer.flush();

            // Starts the query
            connection.connect();

            // read the response code while we're ready to catch the IO exception
            int responseCode = connection.getResponseCode();

            // process the response
            this.onResponse(connection, responseCode, payload);
        } catch (IOException e) {
            InternalLogging.error(TAG, e.toString());
            Persistence persistence = Persistence.getInstance();
            if (persistence != null) {
                persistence.persist(payload);
            }
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    // no-op
                }
            }
        }
    }

    /**
     * Handler for the http response from the sender
     *
     * @param connection   a connection containing a response
     * @param responseCode the response code from the connection
     * @param payload      the payload which generated this response
     * @return null if the request was successful, the server response otherwise
     */
    protected String onResponse(HttpURLConnection connection, int responseCode, String payload) {
        StringBuilder builder = new StringBuilder();

        InternalLogging.info(TAG, "response code", Integer.toString(responseCode));
        boolean isExpected = responseCode == 200;
        boolean isRecoverableError = responseCode > 500 && responseCode != 529;

        // If this was expected and developer mode is enabled, read the response
        if(isExpected) {
            this.onExpected(connection, builder);
        }

        // If there was a server issue, persist the data
        if (isRecoverableError) {
            this.onRecoverable(payload);
        }

        // If it isn't the usual success code (200), log the response from the server.
        if (!isExpected) {
            this.onUnexpected(connection, responseCode, builder);
        }

        return builder.toString();
    }

    /**
     * Process the expected response. If {code:TelemetryChannelConfig.isDeveloperMode}, read the
     * response and log it.
     *
     * @param connection a connection containing a response
     * @param builder    a string builder for storing the response
     */
    protected void onExpected(HttpURLConnection connection, StringBuilder builder) {
        if (this.config.isDeveloperMode()) {
            this.readResponse(connection, builder);
        }
    }

    /**
     *
     * @param connection   a connection containing a response
     * @param responseCode the response code from the connection
     * @param builder      a string builder for storing the response
     */
    protected void onUnexpected(HttpURLConnection connection, int responseCode, StringBuilder builder) {
        String message = String.format(Locale.ROOT, "Unexpected response code: %d", responseCode);
        builder.append(message);
        builder.append("\n");

        // log the unexpected response
        InternalLogging.warn(TAG, message);

        // attempt to read the response stream
        this.readResponse(connection, builder);
    }

    /**
     * Writes the payload to disk if the response code indicates that the server or network caused
     * the failure instead of the client.
     *
     * @param payload the payload which generated this response
     */
    protected void onRecoverable(String payload) {
        InternalLogging.info(TAG, "Server error, persisting data", payload);
        Persistence persistence = Persistence.getInstance();
        if (persistence != null) {
            persistence.persist(payload);
        }
    }

    /**
     * Reads the response from a connection.
     *
     * @param connection the connection which will read the response
     * @param builder a string builder for storing the response
     */
    private void readResponse(HttpURLConnection connection, StringBuilder builder) {
        BufferedReader reader = null;
        try {
            InputStream inputStream = connection.getErrorStream();
            if (inputStream == null) {
                inputStream = connection.getInputStream();
            }

            if (inputStream != null) {
                InputStreamReader streamReader = new InputStreamReader(inputStream, "UTF-8");
                reader = new BufferedReader(streamReader);
                String responseLine = reader.readLine();
                while (responseLine != null) {
                    builder.append(responseLine);
                    responseLine = reader.readLine();
                }
            } else {
                builder.append(connection.getResponseMessage());
            }
        } catch (IOException e) {
            InternalLogging.error(TAG, e.toString());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    InternalLogging.error(TAG, e.toString());
                }
            }
        }
    }

    /**
     * Gets a writer from the connection stream (allows for test hooks into the write stream)
     *
     * @param connection the connection to which the stream will be flushed
     * @return a writer for the given connection stream
     * @throws java.io.IOException
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    protected Writer getWriter(HttpURLConnection connection) throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
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
}

