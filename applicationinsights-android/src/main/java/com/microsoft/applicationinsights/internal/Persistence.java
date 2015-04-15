package com.microsoft.applicationinsights.internal;

import android.content.Context;

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
import java.util.ArrayList;
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

    private ArrayList<File> servedFiles;

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
     * @param context android Context object
     */
    protected Persistence(Context context) {
        this.weakContext = new WeakReference<Context>(context);
        createDirectoriesIfNecessary();
        this.servedFiles = new ArrayList<File>(51);
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
     * Serializes a IJsonSerializable[] and calls:
     *
     * @param data         the data to serialize and save to disk
     * @param highPriority the priority to save the data with
     * @see Persistence#persist(String, Boolean)
     *
     * @return indicate if persisting data worked
     */
    protected boolean persist(IJsonSerializable[] data, Boolean highPriority) {
        if (!this.isFreeSpaceAvailable(highPriority)) {
            InternalLogging.warn(TAG, "No free space on disk to flush data.");
            Sender.getInstance().send();
            return false;
        }

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

            if(isSuccess) {
                Sender sender = Sender.getInstance();
                if(sender != null) {
                    sender.send();
                }
            }
        } catch (IOException e) {
            InternalLogging.error(TAG, e.toString());
            return false;
        }

        return isSuccess;
    }

    /**
     * Saves a string to disk.
     *
     * @param data         the string to save
     * @param highPriority the priority we want to use for persisting the data
     * @return true if the operation was successful, false otherwise
     */
    protected boolean persist(String data, Boolean highPriority) {
        String uuid = UUID.randomUUID().toString();
        Boolean isSuccess = false;
        Context context = this.getContext();
        if (context != null) {
            FileOutputStream outputStream;
            try {
                File filesDir = getContext().getFilesDir();
                if (highPriority) {
                    filesDir = new File(filesDir + HIGH_PRIO_DIRECTORY + uuid);
                    outputStream = new FileOutputStream(filesDir, true);
                } else {
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
     * Retrieves the data from a given path.
     *
     * @param file reference to a file on disk
     * @return the next item from disk or empty string if anything goes wrong
     */
    public String load(File file) {
        StringBuilder buffer = new StringBuilder();
        if (file != null) {
            try {
                FileInputStream inputStream = new FileInputStream(file);
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
        }

        return buffer.toString();
    }

    /**
     * Get a reference to the next available file. High priority is served before regular priority.
     *
     * @return the next available file.
     */
    public File nextAvailableFile() {
        File file = this.nextHighPrioFile();
        if (file != null) {
            return file;
        } else {
            return this.nextRegularPrioFile();
        }
    }


    private File nextHighPrioFile() {
        Context context = getContext();
        if (context != null) {
            String path = context.getFilesDir() + HIGH_PRIO_DIRECTORY;
            File directory = new File(path);
            return this.nextAvailableFileInDirectory(directory);
        }

        InternalLogging.error(TAG, "The context for persistence is null");
        return null;
    }


    private File nextRegularPrioFile() {
        Context context = getContext();
        if (context != null) {
            String path = context.getFilesDir() + REGULAR_PRIO_DIRECTORY;
            File directory = new File(path);
            return this.nextAvailableFileInDirectory(directory);
        }

        InternalLogging.error(TAG, "The context for persistence is null");
        return null;
    }

    /**
     * @param directory reference to the directory
     * @return reference to the next available file, null if no file is available
     */
    private File nextAvailableFileInDirectory(File directory) {
        synchronized (Persistence.LOCK) {
            if (directory != null) {
                File[] files = directory.listFiles();
                File file;
                if ((files != null) && (files.length > 0)) {
                    for (int i = 0; i < files.length - 1; i++) {
                        file = files[i];
                        if (!this.servedFiles.contains(file)) {
                            this.servedFiles.add(file);
                            return file;//we haven't served the file, return it
                        }
                    }

                }
            }

            return null; //no files in directory or no directory
        }
    }

    /**
     * delete a file from disk and remove it from the list of served files if deletion was successful
     *
     * @param file reference to the file we want to delete
     */
    protected void deleteFile(File file) {
        if (file != null) {
            synchronized (Persistence.LOCK) {
                // always delete the file
                boolean deletedFile = file.delete();
                if (!deletedFile) {
                    InternalLogging.error(TAG, "Error deleting telemetry file " + file.toString());
                } else {
                    servedFiles.remove(file);
                }
            }
        } else {
            InternalLogging.error(TAG, "Couldn't delete file, the reference to the file was null");
        }
    }

    /**
     * Make a file available to be served again
     *
     * @param file reference to the file that should be made available so it can be sent again later
     */
    protected void makeAvailable(File file) {
        synchronized (Persistence.LOCK) {
            if (file != null) {
                servedFiles.remove(file);
            }
        }
    }

    /**
     * Check if we haven't reached MAX_FILE_COUNT yet
     *
     * @param highPriority indicates which directory to check for available files
     */
    private Boolean isFreeSpaceAvailable(Boolean highPriority) {
        synchronized (Persistence.LOCK) {
            Context context = getContext();
            if (context != null) {
                String path = highPriority ? (context.getFilesDir() + HIGH_PRIO_DIRECTORY) :
                      (getContext().getFilesDir() + REGULAR_PRIO_DIRECTORY);
                File dir = new File(path);
                return (dir.listFiles().length < MAX_FILE_COUNT);
            }

            return false;
        }
    }

    /**
     * create local folders for both priorities if they are not present, yet.
     */
    private void createDirectoriesIfNecessary() {
        String filesDirPath = getContext().getFilesDir().getPath();
        //create high prio directory
        File dir = new File(filesDirPath + HIGH_PRIO_DIRECTORY);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        //create high prio directory
        dir = new File(filesDirPath + REGULAR_PRIO_DIRECTORY);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * Retrieves the weak context reference
     *
     * @return the context object for this instance
     */
    private Context getContext() {
        Context context = null;
        if (weakContext != null) {
            context = weakContext.get();
        }

        return context;
    }
}