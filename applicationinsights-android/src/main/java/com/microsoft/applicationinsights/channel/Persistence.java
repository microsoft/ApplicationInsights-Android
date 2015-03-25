package com.microsoft.applicationinsights.channel;
import android.content.Context;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;

public class Persistence {
    private static String filePath = "outtext.txt";
    private static Context ctx;
    private static Persistence instance;
    private static String TAG = "Persistence";

    private Persistence() {
    }

    public static Persistence getInstance() {
        if(instance == null) {
            instance = new Persistence();
        }
        return instance;
    }

    public void setPersistenceContext(Context ctx) {
        this.ctx = ctx;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getfilePath() {
        return this.filePath;
    }

    public void saveData(String saveThis) {
        if ( ctx != null) {
            FileOutputStream outputStream;
            try {
                outputStream = ctx.openFileOutput(this.filePath, Context.MODE_PRIVATE);
                outputStream.write(saveThis.getBytes());
                outputStream.close();
            } catch (Exception e) {
                //Do nothing
              InternalLogging._error(TAG, "Error writing telemetry data to file");
            }
        }
    }

    public String getData() {
        StringBuilder buffer = new StringBuilder();
        if(ctx != null) {
            try {
                FileInputStream inputStream = ctx.openFileInput(this.filePath);
                InputStreamReader streamReader = new InputStreamReader(inputStream);

                BufferedReader reader = new BufferedReader(streamReader);
                String str = "";
                while ((str = reader.readLine()) != null) {
                    buffer.append(str);
                }
                reader.close();
            } catch (Exception e) {
                ctx.deleteFile(this.filePath);
              InternalLogging._error(TAG, "Error reading telemetry data from file");

              return "";
            }
        }
        return buffer.toString();
    }

    public void clearData() {
        if( ctx != null ) {
            ctx.deleteFile(this.filePath);
        }
    }
}
