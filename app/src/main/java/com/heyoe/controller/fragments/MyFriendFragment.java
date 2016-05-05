package com.heyoe.controller.fragments;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
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
import com.daimajia.swipe.SimpleSwipeListener;
import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.BaseSwipeAdapter;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.heyoe.R;
import com.heyoe.controller.ChatActivity;
import com.heyoe.controller.ProfileActivity;
import com.heyoe.controller.adapters.MediaAdapter;
import com.heyoe.model.API;
import com.heyoe.model.Constant;
import com.heyoe.model.UserModel;
import com.heyoe.utilities.Utils;
import com.heyoe.utilities.image_downloader.UrlImageViewCallback;
import com.heyoe.utilities.image_downloader.UrlRectangleImageViewHelper;
import com.heyoe.widget.MyCircularImageView;
import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBSession;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBGroupChatManager;
import com.quickblox.chat.QBPrivateChatManager;
import com.quickblox.chat.QBRoster;
import com.quickblox.chat.listeners.QBRosterListener;
import com.quickblox.chat.listeners.QBSubscriptionListener;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.chat.model.QBPresence;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.QBSettings;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A simple {@link Fragment} subclass.
 */
public class MyFriendFragment extends Fragment {
    private ListView lvHome;
    private PullToRefreshListView mPullRefreshHomeListView;
    private FriendAdapter friendAdapter;
    private BlockedFriendAdapter blockedFriendAdapter;
    private Activity mActivity;
    static boolean isLast;
    static int offset;
    private ArrayList<UserModel> arrActiveUsers;
    private ArrayList<UserModel> arrBlockedUsers;
    private Button btnFriend, btnBlocked;
    private int state;
    private int chattingFriendNum;

    public MyFriendFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_my_friend, container, false);
        initVariables();
        initUI(view);
        getFriends();

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

        friendAdapter = new FriendAdapter(arrActiveUsers);
        lvHome.setAdapter(friendAdapter);
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

        CustomRequest signinRequest = new CustomRequest(Request.Method.POST, API.GET_FRIEND_LIST, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
//                        Utils.hideProgress();
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
                                    String qb_id = userObject.getString("qb_id");
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
                                    userModel.setQb_id(qb_id);
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
                                createSession();

//                                friendAdapter = new FriendAdapter(arrActiveUsers);
//                                lvHome.setAdapter(friendAdapter);

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
                                if (state == 0) {
                                    arrActiveUsers.remove(position);

                                    friendAdapter.notifyDataSetChanged();
                                } else {
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

                    if (!arrActiveUsers.get(position).getDialog_id().equals("0")) {
                        deleteDialog(arrActiveUsers.get(position).getDialog_id());
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
                    Intent intent = new Intent(mActivity, ProfileActivity.class);
                    intent.putExtra("user_id", userModel.getUser_id());
                    mActivity.startActivity(intent);
                }
            });

            TextView tvFullname = (TextView)convertView.findViewById(R.id.tv_if_fullname);
            tvFullname.setText(userModel.getFullname());
            tvFullname.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mActivity, ProfileActivity.class);
                    intent.putExtra("user_id", userModel.getUser_id());
                    mActivity.startActivity(intent);
                }
            });
