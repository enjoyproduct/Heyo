package com.heyoe.controller;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.crashlytics.android.Crashlytics;
import com.heyoe.R;
import com.heyoe.utilities.ExceptionHandler;
import io.fabric.sdk.android.Fabric;


public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_splash);

        ///set exception handler
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashActivity.this, SignActivity.class));
                finish();
            }
        }, 2000);
    }

}
