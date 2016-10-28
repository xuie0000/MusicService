package com.xuie.musicservice;

import android.app.Application;
import android.content.Context;

/**
 * Created by xuie on 16-10-26.
 */

public class App extends Application {

    private static Context context;

    @Override public void onCreate() {
        super.onCreate();
        context = this;
    }

    public static Context getContext() {
        return context;
    }
}
