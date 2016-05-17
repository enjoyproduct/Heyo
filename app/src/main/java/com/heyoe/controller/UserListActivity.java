package com.heyoe.controller;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
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
import com.heyoe.model.Constant;
import com.heyoe.model.PostModel;
import com.heyoe.model.PushModel;

import org.w3c.dom.Text;

import java.util.zip.CheckedInputStream;

public class UserListActivity extends AppCompatActivity {

    private ImageButton ibBack;
    private static FragmentManager fragmentManager;
    private static Activity mActivity;
    private static TextView tvTitle;

    public static String type; ///detail_post, like_users, dislike_users, checkin;
    public static PostModel postModel;
    private String checkin;

    static int currentFragmentNum;

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
        LocalBroadcastManager.getInstance(this).registerReceiver(mHandleMessageReceiver, new IntentFilter("pushData"));
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
            currentFragmentNum = 0;
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, new LikeUsersFragment())
                    .commit();
        } else {
            currentFragmentNum = 1;
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, new CheckinFragment())
                    .commit();
        }

    }
    public static void setTitle(String title) {
        tvTitle.setText(title);
    }

    public static void changeCheckinChatStatus(String id, String type) {
        if (currentFragmentNum == 1) {
            CheckinFragment.updateCheckinRequest(id, type);
        }
    }
    public BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver(){

        public void onReceive(Context context, Intent intent) {

            PushModel data = (PushModel)intent.getExtras().getSerializable(Constant.PUSH_DATA);

            if (currentFragmentNum == 1) {
                changeCheckinChatStatus(data.user_id, data.type);
            }

        };
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mHandleMessageReceiver);
    }
}
