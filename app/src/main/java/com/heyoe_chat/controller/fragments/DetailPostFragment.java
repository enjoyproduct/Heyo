package com.heyoe_chat.controller.fragments;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.heyoe_chat.R;
import com.heyoe_chat.controller.DetailPostActivity;
import com.heyoe_chat.controller.MediaPlayActivity;
import com.heyoe_chat.controller.adapters.FriendTagAdapter;
import com.heyoe_chat.model.API;
import com.heyoe_chat.model.CommentModel;
import com.heyoe_chat.model.Constant;
import com.heyoe_chat.model.PostModel;
import com.heyoe_chat.model.UserModel;
import com.heyoe_chat.utilities.FileUtility;
import com.heyoe_chat.utilities.StringUtility;
import com.heyoe_chat.utilities.TimeUtility;
import com.heyoe_chat.utilities.UIUtility;
import com.heyoe_chat.utilities.Utils;
import com.heyoe_chat.utilities.image_downloader.UrlImageViewCallback;
import com.heyoe_chat.utilities.image_downloader.UrlRectangleImageViewHelper;
import com.heyoe_chat.widget.MyCircularImageView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.kaede.tagview.OnTagClickListener;
import me.kaede.tagview.Tag;
import me.kaede.tagview.TagView;

/**
 * A simple {@link Fragment} subclass.
 */
public class DetailPostFragment extends Fragment {

    private ArrayList<CommentModel> mArrComments = new ArrayList<>();
//    private RecyclerView recyclerView;
//    private RecyclerAdapter recyclerAdapter;
    private ListView listView;
    private UserAdpater userAdpater;

    private static Activity mActivity;
    public static PostModel postModel;
    private LayoutInflater layoutInflater;
    private FloatingActionButton floatingActionButton;

    ImageButton ibFavorite;

    ImageButton ibLike, ibDislike, ibComment;

    EditText etComment;
    private ArrayList<UserModel> arrFriends;
    private ArrayList<UserModel> arrTagedFriends;
    String tagedFriendIds;

    public DetailPostFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
//        mActivity = activity;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the text_layout for this fragment
        View view = inflater.inflate(R.layout.fragment_detail_post, container, false);

        initVariables();
        initUI(view);
        getFriends();
        viewedPost();

