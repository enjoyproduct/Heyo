package com.heyoe.controller;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
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
import com.facebook.FacebookSdk;
import com.facebook.share.model.AppInviteContent;
import com.facebook.share.widget.AppInviteDialog;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.heyoe.R;
import com.heyoe.controller.adapters.SearchUserAutoCompleteAdapter;
import com.heyoe.controller.fragments.ActivityFragment;
import com.heyoe.controller.fragments.FAQFragment;
import com.heyoe.controller.fragments.MainFragment;
import com.heyoe.controller.fragments.MainMenuFragment;
import com.heyoe.controller.fragments.MoreFriendFragment;
import com.heyoe.controller.fragments.MyBlackFriendsFregment;
import com.heyoe.controller.fragments.MyFriendFragment;
import com.heyoe.controller.fragments.NewPostFragment;
import com.heyoe.controller.fragments.ProfileFragment;
import com.heyoe.controller.fragments.RequestFragment;
import com.heyoe.controller.pushnotifications.GcmServiceManager;
import com.heyoe.controller.qb_chat.chat.ChatHelper;
import com.heyoe.model.API;
import com.heyoe.model.Constant;
import com.heyoe.model.Global;
import com.heyoe.model.PostModel;
import com.heyoe.model.PushModel;
import com.heyoe.model.UserModel;
import com.heyoe.utilities.UIUtility;
import com.heyoe.utilities.Utils;
import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBSession;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.QBSettings;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.model.QBUser;

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
    Toolbar toolbar;
    private static AutoCompleteTextView autoCompleteTextView;
    private static DribSearchView dribSearchView;
    private static MaterialMenuDrawable materialMenu;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private static SearchUserAutoCompleteAdapter searchUserAutoCompleteAdapter;

    private ImageView ivMain, ivFriends, ivNewPost, ivCheckin, ivActivity;
    private RelativeLayout rlMain, rlFriends, rlNewPost, rlCheckin, rlActivity;
    public static DrawerLayout mDrawerLayout;
    private static FragmentManager fragmentManager;
    private static MainMenuFragment mainMenuFragment;
    private static TextView tvTitle;
    private static TextView tvMsgCount;
    private static TextView tvActivityCount;
    private static Activity mActivity;

    private static ArrayList<UserModel> arrAllUsers;

    private static int currentFragmentNum;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

//        GcmServiceManager gcmServiceManager = GcmServiceManager.getInstance();
//        gcmServiceManager.startGcmService(this);

        //INIT QB SDK
        QBSettings.getInstance().init(this, Constant.APP_ID, Constant.AUTH_KEY, Constant.AUTH_SECRET);
        QBSettings.getInstance().setAccountKey(Constant.ACCOUNT_KEY);

        initVariables();
        initUI();

//        getAllUsers();

        qb_login();
    }
    public BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver(){

        public void onReceive(Context context, Intent intent) {

            PushModel data = (PushModel)intent.getExtras().getSerializable(Constant.PUSH_DATA);
            if (data.type.equals("increase_activity_count")) {
                Global.getInstance().increaseActivityCount();
                showActivityBadge();
            }
            if (data.type.equals("increase_message_count")) {
                if (!isCheckinMsg(data.user_id)) {
                    Global.getInstance().increaseMessageCount();
                    showMsgBadge(data.user_id);
                }
            }
            if (data.type.equals("increase_black_message_count")) {
                if (currentFragmentNum == 11 && !data.user_id.equals("")) {
                    MyBlackFriendsFregment.updateUnreadMsgCount(data.user_id);
                }
            }
        }
    };
    private boolean isCheckinMsg(String user_id) {
        boolean flag = false;
        for (UserModel userModel : Global.getInstance().arrCheckinChatUsers) {
            if (userModel.getQb_id().equals(user_id)) {
                flag = true;
                break;
            }
        }
        return flag;
    }
    private void initVariables() {
        fragmentManager = getSupportFragmentManager();
        mActivity = this;
        arrAllUsers = new ArrayList<>();
        LocalBroadcastManager.getInstance(this).registerReceiver(mHandleMessageReceiver, new IntentFilter("pushData"));
    }

    private void initUI() {

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tvTitle = (TextView)findViewById(R.id.tv_home_title);
        this.setTitle("");

        tvMsgCount = (TextView)findViewById(R.id.txt_msg_count);
        tvActivityCount = (TextView)findViewById(R.id.txt_activity_count);

        //init autocomplete search view/////////////////////////////////////////////////




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
                        getAllUsers();
                        showHideSearchView(true);
                        break;
                    case SEARCH:
                        arrAllUsers = new ArrayList<UserModel>();
                        searchUserAutoCompleteAdapter = null;
                        autoCompleteTextView.setAdapter(null);
                        autoCompleteTextView.removeTextChangedListener(textWatcher);
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

        autoCompleteTextView = (AutoCompleteTextView)toolbar.findViewById(R.id.sv_menu);

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
        int pagenumber = getIntent().getIntExtra("page_num", 0);
        if (pagenumber > 4) {
            menuNavigateTo( pagenumber - 5);
        } else {
            navigateTo(pagenumber);
        }

        showActivityBadge();
        showMsgBadge("");
    }
    public static void showActivityBadge() {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                int activityCount = Utils.getIntFromPreference(mActivity, Constant.ACTIVITY_COUNT);
                if (activityCount > 0) {
                    tvActivityCount.setVisibility(View.VISIBLE);
                    tvActivityCount.setText(String.valueOf(activityCount));
                } else {
                    tvActivityCount.setVisibility(View.INVISIBLE);
                }
            }
        });


    }
    public static void showMsgBadge(final String user_id) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                int msgCount = Utils.getIntFromPreference(mActivity, Constant.MSG_COUNT);

                if (msgCount > 0) {
                    tvMsgCount.setVisibility(View.VISIBLE);
                    tvMsgCount.setText("!");
                    if (currentFragmentNum == 1 && !user_id.equals("")) {
                        MyFriendFragment.updateUnreadMsgCount(user_id);
                    }
                } else {
                    tvMsgCount.setVisibility(View.INVISIBLE);
                }
            }
        });


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
        autoCompleteTextView = (AutoCompleteTextView)toolbar.findViewById(R.id.sv_menu);
