package com.heyoe_chat.controller.fragments;


import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.CustomRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.heyoe_chat.R;
import com.heyoe_chat.controller.App;
import com.heyoe_chat.controller.HomeActivity;
import com.heyoe_chat.controller.UserListActivity;
import com.heyoe_chat.controller.layer_chat.MessagesListActivity;
import com.heyoe_chat.controller.push.GcmBroadcastReceiver;
import com.heyoe_chat.model.API;
import com.heyoe_chat.model.Constant;
import com.heyoe_chat.model.Global;
import com.heyoe_chat.model.UserModel;
import com.heyoe_chat.utilities.Utils;
import com.heyoe_chat.utilities.image_downloader.UrlImageViewCallback;
import com.heyoe_chat.utilities.image_downloader.UrlRectangleImageViewHelper;
import com.heyoe_chat.widget.AttachmentPreviewAdapterView;
import com.heyoe_chat.widget.MyCircularImageView;
import com.layer.sdk.LayerClient;
import com.layer.sdk.exceptions.LayerConversationException;
import com.layer.sdk.messaging.Conversation;
import com.layer.sdk.query.Query;
import com.layer.sdk.query.SortDescriptor;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBPrivateChatManager;

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
public class CheckinFragment extends Fragment {

    private static ListView lvHome;
    private static PullToRefreshListView mPullRefreshHomeListView;
    private static CheckinUserAdapter checkinUserAdapter;
    private static Activity mActivity;
    static boolean isLast;
    static int offset;
    private static ArrayList<UserModel> arrUsers;

    String checkin;

    public CheckinFragment() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the text_layout for this fragment
        View view = inflater.inflate(R.layout.fragment_checkin_user, container, false);

