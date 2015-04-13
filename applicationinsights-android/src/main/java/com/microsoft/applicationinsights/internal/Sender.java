package com.microsoft.applicationinsights.internal;

import android.annotation.TargetApi;
import android.os.Build;

import com.microsoft.applicationinsights.internal.logging.InternalLogging;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimerTask;
import java.util.zip.GZIPOutputStream;

/**
 * This singleton class sends data to the endpoint
 */
public class Sender {

    private static final String TAG = "Sender";

    /**
     * Volatile boolean for double checked synchronize block
     */
    private static volatile boolean isSenderLoaded = false;


    /**
     * Synchronization LOCK for setting static config
     */
    private static final Object LOCK = new Object();

    /**
     * The configuration for this sender
     */
    protected final TelemetryConfig config;

    private static Sender instance;

    private HashMap<String, TimerTask> currentTasks = new HashMap<>(10);

    /**
     * Restrict access to the default constructor
     */
    protected Sender(TelemetryConfig config) {
        this.config = config;
    }

    /**
     * Initialize the INSTANCE of persistence
     *
     * @param config the config for the INSTANCE
     */
    public static void initialize(TelemetryConfig config) {
        // note: isSenderLoaded must be volatile for the double-checked LOCK to work
        if (!Sender.isSenderLoaded) {
            synchronized (Sender.LOCK) {
                if (!Sender.isSenderLoaded) {
                    Sender.isSenderLoaded = true;
                    Sender.instance = new Sender(config);
                }
            }
        }
    }

    /**
     * @return the INSTANCE of the sender or null if not yet initialized
     */
    public static Sender getInstance() {
        if (Sender.instance == null) {
            InternalLogging.error(TAG, "getInstance was called before initialization");
        }

        return Sender.instance;
    }


    public void send() {
        if(runningRequestCount() < 10) {
            // Send the persisted data
            Persistence persistence = Persistence.getInstance();
            if (persistence != null) {
                File fileToSend = persistence.nextAvailableFile();
                if(fileToSend != null) {
                    String persistedData = persistence.load(fileToSend);
                    if (!persistedData.isEmpty()) {
                        InternalLogging.info(TAG, "sending persisted data", persistedData);
                        SendingTask sendingTask = new SendingTask(persistedData, fileToSend);
                        this.addToRunning(fileToSend.toString(), sendingTask);
                        sendingTask.run();

                        //TODO add comment for this
                        Thread sendingThread = new Thread(sendingTask);
                        sendingThread.setDaemon(false);
                    }
                }
            }
        }
        else {
            InternalLogging.info(TAG, "We have already 10 pending reguests", "");
        }
}

    private void addToRunning(String key, SendingTask task) {
        synchronized (Sender.LOCK) {
            this.currentTasks.put(key, task);
        }
    }

    private void removeFromRunning(String key) {
        synchronized (Sender.LOCK) {
            this.currentTasks.remove(key);
        }
    }

    private int runningRequestCount() {
        synchronized (Sender.LOCK) {
            return getInstance().currentTasks.size();
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
    protected String onResponse(HttpURLConnection connection, int responseCode, String payload, File fileToSend) {
        this.removeFromRunning(fileToSend.toString());

        StringBuilder builder = new StringBuilder();

        InternalLogging.info(TAG, "response code", Integer.toString(responseCode));
        boolean isExpected = ((responseCode > 199) && (responseCode < 203));
        boolean isRecoverableError = (responseCode == 429) || (responseCode == 408) ||
              (responseCode == 500) || (responseCode == 503) || (responseCode == 511);
        boolean deleteFile = isExpected || !isRecoverableError;

        // If this was expected and developer mode is enabled, read the response
        if(isExpected) {
            this.onExpected(connection, builder, fileToSend);
            //TODO don't trigger sending endlessly?
            this.send();
        }

        if(deleteFile) {
            Persistence persistence = Persistence.getInstance();
            if(persistence != null) {
                persistence.deleteFile(fileToSend);
            }
        }

        // If there was a server issue, flush the data
        if (isRecoverableError) {
            this.onRecoverable(payload, fileToSend);
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
     *  @param connection a connection containing a response
     * @param builder    a string builder for storing the response
     * @param fileToSend
     */
    protected void onExpected(HttpURLConnection connection, StringBuilder builder, File fileToSend) {
        if (this.config.isDeveloperMode()) {
            this.readResponse(connection, builder);
        }
    }

    /**
     *  @param connection   a connection containing a response
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
     * @param fileToSend
     */
    protected void onRecoverable(String payload, File fileToSend) {
        InternalLogging.info(TAG, "Server error, persisting data", payload);
        Persistence persistence = Persistence.getInstance();
        if (persistence != null) {
            persistence.makeAvailable(fileToSend);
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


    private class SendingTask extends TimerTask {
        private String payload;
        private File fileToSend;

        public SendingTask(String payload, File fileToSend) {
            this.payload = payload;
            this.fileToSend = fileToSend;
        }

        @Override
        public void run() {
            if (this.payload != null) {
                try {
                    InternalLogging.info(TAG, "sending persisted data", payload);
                    this.sendRequestWithPayload(payload);
                } catch (IOException e) {
                    InternalLogging.error(TAG, e.toString());
                }
            }
        }

        protected void sendRequestWithPayload(String payload) throws IOException {
            Writer writer = null;
            URL url = new URL(config.getEndpointUrl());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setReadTimeout(config.getSenderReadTimeout());
            connection.setConnectTimeout(config.getSenderConnectTimeout());
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setUseCaches(false);

            try {
                InternalLogging.info(TAG, "writing payload", payload);
                writer = getWriter(connection);
                writer.write(payload);
                writer.flush();

                // Starts the query
                connection.connect();

                // read the response code while we're ready to catch the IO exception
                int responseCode = connection.getResponseCode();

                // process the response
                onResponse(connection, responseCode, payload, fileToSend);
            } catch (IOException e) {
                InternalLogging.error(TAG, e.toString());
                Persistence persistence = Persistence.getInstance();
                if (persistence != null) {
                    persistence.makeAvailable(fileToSend); //send again later
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

    }
}