//            TextView tvLastMsg = (TextView)convertView.findViewById(R.id.tv_if_last_msg);

            ImageView ivChat = (ImageView)convertView.findViewById(R.id.iv_if_chat);
            ivChat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    chattingFriendNum = position;
                    Intent intent = new Intent(mActivity, ChatActivity.class);
                    intent.putExtra("userModel", arrActiveUsers.get(position));
                    intent.putExtra("me", user);
                    mActivity.startActivity(intent);
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

    @Override
    public void onResume() {
        super.onResume();
        if (state == 0) {
//            arrActiveUsers.get(chattingFriendNum).setUnreadMsgCount(0);
//            friendAdapter.notifyDataSetChanged();
        }
    }

    private ArrayList<QBUser> arrUsers;
    private QBPrivateChatManager privateChatManager;
    private QBChatService chatService;
    private QBUser user;

    private void createSession()
    {
//        Utils.showProgress(mActivity);
        chatService = QBChatService.getInstance();
        user = new QBUser(Utils.getFromPreference(mActivity, Constant.EMAIL), Constant.DEFAULT_PASSWORD);
        QBSettings.getInstance().fastConfigInit(Constant.APP_ID, Constant.AUTH_KEY, Constant.AUTH_SECRET);
        QBAuth.createSession(user, new QBEntityCallback<QBSession>() {
            @Override
            public void onSuccess(QBSession qbSession, Bundle bundle) {


                user.setId(qbSession.getUserId());

//                fetchUserList();
                getDialogs();
            }

            @Override
            public void onError(QBResponseException e) {
                Utils.hideProgress();

                Utils.showToast(mActivity, e.getLocalizedMessage());
            }
        });
    }
    private void getDialogs()
    {
        QBRequestGetBuilder requestGetBuilder = new QBRequestGetBuilder();
        QBChatService.getChatDialogs(null, requestGetBuilder, new QBEntityCallback<ArrayList<QBDialog>>() {
            @Override
            public void onSuccess(ArrayList<QBDialog> qbDialogs, Bundle bundle) {

                Utils.hideProgress();

                ArrayList<QBDialog> arr = qbDialogs;
                for(int i = 0; i < arrActiveUsers.size(); i ++) {
                    arrActiveUsers.get(i).setUnreadMsgCount(0);
                    arrActiveUsers.get(i).setDialog_id("0");
                    for (QBDialog dialog : arr) {
                        if (dialog.getOccupants().contains(user.getId()) && dialog.getOccupants().contains(Integer.parseInt(arrActiveUsers.get(i).getQb_id()))) {
                            ///get unread message count and dialog id
                            arrActiveUsers.get(i).setUnreadMsgCount(dialog.getUnreadMessageCount());
                            arrActiveUsers.get(i).setDialog_id(dialog.getDialogId());


                            break;
                        }
                    }
                    ///get online status
//                    QBRoster chatRoster = QBChatService.getInstance().getRoster(QBRoster.SubscriptionMode.mutual, subscriptionListener);
//                    chatRoster.addRosterListener(rosterListener);
//                    QBPresence presence = chatRoster.getPresence(Integer.parseInt(arrActiveUsers.get(i).getQb_id()));
//                    if (presence == null) {
//                        // No user in your roster
//                        break;
//                    }
//
//                    if (presence.getType() == QBPresence.Type.online) {
//                        // User is online
//                        arrActiveUsers.get(i).setOnline(true);
//                    }else{
//                        // User is offline
//                        arrActiveUsers.get(i).setOnline(false);
//                    }

                }
                friendAdapter.notifyDataSetChanged();
                mPullRefreshHomeListView.onRefreshComplete();
            }

            @Override
            public void onError(QBResponseException e) {
                Utils.hideProgress();
            }
        });
    }
    private void deleteDialog(String dialogId) {
        QBGroupChatManager groupChatManager = QBChatService.getInstance().getGroupChatManager();
        groupChatManager.deleteDialog(dialogId, new QBEntityCallback<Void>() {

            @Override
            public void onSuccess(Void aVoid, Bundle bundle) {
                Toast.makeText(mActivity, "Cleared chat history successfully", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(QBResponseException errors) {

            }
        });
    }
    QBRosterListener rosterListener = new QBRosterListener() {
        @Override
        public void entriesDeleted(Collection<Integer> userIds) {

        }

        @Override
        public void entriesAdded(Collection<Integer> userIds) {

        }

        @Override
        public void entriesUpdated(Collection<Integer> userIds) {

        }

        @Override
        public void presenceChanged(QBPresence presence) {

        }
    };

    QBSubscriptionListener subscriptionListener = new QBSubscriptionListener() {
        @Override
        public void subscriptionRequested(int userId) {

        }
    };




    private void fetchUserList()
    {   Utils.showProgress(mActivity);
        QBPagedRequestBuilder pagedRequestBuilder = new QBPagedRequestBuilder();
        pagedRequestBuilder.setPage(1);
        pagedRequestBuilder.setPerPage(100);

        arrUsers = new ArrayList<>();





        QBUsers.getUsers(pagedRequestBuilder, new QBEntityCallback<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> qbUsers, Bundle bundle) {
                Utils.hideProgress();
                arrUsers = qbUsers;
                arrUsers.remove(0);
                for (int i = 0; i < arrActiveUsers.size(); i ++) {
                    for (int j = 0; j < arrUsers.size(); j ++) {
                        if (arrUsers.get(j).getEmail().equals(arrActiveUsers.get(i).getEmail())) {

                            break;
                        }
                    }
                }
                for (int i = 0; i < arrBlockedUsers.size(); i ++) {
                    for (int j = 0; j < arrUsers.size(); j ++) {
                        if (arrUsers.get(j).getEmail().equals(arrActiveUsers.get(i).getEmail())) {
                            break;
                        }
                    }
                }

            }

            @Override
            public void onError(QBResponseException e) {
                Utils.hideProgress();
                Utils.showToast(mActivity, e.getLocalizedMessage());
            }
        });
    }
}