        initVariables();
        initUI(view);
        onCheckinClick();
        return view;
    }
    private void initVariables() {
        mActivity = getActivity();
        isLast = false;
        offset = 0;
        arrUsers = new ArrayList<>();
        checkin = "";

    }
    private void initUI(View view) {
        ///create listview
        mPullRefreshHomeListView = (PullToRefreshListView) view.findViewById(R.id.lv_checkin_user);

        lvHome = mPullRefreshHomeListView.getRefreshableView();
        lvHome.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });
        checkinUserAdapter = new CheckinUserAdapter(arrUsers);
        lvHome.setAdapter(checkinUserAdapter);
    }

    //    get google place
    public void onCheckinClick() {
        // Construct an intent for the place picker
        try {
            PlacePicker.IntentBuilder intentBuilder = new PlacePicker.IntentBuilder();
            Intent intent = intentBuilder.build(mActivity);
            // Start the intent by requesting a result,
            // identified by a request code.
            startActivityForResult(intent, Constant.REQUEST_PLACE_PICKER);

        } catch (GooglePlayServicesRepairableException e) {
            GooglePlayServicesUtil.getErrorDialog(e.getConnectionStatusCode(), getActivity(), 0);
        } catch (GooglePlayServicesNotAvailableException e) {
            Toast.makeText(getActivity(), "Google Play Services is not available.",
                    Toast.LENGTH_LONG)
                    .show();
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constant.REQUEST_PLACE_PICKER) {
            if (resultCode == Activity.RESULT_OK) {
                // The user has selected a place. Extract the name and address.
                final Place place = PlacePicker.getPlace(data, mActivity);

                final CharSequence name = place.getName();
                final CharSequence address = place.getAddress();
                String attributions = PlacePicker.getAttributions(data);
                checkin = name.toString();
                UserListActivity.setTitle(checkin);
                enterCheckin();
            } else if (resultCode == Activity.RESULT_CANCELED) {
                mActivity.finish();
            }
        }
    }
    public static void updateUserState(String user_id, Uri conversation_id) {
        for (int i = 0; i < arrUsers.size(); i ++) {
            if (arrUsers.get(i).getUser_id().equals(user_id)) {
                if(arrUsers.get(i).getConversation() != null) {
                    Global.getInstance().decreaseMessageCount(arrUsers.get(i).getConversation().getTotalUnreadMessageCount());
                    HomeActivity.showMsgBadge("", "");
                }
                ///
                Conversation conversation = App.getLayerClient().getConversation(conversation_id);
                arrUsers.get(i).setConversation(conversation);
                arrUsers.get(i).setUnreadMsgCount(0);
                ///

            }
        }
        checkinUserAdapter.notifyDataSetChanged();
    }

    static List<Conversation> arrConversations;
    private static void getAllConversations() {
        arrConversations = new ArrayList<>();

        //Fetch all conversations, sorted by latest message received
        Query query = Query.builder(Conversation.class)
                .sortDescriptor(new SortDescriptor(Conversation.Property.LAST_MESSAGE_RECEIVED_AT, SortDescriptor.Order.DESCENDING))
                .build();

        arrConversations = App.getLayerClient().executeQuery(query, Query.ResultType.OBJECTS);
        Utils.hideProgress();
        addConversation();
    }
    private static void addConversation() {
        //get dialog of active friends
        for (int i = 0; i < arrUsers.size(); i++) {
            arrUsers.get(i).setUnreadMsgCount(0);
            arrUsers.get(i).setLastMsgSentTime(0);
            for (int j = 0; j < arrConversations.size(); j ++) {
                Conversation conversation = arrConversations.get(j);
                if (conversation.getParticipants().contains(arrUsers.get(i).getUser_id())) {

                    ///get unread message count and dialog id
                    arrUsers.get(i).setUnreadMsgCount(conversation.getTotalUnreadMessageCount());
                    arrUsers.get(i).setConversation(conversation);
                    arrUsers.get(i).setLastMsgSentTime(conversation.getLastMessage().getSentAt().getTime());
                    break;
                }
            }


        }
        checkinUserAdapter = new CheckinUserAdapter(arrUsers);
        lvHome.setAdapter(checkinUserAdapter);
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

    private void getCheckinUsers() {
        Utils.showProgress(mActivity);

        Map<String, String> params = new HashMap<String, String>();
        params.put(Constant.DEVICE_TYPE, Constant.ANDROID);
        params.put(Constant.DEVICE_TOKEN, Utils.getFromPreference(mActivity, Constant.DEVICE_TOKEN));
        params.put("user_id", Utils.getFromPreference(mActivity, Constant.USER_ID));
        params.put("checkin", checkin);
        params.put("offset", String.valueOf(offset));

        CustomRequest signinRequest = new CustomRequest(Request.Method.POST, API.GET_CHECKIN_USERS, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String status = response.getString("status");
                            if (status.equals("200")) {
                                JSONArray jsonArray = response.getJSONArray("data");
                                int userCount = jsonArray.length();
//                                if (userCount == 0) {
//                                    isLast = true;
//                                }
//                                offset ++;
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
                                    String onlineStatus = userObject.getString("online_status");
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

                                    if (onlineStatus.equals("on")) {
                                        userModel.setOnline(true);
                                    } else {
                                        userModel.setOnline(false);
                                    }

                                    if (friend_status.equals("active")) {
                                        userModel.setCheckinRequestState(3);
                                    } else {
                                        userModel.setCheckinRequestState(0);
                                    }


                                    arrUsers.add(userModel);
                                }
                                Global.getInstance().arrCheckinChatUsers = arrUsers;
                                if (userCount > 0) {
                                    getAllConversations();
                                } else {
                                    Utils.hideProgress();
                                    mPullRefreshHomeListView.onRefreshComplete();
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
    private static void sendFriendRequest(String user_id, final int position) {
        Utils.showProgress(mActivity);

        Map<String, String> params = new HashMap<String, String>();
        params.put(Constant.DEVICE_TYPE, Constant.ANDROID);
        params.put(Constant.DEVICE_TOKEN, Utils.getFromPreference(mActivity, Constant.DEVICE_TOKEN));
        params.put("my_id", Utils.getFromPreference(mActivity, Constant.USER_ID));
        params.put("user_id", user_id);

        CustomRequest signinRequest = new CustomRequest(Request.Method.POST, API.INVITE_FRIEND, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Utils.hideProgress();
                        try {
                            String status = response.getString("status");
                            if (status.equals("200")) {
                                arrUsers.get(position).setFriendStatus("waiting");
                                checkinUserAdapter.notifyDataSetChanged();
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
    
    private void enterCheckin() {
        Utils.showProgress(mActivity);

        Map<String, String> params = new HashMap<String, String>();
        params.put(Constant.DEVICE_TYPE, Constant.ANDROID);
        params.put(Constant.DEVICE_TOKEN, Utils.getFromPreference(mActivity, Constant.DEVICE_TOKEN));
        params.put("user_id", Utils.getFromPreference(mActivity, Constant.USER_ID));
        params.put("checkin", checkin);

        CustomRequest signinRequest = new CustomRequest(Request.Method.POST, API.ENTER_CHECKIN, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Utils.hideProgress();
                        try {
                            String status = response.getString("status");
                            if (status.equals("200")) {
                                getCheckinUsers();
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
    private void exitCheckin() {
        Map<String, String> params = new HashMap<String, String>();
        params.put(Constant.DEVICE_TYPE, Constant.ANDROID);
        params.put(Constant.DEVICE_TOKEN, Utils.getFromPreference(mActivity, Constant.DEVICE_TOKEN));
        params.put("user_id", Utils.getFromPreference(mActivity, Constant.USER_ID));
        params.put("checkin", checkin);

        CustomRequest signinRequest = new CustomRequest(Request.Method.POST, API.EXIT_CHECKIN, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Utils.hideProgress();
                        try {
                            String status = response.getString("status");
                            if (status.equals("200")) {

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

    
    public static class CheckinUserAdapter extends BaseAdapter {

        LayoutInflater mlayoutInflater;
        ArrayList<UserModel> arrFriends;
        public CheckinUserAdapter (ArrayList<UserModel> arrFriends) {
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
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view = mlayoutInflater.inflate(R.layout.item_checkin_user, null);

            MyCircularImageView myCircularImageView = (MyCircularImageView)view.findViewById(R.id.civ_imf_friend_avatar3);
            if (!arrFriends.get(position).getAvatar().equals("")) {
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
            }else {
                myCircularImageView.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.default_user));
            }
            myCircularImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    HomeActivity.navigateToProfile(arrFriends.get(position).getUser_id());
                }
            });
            TextView tvName = (TextView)view.findViewById(R.id.tv_imf_friend_name);
            tvName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    HomeActivity.navigateToProfile(arrFriends.get(position).getUser_id());
                }
            });

            TextView tvAboutMe = (TextView)view.findViewById(R.id.tv_imf_aboutme);
            ImageButton ibAdd = (ImageButton)view.findViewById(R.id.ib_imf_add);
            ImageButton ibChat = (ImageButton)view.findViewById(R.id.ib_imf_chat);
            RelativeLayout rlChatBubble = (RelativeLayout)view.findViewById(R.id.rl_checkin_chat_bubble);
            TextView tvUnreadMsgCount = (TextView)view.findViewById(R.id.tv_if_unreadmsgcount);

            tvName.setText(arrFriends.get(position).getFullname());
            tvAboutMe.setText(arrFriends.get(position).getAbout_you());
            ibAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (arrFriends.get(position).getFriendStatus().equals("none")) {
                        sendFriendRequest(arrFriends.get(position).getUser_id(), position);
                    } else if (arrFriends.get(position).getFriendStatus().equals("waiting")) {
                        mActivity.setResult(22);
                        mActivity.finish();
//                        acceptFriend(position);
                    }

                }
            });

            if (arrFriends.get(position).getFriendStatus().equals("waiting")) {
                ibAdd.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.sandglass_small));
//            } else if (arrFriends.get(position).getFriendStatus().equals("none")){
//                ibAdd.setImageDrawable(getResources().getDrawable(R.drawable.ic_green_plus));
            } else if (arrFriends.get(position).getFriendStatus().equals("active")){
                ibAdd.setVisibility(View.INVISIBLE);
            } else {
                ibAdd.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.ic_green_plus));
            }

            ibChat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (arrFriends.get(position).getCheckinRequestState() == 0) {///0:none, 1:send request, 2: received request, 3:accepted request
                        sendCheckinRequest(position);
                        arrFriends.get(position).setCheckinRequestState(1);
                        notifyDataSetChanged();
                    } else if (arrFriends.get(position).getCheckinRequestState() == 2) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                        builder.setTitle(Constant.INDECATOR);
                        builder.setMessage("Do you want accept " + arrFriends.get(position).getFullname() + "'s request?");
                        builder.setCancelable(true);
                        builder.setPositiveButton("Accept",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        acceptCheckinRequest(position);
                                        arrFriends.get(position).setCheckinRequestState(3);
                                        notifyDataSetChanged();

                                        dialog.cancel();
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


                    } else if (arrFriends.get(position).getCheckinRequestState() == 3) {
                        Intent intent = new Intent(mActivity, MessagesListActivity.class);
                        if (arrFriends.get(position).getConversation() != null) {
                            intent.putExtra(GcmBroadcastReceiver.LAYER_CONVERSATION_KEY, arrFriends.get(position).getConversation().getId());
                        }
                        intent.putExtra("user_id",arrFriends.get(position).getUser_id());
                        intent.putExtra("avatar", arrFriends.get(position).getAvatar());
                        intent.putExtra("fullname", arrFriends.get(position).getFullname());
                        intent.putExtra("page", "checkin_chat");
                        mActivity.startActivityForResult(intent, 201);
                    }

                }
            });

            if (arrFriends.get(position).getCheckinRequestState() == 1 || arrFriends.get(position).getCheckinRequestState() == 2) {
                tvUnreadMsgCount.setText("?");
            } else if (arrFriends.get(position).getCheckinRequestState() == 3) {
                tvUnreadMsgCount.setText(String.valueOf(arrFriends.get(position).getUnreadMsgCount()));
            } else {
                tvUnreadMsgCount.setText("");
            }
            return view;
        }
    }
    private static void sendCheckinRequest(final int position) {
        Utils.showProgress(mActivity);

        Map<String, String> params = new HashMap<String, String>();
        params.put(Constant.DEVICE_TYPE, Constant.ANDROID);
        params.put(Constant.DEVICE_TOKEN, Utils.getFromPreference(mActivity, Constant.DEVICE_TOKEN));
        params.put("my_id", Utils.getFromPreference(mActivity, Constant.USER_ID));
        params.put("user_id", arrUsers.get(position).getUser_id());

        CustomRequest signinRequest = new CustomRequest(Request.Method.POST, API.SEND_CHECKIN_REQUEST, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Utils.hideProgress();
                        try {
                            String status = response.getString("status");
                            if (status.equals("200")) {
                                arrUsers.get(position).setCheckinRequestState(1);
                                checkinUserAdapter.notifyDataSetChanged();
                                Utils.showToast(mActivity, "Sent request successfully");
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

    private static void acceptCheckinRequest(final int position) {
        Utils.showProgress(mActivity);

        Map<String, String> params = new HashMap<String, String>();
        params.put(Constant.DEVICE_TYPE, Constant.ANDROID);
        params.put(Constant.DEVICE_TOKEN, Utils.getFromPreference(mActivity, Constant.DEVICE_TOKEN));
        params.put("my_id", Utils.getFromPreference(mActivity, Constant.USER_ID));
        params.put("user_id", arrUsers.get(position).getUser_id());

        CustomRequest signinRequest = new CustomRequest(Request.Method.POST, API.ACCEPT_CHECKIN_REQUEST, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Utils.hideProgress();
                        try {
                            String status = response.getString("status");
                            if (status.equals("200")) {
                                arrUsers.get(position).setCheckinRequestState(3);
                                checkinUserAdapter.notifyDataSetChanged();
                                Utils.showToast(mActivity, "Accepted request successfully");
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
    public static void updateCheckinRequest (String user_id, String type) {

        if (type.equals("2") || type.equals("3")) {
            for (int i = 0; i < arrUsers.size(); i ++) {
                if (arrUsers.get(i).getUser_id().equals(user_id)) {
                    arrUsers.get(i).setCheckinRequestState(Integer.parseInt(type));
                    if (type.equals("2")) {
                        showAcceptAlert(i);
                    } else if (type.equals("3")) {
                        showAcceptedAlert(i);
                    }
                    break;
                }
            }
        } else if (type.equals("increase_message_count")){
            for (int i = 0; i < arrUsers.size(); i ++) {
                if (arrUsers.get(i).getUser_id().equals(user_id)) {
                    int unreadMsgCount = arrUsers.get(i).getUnreadMsgCount();
                    unreadMsgCount ++;
                    arrUsers.get(i).setUnreadMsgCount(unreadMsgCount);
                    checkinUserAdapter.notifyDataSetChanged();
                    break;
                }
            }
        }


        checkinUserAdapter.notifyDataSetChanged();
    }
    private static void showAcceptedAlert(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle(Constant.INDECATOR);
        View view = mActivity.getLayoutInflater().inflate(R.layout.item_more_friend, null);
        RelativeLayout relativeLayout = (RelativeLayout)view.findViewById(R.id.rl_imf_social);
        relativeLayout.setVisibility(View.GONE);
        TextView tvMsg = (TextView)view.findViewById(R.id.tv_imf_friend_name);
        tvMsg.setText(arrUsers.get(position).getFullname() + " accepted your request");
        MyCircularImageView circularImageView = (MyCircularImageView)view.findViewById(R.id.civ_imf_friend_avatar3);
        if (!arrUsers.get(position).equals("")) {
            UrlRectangleImageViewHelper.setUrlDrawable(circularImageView, API.BASE_AVATAR +arrUsers.get(position).getAvatar(), R.drawable.default_user, new UrlImageViewCallback() {
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
            circularImageView.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.default_user));
        }
        builder.setView(view);
//        builder.setMessage("Do you want accept " + arrUsers.get(position).getFullname() + "'s request?");
        builder.setCancelable(true);

        builder.setNegativeButton("Close",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
    }
    private static void showAcceptAlert(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle(Constant.INDECATOR);
        View view = mActivity.getLayoutInflater().inflate(R.layout.item_more_friend, null);
        RelativeLayout relativeLayout = (RelativeLayout)view.findViewById(R.id.rl_imf_social);
        relativeLayout.setVisibility(View.GONE);
        TextView tvMsg = (TextView)view.findViewById(R.id.tv_imf_friend_name);
        tvMsg.setText("Do you want accept " + arrUsers.get(position).getFullname() + "'s request?");
        MyCircularImageView circularImageView = (MyCircularImageView)view.findViewById(R.id.civ_imf_friend_avatar3);
        if (!arrUsers.get(position).equals("")) {
            UrlRectangleImageViewHelper.setUrlDrawable(circularImageView, API.BASE_AVATAR +arrUsers.get(position).getAvatar(), R.drawable.default_user, new UrlImageViewCallback() {
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
            circularImageView.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.default_user));
        }
        builder.setView(view);
//        builder.setMessage("Do you want accept " + arrUsers.get(position).getFullname() + "'s request?");
        builder.setCancelable(true);
        builder.setPositiveButton("Accept",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        acceptCheckinRequest(position);
                        arrUsers.get(position).setCheckinRequestState(3);
                        checkinUserAdapter.notifyDataSetChanged();

                        dialog.cancel();
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

    public static void updateFriendRequestState(String user_id, String type) {
        if (type.equals("receive_invite")) {
            for (int i = 0; i < arrUsers.size(); i ++) {
                if (arrUsers.get(i).getUser_id().equals(user_id)) {
                    arrUsers.get(i).setFriendStatus("waiting");
                    break;
                }
            }
        } else if (type.equals("accept_friend")) {
            for (int i = 0; i < arrUsers.size(); i ++) {
                if (arrUsers.get(i).getUser_id().equals(user_id)) {
                    arrUsers.get(i).setFriendStatus("active");
                    break;
                }
            }
        }
        checkinUserAdapter.notifyDataSetChanged();
    }
    public static void addNewUser(UserModel userModel, String type) {
        if (type.equals("enter_checkin")) {
            if (userModel.getFriendStatus().equals("active")) {
                userModel.setCheckinRequestState(3);
            } else {
                userModel.setCheckinRequestState(0);
            }
            boolean isExist = false;
            for (UserModel userModel1 : arrUsers) {
                if (userModel.getUser_id().equals(userModel1.getUser_id())) {
                    isExist = true;
                    break;
                }
            }
            if (!isExist) {
                arrUsers.add(0, userModel);
                getAllConversations();
            }

        } else if (type.equals("exit_checkin")) {
            for (int i = 0; i < arrUsers.size(); i ++) {
                if (arrUsers.get(i).getUser_id().equals(userModel.getUser_id())) {
                    arrUsers.remove(i);
                    break;
                }
            }
            checkinUserAdapter.notifyDataSetChanged();
        }

    }

    @Override
    public void onDestroy() {

        exitCheckin();
        clear_chat_history();
        Global.getInstance().arrCheckinChatUsers = new ArrayList<>();
        super.onDestroy();
    }



    private void clear_chat_history() {
        for (int i = 0; i < arrUsers.size(); i ++ ) {
            if (arrUsers.get(i).getFriendStatus().equals("active")) {
                continue;
            }
            Conversation conversation = arrUsers.get(i).getConversation();
            if (conversation != null) {
                conversation.delete(LayerClient.DeletionMode.ALL_PARTICIPANTS);
            }
        }

    }
}
