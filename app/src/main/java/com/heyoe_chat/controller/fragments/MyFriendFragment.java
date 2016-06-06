package com.heyoe_chat.controller.fragments;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.CustomRequest;
import com.android.volley.toolbox.Volley;
import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.BaseSwipeAdapter;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.heyoe_chat.R;
import com.heyoe_chat.controller.App;
import com.heyoe_chat.controller.HomeActivity;
import com.heyoe_chat.controller.layer_chat.MessagesListActivity;
import com.heyoe_chat.controller.push.GcmBroadcastReceiver;
import com.heyoe_chat.model.API;
import com.heyoe_chat.model.Constant;
import com.heyoe_chat.model.Global;
import com.heyoe_chat.model.UserModel;
import com.heyoe_chat.utilities.TimeUtility;
import com.heyoe_chat.utilities.Utils;
import com.heyoe_chat.utilities.image_downloader.UrlImageViewCallback;
import com.heyoe_chat.utilities.image_downloader.UrlRectangleImageViewHelper;
import com.heyoe_chat.widget.MyCircularImageView;
import com.layer.atlas.util.Util;
import com.layer.sdk.LayerClient;
import com.layer.sdk.exceptions.LayerConversationException;
import com.layer.sdk.messaging.Conversation;
import com.layer.sdk.query.Query;
import com.layer.sdk.query.SortDescriptor;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class MyFriendFragment extends Fragment  {
    private ListView lvHome;
    private PullToRefreshListView mPullRefreshHomeListView;
    private static FriendAdapter friendAdapter;
    private BlockedFriendAdapter blockedFriendAdapter;
    private static Activity mActivity;
    static boolean isLast;
    static int offset;
    private static ArrayList<UserModel> arrActiveUsers;
    private ArrayList<UserModel> arrBlockedUsers;
    private Button btnFriend, btnBlocked;
    private int state;
    private int chattingFriendNum;

    List<Conversation> arrConversations;
    private void getAllConversations() {
        arrConversations = new ArrayList<>();

        //Fetch all conversations, sorted by latest message received
        Query query = Query.builder(Conversation.class)
                .sortDescriptor(new SortDescriptor(Conversation.Property.LAST_MESSAGE_RECEIVED_AT, SortDescriptor.Order.DESCENDING))
                .build();

        arrConversations = App.getLayerClient().executeQuery(query, Query.ResultType.OBJECTS);
        Utils.hideProgress();
        addConversation();
    }
    private void addConversation() {
        //get dialog of active friends
        for (int i = 0; i < arrActiveUsers.size(); i++) {
            arrActiveUsers.get(i).setUnreadMsgCount(0);
            arrActiveUsers.get(i).setLastMsgSentTime(0);
            for (int j = 0; j < arrConversations.size(); j ++) {
                Conversation conversation = arrConversations.get(j);
                if (conversation.getParticipants().contains(arrActiveUsers.get(i).getUser_id())) {

                    ///get unread message count and dialog id
                    arrActiveUsers.get(i).setUnreadMsgCount(conversation.getTotalUnreadMessageCount());
                    arrActiveUsers.get(i).setConversation(conversation);
                    arrActiveUsers.get(i).setLastMsgSentTime(conversation.getLastMessage().getSentAt().getTime());
                    break;
                }
            }
        }
        friendAdapter = new FriendAdapter(arrActiveUsers);
        lvHome.setAdapter(friendAdapter);

        for (int i = 0; i < arrBlockedUsers.size(); i++) {
            arrBlockedUsers.get(i).setUnreadMsgCount(0);
            arrBlockedUsers.get(i).setLastMsgSentTime(0);
            for (int j = 0; j < arrConversations.size(); j ++) {
                Conversation conversation = arrConversations.get(j);
                if (conversation.getParticipants().contains(arrBlockedUsers.get(i).getUser_id())) {
                    ///get unread message count and dialog id
                    arrBlockedUsers.get(i).setUnreadMsgCount(conversation.getTotalUnreadMessageCount());
                    arrBlockedUsers.get(i).setConversation(conversation);
                    arrBlockedUsers.get(i).setLastMsgSentTime(TimeUtility.getTimeStampFromString(conversation.getLastMessage().getSentAt().toString()));
                    break;
                }
            }

        }
        if (state == 0) {
//                    ArrayList<UserModel> temp = new ArrayList<UserModel>();
//                    temp.addAll(Global.getInstance().qsortUsersByMsgDate(arrActiveUsers));
//                    arrActiveUsers = new ArrayList<UserModel>();
//                    arrActiveUsers.addAll(temp);


        } else {
            blockedFriendAdapter = new BlockedFriendAdapter(arrBlockedUsers);
            lvHome.setAdapter(blockedFriendAdapter);
        }
        mPullRefreshHomeListView.onRefreshComplete();
    }
    private Conversation createConversation(String friend_id) {
        Conversation conversation;
        try {
            // Try creating a new distinct conversation with the given user
            conversation = App.getLayerClient().newConversation(Arrays.asList(friend_id));
        } catch (LayerConversationException e) {
            // If a distinct conversation with the given user already exists, use that one instead
            conversation = e.getConversation();
        }
        return conversation;
    }



    public static void updateUnreadMsgCount(String userId, String conversation_id) {
        for(int i = 0; i < arrActiveUsers.size(); i ++ ) {
            if (arrActiveUsers.get(i).getUser_id().equals(userId) ) {
                int unreadMsgCount = arrActiveUsers.get(i).getUnreadMsgCount();
                unreadMsgCount ++;
                arrActiveUsers.get(i).setUnreadMsgCount(unreadMsgCount);
                friendAdapter.notifyDataSetChanged();
                break;
            }
        }
    }

    public MyFriendFragment() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the text_layout for this fragment
        View view = inflater.inflate(R.layout.fragment_my_friend, container, false);
        initVariables();
        initUI(view);
        getFriends();
        clearBadge();
        return view;
    }
    private void initVariables() {
        mActivity = getActivity();
        isLast = false;
        offset = 0;
        arrActiveUsers = new ArrayList<>();
        arrBlockedUsers = new ArrayList<>();
        state = 0;
        chattingFriendNum = 0;
    }
    private void initUI(View view) {
        btnFriend = (Button)view.findViewById(R.id.btn_my_friend);
        btnBlocked = (Button)view.findViewById(R.id.btn_my_friend_blocked);

        btnFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                btnBlocked.setBackgroundColor(mActivity.getResources().getColor(R.color.grey));
                btnBlocked.setTextColor(mActivity.getResources().getColor(R.color.white));
                btnFriend.setBackgroundColor(mActivity.getResources().getColor(R.color.white));
                btnFriend.setTextColor(mActivity.getResources().getColor(R.color.green));

                state = 0;
                friendAdapter = new FriendAdapter(arrActiveUsers);
                lvHome.setAdapter(friendAdapter);

            }
        });
        btnBlocked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                btnFriend.setBackgroundColor(mActivity.getResources().getColor(R.color.grey));
                btnFriend.setTextColor(mActivity.getResources().getColor(R.color.white));
                btnBlocked.setBackgroundColor(mActivity.getResources().getColor(R.color.white));
                btnBlocked.setTextColor(mActivity.getResources().getColor(R.color.green));

                state = 1;
                blockedFriendAdapter = new BlockedFriendAdapter(arrBlockedUsers);
                lvHome.setAdapter(blockedFriendAdapter);
            }
        });

        ///create listview
        mPullRefreshHomeListView = (PullToRefreshListView) view.findViewById(R.id.lv_my_friend_list);
        mPullRefreshHomeListView.setOnLastItemVisibleListener(new PullToRefreshBase.OnLastItemVisibleListener() {

            @Override
            public void onLastItemVisible() {
                if (!isLast) {
                }
                mPullRefreshHomeListView.onRefreshComplete();

            }
        });
        mPullRefreshHomeListView.setOnPullEventListener(new PullToRefreshBase.OnPullEventListener<ListView>() {
            @Override
            public void onPullEvent(PullToRefreshBase<ListView> refreshView, PullToRefreshBase.State state, PullToRefreshBase.Mode direction) {
                if (state == PullToRefreshBase.State.RELEASE_TO_REFRESH && direction == PullToRefreshBase.Mode.PULL_FROM_START) {
                    getFriends();
                }
            }
        });
        lvHome = mPullRefreshHomeListView.getRefreshableView();
        lvHome.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });



    }
    private void clearBadge() {
//        Global.getInstance().decreaseMessageCount(count);
        Utils.saveIntToPreference(App.getInstance(), Constant.MSG_COUNT, 0);
        HomeActivity.showMsgBadge("", "");
    }
    private void getFriends() {
        Utils.showProgress(mActivity);

        arrActiveUsers.clear();
        arrBlockedUsers.clear();

        Map<String, String> params = new HashMap<String, String>();
        params.put(Constant.DEVICE_TYPE, Constant.ANDROID);
        params.put(Constant.DEVICE_TOKEN, Utils.getFromPreference(mActivity, Constant.DEVICE_TOKEN));
        params.put("my_id", Utils.getFromPreference(mActivity, Constant.USER_ID));
        params.put("user_id", Utils.getFromPreference(mActivity, Constant.USER_ID));

        CustomRequest signinRequest = new CustomRequest(Request.Method.POST, API.GET_MY_FRIEND_LIST, params,
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
                                    String blacker_id = userObject.getString("blacker_id");
                                    String online_status = userObject.getString("online_status");

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
                                    userModel.setBlacker_id(blacker_id);

                                    if (online_status.equals("on")) {
                                        userModel.setOnline(true);
                                    } else {
                                        userModel.setOnline(false);
                                    }
                                    String friend_status = userObject.getString("status");
                                    if (friend_status.equals("active")) {
                                        arrActiveUsers.add(userModel);
                                    } else {
                                        arrBlockedUsers.add(userModel);
                                    }
                                }
                                getAllConversations();

                            } else  if (status.equals("400")) {
                                Utils.showOKDialog(mActivity, getResources().getString(R.string.access_denied));
                            } else if (status.equals("402")) {
//                                Utils.showOKDialog(mActivity, getResources().getString(R.string.incorrect_password));
                            }
                        }catch (Exception e) {
                            Utils.hideProgress();
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
    private void blockFriend(final int position) {

        Utils.showProgress(mActivity);

        Map<String, String> params = new HashMap<String, String>();
        params.put(Constant.DEVICE_TYPE, Constant.ANDROID);
        params.put(Constant.DEVICE_TOKEN, Utils.getFromPreference(mActivity, Constant.DEVICE_TOKEN));
        params.put("my_id", Utils.getFromPreference(mActivity, Constant.USER_ID));
        params.put("friend_id", arrActiveUsers.get(position).getUser_id());

        CustomRequest signinRequest = new CustomRequest(Request.Method.POST, API.BLOCK_FRIEND, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Utils.hideProgress();
                        try {
                            String status = response.getString("status");
                            if (status.equals("200")) {
                                arrBlockedUsers.add(0, arrActiveUsers.get(position));
                                arrActiveUsers.remove(position);

                                friendAdapter.notifyDataSetChanged();

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
    private void deleteFriend(final int position) {

        Utils.showProgress(mActivity);

        Map<String, String> params = new HashMap<String, String>();
        params.put(Constant.DEVICE_TYPE, Constant.ANDROID);
        params.put(Constant.DEVICE_TOKEN, Utils.getFromPreference(mActivity, Constant.DEVICE_TOKEN));
        params.put("my_id", Utils.getFromPreference(mActivity, Constant.USER_ID));
        if (state == 0) {
            params.put("friend_id", arrActiveUsers.get(position).getUser_id());
        } else {
            params.put("friend_id", arrBlockedUsers.get(position).getUser_id());
        }


        CustomRequest signinRequest = new CustomRequest(Request.Method.POST, API.DELETE_FRIEND, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Utils.hideProgress();
                        try {
                            String status = response.getString("status");
                            if (status.equals("200")) {
                                Utils.showToast(mActivity, "Deleted friend successfully");
                                if (state == 0) {
                                    deleteChatOfActiveFriend(position);
                                    arrActiveUsers.remove(position);
                                    friendAdapter.notifyDataSetChanged();
                                } else {
                                    deleteChatOfBlockedFriend(position);
                                    arrBlockedUsers.remove(position);
                                    blockedFriendAdapter.notifyDataSetChanged();
                                }
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
    private void recoverFriend(final int position) {

        Utils.showProgress(mActivity);

        Map<String, String> params = new HashMap<String, String>();
        params.put(Constant.DEVICE_TYPE, Constant.ANDROID);
        params.put(Constant.DEVICE_TOKEN, Utils.getFromPreference(mActivity, Constant.DEVICE_TOKEN));
        params.put("my_id", Utils.getFromPreference(mActivity, Constant.USER_ID));
        params.put("friend_id", arrBlockedUsers.get(position).getUser_id());

        CustomRequest signinRequest = new CustomRequest(Request.Method.POST, API.RECOVER_FRIEND, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Utils.hideProgress();
                        try {
                            String status = response.getString("status");
                            if (status.equals("200")) {
                                arrActiveUsers.add(0, arrBlockedUsers.get(position));
                                arrBlockedUsers.remove(position);
                                blockedFriendAdapter.notifyDataSetChanged();

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
    private void addToBlackList(final int position) {
        Utils.showProgress(mActivity);

        Map<String, String> params = new HashMap<String, String>();
        params.put(Constant.DEVICE_TYPE, Constant.ANDROID);
        params.put(Constant.DEVICE_TOKEN, Utils.getFromPreference(mActivity, Constant.DEVICE_TOKEN));
        params.put("my_id", Utils.getFromPreference(mActivity, Constant.USER_ID));
        params.put("friend_id", arrActiveUsers.get(position).getUser_id());

        CustomRequest signinRequest = new CustomRequest(Request.Method.POST, API.ADD_BLACK_FRIEND, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Utils.hideProgress();
                        try {
                            String status = response.getString("status");
                            if (status.equals("200")) {
                                arrActiveUsers.remove(position);

                                friendAdapter.notifyDataSetChanged();

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
    public static void updateUserState(String user_id, Uri conversation_id) {
        for (int i = 0; i < arrActiveUsers.size(); i ++) {
            if (arrActiveUsers.get(i).getUser_id().equals(user_id)) {
                if(arrActiveUsers.get(i).getConversation() != null) {
                    Global.getInstance().decreaseMessageCount(arrActiveUsers.get(i).getConversation().getTotalUnreadMessageCount());
                    HomeActivity.showMsgBadge("", "");
                }
                ///
                Conversation conversation = App.getLayerClient().getConversation(conversation_id);
                arrActiveUsers.get(i).setConversation(conversation);
                arrActiveUsers.get(i).setUnreadMsgCount(0);
                ///

            }
        }
        friendAdapter.notifyDataSetChanged();
    }



    public class FriendAdapter extends BaseSwipeAdapter {

        LayoutInflater mlayoutInflater;
        ArrayList<UserModel> arrFriends;
        public FriendAdapter (ArrayList<UserModel> arrFriends) {
            mlayoutInflater = LayoutInflater.from(mActivity);
            this.arrFriends = arrFriends;
        }
        @Override
        public int getCount() {
            return arrFriends.size();
        }

        @Override
        public Object getItem(int position) {
            return arrFriends.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public int getSwipeLayoutResourceId(int position) {
            return R.id.swipe_friend;
        }

        @Override
        public View generateView(final int position, final ViewGroup parent) {


            final View view = LayoutInflater.from(mActivity).inflate(R.layout.item_friend, null);

            SwipeLayout swipeLayout = (SwipeLayout)view.findViewById(getSwipeLayoutResourceId(position));
            swipeLayout.setShowMode(SwipeLayout.ShowMode.LayDown);
            swipeLayout.addDrag(SwipeLayout.DragEdge.Right, swipeLayout.findViewWithTag("item_friend"));

            final ImageView ivNav = (ImageView)view.findViewById(R.id.iv_if_close_swipe);
            final ImageView ivChat = (ImageView)view.findViewById(R.id.iv_if_chat);
            final TextView tvUnreadMsgCount = (TextView)view.findViewById(R.id.tv_if_unreadmsgcount);

            ivNav.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mItemManger.closeAllItems();
                }
            });
            swipeLayout.findViewById(R.id.clear_chat_history).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                if (arrActiveUsers.get(position).getConversation() != null) {
                    Utils.showToast(mActivity, "Cleared chat history successfully");
                    deleteChatOfActiveFriend(position);
                }
                mItemManger.closeAllItems();
                }
            });

            swipeLayout.findViewById(R.id.block_friend).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    blockFriend(position);
                    mItemManger.closeAllItems();
                }
            });

            swipeLayout.findViewById(R.id.delete_friend).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteFriend(position);
                    mItemManger.closeAllItems();
                }
            });
            swipeLayout.findViewById(R.id.black_chat).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addToBlackList(position);
                    mItemManger.closeAllItems();
                }
            });
            swipeLayout.addSwipeListener(new SwipeLayout.SwipeListener() {
                @Override
                public void onStartOpen(SwipeLayout layout) {

                }

                @Override
                public void onOpen(SwipeLayout layout) {
                    ivChat.setVisibility(View.GONE);
                    tvUnreadMsgCount.setVisibility(View.GONE);
                    ivNav.setVisibility(View.VISIBLE);
                }

                @Override
                public void onStartClose(SwipeLayout layout) {

                }

                @Override
                public void onClose(SwipeLayout layout) {

                    ivChat.setVisibility(View.VISIBLE);
                    tvUnreadMsgCount.setVisibility(View.VISIBLE);
                    ivNav.setVisibility(View.GONE);
                }

                @Override
                public void onUpdate(SwipeLayout layout, int leftOffset, int topOffset) {

                }

                @Override
                public void onHandRelease(SwipeLayout layout, float xvel, float yvel) {

                }
            });

            return view;
        }

        @Override
        public void fillValues(final int position, View convertView) {

            final UserModel userModel = arrFriends.get(position);
            MyCircularImageView myCircularImageView = (MyCircularImageView)convertView.findViewById(R.id.civ_if_avatar);
            if (!userModel.getAvatar().equals("")) {
                UrlRectangleImageViewHelper.setUrlDrawable(myCircularImageView, API.BASE_AVATAR + arrFriends.get(position).getAvatar(), R.drawable.default_user, new UrlImageViewCallback() {
                    @Override
                    public void onLoaded(ImageView imageView, Bitmap loadedBitmap, String url, boolean loadedFromCache) {
                        if (!loadedFromCache) {
                            ScaleAnimation scale = new ScaleAnimation(0, 1, 0, 1, ScaleAnimation.RELATIVE_TO_SELF, .5f, ScaleAnimation.RELATIVE_TO_SELF, .5f);
                            scale.setDuration(10);
                            scale.setInterpolator(new OvershootInterpolator());
                            imageView.startAnimation(scale);
                        }
                    }
                });
            } else {
                myCircularImageView.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.default_user));
            }
            myCircularImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    HomeActivity.navigateToProfile(userModel.getUser_id());
                }
            });

            TextView tvFullname = (TextView)convertView.findViewById(R.id.tv_if_fullname);
            tvFullname.setText(userModel.getFullname());
            tvFullname.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    HomeActivity.navigateToProfile(userModel.getUser_id());
                }
            });

            ImageView ivChat = (ImageView)convertView.findViewById(R.id.iv_if_chat);
            ivChat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    chattingFriendNum = position;
                    if (Integer.parseInt(arrActiveUsers.get(position).getBlacker_id()) == 0
                            || Integer.parseInt(arrActiveUsers.get(position).getBlacker_id()) == Integer.parseInt(Utils.getFromPreference(mActivity, Constant.USER_ID))) {
                        Utils.saveToPreference(mActivity, "is_black", "white");
                    } else {
                        Utils.saveToPreference(mActivity, "is_black", "black");
                    }

                    Intent intent = new Intent(mActivity, MessagesListActivity.class);
                    if (arrActiveUsers.get(position).getConversation() != null) {
                        intent.putExtra(GcmBroadcastReceiver.LAYER_CONVERSATION_KEY, arrActiveUsers.get(position).getConversation().getId());
                    }
                    intent.putExtra("user_id",arrActiveUsers.get(position).getUser_id());
                    intent.putExtra("avatar", arrActiveUsers.get(position).getAvatar());
                    intent.putExtra("fullname", arrActiveUsers.get(position).getFullname());
                    intent.putExtra("blacker_id", Integer.parseInt(arrActiveUsers.get(position).getBlacker_id()));
                    intent.putExtra("page", "white_chat");
                    mActivity.startActivityForResult(intent, 106);
                }
            });
            ImageView ivCloseNav = (ImageView)convertView.findViewById(R.id.iv_if_close_swipe);

            TextView tvUnreadMsgCount = (TextView)convertView.findViewById(R.id.tv_if_unreadmsgcount);
            tvUnreadMsgCount.setVisibility(View.VISIBLE);
            tvUnreadMsgCount.setText(String.valueOf(userModel.getUnreadMsgCount()));

            ImageView ivOnline = (ImageView)convertView.findViewById(R.id.iv_if_online);
            if (userModel.isOnline()) {
                ivOnline.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.ic_green_circle));
            } else {
                ivOnline.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.ic_grey_circle));
            }
        }


    }
    public class BlockedFriendAdapter extends BaseSwipeAdapter {

        LayoutInflater mlayoutInflater;
        ArrayList<UserModel> arrFriends;
        public BlockedFriendAdapter (ArrayList<UserModel> arrFriends) {
            mlayoutInflater = LayoutInflater.from(mActivity);
            this.arrFriends = arrFriends;
        }
        @Override
        public int getCount() {
            return arrFriends.size();
        }

        @Override
        public Object getItem(int position) {
            return arrFriends.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public int getSwipeLayoutResourceId(int position) {
            return R.id.swipe_friend_blocked;
        }

        @Override
        public View generateView(final int position, ViewGroup parent) {

            final View view = LayoutInflater.from(mActivity).inflate(R.layout.item_friend_blocked, null);
            SwipeLayout swipeLayout = (SwipeLayout)view.findViewById(getSwipeLayoutResourceId(position));
            swipeLayout.setShowMode(SwipeLayout.ShowMode.LayDown);
            swipeLayout.addDrag(SwipeLayout.DragEdge.Right, swipeLayout.findViewWithTag("item_friend"));

            final ImageView ivNav = (ImageView) view.findViewById(R.id.iv_if_close_swipe);
            ivNav.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mItemManger.closeAllItems();
                }
            });
            swipeLayout.findViewById(R.id.block_friend).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    recoverFriend(position);
                    mItemManger.closeAllItems();
                }
            });

            swipeLayout.findViewById(R.id.delete_friend).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteFriend(position);
                    mItemManger.closeAllItems();
                }
            });

            swipeLayout.addSwipeListener(new SwipeLayout.SwipeListener() {
                @Override
                public void onStartOpen(SwipeLayout layout) {

                }

                @Override
                public void onOpen(SwipeLayout layout) {


                    ivNav.setVisibility(View.VISIBLE);
                }

                @Override
                public void onStartClose(SwipeLayout layout) {

                }

                @Override
                public void onClose(SwipeLayout layout) {

                    ivNav.setVisibility(View.GONE);
                }

                @Override
                public void onUpdate(SwipeLayout layout, int leftOffset, int topOffset) {

                }

                @Override
                public void onHandRelease(SwipeLayout layout, float xvel, float yvel) {

                }
            });
            return view;
        }

        @Override
        public void fillValues(int position, View convertView) {

            UserModel userModel = arrFriends.get(position);
            MyCircularImageView myCircularImageView = (MyCircularImageView)convertView.findViewById(R.id.civ_if_avatar);
            if (!userModel.getAvatar().equals("")) {
                UrlRectangleImageViewHelper.setUrlDrawable(myCircularImageView, API.BASE_AVATAR + arrFriends.get(position).getAvatar(), R.drawable.default_user, new UrlImageViewCallback() {
                    @Override
                    public void onLoaded(ImageView imageView, Bitmap loadedBitmap, String url, boolean loadedFromCache) {
                        if (!loadedFromCache) {
                            ScaleAnimation scale = new ScaleAnimation(0, 1, 0, 1, ScaleAnimation.RELATIVE_TO_SELF, .5f, ScaleAnimation.RELATIVE_TO_SELF, .5f);
                            scale.setDuration(10);
                            scale.setInterpolator(new OvershootInterpolator());
                            imageView.startAnimation(scale);
                        }
                    }
                });
            } else {
                myCircularImageView.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.default_user));
            }
            TextView tvFullname = (TextView)convertView.findViewById(R.id.tv_if_fullname);
            tvFullname.setText(userModel.getFullname());
            TextView tvLastMsg = (TextView)convertView.findViewById(R.id.tv_if_last_msg);


        }


    }

    private void deleteChatOfBlockedFriend(final int position) {
        if (arrBlockedUsers.get(position).getConversation() == null) {
            return;
        }
        clear_chat_history(position, "block");

    }
    private void deleteChatOfActiveFriend(final int position) {
        if (arrActiveUsers.get(position).getConversation() == null) {
            return;
        }
        clear_chat_history(position, "active");

    }
    private void clear_chat_history(int position, String type) {
        Conversation conversation = null;
        if (type.equals("active")) {
            conversation = arrActiveUsers.get(position).getConversation();
        } else {
            conversation = arrBlockedUsers.get(position).getConversation();
        }
        if (conversation != null) {
            conversation.delete(LayerClient.DeletionMode.ALL_PARTICIPANTS);
        }
    }
   
}
