package com.heyoe_chat.controller;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.CustomRequest;
import com.android.volley.toolbox.Volley;
import com.heyoe_chat.R;
import com.heyoe_chat.controller.fragments.FriendListFragment;
import com.heyoe_chat.controller.fragments.MediaFragment;
import com.heyoe_chat.controller.fragments.ProfileInfoFragment;
import com.heyoe_chat.model.API;
import com.heyoe_chat.model.Constant;
import com.heyoe_chat.model.UserModel;
import com.heyoe_chat.utilities.Utils;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private static FragmentManager fragmentManager;
    public static UserModel userModel;
    private static Activity mActivity;
    private static TextView tvTitle;
    private static ImageButton ibAddFriend;

//    public static String userId;
    private int toWhere; //0; profile info, 1: media , 2: friend list
    String friend_status;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        initVariables();
        initUI();

    }
    private void getFriendStatus() {
        Map<String, String> params = new HashMap<String, String>();
        params.put(Constant.DEVICE_TYPE, Constant.ANDROID);
        params.put(Constant.DEVICE_TOKEN, Utils.getFromPreference(mActivity, Constant.DEVICE_TOKEN));
        params.put("my_id", Utils.getFromPreference(mActivity, Constant.USER_ID));
        params.put("user_id", userModel.getUser_id());

        CustomRequest signinRequest = new CustomRequest(Request.Method.POST, API.GET_FRIEND_STATUS, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Utils.hideProgress();
                        try {
                            String status = response.getString("status");
                            if (status.equals("200")) {
                                friend_status = response.getString("friend_status");
                                if (!friend_status.equals("friend") && !friend_status.equals("celebrity")) {
                                    ibAddFriend.setVisibility(View.VISIBLE);
                                    if (friend_status.equals("invited")) {
                                        ibAddFriend.setImageDrawable(getResources().getDrawable(R.drawable.sandglass_white));
                                    }
                                }
                            } else  if (status.equals("400")) {
                                Utils.showOKDialog(mActivity, getResources().getString(R.string.access_denied));
                            } else if (status.equals("402")) {
                            }
                        }catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Utils.hideProgress();
                        Toast.makeText(mActivity, error.toString(), Toast.LENGTH_LONG).show();
                    }
                });
        RequestQueue requestQueue = Volley.newRequestQueue(mActivity);
        requestQueue.add(signinRequest);
    }

    private void initVariables() {
        mActivity = this;
        fragmentManager = getSupportFragmentManager();
//        userId = getIntent().getStringExtra("user_id");
        userModel = (UserModel)getIntent().getSerializableExtra("userModel");
        toWhere = getIntent().getIntExtra("to_where", 0);
    }
    private void initUI() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        tvTitle = (TextView)toolbar.findViewById(R.id.tv_home_title);

        ImageButton ibBack = (ImageButton)toolbar.findViewById(R.id.ib_back);
        ibBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popFragment();
            }
        });

        pushFragment(toWhere);

    }
    public static void setTitle(String title) {
        tvTitle.setText(title);
    }
    public static void pushFragment(int num) {
        switch (num) {
//            case 0:
//                setTitle(mActivity.getResources().getString(R.string.profile));
//                fragmentManager.beginTransaction()
//                        .add(R.id.fragment_container, new ProfileFragment())
//                        .addToBackStack("profile")
//                        .commit();
//                break;
            case 0:
                setTitle("");
                fragmentManager.beginTransaction()
                        .add(R.id.fragment_container, new ProfileInfoFragment())
                        .addToBackStack("profileinfo")
                        .commit();
                break;
            case 1:
                setTitle(mActivity.getResources().getString(R.string.media));
                fragmentManager.beginTransaction()
                        .add(R.id.fragment_container, new MediaFragment())
                        .addToBackStack("media")
                        .commit();
                break;
            case 2:
                setTitle(mActivity.getResources().getString(R.string.Friends));
                fragmentManager.beginTransaction()
                        .add(R.id.fragment_container, new FriendListFragment())
                        .addToBackStack("friend")
                        .commit();
                break;

        }
    }
    public static void popFragment() {
        int fragmentCount = fragmentManager.getBackStackEntryCount();
        if (fragmentCount > 1) {
            fragmentManager.beginTransaction()
                    .remove(fragmentManager.getFragments().get(fragmentCount - 1))
                    .commit();
            fragmentManager.popBackStack();
        } else {
            mActivity.finish();
        }


    }

    @Override
    public void onBackPressed() {
        popFragment();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
//            case 101:
//                if (data != null) {
//                    PostModel postModel = (PostModel) data.getSerializableExtra("post");
//                    if (postModel != null) {
//                        if (resultCode == RESULT_OK) {
//                            ProfileFragment.updatePostFeed(postModel);
//                        }
//                        if (resultCode == 40) {
//                            ProfileFragment.deletePost(postModel);
//                        }
//                    }
//                }
//                break;
//            case 102:
//
//                if (resultCode == RESULT_OK) {
//                    ProfileFragment.updateSharedCount();
//                }
//                break;
        }
    }
}
