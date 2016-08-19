package com.heyoe_chat.controller.fragments;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.BaseAdapter;
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
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.heyoe_chat.R;
import com.heyoe_chat.controller.DetailPostActivity;
import com.heyoe_chat.controller.HomeActivity;
import com.heyoe_chat.model.API;
import com.heyoe_chat.model.ActivityModel;
import com.heyoe_chat.model.CommentModel;
import com.heyoe_chat.model.Constant;
import com.heyoe_chat.model.PostModel;
import com.heyoe_chat.utilities.TimeUtility;
import com.heyoe_chat.utilities.Utils;
import com.heyoe_chat.utilities.image_downloader.UrlImageViewCallback;
import com.heyoe_chat.utilities.image_downloader.UrlRectangleImageViewHelper;
import com.heyoe_chat.widget.MyCircularImageView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class ActivityFragment extends Fragment {

    private ListView lvHome;
    private PullToRefreshListView mPullRefreshHomeListView;
    private ActivityAdapter activityAdapter;
    private Activity mActivity;
    static boolean isLast;
    static int offset;
    private ArrayList<ActivityModel> arrActivities;


    public ActivityFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the text_layout for this fragment
        View view = inflater.inflate(R.layout.fragment_activity, container, false);
        initVariables();
        initUI(view);
        getMyActivity();
        return view;
    }

    private void initVariables() {
        mActivity = getActivity();
        isLast = false;
        offset = 0;
        arrActivities = new ArrayList<>();
    }
    private void initUI(View view) {
        mPullRefreshHomeListView = (PullToRefreshListView) view.findViewById(R.id.lv_activity);
        mPullRefreshHomeListView.setOnLastItemVisibleListener(new PullToRefreshBase.OnLastItemVisibleListener() {

            @Override
            public void onLastItemVisible() {
                if (!isLast) {
                    getMyActivity();
                }
                mPullRefreshHomeListView.onRefreshComplete();

            }
        });
        mPullRefreshHomeListView.setOnPullEventListener(new PullToRefreshBase.OnPullEventListener<ListView>() {
            @Override
            public void onPullEvent(PullToRefreshBase<ListView> refreshView, PullToRefreshBase.State state, PullToRefreshBase.Mode direction) {
                if (state == PullToRefreshBase.State.RELEASE_TO_REFRESH && direction == PullToRefreshBase.Mode.PULL_FROM_START) {
                    isLast = false;
                    offset = 0;
                    arrActivities.clear();
                    getMyActivity();
                }
            }
        });
        lvHome = mPullRefreshHomeListView.getRefreshableView();
        activityAdapter = new ActivityAdapter(arrActivities);
        lvHome.setAdapter(activityAdapter);

        clearBadge();
    }
    private void clearBadge() {
        Utils.saveIntToPreference(mActivity, Constant.ACTIVITY_COUNT, 0);
        HomeActivity.showActivityBadge();
    }
    private void getMyActivity() {

        Utils.showProgress(mActivity);
        Map<String, String> params = new HashMap<String, String>();
        params.put(Constant.DEVICE_TYPE, Constant.ANDROID);
        params.put(Constant.DEVICE_TOKEN, Utils.getFromPreference(mActivity, Constant.DEVICE_TOKEN));
        params.put("my_id", Utils.getFromPreference(mActivity, Constant.USER_ID));
        params.put("offset", String.valueOf(offset));

        CustomRequest signinRequest = new CustomRequest(Request.Method.POST, API.GET_ACTIVITY, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Utils.hideProgress();
                        try {
                            String status = response.getString("status");
                            if (status.equals("200")) {
                                JSONArray jsonArray = response.getJSONArray("data");
                                int userCount = jsonArray.length();
                                if (userCount == 0) {
                                    isLast = true;
                                }
                                offset ++;
                                for (int i = 0; i < userCount; i++) {

                                    JSONObject userObject = jsonArray.getJSONObject(i);

                                    ActivityModel activityModel = new ActivityModel();

                                    activityModel.setType(userObject.getString("type"));
                                    activityModel.setUser_id(userObject.getString("user_id"));
                                    activityModel.setFullname(userObject.getString("fullname"));
                                    activityModel.setUser_avatar(userObject.getString("avatar"));
                                    activityModel.setDate(userObject.getString("date"));
                                    activityModel.setRead_status(userObject.getString("status"));
                                    activityModel.setTarget_id(userObject.getString("target_id"));

                                    if (userObject.getString("type").equals("like") ||
                                            userObject.getString("type").equals("dislike") ||
                                            userObject.getString("type").equals("comment") ||
                                            userObject.getString("type").equals("share") ||
                                            userObject.getString("type").equals("taged")) {

                                        JSONObject postObject = userObject.getJSONObject("post");

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
                                        postModel.setHashtag(postObject.getString("hashtag"));
                                        postModel.setCommented(postObject.getString("commented"));
                                        postModel.setFavorite(postObject.getString("favorite"));
                                        postModel.setImageWidth(Integer.parseInt(postObject.getString("width")));
                                        postModel.setImageHeight(Integer.parseInt(postObject.getString("height")));
                                        postModel.setClickedTime((long) 0);

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

                                        activityModel.setPostModel(postModel);
                                    }
                                    arrActivities.add(activityModel);

                                }
                                clearBadge();
                                activityAdapter.notifyDataSetChanged();
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

    public class ActivityAdapter extends BaseAdapter {

        LayoutInflater mlayoutInflater;
        ArrayList<ActivityModel> arrActivity;
        public ActivityAdapter (ArrayList<ActivityModel> arrActivity) {
            mlayoutInflater = LayoutInflater.from(mActivity);
            this.arrActivity = arrActivity;
        }
        @Override
        public int getCount() {
            return arrActivity.size();
        }

        @Override
        public Object getItem(int position) {
            return arrActivity.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = mlayoutInflater.inflate(R.layout.item_activity, null);

            final ActivityModel activityModel = arrActivity.get(position);

            RelativeLayout relativeLayout = (RelativeLayout)view.findViewById(R.id.rl_activity);
            MyCircularImageView myCircularImageView = (MyCircularImageView)view.findViewById(R.id.civ_ia_friend_avatar3);
            TextView tvFullname = (TextView)view.findViewById(R.id.tv_ia_fullname);
            TextView tvDate = (TextView)view.findViewById(R.id.tv_ia_time);
            ImageView ivButton = (ImageView)view.findViewById(R.id.iv_ia_type_button);
            ImageView ivType = (ImageView)view.findViewById(R.id.iv_ia_type);

            if (activityModel.getRead_status().equals("unread")) {
                relativeLayout.setBackgroundColor(getResources().getColor(R.color.grey_light));
            } else {
                relativeLayout.setBackgroundColor(getResources().getColor(R.color.white));
            }
            if (!activityModel.getUser_avatar().equals("")) {
                UrlRectangleImageViewHelper.setUrlDrawable(myCircularImageView, activityModel.getUser_avatar(), R.drawable.default_user, new UrlImageViewCallback() {
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

            tvDate.setText(TimeUtility.countTime(mActivity, Long.parseLong(activityModel.getDate())));

            String str = "";
            if (activityModel.getType().equals("like")) {
                ivType.setImageDrawable(getResources().getDrawable(R.drawable.activity_like_white));
                str = "<b>" + activityModel.getFullname() + "</b>" + " " + getResources().getString(R.string.liked_your_post);
            } else if (activityModel.getType().equals("dislike")) {
                ivType.setImageDrawable(getResources().getDrawable(R.drawable.activity_dislike));
                str = "<b>" + activityModel.getFullname() + "</b>" + " " + getResources().getString(R.string.disliked_your_post);
            } else if (activityModel.getType().equals("comment")) {
                ivType.setImageDrawable(getResources().getDrawable(R.drawable.activity_comment_white41));
                str = "<b>" + activityModel.getFullname() + "</b>" + " " + getResources().getString(R.string.commented_on_your_post);
            } else if (activityModel.getType().equals("share")) {
                ivType.setImageDrawable(getResources().getDrawable(R.drawable.activity_share57));
                str = "<b>" + activityModel.getFullname() + "</b>" + " " + getResources().getString(R.string.shared_your_post);
            } else if (activityModel.getType().equals("invite")) {
                ivType.setImageDrawable(getResources().getDrawable(R.drawable.ic_menu_plus));
                str = "<b>" + activityModel.getFullname() + "</b>" + " " + getResources().getString(R.string.sent_friend_request_to_you);
            } else if (activityModel.getType().equals("accept")) {
                ivType.setImageDrawable(getResources().getDrawable(R.drawable.ic_menu_plus));
                str = "<b>" + activityModel.getFullname() + "</b>" + " " + getResources().getString(R.string.is_now_your_friend);
            } else if (activityModel.getType().equals("reject")) {
                ivType.setImageDrawable(getResources().getDrawable(R.drawable.activity_cross_line_white49));
                str = "<b>" + activityModel.getFullname() + "</b>" + " "  + getResources().getString(R.string.rejected_your_friend_request);
            } else if (activityModel.getType().equals("taged")) {
                ivType.setImageDrawable(getResources().getDrawable(R.drawable.ic_small_comment_white));
                str = "<b>" + activityModel.getFullname() + "</b>" + " " + getResources().getString(R.string.tagged_you_in_post);
            }
            tvFullname.setText(Html.fromHtml(str));
            ivButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (activityModel.getType().endsWith("invite")) {
                        HomeActivity.menuNavigateTo(1);
                    }else if (activityModel.getType().equals("accept") ||
                            activityModel.getType().equals("reject")) {
                        HomeActivity.navigateToProfile(activityModel.getUser_id());
                    } else {
                        if (activityModel.getPostModel() != null) {
                            Intent intent = new Intent(mActivity, DetailPostActivity.class);
                            intent.putExtra("post", activityModel.getPostModel());
                            mActivity.startActivity(intent);
                        }
                    }
                }
            });
            return view;
        }
    }


}
