package com.microsoft.appinsights_android;

import android.content.Context;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.microsoft.applicationinsights.TelemetryClient;
import com.microsoft.applicationinsights.channel.Sender;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String instrumentationKey = "2b240a15-4b1c-4c40-a4f0-0e8142116250";
        Context context = this.getApplicationContext();
        TelemetryClient client = new TelemetryClient(instrumentationKey, context);

        client.trackEvent("test app started");
        client.trackMetric("test app startup complete", 1);
        client.trackTrace("test app example trace message");
        client.trackException(new Exception("example exception"));

        // flush all data asynchronously from the singleton sender
        Sender.instance.flush();

        // update configuration to flush every 1 second or when 3 items are queued
        Sender.instance.getConfig().setMaxBatchIntervalMs(1000);
        Sender.instance.getConfig().setMaxBatchCount(3);

        client.trackEvent("test app started");
        client.trackMetric("test app startup complete", 1);
        client.trackTrace("test app example trace message"); // 3rd item will trigger a flush
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