        return view;
    }
    private void initVariables() {
        mActivity = getActivity();
        postModel = DetailPostActivity.postModel;
        layoutInflater = LayoutInflater.from(mActivity);
        arrFriends        = new ArrayList<>();
        arrTagedFriends   = new ArrayList<>();
    }

    private void initUI(View view) {

        floatingActionButton = (FloatingActionButton)view.findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                showCommentDlg();
                tagedFriendIds = "";
                DialogFragment selectWeekDayFragment = new ShowCommentDialog();
                selectWeekDayFragment.show(getFragmentManager(), "");
            }
        });

        listView = (ListView)view.findViewById(R.id.recycler_view);
        mArrComments = postModel.getArrComments();
        userAdpater = new UserAdpater();
        listView.setAdapter(userAdpater);
    }

    private void getFriends() {
        Utils.showProgress(mActivity);

        Map<String, String> params = new HashMap<String, String>();
        params.put(Constant.DEVICE_TYPE, Constant.ANDROID);
        params.put(Constant.DEVICE_TOKEN, Utils.getFromPreference(mActivity, Constant.DEVICE_TOKEN));
        params.put("my_id", Utils.getFromPreference(mActivity, Constant.USER_ID));
        params.put("user_id", Utils.getFromPreference(mActivity, Constant.USER_ID));

        CustomRequest signinRequest = new CustomRequest(Request.Method.POST, API.GET_MY_FRIEND_LIST, params,
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
                                    String friend_status = userObject.getString("status");

                                    UserModel userModel = new UserModel();

                                    userModel.setUser_id(user_id);
                                    userModel.setFullname("@" + fullname);
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


                                    if (friend_status.equals("active")) {
                                        arrFriends.add(userModel);
                                    } else {
                                    }

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
    public void viewedPost() {

        Map<String, String> params = new HashMap<String, String>();
        params.put(Constant.DEVICE_TYPE, Constant.ANDROID);
        params.put(Constant.DEVICE_TOKEN, Utils.getFromPreference(mActivity, Constant.DEVICE_TOKEN));
        params.put("my_id", Utils.getFromPreference(mActivity, Constant.USER_ID));
        params.put("post_id", postModel.getPost_id());

        CustomRequest customRequest = new CustomRequest(Request.Method.POST, API.VIEWED_POST, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String status = response.getString("status");
                            if (status.equals("200")) {
                                int viewedCount = Integer.parseInt(postModel.getViewed_count());
                                viewedCount ++;
                                postModel.setViewed_count(String.valueOf(viewedCount));
    //                            tvViewedCount.setText(String.valueOf(viewedCount));
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
    public static void sharePost() {

        Map<String, String> params = new HashMap<String, String>();
        params.put(Constant.DEVICE_TYPE, Constant.ANDROID);
        params.put(Constant.DEVICE_TOKEN, Utils.getFromPreference(mActivity, Constant.DEVICE_TOKEN));
        params.put("my_id", Utils.getFromPreference(mActivity, Constant.USER_ID));
        params.put("post_id", postModel.getPost_id());

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
    public  void setFavorite( final boolean flag) {
//        Utils.showProgress(mActivity);

        Map<String, String> params = new HashMap<String, String>();
        params.put(Constant.DEVICE_TYPE, Constant.ANDROID);
        params.put(Constant.DEVICE_TOKEN, Utils.getFromPreference(mActivity, Constant.DEVICE_TOKEN));
        params.put("my_id", Utils.getFromPreference(mActivity, Constant.USER_ID));
        params.put("post_id", postModel.getPost_id());
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
                                    postModel.setFavorite("favorite");
//                                    ibFavorite.setImageDrawable(getResources().getDrawable(R.drawable.ic_favorite_star_white));
                                } else {
                                    postModel.setFavorite("unfavorite");
//                                    ibFavorite.setImageDrawable(getResources().getDrawable(R.drawable.ic_favorite_star_empty));
                                }
                                userAdpater.notifyDataSetChanged();
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

    public void setLike(final String type) {
        final Map<String, String> params = new HashMap<String, String>();
        params.put(Constant.DEVICE_TYPE, Constant.ANDROID);
        params.put(Constant.DEVICE_TOKEN, Utils.getFromPreference(mActivity, Constant.DEVICE_TOKEN));
        params.put("my_id", Utils.getFromPreference(mActivity, Constant.USER_ID));
        params.put("post_id", postModel.getPost_id());
        params.put("status", type);

        CustomRequest customRequest = new CustomRequest(Request.Method.POST, API.LIKE_POST, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String status = response.getString("status");
                            if (status.equals("200")) {
                                postModel.setClickedTime(Long.parseLong(TimeUtility.getCurrentTimeStamp()));

                                if (type.equals("like")) {
                                    postModel.setLike("like");

                                    int like_count = Integer.parseInt(postModel.getLike_count());
                                    like_count ++;
                                    postModel.setLike_count(String.valueOf(like_count));

                                } else if (type.equals("dislike")){
                                    int dislike_count = Integer.parseInt(postModel.getDislike_count());
                                    dislike_count ++;
                                    postModel.setDislike_count(String.valueOf(dislike_count));

                                    postModel.setLike("dislike");

                                } else if (type.equals("cancel_like")){
                                    int like_count = Integer.parseInt(postModel.getLike_count());
                                    like_count --;
                                    if (like_count < 0) {
                                        like_count = 0;
                                    }
                                    postModel.setLike_count(String.valueOf(like_count));

                                    postModel.setLike("none");

                                } else if (type.equals("cancel_dislike")){
                                    int dislike_count = Integer.parseInt(postModel.getDislike_count());
                                    dislike_count --;
                                    if (dislike_count < 0) {
                                        dislike_count = 0;
                                    }
                                    postModel.setDislike_count(String.valueOf(dislike_count));

                                    postModel.setLike("none");
                                }
                                userAdpater.notifyDataSetChanged();

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
    @SuppressLint("ValidFragment")
    public class ShowCommentDialog extends DialogFragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.custom_comment, container, true);
            etComment = (EditText)view.findViewById(R.id.et_comment);
            UIUtility.showSoftKeyboard(mActivity, etComment);
            arrTagedFriends.clear();
            Button btnTag = (Button)view.findViewById(R.id.btn_comment_tag);
            Button btnCancel = (Button)view.findViewById(R.id.btn_comment_cancel);
            Button btnSend = (Button)view.findViewById(R.id.btn_comment_send);
            btnTag.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showTagDialog();
                }
            });
            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
            btnSend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (etComment.getText().toString().length() > 0) {
                        for (int i = 0; i < arrTagedFriends.size(); i ++) {
                            if (etComment.getText().toString().contains(arrTagedFriends.get(i).getFullname().replace("@", ""))) {
                                tagedFriendIds = tagedFriendIds + arrTagedFriends.get(i).getUser_id() + ",";
                                continue;
                            } else {

                            }
                        }
                        writeComment(Html.toHtml(etComment.getText()));
                    }
                    dismiss();
                }
            });
            return view;
        }
    }

    public void writeComment( final String comment) {
        Utils.showProgress(mActivity);

        Map<String, String> params = new HashMap<String, String>();
        params.put(Constant.DEVICE_TYPE, Constant.ANDROID);
        params.put(Constant.DEVICE_TOKEN, Utils.getFromPreference(mActivity, Constant.DEVICE_TOKEN));
        params.put("my_id", Utils.getFromPreference(mActivity, Constant.USER_ID));
        params.put("post_id", postModel.getPost_id());
        params.put("comment", comment);
        params.put("taged_friend_ids", tagedFriendIds);

        CustomRequest customRequest = new CustomRequest(Request.Method.POST, API.COMMENT_POST, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Utils.hideProgress();
                        try {
                            String status = response.getString("status");
                            if (status.equals("200")) {
                                CommentModel commentModel = new CommentModel();
                                commentModel.setFullname(Utils.getFromPreference(mActivity, Constant.FULLNAME));
                                commentModel.setComment(comment);
                                commentModel.setAvatar(Utils.getFromPreference(mActivity, Constant.AVATAR));
                                commentModel.setTime(TimeUtility.getCurrentTimeStamp());
                                postModel.getArrComments().add(0,commentModel);
//                                mArrComments.add(0, commentModel);
                                userAdpater.notifyDataSetChanged();

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
        requestQueue.add(customRequest);
    }
    AlertDialog.Builder builder1;
    public void showTagDialog(){
        builder1 = new AlertDialog.Builder(mActivity);
        FriendTagAdapter friendTagAdapter = new FriendTagAdapter(mActivity, arrFriends);
        builder1.setCancelable(true);
        builder1.setAdapter(friendTagAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                inputTag(arrFriends.get(which).getFullname().replace("@", ""));
                arrTagedFriends.add(arrFriends.get(which));
            }
        });
        builder1.setNegativeButton( mActivity.getResources().getString(R.string.dlg_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder1.create();
        alert.show();
    }
    public void inputTag(String strTag) {
        String str = "<b>  " + strTag + " </b>";
        etComment.append(Html.fromHtml(str));
    }
    public void sharing(){
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, postModel.getPoster_fullname());
        sharingIntent.putExtra(Intent.EXTRA_TEXT, "http://heyoe.com/");
        mActivity.startActivityForResult(Intent.createChooser(sharingIntent, "Heyoe"), 102);

    }

    public static void updateSharedCount() {
        sharePost();
        int shardCount  = Integer.parseInt(postModel.getShared_count());
        shardCount ++;
        postModel.setShared_count(String.valueOf(shardCount));
//        tvSharedCount.setText(String.valueOf(shardCount));
    }

    public class UserAdpater extends BaseAdapter {

        LayoutInflater mlayoutInflater;
        public UserAdpater () {
            mlayoutInflater = LayoutInflater.from(mActivity);
        }
        @Override
        public int getCount() {
            return mArrComments.size() + 1;
        }

        @Override
        public Object getItem(int position) {
            if (position > 0) {
                return mArrComments.get(position - 1);
            } else {
                return postModel;
            }

        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (position == 0) {
                view = mlayoutInflater.inflate(R.layout.item_detail_post, null);

                ibFavorite = (ImageButton)view.findViewById(R.id.iv_ipff_favorite);
                ibFavorite.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (postModel.getFavorite().equals("favorite")) {
                            setFavorite(false);
                        } else {
                            setFavorite(true);
                        }
                    }
                });
//                ivLikeCount = (ImageView)view.findViewById(R.id.iv_ipff_like_count);
//                ivDislikeCount = (ImageView)view.findViewById(R.id.iv_ipff_dislike_count);
//                ivCommentCounts = (ImageView)view.findViewById(R.id.iv_ipff_comments);
//
//                ivLikeCount.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//
//                    }
//                });
//
//                ivDislikeCount.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//
//                    }
//                });

                if (postModel.getFavorite().equals("favorite")) {
                    ibFavorite.setImageDrawable(getResources().getDrawable(R.drawable.ic_favorite_star_white));
                } else {
                    ibFavorite.setImageDrawable(getResources().getDrawable(R.drawable.ic_favorite_star_empty));
                }
//                if (postModel.getLike().equals("like")) {
//                    ivLikeCount.setImageDrawable(getResources().getDrawable(R.drawable.green_like_count));
//                    ivDislikeCount.setImageDrawable(getResources().getDrawable(R.drawable.dislike_count_empty));
//                } else if (postModel.getLike().equals("dislike")) {
//                    ivLikeCount.setImageDrawable(getResources().getDrawable(R.drawable.like_count_empty));
//                    ivDislikeCount.setImageDrawable(getResources().getDrawable(R.drawable.green_dislike_count));
//                } else {
//                    ivLikeCount.setImageDrawable(getResources().getDrawable(R.drawable.like_count_empty));
//                    ivDislikeCount.setImageDrawable(getResources().getDrawable(R.drawable.dislike_count_empty));
//                }
//
//                if (postModel.getCommented().equals("yes")) {
//                    ivCommentCounts.setImageDrawable(getResources().getDrawable(R.drawable.green_comment_count));
//                } else {
//                    ivCommentCounts.setImageDrawable(getResources().getDrawable(R.drawable.comment_count_empty));
//                }
//                tvLikeCount = (TextView)view.findViewById(R.id.tv_ipff_like_count);
//                tvDislikeCount = (TextView)view.findViewById(R.id.tv_ipff_dislike_count);
//                tvCommentCount = (TextView)view.findViewById(R.id.tv_ipff_comment_count);
//                tvViewedCount = (TextView)view.findViewById(R.id.tv_ipff_viewed_count);
//                tvSharedCount = (TextView)view.findViewById(R.id.tv_ipff_shared_count);
//
//                tvLikeCount.setText(postModel.getLike_count());
//                tvDislikeCount.setText(postModel.getDislike_count());
//                tvCommentCount.setText(String.valueOf(mArrComments.size()));
//                tvViewedCount.setText(postModel.getViewed_count());
//                tvSharedCount.setText(postModel.getShared_count());


                MyCircularImageView myCircularImageView = (MyCircularImageView)view.findViewById(R.id.civ_ipff_avatar);
                myCircularImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });

                if (!postModel.getPoster_avatar().equals("")) {
                    UrlRectangleImageViewHelper.setUrlDrawable(myCircularImageView, postModel.getPoster_avatar(), R.drawable.default_circular_user_photo, new UrlImageViewCallback() {
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
                }


                final ImageView ivMedia = (ImageView)view.findViewById(R.id.iv_ipff_media);

                String imageUrl = "";
                String videoUrl = "";
                if (postModel.getImageWidth() != 0 && postModel.getImageHeight() != 0) {
                    double ratio = ((double)postModel.getImageHeight() / (double)postModel.getImageWidth());
                    double height = ratio * UIUtility.getScreenWidth(mActivity);
                    UIUtility.setImageViewSize(ivMedia, UIUtility.getScreenWidth(mActivity), (int)height);
                } else {
                    double height = (int)( UIUtility.getScreenWidth(mActivity) * 0.75);
                    UIUtility.setImageViewSize(ivMedia, UIUtility.getScreenWidth(mActivity),(int) height);
                }
                if (postModel.getMedia_type().equals("post_photo")) {
                    imageUrl = API.BASE_POST_PHOTO + postModel.getMedia_url();

                } else if (postModel.getMedia_type().equals("post_video")){
                    imageUrl = API.BASE_THUMBNAIL + postModel.getMedia_url().substring(0, postModel.getMedia_url().length() - 3) + "jpg";
                    videoUrl = API.BASE_POST_VIDEO + postModel.getMedia_url();
                } else if (postModel.getMedia_type().equals("youtube")) {
                    imageUrl = API.BASE_YOUTUB_PREFIX + FileUtility.getFilenameFromPath(postModel.getMedia_url()) + API.BASE_YOUTUB_SURFIX;
                    videoUrl = postModel.getMedia_url();
                }
                if (!postModel.getMedia_url().equals("")) {
                    ivMedia.setVisibility(View.VISIBLE);
                    UrlRectangleImageViewHelper.setUrlDrawable(ivMedia, imageUrl, R.drawable.default_tour, new UrlImageViewCallback() {
                        @Override
                        public void onLoaded(ImageView imageView, Bitmap loadedBitmap, String url, boolean loadedFromCache) {
                            if (!loadedFromCache) {

                                ScaleAnimation scale = new ScaleAnimation(0, 1, 0, 1, ScaleAnimation.RELATIVE_TO_SELF, .5f, ScaleAnimation.RELATIVE_TO_SELF, .5f);
                                scale.setDuration(500);
                                scale.setInterpolator(new OvershootInterpolator());
                                imageView.startAnimation(scale);


                            }
                        }
                    });
                }else {
                    ivMedia.setVisibility(View.GONE);
                }

                ImageButton ibPlay = (ImageButton)view.findViewById(R.id.ib_ipff_play);
                if (postModel.getMedia_type().equals("post_photo")  || postModel.getMedia_type().equals("")) {
                    ibPlay.setVisibility(View.GONE);
                } else {
                    ibPlay.setVisibility(View.VISIBLE);
                }
                final String finalVideoUrl = videoUrl;
                ibPlay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mActivity, MediaPlayActivity.class);
                        intent.putExtra("url", finalVideoUrl);
                        if (postModel.getMedia_type().equals("post_video")) {
                            intent.putExtra("type", "video");
                        } else {
                            intent.putExtra("type", "youtube");
                        }
                        startActivity(intent);
                    }
                });
                TextView tvName = (TextView)view.findViewById(R.id.tv_ipff_fullname);
                tvName.setText(postModel.getPoster_fullname());

                TextView tvPostedDate = (TextView)view.findViewById(R.id.tv_ipff_date);
                tvPostedDate.setText(TimeUtility.countTime(mActivity, Long.parseLong(postModel.getPosted_date())));

                TextView tvDescription = (TextView)view.findViewById(R.id.tv_ipff_description);
                String str1 = postModel.getDescription();
