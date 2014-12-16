package com.microsoft.applicationinsights.channel.contracts.shared;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;

/**
 * This is the helper class to have the json to integer/double/string and ext
 * converting.
 */
public final class JsonHelper {
    /**
     * prevent caller to construct this object.
     */
    private JsonHelper() {
    }

    /**
     * integer to string.
     * 
     * @param value value of the integer.
     * @return String to represent the integer
     */
    public static String convert(Integer value) {
        return Integer.toString(value);
    }

    /**
     * long to string.
     * 
     * @param value value of the integer.
     * @return String to represent the long
     */
    public static String convert(Long value) {
        return Long.toString(value);
    }

    /**
     * character to string.
     * 
     * @param value value of the character.
     * @return String to represent the character
     */
    public static String convert(char value) {
        return Character.toString(value);
    }

    /**
     * double to string.
     * 
     * @param value value of the double.
     * @return String to represent the double
     */
    public static String convert(Double value) {
        return Double.toString(value);
    }

    /**
     * boolean to string.
     * 
     * @param value value of the boolean.
     * @return String to represent the boolean
     */
    public static String convert(boolean value) {
        return Boolean.toString(value);
    }

    /**
     * String to JSON String
     * 
     * @param value value of the string
     * @return JSON string to represent the string input parameter.
     */
    public static String convert(String value) {
        if (value == null) {
            return "null";
        }

        if (value.length() == 0) {
            return "\"\"";
        }

        // temporary trick to apply JSON encoding to the string
        JSONObject o = new JSONObject();
        o.put("v", value);
        String jsonString = o.toString();
        int begPos = jsonString.indexOf(':');
        if (begPos != -1) {
            begPos = jsonString.indexOf('"', begPos);
        }

        int endPos = jsonString.lastIndexOf('"');
        if (begPos != -1 && endPos != -1) {
            return jsonString.substring(begPos, endPos + 1);
        }
        else {
            return "\"" + value + "\"";
        }
    }

    /**
     * serialize the IJsonSerializable to writer
     * 
     * @param writer Writer object
     * @param value IJsonSerializable object
     * @throws IOException
     */
    public static void writeJsonSerializable(Writer writer, IJsonSerializable value)
            throws IOException {
        if (value != null) {
            value.serialize(writer);
        }
    }

    /**
     * serialize the map object to writer
     * 
     * @param writer Writer object
     * @param value Map object
     * @throws IOException
     */
    public static <K, V> void writeDictionary(Writer writer, Map<K, V> value) throws IOException {
        if (value == null) {
            writer.write("null");
        }

        JSONObject o = new JSONObject(value);
        writer.write(o.toString());
    }

    /**
     * serialize the List object to writer
     * 
     * @param writer Writer object
     * @param value List object
     * @throws IOException
     */
    public static <V> void writeList(Writer writer, List<V> value) throws IOException {
        if (value == null) {
            writer.write("null");
        }

        JSONArray jsonArr = new JSONArray();
        for (V singleValue : value) {
            jsonArr.put(singleValue);
        }

        writer.write(jsonArr.toString());
    }
}
