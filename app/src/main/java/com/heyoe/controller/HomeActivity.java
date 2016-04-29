package com.heyoe.controller;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.support.v7.widget.SearchView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.heyoe.R;
import com.heyoe.controller.fragments.ActivityFragment;
import com.heyoe.controller.fragments.CheckinChatFragment;
import com.heyoe.controller.fragments.FavoriteFragment;
import com.heyoe.controller.fragments.InviteFriendFragment;
import com.heyoe.controller.fragments.MainFragment;
import com.heyoe.controller.fragments.MainMenuFragment;
import com.heyoe.controller.fragments.MoreFriendFragment;
import com.heyoe.controller.fragments.MyFriendFragment;
import com.heyoe.controller.fragments.NewPostFragment;
import com.heyoe.controller.fragments.RequestFragment;
import com.heyoe.controller.pushnotifications.GcmServiceManager;
import com.heyoe.model.Constant;
import com.heyoe.model.PostModel;
import com.heyoe.utilities.Utils;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener{

    private ImageButton ibMenu;
//    private SearchView searchView;
//    private AutoCompleteTextView autoCompleteTextView;
    private ImageView ivMain, ivFriends, ivNewPost, ivCheckin, ivActivity;
    private RelativeLayout rlMain, rlFriends, rlNewPost, rlCheckin, rlActivity;
    public static DrawerLayout mDrawerLayout;
    private static FragmentManager fragmentManager;
    private MainMenuFragment mainMenuFragment;
    private static TextView tvTitle;
    private static Activity mActivity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        GcmServiceManager gcmServiceManager = GcmServiceManager.getInstance();
        gcmServiceManager.startGcmService(this);

        initVariables();
        initUI();
    }
    private void initVariables() {
        fragmentManager = getSupportFragmentManager();
        mActivity = this;
    }
    private void initUI() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ibMenu = (ImageButton)toolbar.findViewById(R.id.ib_menu);
        ibMenu.setOnClickListener(this);
        tvTitle = (TextView)findViewById(R.id.tv_home_title);
        this.setTitle("");

//        ibNewPost = (ImageButton)toolbar.findViewById(R.id.ib_menu_new_compose);
//        ibNewPost.setOnClickListener(this);
//        searchView = (SearchView)toolbar.findViewById(R.id.sv_menu);

//        autoCompleteTextView = (AutoCompleteTextView)toolbar.findViewById(R.id.sv_menu);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.home_drawerlayout);
        mainMenuFragment = new MainMenuFragment();
        fragmentManager.beginTransaction()
                .replace(R.id.main_menu_container, mainMenuFragment)
                .commit();

        ivMain = (ImageView)findViewById(R.id.iv_home_main);
        ivFriends = (ImageView)findViewById(R.id.iv_home_friend);
        ivNewPost = (ImageView)findViewById(R.id.iv_home_new_post);
        ivCheckin = (ImageView)findViewById(R.id.iv_home_checkin);
        ivActivity = (ImageView)findViewById(R.id.iv_home_activity);

//        ivMain.setOnClickListener(this);
//        ivFriends.setOnClickListener(this);
//        ivNewPost.setOnClickListener(this);
//        ivCheckin.setOnClickListener(this);
//        ivActivity.setOnClickListener(this);

        rlMain = (RelativeLayout)findViewById(R.id.rl_home_main);
        rlFriends = (RelativeLayout)findViewById(R.id.rl_home_friend);
        rlNewPost = (RelativeLayout)findViewById(R.id.rl_home_post);
        rlCheckin = (RelativeLayout)findViewById(R.id.rl_home_checkin);
        rlActivity = (RelativeLayout)findViewById(R.id.rl_home_activity);

        rlMain.setOnClickListener(this);
        rlFriends.setOnClickListener(this);
        rlNewPost.setOnClickListener(this);
        rlCheckin.setOnClickListener(this);
        rlActivity.setOnClickListener(this);

        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, new MainFragment())
                .commit();
    }
    private static void setTitle(String title) {
        tvTitle.setText(title);

    }

    public static void navigateTo(int num) {
        switch (num) {
            case 0:
                fragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, new MainFragment())
                        .commit();
                searchItem.setVisible(true);
                setTitle("");
                break;
            case 1:
                fragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, new MyFriendFragment())
                        .commit();
                searchItem.setVisible(true);
                setTitle("");
                break;
            case 2:
                fragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, new NewPostFragment())
                        .commit();
                searchItem.setVisible(true);
                setTitle("");
                break;
            case 3:
                fragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, new CheckinChatFragment())
                        .commit();
                searchItem.setVisible(true);
                setTitle("");
                break;
            case 4:
                fragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, new ActivityFragment())
                        .commit();
                searchItem.setVisible(true);
                setTitle("");
                break;
        }
    }

    public static void menuNavigateTo(int num) {
        switch (num) {
            case 0:
                fragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, new FavoriteFragment())
                        .commit();
                searchItem.setVisible(false);
                setTitle(mActivity.getResources().getString(R.string.favorite));

                break;
            case 1:
                fragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, new RequestFragment())
                        .commit();
                searchItem.setVisible(false);
                setTitle(mActivity.getResources().getString(R.string.requests));
                break;
            case 2:
                fragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, new MoreFriendFragment())
                        .commit();
                searchItem.setVisible(false);
                setTitle(mActivity.getResources().getString(R.string.more_friends));
                break;
            case 3:
                fragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, new InviteFriendFragment())
                        .commit();
                searchItem.setVisible(false);
                setTitle(mActivity.getResources().getString(R.string.invited_friends));
                break;
            case 4:
