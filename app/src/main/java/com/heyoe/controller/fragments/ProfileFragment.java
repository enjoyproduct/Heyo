package com.heyoe.controller.fragments;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.error.AuthFailureError;
import com.android.volley.error.NetworkError;
import com.android.volley.error.NoConnectionError;
import com.android.volley.error.ParseError;
import com.android.volley.error.ServerError;
import com.android.volley.error.TimeoutError;
import com.android.volley.error.VolleyError;
import com.android.volley.request.CustomMultipartRequest;
import com.android.volley.request.CustomRequest;
import com.android.volley.toolbox.Volley;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.heyoe.R;
import com.heyoe.controller.DetailPostActivity;
import com.heyoe.controller.HomeActivity;
import com.heyoe.controller.MediaPlayActivity;
import com.heyoe.controller.ProfileActivity;
import com.heyoe.controller.UserListActivity;
import com.heyoe.controller.photo_crop.PhotoCropActivity;
import com.heyoe.model.API;
import com.heyoe.model.CommentModel;
import com.heyoe.model.Constant;
import com.heyoe.model.PostModel;
import com.heyoe.model.UserModel;
import com.heyoe.utilities.BitmapUtility;
import com.heyoe.utilities.DeviceUtility;
import com.heyoe.utilities.FileUtility;
import com.heyoe.utilities.StringUtility;
import com.heyoe.utilities.TimeUtility;
import com.heyoe.utilities.UIUtility;
import com.heyoe.utilities.Utils;
import com.heyoe.utilities.camera.AlbumStorageDirFactory;
import com.heyoe.utilities.camera.BaseAlbumDirFactory;
import com.heyoe.utilities.camera.FroyoAlbumDirFactory;
import com.heyoe.utilities.image_downloader.UrlImageViewCallback;
import com.heyoe.utilities.image_downloader.UrlRectangleImageViewHelper;
import com.heyoe.widget.MyCircularImageView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.kaede.tagview.Constants;
import me.kaede.tagview.OnTagClickListener;
import me.kaede.tagview.Tag;
import me.kaede.tagview.TagView;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    private static final int take_photo_from_gallery = 1;
    private static final int take_photo_from_camera = 2;
    private static final int take_video_from_gallery = 3;
    private static final int take_video_from_camera = 4;
    private static final int change_avatar_from_gallery = 5;
    private static final int change_avatar_from_camera = 6;

    private static final String JPEG_FILE_PREFIX = "Heyoe_header_photo_";
    private static final String JPEG_FILE_SUFFIX = ".jpg";
    private static final String VIDEO_FILE_PREFIX = "Heyoe_header_video_";
    private static final String VIDEO_FILE_SUFFIX = ".mp4";

    private String photoPath, videoPath, avatarPath;
    private AlbumStorageDirFactory mAlbumStorageDirFactory = null;

    private static Activity mActivity;
    private String userId;
    private static UserModel userModel;
    private static ArrayList<PostModel> mArrPost;
    private static ProfileAdapter mProfileAdapter;
    private ListView lvMain;
    private PullToRefreshListView mPullRefreshHomeListView;



    int offset;
    boolean isLast;

    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the text_layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        initVariable();
        initUI(view);
        getProfile();
        return view;
    }
    private void initVariable() {
        mActivity = getActivity();
        isLast = false;
        offset = 0;
        mArrPost = new ArrayList<>();
        userModel = new UserModel();
        userId = getArguments().getString("user_id");

        initMediaPath();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            mAlbumStorageDirFactory = new FroyoAlbumDirFactory();
        } else {
            mAlbumStorageDirFactory = new BaseAlbumDirFactory();
        }
        avatarPath = "";
    }
    private void initMediaPath() {
        photoPath = "";
        videoPath = "";
        avatarPath = "";
    }
    private void initUI(View view) {

        ///create listview
        mPullRefreshHomeListView = (PullToRefreshListView)view.findViewById(R.id.lv_profile);
        mPullRefreshHomeListView.setOnLastItemVisibleListener(new PullToRefreshBase.OnLastItemVisibleListener() {

            @Override
            public void onLastItemVisible() {
                if (!isLast) {
                    getProfile();
                }
                mPullRefreshHomeListView.onRefreshComplete();
            }
        });
        lvMain = mPullRefreshHomeListView.getRefreshableView();
        mProfileAdapter = new ProfileAdapter(mArrPost);
    }

    public static void setCelebrity() {
//        if (ivCelebrity != null && isMe()) {
//            if (Utils.getFromPreference(mActivity, Constant.CELEBRITY).equals("yes")) {
//                ivCelebrity.setVisibility(View.VISIBLE);
//            } else {
//                ivCelebrity.setVisibility(View.INVISIBLE);
//            }
//        }
        if (isMe()) {
            tvFullname.setText(Utils.getFromPreference(mActivity, Constant.FULLNAME));
        }
    }

    private void getProfile() {
        Utils.showProgress(mActivity);

        Map<String, String> params = new HashMap<String, String>();
        params.put(Constant.DEVICE_TYPE, Constant.ANDROID);
        params.put(Constant.DEVICE_TOKEN, Utils.getFromPreference(mActivity, Constant.DEVICE_TOKEN));
        params.put("user_id", userId);
        params.put("my_id", Utils.getFromPreference(mActivity, Constant.USER_ID));
        params.put("offset", String.valueOf(offset));

        CustomRequest signinRequest = new CustomRequest(Request.Method.POST, API.GET_PROFILE, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Utils.hideProgress();
                        try {
                            String status = response.getString("status");
                            if (status.equals("200")) {
                                if (offset == 0) {

                                    JSONObject jsonObject = response.getJSONObject("data");

                                    String user_id = jsonObject.getString("user_id");
                                    String fullname = jsonObject.getString("fullname");
                                    String email = jsonObject.getString("email");
                                    String password = jsonObject.getString("password");
                                    String city = jsonObject.getString("city");
                                    String country = jsonObject.getString("country");
                                    String birthday = jsonObject.getString("birthday");
                                    String gender = jsonObject.getString("gender");
                                    String celebrity = jsonObject.getString("celebrity");
                                    String about_me = jsonObject.getString("about_you");
                                    String media_count = jsonObject.getString("post_count");
                                    String friend_count = jsonObject.getString("friend_count");
                                    String avatar = jsonObject.getString("avatar");
                                    String header_photo_url = jsonObject.getString("header_photo");
                                    String header_video_url = jsonObject.getString("header_video");
                                    String qb_id = jsonObject.getString("qb_id");
                                    String friend_status = jsonObject.getString("friend_status");
                                    String online_status = jsonObject.getString("online_status");

                                    userModel.setUser_id(user_id);
                                    userModel.setFullname(fullname);
                                    userModel.setEmail(email);
                                    userModel.setPassword(password);
                                    userModel.setCity(city);
                                    userModel.setCountry(country);
                                    userModel.setBirthday(birthday);
                                    userModel.setGender(gender);
                                    userModel.setCelebrity(celebrity);
                                    userModel.setAbout_you(about_me);
                                    userModel.setAvatar(avatar);
                                    userModel.setMedia_count(media_count);
                                    userModel.setFriend_count(friend_count);
                                    userModel.setHeader_photo(header_photo_url);
                                    userModel.setHeader_video(header_video_url);
                                    userModel.setQb_id(qb_id);
                                    userModel.setFriendStatus(friend_status);
                                }


                                JSONArray jsonArray = response.getJSONArray("posts");
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
//                                    postModel.setFriend_tag(postObject.getString("tag"));
//                                    postModel.setFriend_ids(postObject.getString("tag_ids"));
                                    postModel.setHashtag(postObject.getString("hashtag"));
                                    postModel.setCommented(postObject.getString("commented"));
                                    postModel.setFavorite(postObject.getString("favorite"));
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

                                    postModel.setClickedTime((long) 0);
                                    mArrPost.add(postModel);

                                }
                                if (offset == 1) {
                                    lvMain.setAdapter(mProfileAdapter);
                                } else {
                                    mProfileAdapter.notifyDataSetChanged();
                                }

                            } else  if (status.equals("400")) {
                                Utils.showOKDialog(mActivity, getResources().getString(R.string.access_denied));
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
    private void uploadHeaderMedia(final int type) {
        Utils.showProgress(mActivity);
        CustomMultipartRequest customMultipartRequest = new CustomMultipartRequest(API.SET_HEADER_MEDIA,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Utils.hideProgress();
                        try {
                            String status = response.getString("status");
                            if (status.equals("200")) {
                                Utils.showToast(mActivity, getResources().getString(R.string.success));

                                String headerMediaUrl = response.getString("data");
                                if (type == 2) {
                                    Utils.saveToPreference(mActivity, Constant.HEADER_VIDEO, headerMediaUrl);
                                    userModel.setHeader_video(headerMediaUrl);
                                    FileUtility.deleteFile(videoPath);

                                } else if (type == 1) {
                                    Utils.saveToPreference(mActivity, Constant.HEADER_PHOTO, headerMediaUrl);
                                    userModel.setHeader_photo(headerMediaUrl);
                                    FileUtility.deleteFile(photoPath);
                                }
                            } else  if (status.equals("400")) {
                                Utils.showOKDialog(mActivity, getResources().getString(R.string.access_denied));
                            }else  if (status.equals("401")) {
                                Utils.showOKDialog(mActivity, response.getString("reason"));
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
                        if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                            Toast.makeText(mActivity, "TimeoutError", Toast.LENGTH_LONG).show();
                        } else if (error instanceof AuthFailureError) {
                            //TODO
                            Toast.makeText(mActivity, "AuthFailureError", Toast.LENGTH_LONG).show();
                        } else if (error instanceof ServerError) {
                            //TODO
                            Toast.makeText(mActivity, "ServerError", Toast.LENGTH_LONG).show();
                        } else if (error instanceof NetworkError) {
                            //TODO
                            Toast.makeText(mActivity, "NetworkError", Toast.LENGTH_LONG).show();
                        } else if (error instanceof ParseError) {
                            //TODO
                            Toast.makeText(mActivity, "ParseError", Toast.LENGTH_LONG).show();
                        } else {
                            //TODO
                            Toast.makeText(mActivity, "UnknownError", Toast.LENGTH_LONG).show();
                        }
                    }
                });
        customMultipartRequest
                .addStringPart(Constant.DEVICE_TYPE, Constant.ANDROID)
                .addStringPart(Constant.DEVICE_TOKEN, Utils.getFromPreference(mActivity, Constant.DEVICE_TOKEN))
                .addStringPart("my_id", Utils.getFromPreference(mActivity, Constant.USER_ID));


        if (type == 2) {
            customMultipartRequest.addStringPart("media_type", "header_video");
            customMultipartRequest.addVideoPart("header_video", videoPath);

        } else if (type == 1) {
            customMultipartRequest.addStringPart("media_type", "header_photo");
            customMultipartRequest.addStringPart("width", String.valueOf(imageWidth));
            customMultipartRequest.addStringPart("height", String.valueOf(imageHeight));
            customMultipartRequest.addImagePart("header_photo", photoPath);
        }

        RequestQueue requestQueue = Volley.newRequestQueue(mActivity);
        requestQueue.add(customMultipartRequest);
    }
    private void updateAvatar() {
        Utils.showProgress(mActivity);
        CustomMultipartRequest customMultipartRequest = new CustomMultipartRequest(API.CHANGE_AVATAR,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Utils.hideProgress();
                        try {
                            String status = response.getString("status");
                            if (status.equals("200")) {
                                Utils.showToast(mActivity, getResources().getString(R.string.success));

                                String avatarURL = response.getString("data");
                                Utils.saveToPreference(mActivity, Constant.AVATAR, avatarURL);
                                userModel.setAvatar(avatarURL);
                                for(PostModel pm : mArrPost) {
                                    pm.setPoster_avatar(avatarURL);
                                }
                                mProfileAdapter.notifyDataSetChanged();
                                FileUtility.deleteFile(avatarPath);

                                ///update menu avatar
                                HomeActivity.resetMenu();
                            } else  if (status.equals("400")) {
                                Utils.showOKDialog(mActivity, getResources().getString(R.string.access_denied));
                            }else  if (status.equals("401")) {
                                Utils.showOKDialog(mActivity, response.getString("reason"));
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
                        if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                            Toast.makeText(mActivity, "TimeoutError", Toast.LENGTH_LONG).show();
                        } else if (error instanceof AuthFailureError) {
                            //TODO
                            Toast.makeText(mActivity, "AuthFailureError", Toast.LENGTH_LONG).show();
                        } else if (error instanceof ServerError) {
                            //TODO
                            Toast.makeText(mActivity, "ServerError", Toast.LENGTH_LONG).show();
                        } else if (error instanceof NetworkError) {
                            //TODO
                            Toast.makeText(mActivity, "NetworkError", Toast.LENGTH_LONG).show();
                        } else if (error instanceof ParseError) {
                            //TODO
                            Toast.makeText(mActivity, "ParseError", Toast.LENGTH_LONG).show();
                        } else {
                            //TODO
                            Toast.makeText(mActivity, "UnknownError", Toast.LENGTH_LONG).show();
                        }
                    }
                });
        customMultipartRequest
                .addStringPart(Constant.DEVICE_TYPE, Constant.ANDROID)
                .addStringPart(Constant.DEVICE_TOKEN, Utils.getFromPreference(mActivity, Constant.DEVICE_TOKEN))
                .addStringPart("my_id", Utils.getFromPreference(mActivity, Constant.USER_ID));
        customMultipartRequest.addImagePart("avatar", avatarPath);

        RequestQueue requestQueue = Volley.newRequestQueue(mActivity);
        requestQueue.add(customMultipartRequest);
    }
    private void sendFriendRequest() {
        Utils.showProgress(mActivity);

        Map<String, String> params = new HashMap<String, String>();
        params.put(Constant.DEVICE_TYPE, Constant.ANDROID);
        params.put(Constant.DEVICE_TOKEN, Utils.getFromPreference(mActivity, Constant.DEVICE_TOKEN));
        params.put("my_id", Utils.getFromPreference(mActivity, Constant.USER_ID));
        params.put("user_id", userModel.getUser_id());

        CustomRequest signinRequest = new CustomRequest(Request.Method.POST, API.INVITE_FRIEND, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Utils.hideProgress();
                        try {
                            String status = response.getString("status");
                            if (status.equals("200")) {
                                userModel.setFriendStatus("waiting");
                                mProfileAdapter.notifyDataSetChanged();
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
    private static boolean isMe() {
        if (userModel.getUser_id().equals(Utils.getFromPreference(mActivity, Constant.USER_ID))) {
            return true;
        } else {
            return false;
        }
    }

    ImageView ivMedia;
    ImageButton ibPlayVideo;
    static ImageView ivCelebrity;
    static TextView tvFullname;
    MyCircularImageView avatar;

    private class ProfileAdapter extends BaseAdapter {
        ArrayList<PostModel> arrayList;
        public ProfileAdapter(ArrayList<PostModel> arrayList) {
            this.arrayList = arrayList;
        }
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view = null;
            if (position == 0) {
                view = mActivity.getLayoutInflater().inflate(R.layout.item_profile, null);
                RelativeLayout rlInfo, rlMedia, rlFriend;
                rlInfo = (RelativeLayout)view.findViewById(R.id.rl_profile_info);
                rlMedia = (RelativeLayout)view.findViewById(R.id.rl_profile_media);
                rlFriend = (RelativeLayout)view.findViewById(R.id.rl_profile_friend);

                rlInfo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                        ProfileActivity.pushFragment(1);
                        Intent intent = new Intent(mActivity, ProfileActivity.class);
                        intent.putExtra("to_where", 0);
                        intent.putExtra("userModel", userModel);
                        if (isMe()) {
                            startActivityForResult(intent, 104);
                        } else {
                            startActivity(intent);
                        }
                    }
                });
                rlMedia.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                        ProfileActivity.pushFragment(2);
                        Intent intent = new Intent(mActivity, ProfileActivity.class);
                        intent.putExtra("to_where", 1);
                        intent.putExtra("userModel", userModel);

                        startActivity(intent);

                    }
                });
                rlFriend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                        ProfileActivity.pushFragment(3);
                        Intent intent = new Intent(mActivity, ProfileActivity.class);
                        intent.putExtra("to_where", 2);
                        intent.putExtra("userModel", userModel);

                        startActivity(intent);
                    }
                });
                tvFullname = (TextView)view.findViewById(R.id.tv_profile_fullname);
                TextView tvFriendCount = (TextView)view.findViewById(R.id.tv_profile_friends_count);
                ibPlayVideo = (ImageButton)view.findViewById(R.id.ib_profile_play);
                ibPlayVideo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (isMe()) {
                            showVideoChooseDialog();
                        } else {
                            if (userModel.getHeader_video().length() > 0) {
                                Intent intent = new Intent(mActivity, MediaPlayActivity.class);
                                intent.putExtra("url", API.BASE_HEADER_VIDEO +  userModel.getHeader_video());
                                intent.putExtra("type", "video");
                                startActivity(intent);
                            }
                        }
                    }
                });


                ivMedia = (ImageView)view.findViewById(R.id.iv_profile_media);
                if (!userModel.getHeader_photo().equals("")) {

                    UrlRectangleImageViewHelper.setUrlDrawable(ivMedia, API.BASE_HEADER_PHOTO + userModel.getHeader_photo(), R.drawable.default_tour, new UrlImageViewCallback() {
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
                ivMedia.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (isMe()) {
//                            showPictureChooseDialog(0);
                            cropImage();
                        }
                    }
                });
                avatar = (MyCircularImageView)view.findViewById(R.id.civ_profile_avatar);
                if (!userModel.getAvatar().equals("")) {
                    UrlRectangleImageViewHelper.setUrlDrawable(avatar, API.BASE_AVATAR + userModel.getAvatar(), R.drawable.default_user, new UrlImageViewCallback() {
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
                avatar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (isMe()) {
                            showPictureChooseDialog(1);
                        }
                    }
                });

                tvFullname.setText(userModel.getFullname());
                tvFriendCount.setText(userModel.getFriend_count());

                ivCelebrity = (ImageView)view.findViewById(R.id.iv_profile_celebrity);
                if (userModel.getCelebrity().equals("yes")) {
                    ivCelebrity.setVisibility(View.VISIBLE);
                } else {
                    ivCelebrity.setVisibility(View.INVISIBLE);
                }
                ImageButton ibAddFriend = (ImageButton)view.findViewById(R.id.ib_ip_add_friend);
                if (userModel.getFriendStatus().equals("waiting")) {
                    ibAddFriend.setImageDrawable(getResources().getDrawable(R.drawable.sandglass_small));
                } else if (userModel.getFriendStatus().equals("none")){
                    ibAddFriend.setImageDrawable(getResources().getDrawable(R.drawable.ic_green_plus));
                } else {
                    ibAddFriend.setVisibility(View.GONE);
                }
                if (isMe())
                    ibAddFriend.setVisibility(View.GONE);
                ibAddFriend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (userModel.getFriendStatus().equals("none")) {
                            sendFriendRequest();
                        }
                    }
                });
            } else {
                view = mActivity.getLayoutInflater().inflate(R.layout.item_post_for_friend, null);
                final PostModel postModel = arrayList.get(position - 1);
                final ImageButton ibFavorite = (ImageButton)view.findViewById(R.id.iv_ipff_favorite);
                ibFavorite.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (postModel.getFavorite().equals("favorite")) {
                            setFavorite(position - 1, false);
                        } else {
                            setFavorite(position - 1, true);
                        }
                    }
                });
                ///small icons
                ImageView ivLikeCount = (ImageView)view.findViewById(R.id.iv_ipff_like_count);
                ImageView ivDislikeCount = (ImageView)view.findViewById(R.id.iv_ipff_dislike_count);
                ImageView ivComments = (ImageView)view.findViewById(R.id.iv_ipff_comments);

                ivLikeCount.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mActivity, UserListActivity.class);
                        intent.putExtra("type", "like");
                        intent.putExtra("post", postModel);
                        mActivity.startActivity(intent);
                    }
                });

                ivDislikeCount.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mActivity, UserListActivity.class);
                        intent.putExtra("type", "dislike");
                        intent.putExtra("post", postModel);
                        mActivity.startActivity(intent);
                    }
                });
                ivComments.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mActivity, DetailPostActivity.class);
                        intent.putExtra("post", postModel);
                        mActivity.startActivityForResult(intent, 104);
                    }
                });
                TextView tvLikeCount = (TextView)view.findViewById(R.id.tv_ipff_like_count);
                TextView tvDislike = (TextView)view.findViewById(R.id.tv_ipff_dislike_count);

                TextView[] tvNames = new TextView[3];
                tvNames[0] = (TextView)view.findViewById(R.id.tv_ipff_name1);
                tvNames[1] = (TextView)view.findViewById(R.id.tv_ipff_name2);
                tvNames[2] = (TextView)view.findViewById(R.id.tv_ipff_name3);

                TextView[]  tvComments = new TextView[3];
                tvComments[0] = (TextView)view.findViewById(R.id.tv_ipff_comment1);
                tvComments[1] = (TextView)view.findViewById(R.id.tv_ipff_comment2);
                tvComments[2] = (TextView)view.findViewById(R.id.tv_ipff_comment3);

                LinearLayout[] llComments = new LinearLayout[3];
                llComments[0] = (LinearLayout)view.findViewById(R.id.ll_ipff_comment1);
                llComments[1] = (LinearLayout)view.findViewById(R.id.ll_ipff_comment2);
                llComments[2] = (LinearLayout)view.findViewById(R.id.ll_ipff_comment3);

                TextView tvViewedCount = (TextView)view.findViewById(R.id.tv_ipff_viewed_count);
                TextView tvSharedCount = (TextView)view.findViewById(R.id.tv_ipff_shared_count);

                if (postModel.getFavorite().equals("favorite")) {
                    ibFavorite.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.ic_favorite_star_white));
                } else {
                    ibFavorite.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.ic_favorite_star_empty));
                }
                if (postModel.getLike().equals("like")) {
                    ivLikeCount.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.green_like_count));
                    ivDislikeCount.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.dislike_count_empty));
                } else if (postModel.getLike().equals("dislike")) {
                    ivLikeCount.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.like_count_empty));
                    ivDislikeCount.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.green_dislike_count));
                } else {
                    ivLikeCount.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.like_count_empty));
                    ivDislikeCount.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.dislike_count_empty));
                }

                if (postModel.getCommented().equals("yes")) {
                    ivComments.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.green_comment_count));
                } else {
                    ivComments.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.comment_count_empty));
                }

                tvLikeCount.setText(postModel.getLike_count());
                tvDislike.setText(postModel.getDislike_count());
                tvViewedCount.setText(postModel.getViewed_count());
                tvSharedCount.setText(postModel.getShared_count());

                int commentCount = postModel.getArrComments().size() > 3 ? 3 : postModel.getArrComments().size();
                for (int i = 0; i < commentCount; i ++) {
                    CommentModel commentModel = postModel.getArrComments().get(i);
                    tvNames[i].setText(commentModel.getFullname());
                    String str1 = commentModel.getComment();
                    CharSequence str2 = StringUtility.trimTrailingWhitespace(Html.fromHtml(str1));
                    tvComments[i].setText(str2);
                    llComments[i].setVisibility(View.VISIBLE);
                }
                TextView tvCheckoutAllComments = (TextView)view.findViewById(R.id.tv_ipff_checkout_all_comments);
                tvCheckoutAllComments.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mActivity, DetailPostActivity.class);
                        intent.putExtra("post", postModel);
                        mActivity.startActivityForResult(intent, 104);
                    }
                });


                ImageButton ibLike = (ImageButton)view.findViewById(R.id.ib_ipff_like);
                ibLike.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (Long.parseLong(TimeUtility.getCurrentTimeStamp()) - postModel.getClickedTime() > 60 && !postModel.getLike().equals("dislike")) {
                            if (postModel.getLike().equals("like")) {
                                setLike(position - 1, "cancel_like");
                            } else if (postModel.getLike().equals("none")) {
                                setLike(position - 1, "like");
                            }
                        } else {
                            Utils.showToast(mActivity, mActivity.getResources().getString(R.string.wait_one_minute));
                        }

                    }
                });
                ImageButton ibDislike = (ImageButton)view.findViewById(R.id.ib_ipff_dislike);
                ibDislike.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (Long.parseLong(TimeUtility.getCurrentTimeStamp()) - postModel.getClickedTime() > 60 && !postModel.getLike().equals("like")) {
                            if (postModel.getLike().equals("dislike")) {
                                setLike(position - 1, "cancel_dislike");
                            } else if (postModel.getLike().equals("none")) {
                                setLike(position - 1, "dislike");
                            }
                        }else {
                            Utils.showToast(mActivity, mActivity.getResources().getString(R.string.wait_one_minute));
                        }

                    }
                });
                if (postModel.getLike().equals("like")) {
                    ibLike.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.btn_like_green));
                    ibDislike.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.btn_dislike));
                } else if (postModel.getLike().equals("dislike")) {
                    ibLike.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.btn_like));
                    ibDislike.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.btn_dislike_green));
                } else {
                    ibLike.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.btn_like));
                    ibDislike.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.btn_dislike));
                }


                ImageButton ibComment = (ImageButton)view.findViewById(R.id.ib_ipff_comment);
                ibComment.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mActivity, DetailPostActivity.class);
                        intent.putExtra("post", postModel);
                        mActivity.startActivityForResult(intent, 104);
                    }
                });
                if (postModel.getCommented().equals("yes")) {
                    ibComment.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.btn_comment_green));
                } else {
                    ibComment.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.btn_comment));
                }

                ImageButton ibShare = (ImageButton)view.findViewById(R.id.ib_ipff_share);
                ibShare.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                        sharing(position - 1);
                        HomeActivity.navigateToRepost(postModel);
                    }
                });

                MyCircularImageView myCircularImageView = (MyCircularImageView)view.findViewById(R.id.civ_ipff_avatar);

                if (!postModel.getPoster_avatar().equals("")) {
                    UrlRectangleImageViewHelper.setUrlDrawable(myCircularImageView, API.BASE_AVATAR + postModel.getPoster_avatar(), R.drawable.default_user, new UrlImageViewCallback() {
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
                ivMedia.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mActivity, DetailPostActivity.class);
                        intent.putExtra("post", postModel);
                        mActivity.startActivityForResult(intent, 104);
                    }
                });

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
                } else {
                    ivMedia.setVisibility(View.GONE);
                }

                ImageButton ibPlay = (ImageButton)view.findViewById(R.id.ib_ipff_play);
                if (postModel.getMedia_type().equals("post_photo") || postModel.getMedia_type().equals("")) {
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
                        mActivity.startActivity(intent);
                    }
                });
                TextView tvName = (TextView)view.findViewById(R.id.tv_ipff_fullname);
                tvName.setText(postModel.getPoster_fullname());

                TextView tvPostedDate = (TextView)view.findViewById(R.id.tv_ipff_date);
                tvPostedDate.setText(TimeUtility.countTime(mActivity, Long.parseLong(postModel.getPosted_date()) ));

                TextView tvDescription = (TextView)view.findViewById(R.id.tv_ipff_description);
                String str1 = postModel.getDescription();
