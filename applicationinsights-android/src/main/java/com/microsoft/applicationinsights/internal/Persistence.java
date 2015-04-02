package com.microsoft.applicationinsights.internal;

import android.content.Context;

import com.microsoft.applicationinsights.contracts.Internal;
import com.microsoft.applicationinsights.contracts.shared.IJsonSerializable;
import com.microsoft.applicationinsights.internal.logging.InternalLogging;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.lang.ref.WeakReference;
import java.util.UUID;

public class Persistence {

    /**
     * Volatile boolean for double checked synchronize block
     */
    private static volatile boolean isPersistenceLoaded = false;

    /**
     * Synchronization LOCK for setting static context
     */
    private static final Object LOCK = new Object();

    private static final String HIGH_PRIO_DIRECTORY = "/highpriority/";

    private static final String REGULAR_PRIO_DIRECTORY = "/regularpriority/";

    private static final Integer MAX_FILE_COUNT = 50;

    /**
     * The tag for logging
     */
    private static final String TAG = "Persistence";

    /**
     * The singleton INSTANCE of this class
     */
    private static Persistence instance;

    /**
     * A weak reference to the app context
     */
    private WeakReference<Context> weakContext;

    /**
     * Restrict access to the default constructor
     */
    protected Persistence(Context context) {
        this.weakContext = new WeakReference<>(context);
        createDirectoriesIfNecessary();
    }

    /**
     * Initialize the INSTANCE of persistence
     *
     * @param context the app context for the INSTANCE
     */
    public static void initialize(Context context) {
        // note: isPersistenceLoaded must be volatile for the double-checked LOCK to work
        if (!Persistence.isPersistenceLoaded) {
            synchronized (Persistence.LOCK) {
                if (!Persistence.isPersistenceLoaded) {
                    Persistence.isPersistenceLoaded = true;
                    Persistence.instance = new Persistence(context);

                }
            }
        }
    }

    /**
     * @return the INSTANCE of persistence or null if not yet initialized
     */
    public static Persistence getInstance() {
        if (Persistence.instance == null) {
            InternalLogging.error(TAG, "getInstance was called before initialization");
        }

        return Persistence.instance;
    }

    /**
     * Serializes the input and calls:
     *
     * @see Persistence#persist(String, Boolean)
     */
    public boolean persist(IJsonSerializable[] data, Boolean highPriority) {
        StringBuilder buffer = new StringBuilder();
        Boolean isSuccess;
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
            isSuccess = this.persist(serializedData, highPriority);
        } catch (IOException e) {
            InternalLogging.error(TAG, e.toString());
            return false;
        }

        return isSuccess;
    }

    /**
     * Saves a collection of IJsonSerializable objects to disk
     *
     * @param data the serializable collection to save
     * @return true if the operation was successful, false otherwise
     */
    public boolean persist(String data, Boolean highPriority) {
        if(!this.isFreeSpaceAvailable()) {
            InternalLogging.warn(TAG, "No free space on disk to persist data.");
            return false;
        }

        String uuid = UUID.randomUUID().toString();
        Boolean isSuccess = false;
        Context context = this.getContext();
        if (context != null) {
            FileOutputStream outputStream;
            try {
                File filesDir = getContext().getFilesDir();
                if(highPriority) {
                    filesDir = new File(filesDir + HIGH_PRIO_DIRECTORY + uuid);
                    outputStream = new FileOutputStream(filesDir, true);
                }
                else {
                    filesDir = new File(filesDir + REGULAR_PRIO_DIRECTORY + uuid);
                    outputStream = new FileOutputStream(filesDir, true);
                }
                outputStream.write(data.getBytes());
                outputStream.close();
                isSuccess = true;
            } catch (Exception e) {
                //Do nothing
                InternalLogging.error(TAG, "Error writing telemetry data to file");
            }
        }

        return isSuccess;
    }

    /**
     * Retrieves and deletes the next item from disk. Will return a crash if available.
     *
     * @return the next item from disk or empty string if anything goes wrong
     */
    public String getNextItemFromDisk() {
        StringBuilder buffer = new StringBuilder();
        File nextFile = this.nextFile();

        if (nextFile != null) {
            try {
                FileInputStream inputStream = new FileInputStream(nextFile);
                InputStreamReader streamReader = new InputStreamReader(inputStream);

                BufferedReader reader = new BufferedReader(streamReader);
                String str;
                while ((str = reader.readLine()) != null) {
                    buffer.append(str);
                }

                reader.close();
            } catch (Exception e) {
                InternalLogging.error(TAG, "Error reading telemetry data from file");
            }

            // TODO: Do not delete the file before it has been successfully sent
            // always delete the file
            boolean deletedFile = nextFile.delete();
            if(!deletedFile) {
                InternalLogging.error(TAG, "Error deleting telemetry file " + nextFile.toString());
            }
        }

        return buffer.toString();
    }

    private File nextFile() {
        String path = getContext().getFilesDir() + HIGH_PRIO_DIRECTORY;
        File directory = new File(path);
        File[] files = directory.listFiles();
        if(files.length > 0) {
            return files[0];
        }
        else {
            path = getContext().getFilesDir() + REGULAR_PRIO_DIRECTORY;
            directory = new File(path);
            files = directory.listFiles();
            if(files.length > 0) {
                return files[0];
            }
        }
        return null;
    }

    private Boolean isFreeSpaceAvailable() {
        String regularPrioPath = getContext().getFilesDir() + REGULAR_PRIO_DIRECTORY;
        File dir = new File(regularPrioPath);
        if(dir.listFiles().length < MAX_FILE_COUNT) {
            return true;
        }
        else {
            return false;
        }
    }

    private void createDirectoriesIfNecessary() {
        String filesDirPath = getContext().getFilesDir().getPath();
        //create high prio directory
        File dir = new File(filesDirPath + HIGH_PRIO_DIRECTORY);
        if(!dir.exists()) {
            dir.mkdirs();
        }
        //create high prio directory
        dir = new File(filesDirPath + REGULAR_PRIO_DIRECTORY);
        if(!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * Retrieves the weak context reference
     * @return the context object for this instance
     */
    private Context getContext() {
        Context context = null;
        if(weakContext != null) {
            context = weakContext.get();
        }

        return context;
    }
}