package com.heyoe.controller;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.support.v7.widget.SearchView;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.heyoe.R;
import com.heyoe.controller.fragments.ActivityFragment;
import com.heyoe.controller.fragments.CalendarFragment;
import com.heyoe.controller.fragments.CheckinChatFragment;
import com.heyoe.controller.fragments.FAQFragment;
import com.heyoe.controller.fragments.FavoriteFragment;
import com.heyoe.controller.fragments.FriendFragment;
import com.heyoe.controller.fragments.GroupFragment;
import com.heyoe.controller.fragments.InviteFriendFragment;
import com.heyoe.controller.fragments.MainFragment;
import com.heyoe.controller.fragments.MainMenuFragment;
import com.heyoe.controller.fragments.MoreFriendFragment;
import com.heyoe.controller.fragments.MyFriendFragment;
import com.heyoe.controller.fragments.NewPostFragment;
import com.heyoe.controller.fragments.RequestFragment;
import com.heyoe.model.Constant;
import com.heyoe.utilities.Utils;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener{

    private ImageButton ibMenu;
//    private SearchView searchView;
//    private AutoCompleteTextView autoCompleteTextView;
    private ImageView ivMain, ivFriends, ivNewPost, ivCheckin, ivActivity;
    public static DrawerLayout mDrawerLayout;
    private static FragmentManager fragmentManager;
    private MainMenuFragment mainMenuFragment;
    private static Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initVariables();
        initUI();
    }
    private void initVariables() {
        fragmentManager = getSupportFragmentManager();
        context = this;
    }
    private void initUI() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ibMenu = (ImageButton)toolbar.findViewById(R.id.ib_menu);
        ibMenu.setOnClickListener(this);
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

        ivMain.setOnClickListener(this);
        ivFriends.setOnClickListener(this);
        ivNewPost.setOnClickListener(this);
        ivCheckin.setOnClickListener(this);
        ivActivity.setOnClickListener(this);

        navigateTo(0);
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
                        .replace(R.id.fragment_container, new MyFriendFragment())
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
                fragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, new FavoriteFragment())
                        .commit();
                break;
            case 1:
//                fragmentManager.beginTransaction()
//                        .replace(R.id.fragment_container, new FriendFragment())
//                        .commit();
                break;
            case 2:
                fragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, new RequestFragment())
                        .commit();
                break;
            case 3:
                fragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, new MoreFriendFragment())
                        .commit();
                break;
            case 4:
                fragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, new InviteFriendFragment())
                        .commit();
                break;
            case 5:
//                fragmentManager.beginTransaction()
//                        .replace(R.id.fragment_container, new GroupFragment())
//                        .commit();
                break;
            case 6:
//                fragmentManager.beginTransaction()
//                        .replace(R.id.fragment_container, new CalendarFragment())
//                        .commit();
                break;
            case 7:
//                fragmentManager.beginTransaction()
//                        .replace(R.id.fragment_container, new FAQFragment())
//                        .commit();
                break;
            case 8:
                showSignOutAlert();
                break;

        }
        mDrawerLayout.closeDrawers();
    }
    public static void showSignOutAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getResources().getString(R.string.app_name));
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
        Utils.saveToPreference(context, Constant.USER_ID, "");
        Utils.saveToPreference(context, Constant.EMAIL, "");
        Utils.saveToPreference(context, Constant.PASSWORD, "");
        Utils.saveToPreference(context, Constant.FULLNAME, "");
        Utils.saveToPreference(context, Constant.CITY, "");
        Utils.saveToPreference(context, Constant.COUNTRY, "");
        Utils.saveToPreference(context, Constant.BIRTHDAY, "");
        Utils.saveToPreference(context, Constant.GENDER, "");
        Utils.saveToPreference(context, Constant.CELEBRITY, "");
        Utils.saveToPreference(context, Constant.ABOUT_ME, "");
        Utils.saveToPreference(context, Constant.MEDIA_COUNT, "");
        Utils.saveToPreference(context, Constant.FRIEND_COUNT, "");
        Utils.saveToPreference(context, Constant.AVATAR, "");
        Utils.saveToPreference(context, Constant.HEADER_PHOTO, "");
        Utils.saveToPreference(context, Constant.HEADER_VIDEO, "");
        Utils.saveToPreference(context, Constant.FB_ACCESS_TOKEN, "");
        Utils.saveToPreference(context, Constant.FB_NAME, "");
        Utils.saveToPreference(context, Constant.FB_EMAIL, "");
        Utils.saveToPreference(context, Constant.FB_PHOTO, "");

        context.startActivity(new Intent(context, SignActivity.class));
        ((HomeActivity)context).finish();
    }
    @Override
    public void onClick(View v) {
        if (v == ibMenu) {
            mDrawerLayout.openDrawer(Gravity.LEFT);
        }
//        if (v == ibNewPost) {
//            navigateTo(2);
//        }
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
    MenuItem searchItem;
    MenuItem composeItem;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_home, menu);
        searchItem = menu.findItem(R.id.action_search);
        composeItem = menu.findItem(R.id.ic_compose);

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
        } else if (id == R.id.ic_compose) {
            navigateTo(2);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constant.REQUEST_PLACE_PICKER
                && resultCode == Activity.RESULT_OK) {

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
    }
}
