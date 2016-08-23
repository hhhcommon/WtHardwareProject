package com.wotingfm.common.application;

import android.app.Application;
import android.content.Context;
import com.android.volley.toolbox.Volley;
import com.android.volley.RequestQueue;

public class BSApplication extends Application {
    private static Context instance;
    private static RequestQueue queues;

    @Override
    public void onCreate() {
        super.onCreate();
        instance=this;
        queues = Volley.newRequestQueue(this);

    }

    public static Context getAppContext(){
        return instance;
    }

    public static RequestQueue getHttpQueues() {
        return queues;
    }

}
