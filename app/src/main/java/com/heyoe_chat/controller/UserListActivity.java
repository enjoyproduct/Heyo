package com.heyoe_chat.controller;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.heyoe_chat.R;
import com.heyoe_chat.controller.fragments.CheckinFragment;
import com.heyoe_chat.controller.fragments.LikeUsersFragment;
import com.heyoe_chat.model.Constant;
import com.heyoe_chat.model.PostModel;
import com.heyoe_chat.model.PushModel;
import com.heyoe_chat.model.UserModel;

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

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
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
                if (currentFragmentNum == 1) {
                    navigateToCheckinMap();
                } else {
                    finish();
                }

            }
        });

        if (!type.equals("checkin")) {
            currentFragmentNum = 0;
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, new LikeUsersFragment())
                    .commit();
        } else {
            navigateToCheckinMap();
        }

    }
    private void navigateToCheckinMap() {
        currentFragmentNum = 1;
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, new CheckinFragment())
                .commit();
    }
    public static void setTitle(String title) {
        tvTitle.setText(title);
    }

    @Override
    public void onBackPressed() {
        if (currentFragmentNum == 1){
            navigateToCheckinMap();
        } else {
            super.onBackPressed();
        }

    }

    public BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver(){

        public void onReceive(Context context, Intent intent) {

            PushModel data = (PushModel)intent.getExtras().getSerializable(Constant.PUSH_DATA);

            if (currentFragmentNum == 1) {
                if (data.type.equals("receive_invite") || data.type.equals("accept_friend")) {
//                if (data.type.equals("increase_activity_count") || data.type.equals("accept_friend")) {

                    CheckinFragment.updateFriendRequestState(data.user_id, data.type);
                } else if (data.type.equals("enter_checkin") || data.type.equals("exit_checkin")) {
                    UserModel userModel = new UserModel();
                    userModel.setUser_id(data.user_id);
                    userModel.setFullname(data.fullname);
                    userModel.setAvatar(data.avatar);
                    userModel.setFriendStatus(data.friend_status);
                    CheckinFragment.addNewUser(userModel, data.type);

                } else {
                    CheckinFragment.updateCheckinRequest(data.user_id, data.type);

                }
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 201){
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    Uri conversation_id = data.getParcelableExtra("conversation_id");
                    String user_id = data.getStringExtra("user_id");
                    if (conversation_id != null) {
                        CheckinFragment.updateUserState(user_id, conversation_id);
                    }
                }

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mHandleMessageReceiver);
    }
}