//                if (str1.lastIndexOf("\n") != 0) {
//                    str1 = str1.substring(0, str1.length() - 1);
//                }
                CharSequence str2 = StringUtility.trimTrailingWhitespace(Html.fromHtml(str1));
                tvDescription.setText(str2);

                //hashtag
                final TagView hashTagView = (TagView)view.findViewById(R.id.ipff_hashtag);
                List<String> hashTags = new ArrayList<>();
                if (postModel.getHashtag().length() > 0) {
                    hashTags = Arrays.asList(postModel.getHashtag().split("#"));
                    for (int j = 0; j < hashTags.size(); j ++) {

                        Tag tag = new Tag("#" + hashTags.get(j));
//                        tag.radius = 5f;
                        tag.layoutColor = getResources().getColor(R.color.transparent);
                        tag.tagTextColor =  getResources().getColor(R.color.green);
                        hashTagView.addTag(tag);
                    }
                }
                hashTagView.setOnTagClickListener(new OnTagClickListener() {

                    @Override
                    public void onTagClick(Tag tag, int position) {
                        String strTag = tag.text;
                        Intent intent = new Intent();
                        intent.putExtra("hashtag", strTag.replace("#", ""));
                        mActivity.setResult(41, intent);
                        mActivity.finish();
                    }
                });

                ///social icon
                ibLike = (ImageButton)view.findViewById(R.id.ib_ipff_like);
                ibLike.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (Long.parseLong(TimeUtility.getCurrentTimeStamp()) - postModel.getClickedTime() > 15 && !postModel.getLike().equals("dislike")) {
                            if (postModel.getLike().equals("like")) {
                                setLike("cancel_like");
                            } else if (postModel.getLike().equals("none")) {
                                setLike("like");
                            }
                        } else {
                            Utils.showToast(mActivity, mActivity.getResources().getString(R.string.wait_one_minute));
                        }

                    }
                });
                ibDislike = (ImageButton)view.findViewById(R.id.ib_ipff_dislike);
                ibDislike.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (Long.parseLong(TimeUtility.getCurrentTimeStamp()) - postModel.getClickedTime() > 15 && !postModel.getLike().equals("like")) {
                            if (postModel.getLike().equals("dislike")) {
                                setLike("cancel_dislike");
                            } else if (postModel.getLike().equals("none")) {
                                setLike("dislike");
                            }
                        } else {
                            Utils.showToast(mActivity, mActivity.getResources().getString(R.string.wait_one_minute));
                        }

                    }
                });
                if (postModel.getLike().equals("like")) {
                    ibLike.setImageDrawable(getResources().getDrawable(R.drawable.btn_like_green));
                    ibDislike.setImageDrawable(getResources().getDrawable(R.drawable.btn_dislike));
                } else if (postModel.getLike().equals("dislike")) {
                    ibLike.setImageDrawable(getResources().getDrawable(R.drawable.btn_like));
                    ibDislike.setImageDrawable(getResources().getDrawable(R.drawable.btn_dislike_green));
                } else {
                    ibLike.setImageDrawable(getResources().getDrawable(R.drawable.btn_like));
                    ibDislike.setImageDrawable(getResources().getDrawable(R.drawable.btn_dislike));
                }


                ibComment = (ImageButton)view.findViewById(R.id.ib_ipff_comment);

                if (postModel.getCommented().equals("yes")) {
                    ibComment.setImageDrawable(getResources().getDrawable(R.drawable.btn_comment_green));
                } else {
                    ibComment.setImageDrawable(getResources().getDrawable(R.drawable.btn_comment));
                }
                ibComment.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                        showCommentDlg();
                        tagedFriendIds = "";
                        DialogFragment selectWeekDayFragment = new ShowCommentDialog();
                        selectWeekDayFragment.show(getFragmentManager(), "");
                    }
                });
                ImageButton ibShare = (ImageButton)view.findViewById(R.id.ib_ipff_share);
                ibShare.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                        sharing();
                        DetailPostActivity.pushFragment(2);
                    }
                });
            } else {
                view = mlayoutInflater.inflate(R.layout.item_comment, null);

                TextView fullname;
                TextView comment, date;
                MyCircularImageView myCircularImageView;

                fullname = (TextView)view.findViewById(R.id.tv_ic_fullname);
                comment = (TextView)view.findViewById(R.id.tv_ic_comment);
                date = (TextView)view.findViewById(R.id.tv_ic_date);
                myCircularImageView = (MyCircularImageView)view.findViewById(R.id.civ_ic_avatar);

                CommentModel commentModel = mArrComments.get(position - 1);
                fullname.setText(commentModel.getFullname());
                String str1 = commentModel.getComment();
                CharSequence str2 = StringUtility.trimTrailingWhitespace(Html.fromHtml(str1));
                comment.setText(str2);
                date.setText(TimeUtility.countTime(mActivity, Long.parseLong(commentModel.getTime())));
                if (!commentModel.getAvatar().equals("")) {
                    UrlRectangleImageViewHelper.setUrlDrawable(myCircularImageView,  commentModel.getAvatar(), R.drawable.default_circular_user_photo, new UrlImageViewCallback() {
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
                }
            }


            return view;
        }
    }
}



















    //for test
//    private ArrayList<CommentModel> initializeCardItemList(){
//
//        ArrayList<CommentModel> arrayList = new ArrayList<>();
//        for(int i=0;i<30;i++){
//            CommentModel commentModel = new CommentModel();
//            commentModel.setFullname("Test user " + String.valueOf(i));
//            commentModel.setComment("comment......");
//            commentModel.setTime(String.valueOf(TimeUtility.getCurrentTimeStamp()));
//            commentModel.setAvatar("");
//            arrayList.add(commentModel);
//        }
//        return arrayList;
//    }


//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//    private void setupRecyclerView(){
//        recyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
//        recyclerView.setHasFixedSize(true);
////        mArrComments = postModel.getArrComments();
//        mArrComments = initializeCardItemList();
//        recyclerAdapter = new RecyclerAdapter(mActivity, mArrComments );
//        recyclerView.setAdapter(recyclerAdapter);
//    }