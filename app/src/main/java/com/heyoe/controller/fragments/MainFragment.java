package com.heyoe.controller.fragments;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.CustomRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.JsonArray;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.heyoe.R;
import com.heyoe.controller.adapters.PostAdapter;
import com.heyoe.model.API;
import com.heyoe.model.CommentModel;
import com.heyoe.model.Constant;
import com.heyoe.model.PostModel;
import com.heyoe.model.UserModel;
import com.heyoe.utilities.TimeUtility;
import com.heyoe.utilities.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment {

    private static Activity mActivity;
    private static ArrayList<PostModel> mArrPost;
//    private ArrayList<PostModel> mArrBufferPost;
    private ListView lvMain;
    private PullToRefreshListView mPullRefreshHomeListView;
    private static PostAdapter mPostAdapter;

    int offset;
    boolean isLast;

    public MainFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        initVariable();
        mActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        initUI(view);
        getAllPosts();

        return view;
    }
    private void initVariable() {
        mActivity = getActivity();
        isLast = false;
        offset = 0;
        mArrPost = new ArrayList<>();
//        mArrBufferPost = new ArrayList<>();
    }

    private void initUI(View view) {
        ///create listview
        mPullRefreshHomeListView = (PullToRefreshListView)view.findViewById(R.id.lv_main_wall);
        mPullRefreshHomeListView.setOnLastItemVisibleListener(new PullToRefreshBase.OnLastItemVisibleListener() {

            @Override
            public void onLastItemVisible() {
                if (!isLast) {
                    getAllPosts();
                }
                mPullRefreshHomeListView.onRefreshComplete();
            }
        });
        lvMain = mPullRefreshHomeListView.getRefreshableView();

        mPostAdapter = new PostAdapter(getActivity(),mArrPost);
        lvMain.setAdapter(mPostAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

    }

    private void getAllPosts() {
        Utils.showProgress(mActivity);

        Map<String, String> params = new HashMap<String, String>();
        params.put(Constant.DEVICE_TYPE, Constant.ANDROID);
        params.put(Constant.DEVICE_TOKEN, Utils.getFromPreference(mActivity, Constant.DEVICE_TOKEN));
        params.put("user_id", Utils.getFromPreference(mActivity, Constant.USER_ID));
        params.put("offset", String.valueOf(offset));

        CustomRequest signinRequest = new CustomRequest(Request.Method.POST, API.GET_ALL_POSTS, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Utils.hideProgress();
                        try {
                            String status = response.getString("status");
                            if (status.equals("200")) {
                                JSONArray jsonArray = response.getJSONArray("data");
                                int postCount = jsonArray.length();
                                if (postCount == 0) {
                                    isLast = true;
                                }
                                offset ++;
                                for (int i = 0; i < postCount; i ++)  {

                                    JSONObject postObject = jsonArray.getJSONObject(i);
                                    PostModel postModel = new PostModel();
                                    postModel.setPost_id(postObject.getString("post_id"));
                                    postModel.setPosted_date(postObject.getString("posted_date"));
                                    postModel.setPoster_id(postObject.getString("poster_id"));
                                    postModel.setPoster_fullname(postObject.getString("poster_fullname"));
                                    postModel.setPoster_avatar(postObject.getString("poster_avatar"));
                                    postModel.setPoster_celebrity(postObject.getString("poster_celebrity"));
                                    postModel.setMedia_type(postObject.getString("media_type"));
                                    postModel.setMedia_url(postObject.getString("media_url"));
                                    postModel.setLike_count(postObject.getString("like_count"));
                                    postModel.setDislike_count(postObject.getString("dislike_count"));
                                    postModel.setComment_count(postObject.getString("comment_count"));
                                    postModel.setShared_count(postObject.getString("shared_count"));
                                    postModel.setViewed_count(postObject.getString("viewed_count"));
                                    postModel.setLike(postObject.getString("like"));
                                    postModel.setDescription(postObject.getString("description"));
                                    postModel.setCommented(postObject.getString("commented"));
                                    postModel.setFavorite(postObject.getString("favorite"));
                                    postModel.setFriendStatus(postObject.getString("friend_status"));
                                    postModel.setImageWidth(Integer.parseInt(postObject.getString("width")));
                                    postModel.setImageHeight(Integer.parseInt(postObject.getString("height")));

                                    JSONArray jsonArrComments = postObject.getJSONArray("comments");
                                    ArrayList<CommentModel> arrComments = new ArrayList<>();
                                    for (int j = 0; j < jsonArrComments.length(); j ++) {

                                        JSONObject jsonComment = jsonArrComments.getJSONObject(j);
                                        CommentModel commentModel = new CommentModel();

                                        commentModel.setFullname(jsonComment.getString("fullname"));
                                        commentModel.setAvatar(jsonComment.getString("avatar"));
                                        commentModel.setComment(jsonComment.getString("comment"));
                                        commentModel.setTime(jsonComment.getString("date"));

                                        arrComments.add(commentModel);
                                    }
                                    postModel.setArrComments(arrComments);

                                    JSONArray jsonArrFriend = postObject.getJSONArray("liked_friends");
                                    ArrayList<UserModel> arrFriends = new ArrayList<>();
                                    for (int k = 0; k < jsonArrFriend.length(); k ++) {
                                        JSONObject jsonFriend = jsonArrFriend.getJSONObject(k);
                                        UserModel userModel = new UserModel();

                                        userModel.setFullname(jsonFriend.getString("fullname"));
                                        userModel.setAvatar(jsonFriend.getString("avatar"));
                                        userModel.setTime(jsonFriend.getString("date"));

                                        arrFriends.add(userModel);
                                    }
                                    postModel.setArrLiked_friends(arrFriends);

                                    postModel.setClickedTime((long) 0);
                                    mArrPost.add(postModel);

                                }

                                mPostAdapter.notifyDataSetChanged();

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

    public static void setFavorite(final int position, final boolean flag) {
//        Utils.showProgress(mActivity);

        Map<String, String> params = new HashMap<String, String>();
        params.put(Constant.DEVICE_TYPE, Constant.ANDROID);
        params.put(Constant.DEVICE_TOKEN, Utils.getFromPreference(mActivity, Constant.DEVICE_TOKEN));
        params.put("my_id", Utils.getFromPreference(mActivity, Constant.USER_ID));
        params.put("post_id", mArrPost.get(position).getPost_id());
        if (flag) {
            params.put("status", "favorite");
        } else {
            params.put("status", "unfavorite");
        }
        CustomRequest customRequest = new CustomRequest(Request.Method.POST, API.SET_FAVORITE, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Utils.hideProgress();
                        try {
                            String status = response.getString("status");
                            if (status.equals("200")) {
                                if (flag) {
                                    mArrPost.get(position).setFavorite("favorite");
                                } else {
                                    mArrPost.get(position).setFavorite("unfavorite");
                                }
                                mPostAdapter.notifyDataSetChanged();

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
        requestQueue.add(customRequest);
    }

    public static void setLike(final int position, final String type) {
        final Map<String, String> params = new HashMap<String, String>();
        params.put(Constant.DEVICE_TYPE, Constant.ANDROID);
        params.put(Constant.DEVICE_TOKEN, Utils.getFromPreference(mActivity, Constant.DEVICE_TOKEN));
        params.put("my_id", Utils.getFromPreference(mActivity, Constant.USER_ID));
        params.put("post_id", mArrPost.get(position).getPost_id());
        params.put("status", type);

        CustomRequest customRequest = new CustomRequest(Request.Method.POST, API.LIKE_POST, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String status = response.getString("status");
                            if (status.equals("200")) {
                                mArrPost.get(position).setClickedTime(Long.parseLong(TimeUtility.getCurrentTimeStamp()));

                                if (type.equals("like")) {
                                    mArrPost.get(position).setLike("like");

                                    int like_count = Integer.parseInt(mArrPost.get(position).getLike_count());
                                    like_count ++;
                                    mArrPost.get(position).setLike_count(String.valueOf(like_count));

                                } else if (type.equals("dislike")){
                                    int dislike_count = Integer.parseInt(mArrPost.get(position).getDislike_count());
                                    dislike_count ++;
                                    mArrPost.get(position).setDislike_count(String.valueOf(dislike_count));

                                    mArrPost.get(position).setLike("dislike");

                                } else if (type.equals("cancel_like")){
                                    int like_count = Integer.parseInt(mArrPost.get(position).getLike_count());
                                    like_count --;
                                    if (like_count < 0) {
                                        like_count = 0;
                                    }
                                    mArrPost.get(position).setLike_count(String.valueOf(like_count));

                                    mArrPost.get(position).setLike("none");

                                } else if (type.equals("cancel_dislike")){
                                    int dislike_count = Integer.parseInt(mArrPost.get(position).getDislike_count());
                                    dislike_count --;
                                    if (dislike_count < 0) {
                                        dislike_count = 0;
                                    }
                                    mArrPost.get(position).setDislike_count(String.valueOf(dislike_count));

                                    mArrPost.get(position).setLike("none");
                                }

                                mPostAdapter.notifyDataSetChanged();
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
                        Toast.makeText(mActivity, error.toString(), Toast.LENGTH_LONG).show();
                    }
                });
        RequestQueue requestQueue = Volley.newRequestQueue(mActivity);
        requestQueue.add(customRequest);
    }

    public static void addFriend(final int position) {
        Utils.showProgress(mActivity);

        Map<String, String> params = new HashMap<String, String>();
        params.put(Constant.DEVICE_TYPE, Constant.ANDROID);
        params.put(Constant.DEVICE_TOKEN, Utils.getFromPreference(mActivity, Constant.DEVICE_TOKEN));
        params.put("my_id", Utils.getFromPreference(mActivity, Constant.USER_ID));
        params.put("user_id", mArrPost.get(position).getPoster_id());

        CustomRequest signinRequest = new CustomRequest(Request.Method.POST, API.INVITE_FRIEND, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Utils.hideProgress();
                        try {
                            String status = response.getString("status");
                            if (status.equals("200")) {
                                mArrPost.get(position).setFriendStatus("waiting");
                                mPostAdapter.notifyDataSetChanged();
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
    public static void sharePost(int position) {

        Map<String, String> params = new HashMap<String, String>();
        params.put(Constant.DEVICE_TYPE, Constant.ANDROID);
        params.put(Constant.DEVICE_TOKEN, Utils.getFromPreference(mActivity, Constant.DEVICE_TOKEN));
        params.put("my_id", Utils.getFromPreference(mActivity, Constant.USER_ID));
        params.put("post_id", mArrPost.get(position).getPost_id());

        CustomRequest customRequest = new CustomRequest(Request.Method.POST, API.SHARED_POST, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
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
                        Toast.makeText(mActivity, error.toString(), Toast.LENGTH_LONG).show();
                    }
                });
        RequestQueue requestQueue = Volley.newRequestQueue(mActivity);
        requestQueue.add(customRequest);
    }

    public static int shardPostNum = 0;
    public static  void sharing(int position){
        shardPostNum = position;
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, mArrPost.get(position).getPoster_fullname());
        sharingIntent.putExtra(Intent.EXTRA_TEXT, "http://heyoe.com/");
        mActivity.startActivityForResult(Intent.createChooser(sharingIntent, "Heyoe"), 102);

    }
    public static void updateSharedCount() {
        sharePost(shardPostNum);
        int shardCount  = Integer.parseInt(mArrPost.get(shardPostNum).getShared_count());
        shardCount ++;
        mArrPost.get(shardPostNum).setShared_count(String.valueOf(shardCount));
        mPostAdapter.notifyDataSetChanged();
    }
    public static void updatePostFeed(PostModel postModel) {
        for (int i = 0; i < mArrPost.size(); i ++) {
            if (postModel.getPost_id().equals(mArrPost.get(i).getPost_id())) {
                mArrPost.remove(i);
                mArrPost.add(i, postModel);
                mPostAdapter.notifyDataSetChanged();
                break;
            }
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

    public static void showCommentDlg(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle(Constant.INDECATOR);
        builder.setCancelable(true);
        builder.setPositiveButton("Send",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        dialog.cancel();
                    }
                });
        builder.setNegativeButton("No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
//        builder.setNeutralButton("Close", new DialogInterface.OnClickListener() {
//
//            @Override
//            public void onClick(DialogInterface arg0, int arg1) {
//                // TODO Auto-generated method stub
//                Toast.makeText(getApplicationContext(), "Close is clicked", Toast.LENGTH_LONG).show();
//
//            }
//        });
//        dialog.setCancelable(true);
//        dialog.setCanceledOnTouchOutside(false);
        AlertDialog alert = builder.create();
        alert.show();
    }
    public static void writeComment(final int position, String comment) {
        Utils.showProgress(mActivity);

        Map<String, String> params = new HashMap<String, String>();
        params.put(Constant.DEVICE_TYPE, Constant.ANDROID);
        params.put(Constant.DEVICE_TOKEN, Utils.getFromPreference(mActivity, Constant.DEVICE_TOKEN));
        params.put("my_id", Utils.getFromPreference(mActivity, Constant.USER_ID));
        params.put("post_id", mArrPost.get(position).getPost_id());
        params.put("comment", comment);

        CustomRequest signinRequest = new CustomRequest(Request.Method.POST, API.COMMENT_POST, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Utils.hideProgress();
                        try {
                            String status = response.getString("status");
                            if (status.equals("200")) {
                                mArrPost.get(position).setCommented("yes");
                                mPostAdapter.notifyDataSetChanged();
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






















    //for test
//    private ArrayList<PostModel> makeSamplePosts() {
//        ArrayList<PostModel> arrayList = new ArrayList<>();
//        for (int i = 0; i < 10; i ++) {
//            PostModel postModel = new PostModel();
//            arrayList.add(postModel);
//
//        }
//        return arrayList;
//
//    }
}
