package com.heyoe_chat.controller.fragments;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.CustomRequest;
import com.android.volley.toolbox.Volley;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.heyoe_chat.R;
import com.heyoe_chat.controller.App;
import com.heyoe_chat.controller.HomeActivity;
import com.heyoe_chat.model.API;
import com.heyoe_chat.model.Constant;
import com.heyoe_chat.model.Global;
import com.heyoe_chat.model.UserModel;
import com.heyoe_chat.utilities.Utils;
import com.heyoe_chat.utilities.image_downloader.UrlImageViewCallback;
import com.heyoe_chat.utilities.image_downloader.UrlRectangleImageViewHelper;
import com.heyoe_chat.widget.MyCircularImageView;
import com.layer.sdk.exceptions.LayerConversationException;
import com.layer.sdk.messaging.Conversation;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class RequestFragment extends Fragment {
    private ListView listView;
    private PullToRefreshListView mPullRefreshHomeListView;
    private FriendAdapter friendAdapter;
    private RequestAdapter requestAdapter;
    private Button btnIncomming, btnOutgoing;
    private Activity mActivity;

    private ArrayList<UserModel> arrUsers;

    public RequestFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the text_layout for this fragment
        View view = inflater.inflate(R.layout.fragment_request, container, false);
        initVariables();
        initUI(view);
        getInvitedFriends();
        return view;
    }
    private void initVariables() {
        mActivity = getActivity();
        arrUsers = new ArrayList<>();
    }
    private void initUI(View view) {
        btnIncomming = (Button)view.findViewById(R.id.btn_request_friend);
        btnOutgoing = (Button)view.findViewById(R.id.btn_request_request);

        btnIncomming.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnOutgoing.setBackgroundColor(mActivity.getResources().getColor(R.color.grey));
                btnOutgoing.setTextColor(mActivity.getResources().getColor(R.color.white));
                btnIncomming.setBackgroundColor(mActivity.getResources().getColor(R.color.white));
                btnIncomming.setTextColor(mActivity.getResources().getColor(R.color.green));
                
                arrUsers = new ArrayList<>();
                getInvitedFriends();


            }
        });
        btnOutgoing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnIncomming.setBackgroundColor(mActivity.getResources().getColor(R.color.grey));
                btnIncomming.setTextColor(mActivity.getResources().getColor(R.color.white));
                btnOutgoing.setBackgroundColor(mActivity.getResources().getColor(R.color.white));
                btnOutgoing.setTextColor(mActivity.getResources().getColor(R.color.green));
                arrUsers = new ArrayList<>();
                getRequestedUsers();

            }
        });
        ///create sent listview
        mPullRefreshHomeListView = (PullToRefreshListView) view.findViewById(R.id.plv_request_request);
        mPullRefreshHomeListView.setOnLastItemVisibleListener(new PullToRefreshBase.OnLastItemVisibleListener() {

            @Override
            public void onLastItemVisible() {

                mPullRefreshHomeListView.onRefreshComplete();

            }
        });
        listView = mPullRefreshHomeListView.getRefreshableView();
    }

    private void getRequestedUsers() {
        Utils.showProgress(mActivity);

        Map<String, String> params = new HashMap<String, String>();
        params.put(Constant.DEVICE_TYPE, Constant.ANDROID);
        params.put(Constant.DEVICE_TOKEN, Utils.getFromPreference(mActivity, Constant.DEVICE_TOKEN));
        params.put("my_id", Utils.getFromPreference(mActivity, Constant.USER_ID));


        CustomRequest signinRequest = new CustomRequest(Request.Method.POST, API.GET_INVITED_FRIENDS, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Utils.hideProgress();
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

                                    arrUsers.add(userModel);
                                }

                                requestAdapter = new RequestAdapter(arrUsers);
                                listView.setAdapter(requestAdapter);

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
    private void getInvitedFriends() {
        Utils.showProgress(mActivity);

        Map<String, String> params = new HashMap<String, String>();
        params.put(Constant.DEVICE_TYPE, Constant.ANDROID);
        params.put(Constant.DEVICE_TOKEN, Utils.getFromPreference(mActivity, Constant.DEVICE_TOKEN));
        params.put("my_id", Utils.getFromPreference(mActivity, Constant.USER_ID));


        CustomRequest signinRequest = new CustomRequest(Request.Method.POST, API.GET_REQUESTED_USERS, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Utils.hideProgress();
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

                                    arrUsers.add(userModel);
                                }

                                friendAdapter = new FriendAdapter(arrUsers);
                                listView.setAdapter(friendAdapter);

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
    private void acceptFriend(final int position) {

        String user_id = arrUsers.get(position).getUser_id();
        Utils.showProgress(mActivity);

        Map<String, String> params = new HashMap<String, String>();
        params.put(Constant.DEVICE_TYPE, Constant.ANDROID);
        params.put(Constant.DEVICE_TOKEN, Utils.getFromPreference(mActivity, Constant.DEVICE_TOKEN));
        params.put("my_id", Utils.getFromPreference(mActivity, Constant.USER_ID));
        params.put("fullname", Utils.getFromPreference(mActivity, Constant.FULLNAME));
        params.put("user_id", user_id);

        CustomRequest signinRequest = new CustomRequest(Request.Method.POST, API.ACCEPT_FRIEND, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Utils.hideProgress();
                        try {
                            String status = response.getString("status");
                            if (status.equals("200")) {
                                createConversation(arrUsers.get(position).getUser_id());
                                Utils.showOKDialog(mActivity, arrUsers.get(position).getFullname() + " " + getResources().getString(R.string.accept_success));
                                arrUsers.remove(position);
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
    private void createConversation(String friend_id) {
        Conversation conversation;
        try {
            // Try creating a new distinct conversation with the given user
            conversation = App.getLayerClient().newConversation(Arrays.asList(friend_id));
        } catch (LayerConversationException e) {
            // If a distinct conversation with the given user already exists, use that one instead
            conversation = e.getConversation();
        }
    }
    private void rejectFriend(final int position) {
        String user_id = arrUsers.get(position).getUser_id();
        Utils.showProgress(mActivity);

        Map<String, String> params = new HashMap<String, String>();
        params.put(Constant.DEVICE_TYPE, Constant.ANDROID);
        params.put(Constant.DEVICE_TOKEN, Utils.getFromPreference(mActivity, Constant.DEVICE_TOKEN));
        params.put("my_id", Utils.getFromPreference(mActivity, Constant.USER_ID));
        params.put("fullname", Utils.getFromPreference(mActivity, Constant.FULLNAME));
        params.put("user_id", user_id);

        CustomRequest signinRequest = new CustomRequest(Request.Method.POST, API.REJECT_FRIEND, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Utils.hideProgress();
                        try {
                            String status = response.getString("status");
                            if (status.equals("200")) {
                                arrUsers.remove(position);
                                friendAdapter.notifyDataSetChanged();
                                Utils.showOKDialog(mActivity, getResources().getString(R.string.reject_success));
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
    private void cancelInvite(final int position) {
        String user_id = arrUsers.get(position).getUser_id();

        Utils.showProgress(mActivity);

        Map<String, String> params = new HashMap<String, String>();
        params.put(Constant.DEVICE_TYPE, Constant.ANDROID);
        params.put(Constant.DEVICE_TOKEN, Utils.getFromPreference(mActivity, Constant.DEVICE_TOKEN));
        params.put("my_id", Utils.getFromPreference(mActivity, Constant.USER_ID));
        params.put("fullname", Utils.getFromPreference(mActivity, Constant.FULLNAME));
        params.put("user_id", user_id);

        CustomRequest signinRequest = new CustomRequest(Request.Method.POST, API.CANCEL_INVITE, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Utils.hideProgress();
                        try {
                            String status = response.getString("status");
                            if (status.equals("200")) {
                                arrUsers.remove(position);
                                requestAdapter.notifyDataSetChanged();

                                Utils.showOKDialog(mActivity, getResources().getString(R.string.cancel_success));
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
    private void showCancelInviteConfirmDlg(final int position) {

        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle(Constant.INDECATOR);
        builder.setMessage(getResources().getString(R.string.cancel_invite));
        builder.setCancelable(true);
        builder.setPositiveButton( mActivity.getResources().getString(R.string.dlg_ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        cancelInvite(position);
                        dialog.cancel();
                    }
                });
        builder.setNegativeButton( mActivity.getResources().getString(R.string.dlg_cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();

    }
    private void showRejectFriendConfirmDlg(final int position) {

        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle(Constant.INDECATOR);
        builder.setMessage(getResources().getString(R.string.reject_friend));
        builder.setCancelable(true);
        builder.setPositiveButton( mActivity.getResources().getString(R.string.dlg_ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        rejectFriend(position);
                        dialog.cancel();
                    }
                });
        builder.setNegativeButton( mActivity.getResources().getString(R.string.dlg_cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();

    }

    public class FriendAdapter extends BaseAdapter {

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
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = mlayoutInflater.inflate(R.layout.item_request_received, null);
            }
            UserModel userModel = arrFriends.get(position);
            MyCircularImageView myCircularImageView = (MyCircularImageView)view.findViewById(R.id.civ_irr_avatar3);
            myCircularImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    activity.startActivity(new Intent(activity, ProfileActivity.class));
                }
            });
            if (!userModel.getAvatar().equals("")) {
                UrlRectangleImageViewHelper.setUrlDrawable(myCircularImageView,  arrFriends.get(position).getAvatar(), R.drawable.default_user, new UrlImageViewCallback() {
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
//                    Intent intent = new Intent(mActivity, ProfileActivity.class);
//                    intent.putExtra("user_id", arrFriends.get(position).getUser_id());
//                    mActivity.startActivity(intent);
                    HomeActivity.navigateToProfile(arrFriends.get(position).getUser_id());
                }
            });
            TextView tvFullname = (TextView)view.findViewById(R.id.tv_ia_irr_fullname);
            tvFullname.setText(userModel.getFullname());
            tvFullname.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    Intent intent = new Intent(mActivity, ProfileActivity.class);
//                    intent.putExtra("user_id", arrFriends.get(position).getUser_id());
//                    mActivity.startActivity(intent);
                    HomeActivity.navigateToProfile(arrFriends.get(position).getUser_id());
                }
            });

            ImageButton ibReject = (ImageButton)view.findViewById(R.id.ib_reject);
            ImageButton ibAccept = (ImageButton)view.findViewById(R.id.ib_accept);

            ibAccept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    acceptFriend(position);
                }
            });
            ibReject.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showRejectFriendConfirmDlg(position);
                }
            });

            return view;
        }
    }
    public class RequestAdapter extends BaseAdapter {

        LayoutInflater mlayoutInflater;
        ArrayList<UserModel> arrFriends;
        public RequestAdapter (ArrayList<UserModel> arrFriends) {
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
            View view = convertView;
            if (view == null) {
                view = mlayoutInflater.inflate(R.layout.item_request_sent, null);
            }
            final UserModel userModel = arrFriends.get(position);
            LinearLayout linearLayout = (LinearLayout)view.findViewById(R.id.rl_ia_social);
            linearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
            ImageButton ibCancel = (ImageButton)view.findViewById(R.id.ib_irs_cancel);
            ibCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showCancelInviteConfirmDlg(position);
                }
            });
            ImageButton ibSandGlass = (ImageButton)view.findViewById(R.id.ib_irs_sandglass);

            MyCircularImageView myCircularImageView = (MyCircularImageView)view.findViewById(R.id.civ_irs_avatar);
            myCircularImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    activity.startActivity(new Intent(activity, ProfileActivity.class));
                }
            });
            if (!userModel.getAvatar().equals("")) {
                UrlRectangleImageViewHelper.setUrlDrawable(myCircularImageView,  arrFriends.get(position).getAvatar(), R.drawable.default_user, new UrlImageViewCallback() {
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
//                    Intent intent = new Intent(mActivity, ProfileActivity.class);
//                    intent.putExtra("user_id", arrFriends.get(position).getUser_id());
//                    mActivity.startActivity(intent);
                    HomeActivity.navigateToProfile(arrFriends.get(position).getUser_id());
                }
            });
            TextView tvFullname = (TextView)view.findViewById(R.id.tv_irc_fullname);
            tvFullname.setText(userModel.getFullname());
            tvFullname.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    Intent intent = new Intent(mActivity, ProfileActivity.class);
//                    intent.putExtra("user_id", arrFriends.get(position).getUser_id());
//                    mActivity.startActivity(intent);
                    HomeActivity.navigateToProfile(arrFriends.get(position).getUser_id());
                }
            });
            return view;
        }
    }

}
