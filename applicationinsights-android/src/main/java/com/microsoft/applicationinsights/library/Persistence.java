package com.microsoft.applicationinsights.library;

import android.content.Context;

import com.microsoft.applicationinsights.logging.InternalLogging;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.UUID;

class Persistence {

    /**
     * Volatile boolean for double checked synchronize block
     */
    private static volatile boolean isPersistenceLoaded = false;

    /**
     * Synchronization LOCK for setting static context
     */
    private static final Object LOCK = new Object();

    private static final String AI_SDK_DIRECTORY = "/com.microsoft.applicationinsights";

    private static final String HIGH_PRIO_DIRECTORY = "/highpriority/";

    private static final String REGULAR_PRIO_DIRECTORY = "/regularpriority/";

    private static final Integer MAX_FILE_COUNT = 50;

    private final ArrayList<File> servedFiles;

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
     *
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
    protected static void initialize(Context context) {
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
    protected static Persistence getInstance() {
        if (Persistence.instance == null) {
            InternalLogging.error(TAG, "getSharedInstance was called before initialization");
        }

        return Persistence.instance;
    }

    /**
     * Serializes a IJsonSerializable[] and calls:
     *
     * @param data         the data to save to disk
     * @param highPriority the priority to save the data with
     * @see Persistence#writeToDisk(String, Boolean)
     */
    protected void persist(String[] data, Boolean highPriority) {
        if (!this.isFreeSpaceAvailable(highPriority)) {
            InternalLogging.warn(TAG, "No free space on disk to flush data.");
            Sender.getInstance().sendNextFile();
        }else{
            StringBuilder buffer = new StringBuilder();
            Boolean isSuccess;
            for (String aData : data) {
                buffer.append('\n');
                buffer.append(aData);
            }
            String serializedData = buffer.toString();
            isSuccess = this.writeToDisk(serializedData, highPriority);
            if (isSuccess) {
                Sender sender = Sender.getInstance();
                if (sender != null && !highPriority) {
                    Sender.getInstance().sendNextFile();
                }
            }
        }
    }

    /**
     * Saves a string to disk.
     *
     * @param data         the string to save
     * @param highPriority the priority we want to use for persisting the data
     * @return true if the operation was successful, false otherwise
     */
    protected boolean writeToDisk(String data, Boolean highPriority) {
        String uuid = UUID.randomUUID().toString();
        Boolean isSuccess = false;
        Context context = this.getContext();
        if (context != null) {
            FileOutputStream outputStream = null;
            try {
                File filesDir = getContext().getFilesDir();
                if (highPriority) {
                    filesDir = new File(filesDir + AI_SDK_DIRECTORY + HIGH_PRIO_DIRECTORY + uuid);
                    outputStream = new FileOutputStream(filesDir, true);
                    InternalLogging.warn(TAG, "Saving data" + "HIGH PRIO");
                } else {
                    filesDir = new File(filesDir + AI_SDK_DIRECTORY + REGULAR_PRIO_DIRECTORY + uuid);
                    outputStream = new FileOutputStream(filesDir, true);
                    InternalLogging.warn(TAG, "Saving data" + "REGULAR PRIO");
                }
                outputStream.write(data.getBytes());

                isSuccess = true;
                InternalLogging.warn(TAG, "Saved data");
            } catch (Exception e) {
                //Do nothing
                InternalLogging.warn(TAG, "Failed to save data with exception: " + e.toString());
            }finally {
                if(outputStream != null){
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
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
    protected String load(File file) {
        StringBuilder buffer = new StringBuilder();
        if (file != null) {
            BufferedReader reader = null;
            try {
                FileInputStream inputStream = new FileInputStream(file);
                InputStreamReader streamReader = new InputStreamReader(inputStream);
                reader = new BufferedReader(streamReader);
                //comment: we can't use BufferedReader's readline() as this removes linebreaks that
                //are required for JSON stream
                int c;
                while ((c = reader.read()) != -1) {
                    //Cast c to char. As it's not -1, we won't get a problem
                    buffer.append((char) c);
                }
            } catch (Exception e) {
                InternalLogging.warn(TAG, "Error reading telemetry data from file with exception message "
                      + e.getMessage());
            }finally {

                try{
                    if(reader != null) {
                        reader.close();
                    }
                }catch (IOException e){
                    InternalLogging.warn(TAG, "Error closing stream."
                                + e.getMessage());
                }
            }
        }

        return buffer.toString();
    }

    /**
     * Get a reference to the next available file. High priority is served before regular priority.
     *
     * @return the next available file.
     */
    protected File nextAvailableFile() {
        synchronized (Persistence.LOCK) {
            File file = this.nextHighPrioFile();
            if (file != null) {
                return file;
            } else {
                InternalLogging.info(TAG, "High prio file was empty", "(That's the default if no crashes present");
                return this.nextRegularPrioFile();
            }
        }

    }


    private File nextHighPrioFile() {
        Context context = getContext();
        if (context != null) {
            String path = context.getFilesDir() + AI_SDK_DIRECTORY + HIGH_PRIO_DIRECTORY;
            File directory = new File(path);
            InternalLogging.info(TAG, "Returning High Prio File: ", path);

            return this.nextAvailableFileInDirectory(directory);
        }

        InternalLogging.warn(TAG, "Couldn't provide next file, the context for persistence is null");
        return null;
    }


    private File nextRegularPrioFile() {
        Context context = getContext();
        if (context != null) {
            String path = context.getFilesDir() + AI_SDK_DIRECTORY + REGULAR_PRIO_DIRECTORY;
            File directory = new File(path);
            InternalLogging.info(TAG, "Returning Regular Prio File: " + path);
            return this.nextAvailableFileInDirectory(directory);
        }

        InternalLogging.warn(TAG, "Couldn't provide next file, the context for persistence is null");
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
                    for (int i = 0; i <= files.length - 1; i++) {
                        InternalLogging.info(TAG, "The directory " + directory.toString(), " ITERATING over " + files.length + " files");

                        file = files[i];
                        InternalLogging.info(TAG, "The directory " + file.toString(), " FOUND");

                        if (!this.servedFiles.contains(file)) {
                            InternalLogging.info(TAG, "The directory " + file.toString(), " ADDING TO SERVED AND RETURN");

                            this.servedFiles.add(file);
                            return file;//we haven't served the file, return it
                        } else {
                            InternalLogging.info(TAG, "The directory " + file.toString(), " WAS ALREADY SERVED");
                        }
                    }
                }
                InternalLogging.info(TAG, "The directory " + directory.toString(), " NO FILES");

            }
            if(directory != null) {
                InternalLogging.info(TAG, "The directory " + directory.toString(), "Did not contain any unserved files");
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
                    InternalLogging.warn(TAG, "Error deleting telemetry file " + file.toString());
                } else {
                    InternalLogging.info(TAG, "Successfully deleted telemetry file ", file.toString());
                    servedFiles.remove(file);
                }
            }
        } else {
            InternalLogging.warn(TAG, "Couldn't delete file, the reference to the file was null");
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
    protected Boolean isFreeSpaceAvailable(Boolean highPriority) {
        synchronized (Persistence.LOCK) {
            Context context = getContext();
            if (context != null) {
                String path = highPriority ? (context.getFilesDir() + AI_SDK_DIRECTORY + HIGH_PRIO_DIRECTORY) :
                      (getContext().getFilesDir() + AI_SDK_DIRECTORY + REGULAR_PRIO_DIRECTORY);
                if(path != null && (path.length() > 0)) {
                    File dir = new File(path);
                    if(dir != null) {
                        return (dir.listFiles().length < MAX_FILE_COUNT);
                    }
                }
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
        File dir = new File(filesDirPath + AI_SDK_DIRECTORY + HIGH_PRIO_DIRECTORY);
        String successMessage = "Successfully created directory";
        String errorMessage = "Error creating directory";
        if (!dir.exists()) {
            if (dir.mkdirs()) {
                InternalLogging.info(TAG, successMessage, "high priority");
            } else {
                InternalLogging.info(TAG, errorMessage, "high priority");
            }
        }
        //create regular prio directory
        dir = new File(filesDirPath + AI_SDK_DIRECTORY + REGULAR_PRIO_DIRECTORY);
        if (!dir.exists()) {
            if (dir.mkdirs()) {
                InternalLogging.info(TAG, successMessage, "regular priority");
            } else {
                InternalLogging.info(TAG, errorMessage, "regular priority");
            }
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
