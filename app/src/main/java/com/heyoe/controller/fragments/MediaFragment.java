package com.heyoe.controller.fragments;


import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.CustomRequest;
import com.android.volley.toolbox.Volley;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshGridView;
import com.heyoe.R;
import com.heyoe.controller.ProfileActivity;
import com.heyoe.controller.adapters.MediaAdapter;
import com.heyoe.model.API;
import com.heyoe.model.Constant;
import com.heyoe.model.PostModel;
import com.heyoe.utilities.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class MediaFragment extends Fragment {

    private Activity mActivity;
    private ArrayList<PostModel> mArrPost;
//    private StaggeredGridView gridView;
    private PullToRefreshGridView pullToRefreshGridView;
    private GridView gridView;

    private MediaAdapter mMediaAdpater;
    private Button btnVideo, btnPhoto;

    int offset;
    boolean isLast;
    String media_type;

    public MediaFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_media, container, false);
        initVariable();
        initUI(view);
        getMyPosts();
        return view;
    }
    private void initVariable() {
        mActivity = getActivity();
        isLast = false;
        offset = 0;
        media_type = "post_photo";
        mArrPost = new ArrayList<>();
    }
    private void initUI(View view) {
        pullToRefreshGridView = (PullToRefreshGridView)view.findViewById(R.id.grid_view);
        pullToRefreshGridView.setOnLastItemVisibleListener(new PullToRefreshBase.OnLastItemVisibleListener() {

            @Override
            public void onLastItemVisible() {
                if (!isLast) {
                    getMyPosts();
                }
                pullToRefreshGridView.onRefreshComplete();
            }
        });
        gridView = pullToRefreshGridView.getRefreshableView();
        mMediaAdpater = new MediaAdapter(mActivity, mArrPost);
        gridView.setAdapter(mMediaAdpater);

        btnPhoto = (Button)view.findViewById(R.id.btn_media_photos);
        btnVideo = (Button)view.findViewById(R.id.btn_media_videos);

        btnPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnVideo.setBackgroundColor(mActivity.getResources().getColor(R.color.grey));
                btnVideo.setTextColor(mActivity.getResources().getColor(R.color.white));
                btnPhoto.setBackgroundColor(mActivity.getResources().getColor(R.color.white));
                btnPhoto.setTextColor(mActivity.getResources().getColor(R.color.green));

                media_type = "post_photo";
                isLast = false;
                offset = 0;
                mArrPost.clear();

                getMyPosts();

            }
        });
        btnVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnPhoto.setBackgroundColor(mActivity.getResources().getColor(R.color.grey));
                btnPhoto.setTextColor(mActivity.getResources().getColor(R.color.white));
                btnVideo.setBackgroundColor(mActivity.getResources().getColor(R.color.white));
                btnVideo.setTextColor(mActivity.getResources().getColor(R.color.green));

                media_type = "post_video";
                isLast = false;
                offset = 0;
                mArrPost.clear();

                getMyPosts();

            }
        });

    }

    private void getMyPosts() {

        Utils.showProgress(mActivity);

        Map<String, String> params = new HashMap<String, String>();
        params.put(Constant.DEVICE_TYPE, Constant.ANDROID);
        params.put(Constant.DEVICE_TOKEN, Utils.getFromPreference(mActivity, Constant.DEVICE_TOKEN));
        params.put("user_id", ProfileActivity.userModel.getUser_id());
        params.put("media_type", media_type);
        params.put("offset", String.valueOf(offset));

        CustomRequest signinRequest = new CustomRequest(Request.Method.POST, API.GET_MY_POSTS, params,
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

                                    postModel.setPost_id(postObject.getString("id"));
                                    postModel.setPosted_date(postObject.getString("date"));
                                    postModel.setPoster_id(postObject.getString("poster_id"));
                                    postModel.setMedia_type(postObject.getString("media_type"));
                                    postModel.setMedia_url(postObject.getString("media_url"));
                                    postModel.setShared_count(postObject.getString("shared_count"));
                                    postModel.setViewed_count(postObject.getString("viewed_count"));
                                    postModel.setDescription(postObject.getString("description"));
                                    postModel.setImageWidth(Integer.parseInt(postObject.getString("width")));
                                    postModel.setImageHeight(Integer.parseInt(postObject.getString("height")));



                                    mArrPost.add(postModel);

                                }

                                mMediaAdpater.notifyDataSetChanged();

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

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

    }

}
