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

import com.heyoe.R;
import com.heyoe.controller.fragments.DetailPostFragment;
import com.heyoe.controller.fragments.LikeUsersFragment;
import com.heyoe.model.PostModel;

public class UserListActivity extends AppCompatActivity {

    private ImageButton ibBack;
    private static FragmentManager fragmentManager;
    private static Activity mActivity;

    public static String type; ///detail_post, like_users, dislike_users;
    public static PostModel postModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        initVariables();
        initUI();


    }
    private void initVariables() {
        fragmentManager = getSupportFragmentManager();
        mActivity = this;
        type = getIntent().getStringExtra("type");
        postModel = (PostModel) getIntent().getSerializableExtra("post");
    }
    private void initUI() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ibBack = (ImageButton)toolbar.findViewById(R.id.ib_back);
        ibBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, new LikeUsersFragment())
                .commit();
    }

}
