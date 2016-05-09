package com.heyoe.controller;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.CustomRequest;
import com.android.volley.toolbox.Volley;
import com.balysv.materialmenu.MaterialMenuDrawable;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.heyoe.R;
import com.heyoe.controller.adapters.SearchUserAutoCompleteAdapter;
import com.heyoe.controller.fragments.ActivityFragment;
import com.heyoe.controller.fragments.CheckinFragment;
import com.heyoe.controller.fragments.FAQFragment;
import com.heyoe.controller.fragments.InviteFriendFragment;
import com.heyoe.controller.fragments.MainFragment;
import com.heyoe.controller.fragments.MainMenuFragment;
import com.heyoe.controller.fragments.MoreFriendFragment;
import com.heyoe.controller.fragments.MyFriendFragment;
import com.heyoe.controller.fragments.NewPostFragment;
import com.heyoe.controller.fragments.ProfileFragment;
import com.heyoe.controller.fragments.RequestFragment;
import com.heyoe.controller.pushnotifications.GcmServiceManager;
import com.heyoe.model.API;
import com.heyoe.model.Constant;
import com.heyoe.model.PostModel;
import com.heyoe.model.UserModel;
import com.heyoe.utilities.UIUtility;
import com.heyoe.utilities.Utils;

import org.json.JSONArray;
import org.json.JSONObject;
import org.liuyichen.dribsearch.DribSearchView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HomeActivity extends AppCompatActivity implements
         View.OnClickListener{

//    private ImageButton ibMenu;
//    private SearchView searchView;
    private static AutoCompleteTextView autoCompleteTextView;
    private static DribSearchView dribSearchView;
    private static MaterialMenuDrawable materialMenu;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private static SearchUserAutoCompleteAdapter searchUserAutoCompleteAdapter;

    private ImageView ivMain, ivFriends, ivNewPost, ivCheckin, ivActivity;
    private RelativeLayout rlMain, rlFriends, rlNewPost, rlCheckin, rlActivity;
    public static DrawerLayout mDrawerLayout;
    private static FragmentManager fragmentManager;
    private MainMenuFragment mainMenuFragment;
    private static TextView tvTitle;
    private static Activity mActivity;

    private static ArrayList<UserModel> arrAllUsers;

    private static int currentFragmentNum;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        GcmServiceManager gcmServiceManager = GcmServiceManager.getInstance();
        gcmServiceManager.startGcmService(this);

        initVariables();
        initUI();
        setOffline("on");
        getAllUsers();
    }
    private void initVariables() {
        fragmentManager = getSupportFragmentManager();
        mActivity = this;
        arrAllUsers = new ArrayList<>();

    }

    private void initUI() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tvTitle = (TextView)findViewById(R.id.tv_home_title);
        this.setTitle("");

        //init autocomplete search view/////////////////////////////////////////////////

        autoCompleteTextView = (AutoCompleteTextView)toolbar.findViewById(R.id.sv_menu);


        //init search widget///////////////////////////////////////////////////////////

        dribSearchView = (DribSearchView) findViewById(R.id.dribSearchView);
        dribSearchView.setOnClickSearchListener(new DribSearchView.OnClickSearchListener() {
            @Override
            public void onClickSearch() {
                dribSearchView.changeLine();
                materialMenu.animateIconState(MaterialMenuDrawable.IconState.ARROW, true);
            }
        });
        dribSearchView.setOnChangeListener(new DribSearchView.OnChangeListener() {
            @Override
            public void onChange(DribSearchView.State state) {
                switch (state) {
                    case LINE:
                        showHideSearchView(true);
                        break;
                    case SEARCH:
                       showHideSearchView(false);

                        break;
                }
            }
        });

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dribSearchView.changeSearch();
                materialMenu.animateIconState(MaterialMenuDrawable.IconState.BURGER, true);
                if (materialMenu.getIconState() == MaterialMenuDrawable.IconState.BURGER) {
                    UIUtility.hideSoftKeyboard(mActivity);
                    mDrawerLayout.openDrawer(Gravity.LEFT);
                }

            }
        });

        materialMenu = new MaterialMenuDrawable(this, Color.WHITE, MaterialMenuDrawable.Stroke.EXTRA_THIN);
        toolbar.setNavigationIcon(materialMenu);
        materialMenu.setNeverDrawTouch(true);
        //////////////////////////////////////////////////////////////////



        mDrawerLayout = (DrawerLayout) findViewById(R.id.home_drawerlayout);


        ivMain = (ImageView)findViewById(R.id.iv_home_main);
        ivFriends = (ImageView)findViewById(R.id.iv_home_friend);
        ivNewPost = (ImageView)findViewById(R.id.iv_home_new_post);
        ivCheckin = (ImageView)findViewById(R.id.iv_home_checkin);
        ivActivity = (ImageView)findViewById(R.id.iv_home_activity);

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

