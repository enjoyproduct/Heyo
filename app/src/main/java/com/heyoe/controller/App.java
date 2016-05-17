package com.heyoe.controller;

import android.app.Application;

/**
 * Created by dell17 on 5/16/2016.
 */
public class App extends Application {
    private static App instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public static synchronized App getInstance() {
        return instance;
    }
}
