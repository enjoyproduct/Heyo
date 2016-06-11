package com.heyoe_chat.controller;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
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
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.heyoe_chat.R;
import com.heyoe_chat.controller.adapters.SearchUserAutoCompleteAdapter;
import com.heyoe_chat.controller.fragments.ActivityFragment;
import com.heyoe_chat.controller.fragments.FAQFragment;
import com.heyoe_chat.controller.fragments.MainFragment;
import com.heyoe_chat.controller.fragments.MainMenuFragment;
import com.heyoe_chat.controller.fragments.MoreFriendFragment;
import com.heyoe_chat.controller.fragments.MyBlackFriendsFregment;
import com.heyoe_chat.controller.fragments.MyFriendFragment;
import com.heyoe_chat.controller.fragments.NewPostFragment;
import com.heyoe_chat.controller.fragments.ProfileFragment;
import com.heyoe_chat.controller.fragments.RequestFragment;
import com.heyoe_chat.controller.layer_chat.DemoAuthenticationProvider;
import com.heyoe_chat.controller.layer_chat.util.AuthenticationProvider;
import com.heyoe_chat.controller.layer_chat.util.Log;
import com.heyoe_chat.model.API;
import com.heyoe_chat.model.Constant;
import com.heyoe_chat.model.Global;
import com.heyoe_chat.model.PostModel;
import com.heyoe_chat.model.PushModel;
import com.heyoe_chat.model.UserModel;
import com.heyoe_chat.utilities.UIUtility;
import com.heyoe_chat.utilities.Utils;
import com.layer.atlas.util.Util;
import com.layer.sdk.LayerClient;

