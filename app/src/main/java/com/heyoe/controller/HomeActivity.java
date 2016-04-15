package com.heyoe.controller;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SearchView;

import com.heyoe.R;
import com.heyoe.controller.fragments.ActivityFragment;
import com.heyoe.controller.fragments.CheckinChatFragment;
import com.heyoe.controller.fragments.FriendFragment;
import com.heyoe.controller.fragments.MainFragment;
import com.heyoe.controller.fragments.NewPostFragment;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener{

    private ImageButton ibMenu, ibNewPost;
    private SearchView searchView;
    private ImageView ivMain, ivFriends, ivNewPost, ivCheckin, ivActivity;
    public static DrawerLayout mDrawerLayout;
    private static FragmentManager fragmentManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initVariables();
        initUI();
    }
    private void initVariables() {
        fragmentManager = getSupportFragmentManager();
    }
    private void initUI() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ibMenu = (ImageButton)toolbar.findViewById(R.id.ib_menu);
        ibMenu.setOnClickListener(this);
        ibNewPost = (ImageButton)toolbar.findViewById(R.id.ib_menu_new_compose);
        ibNewPost.setOnClickListener(this);
        searchView = (SearchView)toolbar.findViewById(R.id.sv_menu);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.home_drawerlayout);

        ivMain = (ImageView)findViewById(R.id.iv_home_main);
        ivFriends = (ImageView)findViewById(R.id.iv_home_main);
        ivNewPost = (ImageView)findViewById(R.id.iv_home_main);
        ivCheckin = (ImageView)findViewById(R.id.iv_home_main);
        ivActivity = (ImageView)findViewById(R.id.iv_home_main);

        ivMain.setOnClickListener(this);
        ivFriends.setOnClickListener(this);
        ivNewPost.setOnClickListener(this);
        ivCheckin.setOnClickListener(this);
        ivActivity.setOnClickListener(this);
    }

    public static void navigateTo(int num) {
        switch (num) {
            case 0:
                fragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, new MainFragment())
                        .commit();
                break;
            case 1:
                fragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, new FriendFragment())
                        .commit();
                break;
            case 2:
                fragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, new NewPostFragment())
                        .commit();
                break;
            case 3:
                fragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, new CheckinChatFragment())
                        .commit();
                break;
            case 4:
                fragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, new ActivityFragment())
                        .commit();
                break;
        }
    }
    public static void menuNavigateTo(int num) {
        switch (num) {
            case 0:
                break;
            case 1:
                break;
            case 2:
                break;
            case 3:
                break;
            case 4:
                break;
            case 5:
                break;
            case 6:
                break;
            case 7:
                break;
            case 8:
                break;

        }
        mDrawerLayout.closeDrawers();
    }

    @Override
    public void onClick(View v) {
        if (v == ibMenu) {
            mDrawerLayout.openDrawer(Gravity.LEFT);
        }
        if (v == ibNewPost) {
            navigateTo(2);
        }
        if (v == ivMain) {
            navigateTo(0);
        }
        if (v == ivFriends) {
            navigateTo(1);
        }
        if (v == ivNewPost) {
            navigateTo(2);
        }
        if (v == ivCheckin) {
            navigateTo(3);
        }
        if (v == ivActivity) {
            navigateTo(4);
        }
    }
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_home, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
}
