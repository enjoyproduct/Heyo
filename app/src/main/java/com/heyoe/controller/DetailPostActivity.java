package com.heyoe.controller;

import android.app.Activity;
import android.content.Intent;
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
import com.heyoe.controller.fragments.MainFragment;
import com.heyoe.model.PostModel;

public class DetailPostActivity extends AppCompatActivity {

    private ImageButton ibBack;
    private static FragmentManager fragmentManager;
    private static Activity mActivity;
    public static PostModel postModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_post);


        initVariables();
        initUI();

    }
    private void initVariables() {
        fragmentManager = getSupportFragmentManager();
        mActivity = this;
        postModel = (PostModel) getIntent().getSerializableExtra("post");
    }
    private void initUI() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ibBack = (ImageButton)toolbar.findViewById(R.id.ib_back);
        ibBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getIntent();
                intent.putExtra("post", DetailPostFragment.postModel);
//                intent.putExtra("test", "test_value");
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, new DetailPostFragment())
                .commit();
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
//        Intent intent = new Intent();
////        intent.putExtra("post", DetailPostFragment.postModel);
//        intent.putExtra("test", "test_value");
//        setResult(RESULT_OK, intent);
//        finish();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {

            case 102:
                DetailPostFragment.updateSharedCount();
                if (resultCode == RESULT_OK) {

                }
                break;
        }
    }
}
