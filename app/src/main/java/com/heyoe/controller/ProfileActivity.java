package com.heyoe.controller;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;

import com.heyoe.R;
import com.heyoe.controller.fragments.MediaFragment;
import com.heyoe.controller.fragments.ProfileFragment;
import com.heyoe.controller.fragments.ProfileInfoFragment;
import com.heyoe.model.UserModel;

public class ProfileActivity extends AppCompatActivity {

    private static FragmentManager fragmentManager;
    public static UserModel userModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initVariables();
        initUI();

    }
    private void initVariables() {
        fragmentManager = getSupportFragmentManager();
        userModel = (UserModel) getIntent().getSerializableExtra("user");
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
                .replace(R.id.fragment_container, new ProfileFragment())
                .commit();


    }
    public static void navigateTo(int num) {
        switch (num) {
            case 0:
                fragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, new ProfileFragment())
                        .commit();
                break;
            case 1:
                fragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, new ProfileInfoFragment())
                        .commit();
                break;
            case 2:
                fragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, new MediaFragment())
                        .commit();
                break;
            case 3:
//                fragmentManager.beginTransaction()
//                        .replace(R.id.fragment_container, new FriendFragment())
//                        .commit();
                break;

        }
    }

}
