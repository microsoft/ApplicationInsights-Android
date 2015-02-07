package com.microsoft.mocks;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;

public class MockActivity extends Activity {

    public Context context;

    public MockActivity() {
        super();
    }

    public MockActivity(Context context) {
        this.context = context;
    }

    @Override
    public Resources getResources() {
        if(this.context != null) {
            return this.context.getResources();
        } else {
            return super.getResources();
        }
    }

    @Override
    public Context getApplicationContext() {
        if(this.context != null) {
            return this.context;
        } else {
            return super.getApplicationContext();
        }
    }

    @Override
    public String getPackageName() {
        if(this.context != null) {
            return "com.microsoft.applicationinsights.test";
        } else {
            return super.getPackageName();
        }
    }
}