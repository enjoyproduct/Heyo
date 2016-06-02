package com.heyoe_chat.controller;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.heyoe_chat.R;

public class ExceptionViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exception_view);

        TextView textView = (TextView)findViewById(R.id.tv_exception_view);
        String exception = getIntent().getStringExtra("error");
        textView.setText(exception);
    }

}
