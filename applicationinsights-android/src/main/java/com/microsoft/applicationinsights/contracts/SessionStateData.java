package AI;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import com.microsoft.telemetry.ITelemetry;
import com.microsoft.telemetry.ITelemetryData;
import com.microsoft.telemetry.IContext;
import com.microsoft.telemetry.IJsonSerializable;
import com.microsoft.telemetry.Base;
import com.microsoft.telemetry.Data;
import com.microsoft.telemetry.Domain;
import com.microsoft.telemetry.Extension;
import com.microsoft.telemetry.JsonHelper;

/**
 * Data contract class SessionStateData.
 */
public class SessionStateData extends Domain implements
    ITelemetry
{
    /**
     * Backing field for property Ver.
     */
    private int ver = 2;
    
    /**
     * Backing field for property State.
     */
    private int state = SessionState.Start;
    
    /**
     * Initializes a new instance of the <see cref="SessionStateData"/> class.
     */
    public SessionStateData()
    {
        this.InitializeFields();
        this.SetupAttributes();
    }
    
    /**
     * Gets the Ver property.
     */
    public int getVer() {
        return this.ver;
    }
    
    /**
     * Sets the Ver property.
     */
    public void setVer(int value) {
        this.ver = value;
    }
    
    /**
     * Gets the State property.
     */
    public int getState() {
        return this.state;
    }
    
    /**
     * Sets the State property.
     */
    public void setState(int value) {
        this.state = value;
    }
    

    /**
    * Gets the Properties property.
    */
    public LinkedHashMap<String, String> getProperties() {
        //Do nothing - does not currently take properties
        return null;
    }

    /**
    * Sets the Properties property.
    */
    public void setProperties(LinkedHashMap<String, String> value) {
        //Do nothing - does not currently take properties
    }

    /**
     * Serializes the beginning of this object to the passed in writer.
     * @param writer The writer to serialize this object to.
     */
    protected String serializeContent(Writer writer) throws IOException
    {
        String prefix = super.serializeContent(writer);
        writer.write(prefix + "\"ver\":");
        writer.write(JsonHelper.convert(this.ver));
        prefix = ",";
        
        writer.write(prefix + "\"state\":");
        writer.write(JsonHelper.convert(this.state));
        prefix = ",";
        
        return prefix;
    }
    
    /**
     * Sets up the events attributes
     */
    public void SetupAttributes()
    {
    }
    
    /**
     * Optionally initializes fields for the current context.
     */
    protected void InitializeFields() {
        QualifiedName = "AI.SessionStateData";
    }
}
