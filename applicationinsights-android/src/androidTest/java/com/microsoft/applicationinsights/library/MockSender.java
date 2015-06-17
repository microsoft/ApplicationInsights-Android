package com.microsoft.applicationinsights.library;

import com.microsoft.applicationinsights.library.config.ISenderConfig;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class MockSender extends Sender {

    public int responseCode;
    public CountDownLatch sendSignal;
    public CountDownLatch responseSignal;
    public List<String> payloads;
    public List<String> responses;
    public List<Integer> responseCodes;

    public MockSender(int count,
                      ISenderConfig config) {
        super(config);
        this.responseCode = 0;
        this.sendSignal = new CountDownLatch(count);
        this.responseSignal = new CountDownLatch(count);
        this.payloads = new ArrayList<String>();
        this.responses = new ArrayList<String>();
        this.responseCodes = new ArrayList<Integer>();
        this.setInstance(this);
    }

    // make singleton getter here

    public void flush(int count) {
        for(int i = 0; i < count; i++) {
            super.sendNextFile();
        }
    }

    @Override
    protected void sendRequestWithPayload(String payload, File fileToSend) throws IOException {
        super.sendRequestWithPayload(payload, fileToSend);
        this.payloads.add(prettyPrintJSON(payload));
        this.sendSignal.countDown();
    }

    @Override
    protected void onResponse(HttpURLConnection connection, int responseCode, String payload, File fileToSend) {
        super.onResponse(connection, responseCode, payload, fileToSend);
        this.responseCodes.add(responseCode);
        this.responseSignal.countDown();
    }

    @Override
    protected void readResponse(HttpURLConnection connection, StringBuilder builder) {
        super.readResponse(connection, builder);
        this.responses.add(prettyPrintJSON(builder.toString()));
    }

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
