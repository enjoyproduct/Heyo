package com.heyoe.controller;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.heyoe.R;
import com.heyoe.controller.fragments.CheckinFragment;
import com.heyoe.controller.fragments.DetailPostFragment;
import com.heyoe.controller.fragments.LikeUsersFragment;
import com.heyoe.model.PostModel;

import org.w3c.dom.Text;

public class UserListActivity extends AppCompatActivity {

    private ImageButton ibBack;
    private static FragmentManager fragmentManager;
    private static Activity mActivity;
    private static TextView tvTitle;

    public static String type; ///detail_post, like_users, dislike_users, checkin;
    public static PostModel postModel;
    private String checkin;

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
        if (type.equals("checkin")) {
        } else {
            postModel = (PostModel) getIntent().getSerializableExtra("post");
        }

    }
    private void initUI() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        tvTitle = (TextView)toolbar.findViewById(R.id.tv_home_title);
        ibBack = (ImageButton)toolbar.findViewById(R.id.ib_back);
        ibBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        if (!type.equals("checkin")) {
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, new LikeUsersFragment())
                    .commit();
        } else {
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, new CheckinFragment())
                    .commit();
        }

    }
    public static void setTitle(String title) {
        tvTitle.setText(title);
    }

}