//                fragmentManager.beginTransaction()
//                        .replace(R.id.fragment_container, new FAQFragment())
//                        .commit();
//                searchItem.setVisible(false);
                mActivity.startActivity(new Intent(mActivity, DetailPostActivity.class));
                break;
            case 5:
                showSignOutAlert();
                break;

        }
        mDrawerLayout.closeDrawers();
    }
    public static void showSignOutAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle(mActivity.getResources().getString(R.string.app_name));
        builder.setMessage("Do you want to log out?");
        builder.setCancelable(true);
        builder.setPositiveButton("Log Out",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        sign_out();
                    }
                });
        builder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
    }
    private static void sign_out() {
        Utils.saveToPreference(mActivity, Constant.DEVICE_TOKEN, "");
        Utils.saveToPreference(mActivity, Constant.USER_ID, "");
        Utils.saveToPreference(mActivity, Constant.EMAIL, "");
        Utils.saveToPreference(mActivity, Constant.PASSWORD, "");
        Utils.saveToPreference(mActivity, Constant.FULLNAME, "");
        Utils.saveToPreference(mActivity, Constant.CITY, "");
        Utils.saveToPreference(mActivity, Constant.COUNTRY, "");
        Utils.saveToPreference(mActivity, Constant.BIRTHDAY, "");
        Utils.saveToPreference(mActivity, Constant.GENDER, "");
        Utils.saveToPreference(mActivity, Constant.CELEBRITY, "");
        Utils.saveToPreference(mActivity, Constant.ABOUT_ME, "");
        Utils.saveToPreference(mActivity, Constant.MEDIA_COUNT, "");
        Utils.saveToPreference(mActivity, Constant.FRIEND_COUNT, "");
        Utils.saveToPreference(mActivity, Constant.AVATAR, "");
        Utils.saveToPreference(mActivity, Constant.HEADER_PHOTO, "");
        Utils.saveToPreference(mActivity, Constant.HEADER_VIDEO, "");
        Utils.saveToPreference(mActivity, Constant.FB_ACCESS_TOKEN, "");
        Utils.saveToPreference(mActivity, Constant.FB_NAME, "");
        Utils.saveToPreference(mActivity, Constant.FB_EMAIL, "");
        Utils.saveToPreference(mActivity, Constant.FB_PHOTO, "");

        mActivity.startActivity(new Intent(mActivity, SignActivity.class));
        ((HomeActivity)mActivity).finish();
    }
    @Override
    public void onClick(View v) {
        if (v == ibMenu) {
            mDrawerLayout.openDrawer(Gravity.LEFT);
        }

        if (v == rlMain) {
            navigateTo(0);
        }
        if (v == rlFriends) {
            navigateTo(1);
        }
        if (v == rlNewPost) {
            navigateTo(2);
        }
        if (v == rlCheckin) {
            navigateTo(3);
        }
        if (v == rlActivity) {
            navigateTo(4);
        }
    }
    static MenuItem searchItem;
//    MenuItem composeItem;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_home, menu);
        searchItem = menu.findItem(R.id.action_search);

        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // perform query here
                // workaround to avoid issues with some emulators and keyboard devices firing twice if a keyboard enter is used
                // see https://code.google.com/p/android/issues/detail?id=24599
                searchView.clearFocus();
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.length() == 0) {

                }
                return false;
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                return true;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constant.REQUEST_PLACE_PICKER
                && resultCode == RESULT_OK) {

            // The user has selected a place. Extract the name and address.
            final Place place = PlacePicker.getPlace(data, this);

            final CharSequence name = place.getName();
            final CharSequence address = place.getAddress();
            String attributions = PlacePicker.getAttributions(data);
            Utils.showOKDialog(this, (String)name + (String)address + attributions);
            if (attributions == null) {
                attributions = "";
            }

        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
        switch (requestCode) {
            case 101:
                if (data != null) {
//                    String test = data.getStringExtra("test");
                    PostModel postModel = (PostModel) data.getSerializableExtra("post");
                    if (postModel != null) {
                        MainFragment.updatePostFeed(postModel);
                    }

                }
                break;
            case 102:
                MainFragment.updateSharedCount();
                if (resultCode == 0) {

                }
                break;
        }
    }
}
