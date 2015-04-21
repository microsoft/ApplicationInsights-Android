package com.microsoft.applicationinsights.library;

import com.microsoft.applicationinsights.library.Sender;
import com.microsoft.applicationinsights.library.config.SenderConfig;
import com.microsoft.applicationinsights.library.config.ISenderConfig;

import java.io.IOException;
import java.io.Writer;
import java.util.concurrent.CountDownLatch;

public class MockSender extends Sender {

    public int responseCode;
    public CountDownLatch sendSignal;
    public CountDownLatch responseSignal;
    private String lastResponse;

    public MockSender(CountDownLatch sendSignal,
                      CountDownLatch responseSignal,
                      ISenderConfig config) {
        super(config);
        this.responseCode = 0;
        this.sendSignal = sendSignal;
        this.responseSignal = responseSignal;
        this.lastResponse = null;
    }

    public String getLastResponse() {
        if (this.lastResponse == null) {
            return "";
        } else {
            return this.lastResponse;
        }
    }

//    @Override
//    protected void send(IJsonSerializable[] data) {
//        this.sendSignal.countDown();
//        super.send(data);
//    }

    //TODO fix unit tests

//    @Override
//    protected String onResponse(HttpURLConnection connection, int responseCode, String payload) {
//        String response = super.onResponse(connection, responseCode, payload);
//        this.lastResponse = prettyPrintJSON(response);
//        this.responseCode = responseCode;
//        this.responseSignal.countDown();
//        return response;
//    }

    private String prettyPrintJSON(String payload) {
        if (payload == null)
            return "";

        char[] chars = payload.toCharArray();
        StringBuilder sb = new StringBuilder();
        String tabs = "";

        // logcat doesn't like leading spaces, so add '|' to the start of each line
        String logCatNewLine = "\n|";
        sb.append(logCatNewLine);
        for (char c : chars) {
            switch (c) {
                case '[':
                case '{':
                    tabs += "\t";
                    sb.append(" " + c + logCatNewLine + tabs);
                    break;
                case ']':
                case '}':
                    tabs = tabs.substring(0, tabs.length() - 1);
                    sb.append(logCatNewLine + tabs + c);
                    break;
                case ',':
                    sb.append(c + logCatNewLine + tabs);
                    break;
                default:
                    sb.append(c);
            }
        }

        String result = sb.toString();
        result.replaceAll("\t", "  ");

        return result;
    }

    private class WriterListener extends Writer {

        private Writer baseWriter;
        private StringBuilder stringBuilder;

        public WriterListener(Writer baseWriter) {
            this.baseWriter = baseWriter;
            this.stringBuilder = new StringBuilder();
        }

        @Override
        public void close() throws IOException {
            baseWriter.close();
        }

        @Override
        public void flush() throws IOException {
            baseWriter.flush();
        }

        @Override
        public void write(char[] buf, int offset, int count) throws IOException {
            stringBuilder.append(buf);
            baseWriter.write(buf);
        }
    }
}
