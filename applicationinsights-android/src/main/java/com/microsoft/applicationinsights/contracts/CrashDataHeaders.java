/*
 * Generated from CrashDataHeaders.bond (https://github.com/Microsoft/bond)
*/
package com.microsoft.applicationinsights.contracts;
import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;

import com.microsoft.telemetry.IJsonSerializable;
import com.microsoft.telemetry.JsonHelper;

/**
 * Data contract class CrashDataHeaders.
 */
public class CrashDataHeaders
     implements IJsonSerializable, Serializable
{
    /**
     * Backing field for property Id.
     */
    private String id;
    
    /**
     * Backing field for property Process.
     */
    private String process;
    
    /**
     * Backing field for property ProcessId.
     */
    private int processId;
    
    /**
     * Backing field for property ParentProcess.
     */
    private String parentProcess;
    
    /**
     * Backing field for property ParentProcessId.
     */
    private int parentProcessId;
    
    /**
     * Backing field for property CrashThread.
     */
    private int crashThread;
    
    /**
     * Backing field for property ApplicationPath.
     */
    private String applicationPath;
    
    /**
     * Backing field for property ApplicationIdentifier.
     */
    private String applicationIdentifier;
    
    /**
     * Backing field for property ApplicationBuild.
     */
    private String applicationBuild;
    
    /**
     * Backing field for property ExceptionType.
     */
    private String exceptionType;
    
    /**
     * Backing field for property ExceptionCode.
     */
    private String exceptionCode;
    
    /**
     * Backing field for property ExceptionAddress.
     */
    private String exceptionAddress;
    
    /**
     * Backing field for property ExceptionReason.
     */
    private String exceptionReason;
    
    /**
     * Initializes a new instance of the CrashDataHeaders class.
     */
    public CrashDataHeaders()
    {
        this.InitializeFields();
    }
    
    /**
     * Gets the Id property.
     */
    public String getId() {
        return this.id;
    }
    
    /**
     * Sets the Id property.
     */
    public void setId(String value) {
        this.id = value;
    }
    
    /**
     * Gets the Process property.
     */
    public String getProcess() {
        return this.process;
    }
    
    /**
     * Sets the Process property.
     */
    public void setProcess(String value) {
        this.process = value;
    }
    
    /**
     * Gets the ProcessId property.
     */
    public int getProcessId() {
        return this.processId;
    }
    
    /**
     * Sets the ProcessId property.
     */
    public void setProcessId(int value) {
        this.processId = value;
    }
    
    /**
     * Gets the ParentProcess property.
     */
    public String getParentProcess() {
        return this.parentProcess;
    }
    
    /**
     * Sets the ParentProcess property.
     */
    public void setParentProcess(String value) {
        this.parentProcess = value;
    }
    
    /**
     * Gets the ParentProcessId property.
     */
    public int getParentProcessId() {
        return this.parentProcessId;
    }
    
    /**
     * Sets the ParentProcessId property.
     */
    public void setParentProcessId(int value) {
        this.parentProcessId = value;
    }
    
    /**
     * Gets the CrashThread property.
     */
    public int getCrashThread() {
        return this.crashThread;
    }
    
    /**
     * Sets the CrashThread property.
     */
    public void setCrashThread(int value) {
        this.crashThread = value;
    }
    
    /**
     * Gets the ApplicationPath property.
     */
    public String getApplicationPath() {
        return this.applicationPath;
    }
    
    /**
     * Sets the ApplicationPath property.
     */
    public void setApplicationPath(String value) {
        this.applicationPath = value;
    }
    
    /**
     * Gets the ApplicationIdentifier property.
     */
    public String getApplicationIdentifier() {
        return this.applicationIdentifier;
    }
    
    /**
     * Sets the ApplicationIdentifier property.
     */
    public void setApplicationIdentifier(String value) {
        this.applicationIdentifier = value;
    }
    
    /**
     * Gets the ApplicationBuild property.
     */
    public String getApplicationBuild() {
        return this.applicationBuild;
    }
    
    /**
     * Sets the ApplicationBuild property.
     */
    public void setApplicationBuild(String value) {
        this.applicationBuild = value;
    }
    
    /**
     * Gets the ExceptionType property.
     */
    public String getExceptionType() {
        return this.exceptionType;
    }
    
    /**
     * Sets the ExceptionType property.
     */
    public void setExceptionType(String value) {
        this.exceptionType = value;
    }
    
    /**
     * Gets the ExceptionCode property.
     */
    public String getExceptionCode() {
        return this.exceptionCode;
    }
    
    /**
     * Sets the ExceptionCode property.
     */
    public void setExceptionCode(String value) {
        this.exceptionCode = value;
    }
    
    /**
     * Gets the ExceptionAddress property.
     */
    public String getExceptionAddress() {
        return this.exceptionAddress;
    }
    
    /**
     * Sets the ExceptionAddress property.
     */
    public void setExceptionAddress(String value) {
        this.exceptionAddress = value;
    }
    
    /**
     * Gets the ExceptionReason property.
     */
    public String getExceptionReason() {
        return this.exceptionReason;
    }
    
    /**
     * Sets the ExceptionReason property.
     */
    public void setExceptionReason(String value) {
        this.exceptionReason = value;
    }
    

    /**
     * Serializes the beginning of this object to the passed in writer.
     * @param writer The writer to serialize this object to.
     */
    @Override
    public void serialize(Writer writer) throws IOException
    {
        if (writer == null)
        {
            throw new IllegalArgumentException("writer");
        }
        
        writer.write('{');
        this.serializeContent(writer);
        writer.write('}');
    }

    /**
     * Serializes the beginning of this object to the passed in writer.
     * @param writer The writer to serialize this object to.
     */
    protected String serializeContent(Writer writer) throws IOException
    {
        String prefix = "";
        writer.write(prefix + "\"id\":");
        writer.write(JsonHelper.convert(this.id));
        prefix = ",";
        
        if (!(this.process == null))
        {
            writer.write(prefix + "\"process\":");
            writer.write(JsonHelper.convert(this.process));
            prefix = ",";
        }
        
        if (!(this.processId == 0))
        {
            writer.write(prefix + "\"processId\":");
            writer.write(JsonHelper.convert(this.processId));
            prefix = ",";
        }
        
        if (!(this.parentProcess == null))
        {
            writer.write(prefix + "\"parentProcess\":");
            writer.write(JsonHelper.convert(this.parentProcess));
            prefix = ",";
        }
        
        if (!(this.parentProcessId == 0))
        {
            writer.write(prefix + "\"parentProcessId\":");
            writer.write(JsonHelper.convert(this.parentProcessId));
            prefix = ",";
        }
        
        if (!(this.crashThread == 0))
        {
            writer.write(prefix + "\"crashThread\":");
            writer.write(JsonHelper.convert(this.crashThread));
            prefix = ",";
        }
        
        if (!(this.applicationPath == null))
        {
            writer.write(prefix + "\"applicationPath\":");
            writer.write(JsonHelper.convert(this.applicationPath));
            prefix = ",";
        }
        
        if (!(this.applicationIdentifier == null))
        {
            writer.write(prefix + "\"applicationIdentifier\":");
            writer.write(JsonHelper.convert(this.applicationIdentifier));
            prefix = ",";
        }
        
        if (!(this.applicationBuild == null))
        {
            writer.write(prefix + "\"applicationBuild\":");
            writer.write(JsonHelper.convert(this.applicationBuild));
            prefix = ",";
        }
        
        if (!(this.exceptionType == null))
        {
            writer.write(prefix + "\"exceptionType\":");
            writer.write(JsonHelper.convert(this.exceptionType));
            prefix = ",";
        }
        
        if (!(this.exceptionCode == null))
        {
            writer.write(prefix + "\"exceptionCode\":");
            writer.write(JsonHelper.convert(this.exceptionCode));
            prefix = ",";
        }
        
        if (!(this.exceptionAddress == null))
        {
            writer.write(prefix + "\"exceptionAddress\":");
            writer.write(JsonHelper.convert(this.exceptionAddress));
            prefix = ",";
        }
        
        if (!(this.exceptionReason == null))
        {
            writer.write(prefix + "\"exceptionReason\":");
            writer.write(JsonHelper.convert(this.exceptionReason));
            prefix = ",";
        }
        
        return prefix;
    }
    
    /**
     * Optionally initializes fields for the current context.
     */
    protected void InitializeFields() {
        
    }
}