import org.json.JSONArray;
import org.json.JSONObject;
import org.liuyichen.dribsearch.DribSearchView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HomeActivity extends AppCompatActivity implements
        View.OnClickListener {

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


    public static LayerClient layerClient;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initVariables();
        checkPermission();
        initUI();
        login_layer(Utils.getFromPreference(this, Constant.USER_ID));
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    private final static int PERMISSION_REQUEST_CODE_FOR_READ_EXTERNAL_STORAGE = 301;

    public static void checkPermission() {
        int permissionCheck = ContextCompat.checkSelfPermission(mActivity, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {

        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(mActivity,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(mActivity,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_CODE_FOR_READ_EXTERNAL_STORAGE);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE_FOR_READ_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Utils.showOKDialog(this, "You should allow this permission to use full functions of this app.");
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void initVariables() {
        fragmentManager = getSupportFragmentManager();
        mActivity = this;
        arrAllUsers = new ArrayList<>();
        LocalBroadcastManager.getInstance(this).registerReceiver(mHandleMessageReceiver, new IntentFilter("pushData"));
    }

    protected void onResume() {

        setOffline("on");
        resetMenu();

        super.onResume();
    }

    private void login_layer(final String user_id) {
        Utils.showProgress(this);
        App.authenticate(new DemoAuthenticationProvider.Credentials(App.getLayerAppId(), user_id),
                new AuthenticationProvider.Callback() {
                    @Override
                    public void onSuccess(AuthenticationProvider provider, String userId) {
                        Utils.hideProgress();
                        if (Log.isLoggable(Log.VERBOSE)) {
                            Log.v("Successfully authenticated as `" + user_id + "` with userId `" + userId + "`");
                        }
                    }

                    @Override
                    public void onError(AuthenticationProvider provider, final String error) {
                        Utils.hideProgress();
                        if (Log.isLoggable(Log.ERROR)) {
                            Log.e("Failed to authenticate as `" + user_id + "`: " + error);
                        }
                    }
                });
    }

    private void logout_layer() {
        App.deauthenticate(new Util.DeauthenticationCallback() {
            @Override
            public void onDeauthenticationSuccess(LayerClient client) {
                if (Log.isLoggable(Log.VERBOSE)) {
                    Log.v("Successfully deauthenticated");
                }
//                App.routeLogin(HomeActivity.this);
            }

            @Override
            public void onDeauthenticationFailed(LayerClient client, String reason) {
                if (Log.isLoggable(Log.ERROR)) {
                    Log.e("Failed to deauthenticate: " + reason);
                }
                Toast.makeText(HomeActivity.this, getString(R.string.toast_failed_to_deauthenticate, reason), Toast.LENGTH_SHORT).show();
            }
        });
    }


    public BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {

            PushModel data = (PushModel) intent.getExtras().getSerializable(Constant.PUSH_DATA);
            if (data.type.equals("increase_activity_count") ||
                    data.type.equals("receive_invite") ||
                    data.type.equals("reject_invite") ||
                    data.type.equals("accept_friend")) {
                if (data.receiver_id.equals(Utils.getFromPreference(mActivity, Constant.USER_ID))) {
                    Global.getInstance().increaseActivityCount();
                    showActivityBadge();
                }

            }
            if (data.type.equals("increase_message_count")) {
                if (!isCheckinMsg(data.user_id)) {
                    if (Global.getInstance().currentChattingUserId != null) {
                        if (Global.getInstance().currentChattingUserId.equals(data.user_id)) {
                            return;
                        }
                    }
                    Global.getInstance().increaseMessageCount();
                    showMsgBadge(data.user_id, data.conversation_id);

                }
            }
            if (data.type.equals("increase_black_message_count")) {
                if (currentFragmentNum == 11 && !data.user_id.equals("")) {
                    if (Global.getInstance().currentChattingUserId != null) {
                        if (Global.getInstance().currentChattingUserId.equals(data.user_id)) {
                            return;
                        }
                    }

                    MyBlackFriendsFregment.updateUnreadMsgCount(data.user_id, data.conversation_id);

                }
            }
        }
    };

    private boolean isCheckinMsg(String user_id) {
        boolean flag = false;
        for (UserModel userModel : Global.getInstance().arrCheckinChatUsers) {
            if (userModel.getUser_id().equals(user_id)) {
                flag = true;
                break;
            }
        }
        return flag;
    }

    private void initUI() {

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tvTitle = (TextView) findViewById(R.id.tv_home_title);
        this.setTitle("");

        tvMsgCount = (TextView) findViewById(R.id.txt_msg_count);
        tvActivityCount = (TextView) findViewById(R.id.txt_activity_count);

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

        autoCompleteTextView = (AutoCompleteTextView) toolbar.findViewById(R.id.sv_menu);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.home_drawerlayout);


        ivMain = (ImageView) findViewById(R.id.iv_home_main);
        ivFriends = (ImageView) findViewById(R.id.iv_home_friend);
        ivNewPost = (ImageView) findViewById(R.id.iv_home_new_post);
        ivCheckin = (ImageView) findViewById(R.id.iv_home_checkin);
        ivActivity = (ImageView) findViewById(R.id.iv_home_activity);

        rlMain = (RelativeLayout) findViewById(R.id.rl_home_main);
        rlFriends = (RelativeLayout) findViewById(R.id.rl_home_friend);
        rlNewPost = (RelativeLayout) findViewById(R.id.rl_home_post);
        rlCheckin = (RelativeLayout) findViewById(R.id.rl_home_checkin);
        rlActivity = (RelativeLayout) findViewById(R.id.rl_home_activity);

        rlMain.setOnClickListener(this);
        rlFriends.setOnClickListener(this);
        rlNewPost.setOnClickListener(this);
        rlCheckin.setOnClickListener(this);
        rlActivity.setOnClickListener(this);

//        setupNavigationDrawer(toolbar);
        int pagenumber = getIntent().getIntExtra("page_num", 0);
        if (pagenumber > 4) {
            menuNavigateTo(pagenumber - 5);
        } else {
            navigateTo(pagenumber);
        }

        showActivityBadge();
        showMsgBadge("", "");
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

    public static void showMsgBadge(final String user_id, final String conversation_id) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                int msgCount = Utils.getIntFromPreference(mActivity, Constant.MSG_COUNT);

                if (msgCount > 0) {
                    tvMsgCount.setVisibility(View.VISIBLE);
                    tvMsgCount.setText("!");
                    if (currentFragmentNum == 1 && !user_id.equals("")) {
                        MyFriendFragment.updateUnreadMsgCount(user_id, conversation_id);
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
        autoCompleteTextView = (AutoCompleteTextView) toolbar.findViewById(R.id.sv_menu);
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
                        && black_pass.substring(0, 1).equals("*")) {
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
                            } else if (status.equals("400")) {
                                Utils.showOKDialog(mActivity, mActivity.getResources().getString(R.string.access_denied));
                            } else if (status.equals("401")) {
                                Utils.showOKDialog(mActivity, mActivity.getResources().getString(R.string.user_not_exist));
                            }
                        } catch (Exception e) {
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
                bundle1.putInt("isEdit", 0);
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
                mActivity.startActivityForResult(intent, 107);
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

    public static void navigateToRepost(PostModel postModel) {
        showHideSearchView(false);
        dribSearchView.setVisibility(View.VISIBLE);
        setTitle("");
        Bundle bundle1 = new Bundle();
        bundle1.putInt("isEdit", 2);
        bundle1.putSerializable("post", postModel);
        NewPostFragment fragobj = new NewPostFragment();
        fragobj.setArguments(bundle1);
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragobj)
                .commit();
        currentFragmentNum = 12;
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
            Utils.showOKDialog(this, (String) name + (String) address + attributions);
            if (attributions == null) {
                attributions = "";
            }

        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
        switch (requestCode) {
            case 101:////from detail post in main
                if (data != null) {
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

                if (resultCode == RESULT_OK || resultCode == RESULT_CANCELED) {
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
                        Global.getInstance().currentChattingUserId = null;
                        Uri conversation_id = data.getParcelableExtra("conversation_id");
                        String user_id = data.getStringExtra("user_id");
                        if (conversation_id != null) {
                            if (currentFragmentNum == 1) {
                                MyFriendFragment.updateUserState(user_id, conversation_id);
                            }
                            if (currentFragmentNum == 11) {
                                MyBlackFriendsFregment.updateUserState(user_id, conversation_id);
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
                if (resultCode == 22) {
                    menuNavigateTo(1);
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
                                for (int i = 0; i < userCount; i++) {

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
                            } else if (status.equals("400")) {
                                Utils.showOKDialog(mActivity, getResources().getString(R.string.access_denied));
                            } else if (status.equals("402")) {
//                                Utils.showOKDialog(mActivity, getResources().getString(R.string.incorrect_password));
                            }
                        } catch (Exception e) {
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
                            } else if (status.equals("400")) {
                                Utils.showOKDialog(mActivity, mActivity.getResources().getString(R.string.access_denied));
                            } else if (status.equals("402")) {
//                                Utils.showOKDialog(mActivity, getResources().getString(R.string.incorrect_password));
                            }
                        } catch (Exception e) {
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
    public void setupNavigationDrawer(Toolbar toolbar) {
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar,
                R.string.open_drawer, R.string.close_drawer) {
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
        logout_layer();
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
        Utils.saveToPreference(mActivity, Constant.FB_ACCESS_TOKEN, "");
        Utils.saveToPreference(mActivity, Constant.FB_NAME, "");
        Utils.saveToPreference(mActivity, Constant.FB_EMAIL, "");
        Utils.saveToPreference(mActivity, Constant.FB_PHOTO, "");

        Utils.saveIntToPreference(mActivity, Constant.MSG_COUNT, 0);
        Utils.saveIntToPreference(mActivity, Constant.ACTIVITY_COUNT, 0);

        mActivity.startActivity(new Intent(mActivity, SignActivity.class));
        ((HomeActivity) mActivity).finish();
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
                            } else if (status.equals("400")) {
                                Utils.showOKDialog(mActivity, mActivity.getResources().getString(R.string.access_denied));
                            } else if (status.equals("402")) {
                            }
                        } catch (Exception e) {
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


    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Home Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.heyoe_chat/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Home Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.heyoe_chat/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }
}