//                if (str1.lastIndexOf("\n") != 0) {
//                    str1 = str1.substring(0, str1.length() - 1);
//                }
                CharSequence str2 = StringUtility.trimTrailingWhitespace(Html.fromHtml(str1));
                tvDescription.setText(str2);
                tvDescription.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mActivity, DetailPostActivity.class);
                        intent.putExtra("post", postModel);
                        mActivity.startActivityForResult(intent, 104);
                    }
                });

                //hashtag
                List<String> hashTags = new ArrayList<>();
                int lineMargin = 10;
                int tagMargin = 10;
                int mWidth = UIUtility.getScreenWidth(mActivity);
                int total = 0;
                int listIndex = 1;// List Index
                int index_bottom = 1;// The Tag to add below
                int index_header = 1;// The header tag of this line

                RelativeLayout rlTagContainer = (RelativeLayout)view.findViewById(R.id.rl_ipff_tag_container);
                View view_pre = null;
                if (postModel.getHashtag().length() > 0) {
                    hashTags = Arrays.asList(postModel.getHashtag().split("#"));
                    final List<String> finalHashTags = hashTags;
                    for (int j = 0; j < hashTags.size(); j ++) {
//                Tag tag = new Tag("#" + hashTags.get(j));
//                tag.layoutColor = activity.getResources().getColor(R.color.transparent);
//                tag.tagTextColor =  activity.getResources().getColor(R.color.green);
//                hashTagView.addTag(tag);
                        View view1 = mActivity.getLayoutInflater().inflate(R.layout.text_layout, null);
                        view1.setId(listIndex);

                        TextView textView = (TextView)view1.findViewById(R.id.tag);
                        textView.setText("#" + hashTags.get(j));
                        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) textView.getLayoutParams();
                        params.setMargins(10, 5, 10, 5);
                        textView.setLayoutParams(params);
                        final int finalJ = j;
                        textView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
//                                if (MainFragment.isFavorite) {
//                                    HomeActivity.navigateForHashTag(true, finalHashTags.get(finalJ));
//                                } else {
                                    HomeActivity.navigateForHashTag(false, finalHashTags.get(finalJ));
//                                }
                            }
                        });

                        RelativeLayout.LayoutParams tagParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                        //tagParams.setMargins(0, 0, 0, 0);

                        //add margin of each line
                        tagParams.bottomMargin = lineMargin;
                        // calculate　of tag layout width
                        float tagWidth = textView.getPaint().measureText(textView.getText().toString()) + 10 + 10;

                        if (mWidth <= total + tagMargin + tagWidth + me.kaede.tagview.Utils.dpToPx(mActivity, Constants.LAYOUT_WIDTH_OFFSET)) {
                            //need to add in new line
                            tagParams.addRule(RelativeLayout.BELOW, index_bottom);
                            // initialize total param (layout padding left & layout padding right)
                            total = rlTagContainer.getPaddingLeft() + rlTagContainer.getPaddingRight();
                            index_bottom = listIndex;
                            index_header = listIndex;
                        } else {
                            //no need to new line
                            tagParams.addRule(RelativeLayout.ALIGN_TOP, index_header);
                            //not header of the line
                            if (listIndex != index_header) {
                                tagParams.addRule(RelativeLayout.RIGHT_OF, listIndex - 1);
                                tagParams.leftMargin = tagMargin;
                                total += tagMargin;
                                if (view_pre!=null && view_pre.getWidth() < view1.getWidth()) {
                                    index_bottom = listIndex;
                                }
                            }


                        }


                        total += tagWidth;
                        rlTagContainer.addView(view1, tagParams);
                        view_pre = view1;
                        listIndex++;
                    }
                }
            }
            return view;
        }

        @Override
        public int getCount() {
            return arrayList.size() + 1;
        }

        @Override
        public Object getItem(int position) {
            return arrayList.get(position - 1);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

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
                                mProfileAdapter.notifyDataSetChanged();

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

                                mProfileAdapter.notifyDataSetChanged();
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
    static int shardPostNum;
    public static  void sharing(int position){
        shardPostNum = position;
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, mArrPost.get(position).getPoster_fullname());
        sharingIntent.putExtra(Intent.EXTRA_TEXT, "http://heyoe.com/");
        mActivity.startActivityForResult(Intent.createChooser(sharingIntent, "Heyoe"), 105);

    }
    public static void updateSharedCount() {
        sharePost(shardPostNum);
        int shardCount  = Integer.parseInt(mArrPost.get(shardPostNum).getShared_count());
        shardCount ++;
        mArrPost.get(shardPostNum).setShared_count(String.valueOf(shardCount));
        mProfileAdapter.notifyDataSetChanged();
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
    public static void updatePostFeed(PostModel postModel) {
        for (int i = 0; i < mArrPost.size(); i ++) {
            if (postModel.getPost_id().equals(mArrPost.get(i).getPost_id())) {
                mArrPost.remove(i);
                mArrPost.add(i, postModel);
                mProfileAdapter.notifyDataSetChanged();
                break;
            }
        }
    }
    public static void deletePost(PostModel postModel) {
        for (int i = 0; i < mArrPost.size(); i ++) {
            if (postModel.getPost_id().equals(mArrPost.get(i).getPost_id())) {
                mArrPost.remove(i);
                mProfileAdapter.notifyDataSetChanged();
                break;
            }
        }
    }







    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 108:
                if (resultCode == Activity.RESULT_OK && data != null) {
//                    Bitmap bitmap = (Bitmap)data.getParcelableExtra("cropped_image");
                    byte[] byteArray = data.getByteArrayExtra("cropped_image");
                    Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                    if (bitmap != null) {
                        ivMedia.setImageBitmap(bitmap);
                        imageWidth = bitmap.getWidth();
                        imageHeight = bitmap.getHeight();
                        photoPath = BitmapUtility.saveBitmap(bitmap, Constant.MEDIA_PATH + "heyoe", "heyoe_header_photo.jpg");

                        if (photoPath.length() > 0) {
                            askToUploadHeaderMedia(1);
                        }
                    }
                }
                break;
            case change_avatar_from_gallery:
                if (resultCode == Activity.RESULT_OK) {

                    Uri selectedImage = data.getData();
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};

                    Cursor cursor = mActivity.getContentResolver().query(
                            selectedImage, filePathColumn, null, null, null);
                    cursor.moveToFirst();
                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    initMediaPath();
                    avatarPath = cursor.getString(columnIndex);
                    cursor.close();

                    Bitmap bitmap = BitmapUtility.adjustBitmap(avatarPath);
                    avatar.setImageBitmap(bitmap);
                    imageWidth = bitmap.getWidth();
                    imageHeight = bitmap.getHeight();
                    avatarPath = BitmapUtility.saveBitmap(bitmap, Constant.MEDIA_PATH + "heyoe", FileUtility.getFilenameFromPath(avatarPath));

                    if (avatarPath.length() > 0) {
                        askToUpdateAvatar();
                    }


                }
                break;
            case change_avatar_from_camera:
                if (resultCode == Activity.RESULT_OK) {
                    setPic(1);
                    if (avatarPath.length() > 0) {
                        askToUpdateAvatar();
                    }
                }
                break;
            case take_photo_from_gallery:
                if (resultCode == Activity.RESULT_OK) {

                    Uri selectedImage = data.getData();
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};

                    Cursor cursor = mActivity.getContentResolver().query(
                            selectedImage, filePathColumn, null, null, null);
                    cursor.moveToFirst();
                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    initMediaPath();
                    photoPath = cursor.getString(columnIndex);
                    cursor.close();

                    Bitmap bitmap = BitmapUtility.adjustBitmap(photoPath);
                    ivMedia.setImageBitmap(bitmap);
                    imageWidth = bitmap.getWidth();
                    imageHeight = bitmap.getHeight();
                    photoPath = BitmapUtility.saveBitmap(bitmap, Constant.MEDIA_PATH + "heyoe", FileUtility.getFilenameFromPath(photoPath));

                    if (photoPath.length() > 0) {
                        askToUploadHeaderMedia(1);
                    }


                }
                break;
            case take_photo_from_camera:
                if (resultCode == Activity.RESULT_OK) {
                    setPic(0);
                    if (photoPath.length() > 0) {
                        askToUploadHeaderMedia(1);
                    }
                }
                break;

            case take_video_from_gallery:
                if (resultCode == Activity.RESULT_OK) {
                    Uri selectedVideoUri = data.getData();
                    String[] filePathColumn = {MediaStore.Video.VideoColumns.DATA};
                    videoPath = getVideoPath(selectedVideoUri);
                    if (videoPath != null && videoPath.length() > 0) {
                        askToUploadHeaderMedia(2);
                    }
                }
                break;
            case take_video_from_camera:
                if (resultCode == Activity.RESULT_OK) {
                    if (videoPath.length() > 0) {
                        askToUploadHeaderMedia(2);
                    }
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    // User cancelled the video capture
                    Toast.makeText(mActivity, "User cancelled the video capture.",Toast.LENGTH_LONG).show();
                } else {
                    // Video capture failed, advise user
                    Toast.makeText(mActivity, "Video capture failed.",Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    public String getVideoPath(Uri uri) {

        String path = "";
        try {
            Cursor cursor = mActivity.getContentResolver().query(uri, null, null, null, null);
            cursor.moveToFirst();
            String document_id = cursor.getString(0);
            document_id = document_id.substring(document_id.lastIndexOf(":")+1);
            cursor.close();

            cursor = mActivity.getContentResolver().query(android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    null, MediaStore.Video.Media._ID + " = ? ", new String[]{document_id}, null);
            cursor.moveToFirst();
            path = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
            cursor.close();

        }catch (Exception e) {
            e.printStackTrace();
        }

        return path;
    }
    public void askToUpdateAvatar() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle(Constant.INDECATOR);
        builder.setMessage(getResources().getString(R.string.confirm_update_avatar));
        builder.setCancelable(true);
        builder.setPositiveButton("Set",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                       updateAvatar();
                        dialog.cancel();
                    }
                });
        builder.setNegativeButton("Discard",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (userModel.getAvatar().length() == 0) {
                            avatar.setImageDrawable(getResources().getDrawable(R.drawable.default_user));
                        } else {
                            if (!userModel.getHeader_photo().equals("")) {
                                UrlRectangleImageViewHelper.setUrlDrawable(avatar, API.BASE_AVATAR + userModel.getAvatar(), R.drawable.default_user, new UrlImageViewCallback() {
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
                        dialog.cancel();
                    }
                });


        AlertDialog alert = builder.create();
        alert.show();
    }
    private boolean checkFileSize(String filePath) {
        boolean isBigger = false;
        File file =new File(filePath);
        if(file.exists()){

            double bytes = file.length();
            double kilobytes = (bytes / 1024);
            double megabytes = (kilobytes / 1024);
            double gigabytes = (megabytes / 1024);
            double terabytes = (gigabytes / 1024);
            double petabytes = (terabytes / 1024);
            double exabytes = (petabytes / 1024);
            double zettabytes = (exabytes / 1024);
            double yottabytes = (zettabytes / 1024);

            System.out.println("bytes : " + bytes);
            System.out.println("kilobytes : " + kilobytes);
            System.out.println("megabytes : " + megabytes);
            System.out.println("gigabytes : " + gigabytes);
            System.out.println("terabytes : " + terabytes);
            System.out.println("petabytes : " + petabytes);
            System.out.println("exabytes : " + exabytes);
            System.out.println("zettabytes : " + zettabytes);
            System.out.println("yottabytes : " + yottabytes);

            if (megabytes * 5 > DeviceUtility.getFreeRamSize(mActivity)) {
                isBigger = true;
            }
        }else{
            System.out.println("File does not exists!");
        }

        return isBigger;
    }
    public void askToUploadHeaderMedia(final int type) {
        String msg = "";
        switch (type) {
            case 1:
                msg = getResources().getString(R.string.confirm_header_photo);
                break;
            case 2:
                msg = getResources().getString(R.string.confirm_header_video);
                break;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle(Constant.INDECATOR);
        builder.setMessage(msg);
        builder.setCancelable(true);
        builder.setPositiveButton("Set",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (checkFileSize(videoPath)) {
                            Utils.showToast(mActivity, "Video size is too big");
                        } else {
                            uploadHeaderMedia(type);
                        }

                        dialog.cancel();
                    }
                });
        builder.setNegativeButton("Discard",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (type == 1) {
                            if (userModel.getHeader_photo().length() == 0) {
                                ivMedia.setImageDrawable(getResources().getDrawable(R.drawable.post3));
                            } else {
                                if (!userModel.getHeader_photo().equals("")) {
                                    UrlRectangleImageViewHelper.setUrlDrawable(ivMedia, API.BASE_HEADER_PHOTO + userModel.getHeader_photo(), R.drawable.default_tour, new UrlImageViewCallback() {
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

                        }
                        dialog.cancel();
                    }
                });


        AlertDialog alert = builder.create();
        alert.show();
    }


    //    choose video
    private void showVideoChooseDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle(Constant.INDECATOR);
        builder.setMessage(getResources().getString(R.string.choose_video));
        builder.setCancelable(true);
        builder.setPositiveButton("Camera",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        captureVideoFromCamera();
                        dialog.cancel();
                    }
                });
        builder.setNegativeButton("Gallery",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        takeVideoFromGallery();
                        dialog.cancel();
                    }
                });

        builder.setNeutralButton("Play",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (userModel.getHeader_video().length() > 0) {
                            Intent intent = new Intent(mActivity, MediaPlayActivity.class);
                            intent.putExtra("url", API.BASE_HEADER_VIDEO +  userModel.getHeader_video());
                            intent.putExtra("type", "video");
                            startActivity(intent);
                        }

                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
    private void takeVideoFromGallery()
    {
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.putExtra("return-data", true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Video"), take_video_from_gallery);
    }
    private Uri fileUri;
    private void captureVideoFromCamera() {
// create new Intentwith with Standard Intent action that can be
        // sent to have the camera application capture an video and return it.
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

        // create a file to save the video
        fileUri = getOutputMediaFileUri();
        initMediaPath();
        videoPath = fileUri.getPath();
        // set the image file name
        intent.putExtra(MediaStore.EXTRA_OUTPUT,fileUri);

        // set the video image quality to high
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
        // set max time limit
        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 15);
        ///set max size limit
        intent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, DeviceUtility.getFreeRamSize(mActivity) * 1024 * 1024 / 5);
        // start the Video Capture Intent
        startActivityForResult(intent, take_video_from_camera);
    }
    /** Create a file Uri for saving an image or video */
    private Uri getOutputMediaFileUri(){

        return Uri.fromFile(getOutputMediaFile());
    }
    /** Create a File for saving an image or video */
    private File getOutputMediaFile(){

        // Check that the SDCard is mounted
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "HeyoeVideo");


        // Create the storage directory(MyCameraVideo) if it does not exist
        if (! mediaStorageDir.exists()){

            if (! mediaStorageDir.mkdirs()){


                Toast.makeText(mActivity, "Failed to create directory HeyoeVideo.",
                        Toast.LENGTH_LONG).show();

                Log.d("MyCameraVideo", "Failed to create directory HeyoeVideo.");
                return null;
            }
        }


        // Create a media file name

        // For unique file name appending current timeStamp with file name
        java.util.Date date= new java.util.Date();
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(date.getTime());

        File mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                VIDEO_FILE_PREFIX + timeStamp + VIDEO_FILE_SUFFIX);


        return mediaFile;
    }




    ///photo choose dialog
    public void showPictureChooseDialog(final int type){

        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle(Constant.INDECATOR);
        builder.setMessage(getResources().getString(R.string.choose_photo));
        builder.setCancelable(true);
        builder.setPositiveButton("Camera",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dispatchTakePictureIntent(type);
                        dialog.cancel();
                    }
                });
        builder.setNegativeButton("gallery",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        takePictureFromGallery(type);
                        dialog.cancel();
                    }
                });
        builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int arg1) {
                dialog.cancel();

            }
        });