//        setupNavigationDrawer(toolbar);

        navigateTo(0);
    }
    private static void showHideSearchView(boolean flag) {
        if (mActivity == null) {
            return;
        }
        if (flag) {
            autoCompleteTextView.setVisibility(View.VISIBLE);
            autoCompleteTextView.setFocusable(true);
            autoCompleteTextView.setFocusableInTouchMode(true);
            autoCompleteTextView.requestFocus();
            UIUtility.showSoftKeyboard(mActivity, autoCompleteTextView);
        } else {
            autoCompleteTextView.setText("");
            UIUtility.hideSoftKeyboard(mActivity);
            autoCompleteTextView.setVisibility(View.GONE);

            dribSearchView.changeSearch();
            materialMenu.animateIconState(MaterialMenuDrawable.IconState.BURGER, true);
        }
    }
    private void initAutoCompleteTextView() {
//        ArrayAdapter<String> adapter = new ArrayAdapter<String>(mActivity ,android.R.layout.simple_list_item_1, makeSampleData());
        searchUserAutoCompleteAdapter = new SearchUserAutoCompleteAdapter(mActivity, R.layout.item_search, arrAllUsers);
        autoCompleteTextView.setAdapter(searchUserAutoCompleteAdapter);
        autoCompleteTextView.setThreshold(2);
        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (arrAllUsers.get(position).getFriendStatus().equals("none")) {
                    sendFriendRequest(position);
                } else {

                }

            }
        });
        autoCompleteTextView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE
                        || actionId == EditorInfo.IME_ACTION_NEXT
                        || actionId == EditorInfo.IME_ACTION_GO
                        || actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_SEND) {
                    String black_pass = autoCompleteTextView.getText().toString().trim();
                    if (Utils.getFromPreference(mActivity, Constant.BLACK_PASSWORD).equals("")
                            && black_pass.length() > 1
                            && black_pass.substring(0,1).equals("*")) {
                        setBlackPassword(black_pass);
                    }
                }
                return false;
            }
        });
        autoCompleteTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String black_pass = Utils.getFromPreference(mActivity, Constant.BLACK_PASSWORD);
                if (s.toString().equals(black_pass) && !black_pass.equals("")) {
                    Intent intent = new Intent(mActivity, Black_Friend_Activity.class);
                    startActivity(intent);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }
    private void setBlackPassword(final String black_pass) {
        Utils.showProgress(mActivity);
        Map<String, String> params = new HashMap<String, String>();
        params.put(Constant.DEVICE_TYPE, Constant.ANDROID);
        params.put(Constant.DEVICE_TOKEN, Utils.getFromPreference(mActivity, Constant.DEVICE_TOKEN));
        params.put("my_id", Utils.getFromPreference(mActivity, Constant.USER_ID));
        params.put("black_password", black_pass);

        CustomRequest signinRequest = new CustomRequest(Request.Method.POST, API.SET_BLACK_PASSWORD, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Utils.hideProgress();
                        try {
                            String status = response.getString("status");
                            if (status.equals("200")) {
                                Utils.saveToPreference(mActivity, Constant.BLACK_PASSWORD, black_pass);
                                Intent intent = new Intent(mActivity, Black_Friend_Activity.class);
                                startActivity(intent);
                            } else  if (status.equals("400")) {
                                Utils.showOKDialog(mActivity, mActivity.getResources().getString(R.string.access_denied));
                            } else if (status.equals("401")) {
                                Utils.showOKDialog(mActivity, mActivity.getResources().getString(R.string.user_not_exist));
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
    @Override
    protected void onResume() {
        super.onResume();
        mainMenuFragment = new MainMenuFragment();
        fragmentManager.beginTransaction()
                .replace(R.id.main_menu_container, mainMenuFragment)
                .commit();
        if (currentFragmentNum == 10) {
            ProfileFragment.setCelebrity();
        }
    }

    private static void setTitle(String title) {
        tvTitle.setText(title);
    }

    public static void navigateTo(int num) {
        dribSearchView.setVisibility(View.VISIBLE);
        setTitle("");

        switch (num) {
            case 0:
                Fragment fragment = new MainFragment();
                Bundle bundle = new Bundle();
                bundle.putBoolean("isFavorite", false);
                fragment.setArguments(bundle);
                fragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .commit();

                currentFragmentNum = 0;
                break;
            case 1:
                showHideSearchView(false);
                fragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, new MyFriendFragment())
                        .commit();
                currentFragmentNum = 1;
                break;
            case 2:
                showHideSearchView(false);
                Bundle bundle1 = new Bundle();
                bundle1.putBoolean("isEdit", false);
                NewPostFragment fragobj = new NewPostFragment();
                fragobj.setArguments(bundle1);
                fragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, fragobj)
                        .commit();
                currentFragmentNum = 2;
                break;
            case 3:
                showHideSearchView(false);
                Intent intent = new Intent(mActivity, UserListActivity.class);
                intent.putExtra("type", "checkin");
                mActivity.startActivity(intent);
                break;
            case 4:
                showHideSearchView(false);
                fragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, new ActivityFragment())
                        .commit();
                currentFragmentNum = 4;
                break;

        }

    }
    public static void navigateToProfile(String userId) {
        Fragment fragment = new ProfileFragment();
        Bundle bundle = new Bundle();
        bundle.putString("user_id", userId);
        fragment.setArguments(bundle);
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
        dribSearchView.setVisibility(View.INVISIBLE);
        setTitle(mActivity.getResources().getString(R.string.profile));
        currentFragmentNum = 10;
        showHideSearchView(false);
        HomeActivity.mDrawerLayout.closeDrawers();
    }

    public static void menuNavigateTo(int num) {
        dribSearchView.setVisibility(View.INVISIBLE);

        switch (num) {
            case 0:
                Fragment fragment = new MainFragment();
                Bundle bundle = new Bundle();
                bundle.putBoolean("isFavorite", true);
                fragment.setArguments(bundle);
                fragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .commit();

                setTitle(mActivity.getResources().getString(R.string.favorite));
                currentFragmentNum = 5;
                break;
            case 1:
                fragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, new RequestFragment())
                        .commit();
                setTitle(mActivity.getResources().getString(R.string.requests));
                currentFragmentNum = 6;
                break;
            case 2:
                fragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, new MoreFriendFragment())
                        .commit();
                setTitle(mActivity.getResources().getString(R.string.more_friends));
                currentFragmentNum = 7;
                break;
            case 3:
                fragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, new InviteFriendFragment())
                        .commit();
                setTitle(mActivity.getResources().getString(R.string.invited_friends));
                currentFragmentNum = 8;
                break;
            case 4:
                fragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, new FAQFragment())
                        .commit();
                currentFragmentNum = 9;
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

        setOffline("off");

        Utils.saveToPreference(mActivity, Constant.DEVICE_TOKEN, "");
        Utils.saveToPreference(mActivity, Constant.USER_ID, "");
        Utils.saveToPreference(mActivity, Constant.EMAIL, "");
        Utils.saveToPreference(mActivity, Constant.PASSWORD, "");
        Utils.saveToPreference(mActivity, Constant.BLACK_PASSWORD, "");
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
//    static MenuItem searchItem;

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
            case 101:////from detail post
                if (data != null ) {
                    PostModel postModel = (PostModel) data.getSerializableExtra("post");
                    if (postModel != null) {
                        ArrayList<PostModel> postModels = new ArrayList<>();
                        postModels.add(postModel);
                        if (resultCode == RESULT_OK) {
                            MainFragment.updatePostFeed(postModels);
                        }
                    }
                }
                break;
            case 102://from social sharing

                if (resultCode == RESULT_OK) {
                    MainFragment.updateSharedCount();
                }
                break;
            case 103:////from profile activity
                if (currentFragmentNum == 0) {
                    navigateTo(0);
                }

                break;
            case 104:////from detail post from profile
                if (data != null) {
                    PostModel postModel = (PostModel) data.getSerializableExtra("post");
                    if (postModel != null) {
                        if (resultCode == RESULT_OK) {
                            ProfileFragment.updatePostFeed(postModel);
                        }
                        if (resultCode == 40) {
                            ProfileFragment.deletePost(postModel);
                        }
                    }
                }
                break;
            case 105://from social sharing from profile
                if (resultCode == RESULT_OK) {
                    ProfileFragment.updateSharedCount();
                }
                break;
        }
    }
    //get all users
    private void getAllUsers() {

        Map<String, String> params = new HashMap<String, String>();
        params.put(Constant.DEVICE_TYPE, Constant.ANDROID);
        params.put(Constant.DEVICE_TOKEN, Utils.getFromPreference(mActivity, Constant.DEVICE_TOKEN));
        params.put("my_id", Utils.getFromPreference(mActivity, Constant.USER_ID));


        CustomRequest signinRequest = new CustomRequest(Request.Method.POST, API.GET_ALL_USERS, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String status = response.getString("status");
                            if (status.equals("200")) {
                                JSONArray jsonArray = response.getJSONArray("data");
                                int userCount = jsonArray.length();
                                for (int i = 0; i < userCount; i ++)  {

                                    JSONObject userObject = jsonArray.getJSONObject(i);

                                    String user_id = userObject.getString("user_id");
                                    String fullname = userObject.getString("fullname");
                                    String email = userObject.getString("email");
                                    String city = userObject.getString("city");
                                    String country = userObject.getString("country");
                                    String birthday = userObject.getString("birthday");
                                    String gender = userObject.getString("gender");
                                    String celebrity = userObject.getString("celebrity");
                                    String about_you = userObject.getString("about_you");
                                    String friend_count = userObject.getString("friend_count");
                                    String avatar = userObject.getString("avatar");
                                    String header_photo_url = userObject.getString("header_photo");
                                    String header_video_url = userObject.getString("header_video");
                                    String friend_status = userObject.getString("friend_status");

                                    UserModel userModel = new UserModel();

                                    userModel.setUser_id(user_id);
                                    userModel.setFullname(fullname);
                                    userModel.setEmail(email);
                                    userModel.setCity(city);
                                    userModel.setCountry(country);
                                    userModel.setBirthday(birthday);
                                    userModel.setGender(gender);
                                    userModel.setCelebrity(celebrity);
                                    userModel.setAbout_you(about_you);
                                    userModel.setFriend_count(friend_count);
                                    userModel.setAvatar(avatar);
                                    userModel.setHeader_photo(header_photo_url);
                                    userModel.setHeader_video(header_video_url);
                                    userModel.setFriendStatus(friend_status);

                                    arrAllUsers.add(userModel);
                                }
                                initAutoCompleteTextView();
                            } else  if (status.equals("400")) {
                                Utils.showOKDialog(mActivity, getResources().getString(R.string.access_denied));
                            } else if (status.equals("402")) {
//                                Utils.showOKDialog(mActivity, getResources().getString(R.string.incorrect_password));
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
    public static void sendFriendRequest(final int position) {
        Utils.showProgress(mActivity);

        Map<String, String> params = new HashMap<String, String>();
        params.put(Constant.DEVICE_TYPE, Constant.ANDROID);
        params.put(Constant.DEVICE_TOKEN, Utils.getFromPreference(mActivity, Constant.DEVICE_TOKEN));
        params.put("my_id", Utils.getFromPreference(mActivity, Constant.USER_ID));
        params.put("user_id", arrAllUsers.get(position).getUser_id());

        CustomRequest signinRequest = new CustomRequest(Request.Method.POST, API.INVITE_FRIEND, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Utils.hideProgress();
                        try {
                            String status = response.getString("status");
                            if (status.equals("200")) {
                                arrAllUsers.get(position).setFriendStatus("invited");
                                searchUserAutoCompleteAdapter.notifyDataSetChanged();
                                Utils.showOKDialog(mActivity, mActivity.getResources().getString(R.string.invite_sucess));
                            } else  if (status.equals("400")) {
                                Utils.showOKDialog(mActivity, mActivity.getResources().getString(R.string.access_denied));
                            } else if (status.equals("402")) {
//                                Utils.showOKDialog(mActivity, getResources().getString(R.string.incorrect_password));
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

    //    if set up this method, cannot close searchview
    public void setupNavigationDrawer(Toolbar toolbar){
        actionBarDrawerToggle = new ActionBarDrawerToggle(this,mDrawerLayout,toolbar,
                R.string.open_drawer,R.string.close_drawer){
            @Override
            public void onDrawerOpened(View drawerView) {
                mDrawerLayout.openDrawer(Gravity.LEFT);
//                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                mDrawerLayout.closeDrawers();
//                super.onDrawerClosed(drawerView);
            }
        };
        mDrawerLayout.setDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
    }

    @Override
    protected void onDestroy() {
        if (Utils.getFromPreference(mActivity, Constant.USER_ID).length() > 0) {
            setOffline("off");
        }
        super.onDestroy();

    }
    public static void setOffline(String status) {

        Map<String, String> params = new HashMap<String, String>();
        params.put(Constant.DEVICE_TYPE, Constant.ANDROID);
        params.put(Constant.DEVICE_TOKEN, Utils.getFromPreference(mActivity, Constant.DEVICE_TOKEN));
        params.put("my_id", Utils.getFromPreference(mActivity, Constant.USER_ID));
        params.put("status", status);

        CustomRequest signinRequest = new CustomRequest(Request.Method.POST, API.SET_OFFLINE, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Utils.hideProgress();
                        try {
                            String status = response.getString("status");
                            if (status.equals("200")) {
                            } else  if (status.equals("400")) {
                                Utils.showOKDialog(mActivity, mActivity.getResources().getString(R.string.access_denied));
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
}