//        ArrayAdapter<String> adapter = new ArrayAdapter<String>(mActivity ,android.R.text_layout.simple_list_item_1, makeSampleData());
        searchUserAutoCompleteAdapter = new SearchUserAutoCompleteAdapter(mActivity, R.layout.item_search, arrAllUsers);
        autoCompleteTextView.setAdapter(searchUserAutoCompleteAdapter);
        autoCompleteTextView.setThreshold(2);
        autoCompleteTextView.setOnItemClickListener(itemClickListener);
        autoCompleteTextView.setOnEditorActionListener(editorActionListener);
        autoCompleteTextView.addTextChangedListener(textWatcher);
    }
    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String black_pass = Utils.getFromPreference(mActivity, Constant.BLACK_PASSWORD);
            if (s.toString().equals(black_pass) && !black_pass.equals("")) {
//                    Intent intent = new Intent(mActivity, Black_Friend_Activity.class);
//                    startActivity(intent);
                navigateToBlackChat();
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };
    private AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (arrAllUsers.get(position).getFriendStatus().equals("none")) {
                sendFriendRequest(position);
            } else {

            }
        }
    };
    private TextView.OnEditorActionListener editorActionListener = new TextView.OnEditorActionListener() {
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
    };
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
//                                Intent intent = new Intent(mActivity, Black_Friend_Activity.class);
//                                startActivity(intent);
                                navigateToBlackChat();
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
        setOffline("on");
       resetMenu();
    }
    public static void resetMenu() {
        mainMenuFragment = new MainMenuFragment();
        fragmentManager.beginTransaction()
                .replace(R.id.main_menu_container, mainMenuFragment)
                .commit();
        if (currentFragmentNum == 10) {
//            ProfileFragment.setCelebrity();
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
                bundle.putString("hashtag", "");
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
    public static void navigateToBlackChat() {
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, new MyBlackFriendsFregment())
                .commit();
        dribSearchView.setVisibility(View.INVISIBLE);
        currentFragmentNum = 11;
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
                bundle.putString("hashtag", "");
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
                fbInviteDlg();
//                fragmentManager.beginTransaction()
//                        .replace(R.id.fragment_container, new InviteFriendFragment())
//                        .commit();
//                setTitle(mActivity.getResources().getString(R.string.invited_friends));
//                currentFragmentNum = 8;
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
    public static void navigateForHashTag(boolean isFavorite, String hashtag) {
        dribSearchView.setVisibility(View.INVISIBLE);

        Fragment fragment = new MainFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean("isFavorite", isFavorite);
        bundle.putString("hashtag", hashtag);
        fragment.setArguments(bundle);
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();

        setTitle("#" + hashtag);
        if (isFavorite) {
            currentFragmentNum = 5;
        } else {
            currentFragmentNum = 0;
        }
        mDrawerLayout.closeDrawers();
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
            case 101:////from detail post in main
                if (data != null ) {
                    if (resultCode == 41) {
                        String hashtag = data.getStringExtra("hashtag");
                        if (hashtag != null && hashtag.length() > 0) {
                            if (MainFragment.isFavorite) {
                                navigateForHashTag(true, hashtag);
                            } else {
                                navigateForHashTag(false, hashtag);
                            }
                        }
                    } else {
                        PostModel postModel = (PostModel) data.getSerializableExtra("post");
                        if (postModel != null) {
                            ArrayList<PostModel> postModels = new ArrayList<>();
                            postModels.add(postModel);
                            if (resultCode == RESULT_OK) {
                                MainFragment.updatePostFeed(postModels);
                            }
                            if (resultCode == 40) {
                                MainFragment.deletePost(postModels);
                            }

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
            case 104:////from detail post in profile
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
            case 105://from social sharing in profile
                if (resultCode == RESULT_OK) {
                    ProfileFragment.updateSharedCount();
                }
                break;
            case 106: //from chat activity
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        QBDialog dialog = (QBDialog) data.getSerializableExtra("dialog");
                        if (dialog != null) {
                            if (currentFragmentNum == 1) {
                                Global.getInstance().decreaseMessageCount(dialog.getUnreadMessageCount());
                                int msgCount = Utils.getIntFromPreference(mActivity, Constant.MSG_COUNT);
                                if (msgCount == 0) {
                                    tvMsgCount.setVisibility(View.INVISIBLE);
                                }

                                MyFriendFragment.updateUserState(dialog);
                            }
                            if (currentFragmentNum == 11) {
                                MyBlackFriendsFregment.updateUserState(dialog);
                            }
                        }
                    }

                }
                break;
            case 107://from UserListActivity
                if (resultCode == 21) {
                    if (data != null) {
                        String user_id = data.getStringExtra("user_id");
                        HomeActivity.navigateToProfile(user_id);
                    }
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

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mHandleMessageReceiver);

        //logout qb
        if (chatService != null) {
            chatService.logout(new QBEntityCallback<Void>() {
                @Override
                public void onSuccess(Void aVoid, Bundle bundle) {
                    chatService.destroy();
                }

                @Override
                public void onError(QBResponseException e) {
                    Utils.showToast(mActivity, e.getLocalizedMessage());
                }
            });

        }
        super.onDestroy();
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
//        String docken = Utils.getFromPreference(mActivity, Constant.DEVICE_TOKEN);
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
        Utils.saveToPreference(mActivity, Constant.QB_ID, "");
        Utils.saveToPreference(mActivity, Constant.FB_ACCESS_TOKEN, "");
        Utils.saveToPreference(mActivity, Constant.FB_NAME, "");
        Utils.saveToPreference(mActivity, Constant.FB_EMAIL, "");
        Utils.saveToPreference(mActivity, Constant.FB_PHOTO, "");

        Utils.saveIntToPreference(mActivity, Constant.MSG_COUNT, 0);
        Utils.saveIntToPreference(mActivity, Constant.ACTIVITY_COUNT, 0);

        mActivity.startActivity(new Intent(mActivity, SignActivity.class));
        ((HomeActivity)mActivity).finish();
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

    public static void fbInviteDlg() {
        String appLinkUrl, previewImageUrl;
        FacebookSdk.sdkInitialize(mActivity);

        appLinkUrl = "http://www.heyoe.com/";
        previewImageUrl = API.BASE_APP;

        if (AppInviteDialog.canShow()) {
            AppInviteContent content = new AppInviteContent.Builder()
                    .setApplinkUrl(appLinkUrl)
//                    .setPreviewImageUrl(previewImageUrl)
                    .build();
            AppInviteDialog.show(mActivity, content);
        }
    }




    QBChatService chatService;
    private void qb_login() {
        QBUser user = new QBUser(Utils.getFromPreference(mActivity, Constant.EMAIL), Constant.DEFAULT_PASSWORD);
        chatService = QBChatService.getInstance();
        if (!chatService.isLoggedIn()) {
            QBSettings.getInstance().fastConfigInit(Constant.APP_ID, Constant.AUTH_KEY, Constant.AUTH_SECRET);
            final QBUser finalUser = user;
            QBAuth.createSession(user, new QBEntityCallback<QBSession>() {
                @Override
                public void onSuccess(QBSession qbSession, Bundle bundle) {

                    finalUser.setId(qbSession.getUserId());
                    Global.getInstance().qbUser = finalUser;
                    ChatHelper.getInstance().login(finalUser, new QBEntityCallback<Void>() {
                        @Override
                        public void onSuccess(Void result, Bundle bundle) {
//                            Utils.showToast(App.getInstance(), "QB qb_login success");
                        }
                        @Override
                        public void onError(QBResponseException e) {
                            Utils.showToast(App.getInstance(), "QB qb_login failed");
                        }
                    });
                }
                @Override
                public void onError(QBResponseException e) {
                    Utils.showToast(mActivity, e.getLocalizedMessage());
                }
            });

        }
    }

}