//        dialog.setCancelable(true);
//        dialog.setCanceledOnTouchOutside(false);
        AlertDialog alert = builder.create();
        alert.show();
    }
    private void cropImage() {
        Intent intent = new Intent(mActivity, PhotoCropActivity.class);
        startActivityForResult(intent, 108);
    }
    //////////////////take a picture from gallery
    private void takePictureFromGallery(int type)
    {

        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        if (type == 0) {// take picture for header photo
//            startActivityForResult(photoPickerIntent, take_photo_from_gallery);
            cropImage();
        } else {
            startActivityForResult(photoPickerIntent, change_avatar_from_gallery);
        }

    }
    /////////////capture photo
    public void dispatchTakePictureIntent(int type) {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File f = null;
        try {
            f = setUpPhotoFile(type);
            initMediaPath();
            if (type == 0) {// take picture for header photo
                photoPath = f.getAbsolutePath();
            } else {
                avatarPath = f.getAbsolutePath();
            }

            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
        } catch (IOException e) {
            e.printStackTrace();
            f = null;
            photoPath = "";
            avatarPath = "";
        }
        if (type == 0) {// take picture for header photo
//            startActivityForResult(takePictureIntent, take_photo_from_camera);
            cropImage();
        } else {
            startActivityForResult(takePictureIntent, change_avatar_from_camera);
        }


    }
    private File setUpPhotoFile(int type) throws IOException {

        File f = createImageFile();
        initMediaPath();
        return f;
    }
    private File createImageFile() throws IOException {

        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_";
        File albumF = getAlbumDir();
        File imageF = File.createTempFile(imageFileName, JPEG_FILE_SUFFIX, albumF);
        return imageF;
    }
    private File getAlbumDir() {

        File storageDir = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            storageDir = mAlbumStorageDirFactory.getAlbumStorageDir("AllyTours");
            if (storageDir != null) {
                if (! storageDir.mkdirs()) {
                    if (! storageDir.exists()){
                        Log.d("CameraSample", "failed to create directory");
                        return null;
                    }
                }
            }
        } else {
            Log.v(getString(R.string.app_name), "External storage is not mounted READ/WRITE.");
        }
        return storageDir;
    }
    private void setPic(int type) {
        if (photoPath == null) {
            return;
        }

		/* There isn't enough memory to open up more than a couple camera photos */
		/* So pre-scale the target bitmap into which the file is decoded */

		/* Get the size of the ImageView */
        int targetW = ivMedia.getWidth();
        int targetH = ivMedia.getWidth();

		/* Get the size of the image */
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        if (type == 0) {// take picture for header photo
            BitmapFactory.decodeFile(photoPath, bmOptions);
        } else {
            BitmapFactory.decodeFile(avatarPath, bmOptions);
        }


        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

		/* Figure out which way needs to be reduced less */
        int scaleFactor = 1;
        if ((targetW > 0) && (targetH > 0)) {
            scaleFactor = Math.min(photoW/targetW, photoH/targetH);
        }

		/* Set bitmap options to scale the image decode target */
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = null;
        if (type == 0) {// take picture for header photo
            bitmap = BitmapUtility.adjustBitmap(photoPath);
            ivMedia.setImageBitmap(bitmap);
        } else {
            bitmap = BitmapUtility.adjustBitmapForAvatar(avatarPath);
            avatar.setImageBitmap(bitmap);
        }




        imageWidth = bitmap.getWidth();
        imageHeight = bitmap.getHeight();

        if (type == 0) {// take picture for header photo
            photoPath = BitmapUtility.saveBitmap(bitmap, Constant.MEDIA_PATH + "heyoe", FileUtility.getFilenameFromPath(photoPath));
        } else {
            avatarPath = BitmapUtility.saveBitmap(bitmap, Constant.MEDIA_PATH + "heyoe", FileUtility.getFilenameFromPath(avatarPath));

        }

    }
    int imageWidth = 0 , imageHeight = 0;
}
