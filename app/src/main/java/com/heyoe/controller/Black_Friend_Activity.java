package com.heyoe.controller;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.heyoe.R;
import com.heyoe.controller.fragments.MyBlackFriendsFregment;
import com.heyoe.model.UserModel;

public class Black_Friend_Activity extends AppCompatActivity {

    private FragmentManager fragmentManager;
    private Activity mActivity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_black__friend_);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initVariables();
        initUI();

    }
    private void initVariables() {
        mActivity = this;
        fragmentManager = getSupportFragmentManager();
    }
    private void initUI() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ImageButton ibBack = (ImageButton)toolbar.findViewById(R.id.ib_back);
        ibBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, new MyBlackFriendsFregment())
                .commit();
    }
}
