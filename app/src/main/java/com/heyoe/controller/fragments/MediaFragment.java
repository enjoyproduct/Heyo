package com.heyoe.controller.fragments;


import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.heyoe.R;
import com.heyoe.controller.adapters.FavoriteAdapter;
import com.heyoe.controller.adapters.MediaAdapter;
import com.heyoe.model.PostModel;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class MediaFragment extends Fragment {

    private Activity mActivity;
    private ArrayList<PostModel> mArrPost;
    private ArrayList<PostModel> mArrBufferPost;
    private GridView gridView;
    private MediaAdapter mMediaAdpater;
    private Button btnVideo, btnPhoto;

    int offset;
    boolean isLast;

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
        return view;
    }
    private void initVariable() {
        mActivity = getActivity();
        isLast = false;
        offset = 0;
        mArrPost = new ArrayList<>();
        mArrBufferPost = new ArrayList<>();
    }
    private void initUI(View view) {
        gridView = (GridView)view.findViewById(R.id.gv_media);
        btnPhoto = (Button)view.findViewById(R.id.btn_media_photos);
        btnVideo = (Button)view.findViewById(R.id.btn_media_videos);

        btnPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnPhoto.setBackgroundColor(mActivity.getResources().getColor(R.color.grey));
                btnPhoto.setTextColor(mActivity.getResources().getColor(R.color.white));
                btnVideo.setBackgroundColor(mActivity.getResources().getColor(R.color.white));
                btnVideo.setTextColor(mActivity.getResources().getColor(R.color.green));

                mMediaAdpater = new MediaAdapter(mActivity, makeSamplePostsPhoto());
                gridView.setAdapter(mMediaAdpater);
            }
        });
        btnVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnVideo.setBackgroundColor(mActivity.getResources().getColor(R.color.grey));
                btnVideo.setTextColor(mActivity.getResources().getColor(R.color.white));
                btnPhoto.setBackgroundColor(mActivity.getResources().getColor(R.color.white));
                btnPhoto.setTextColor(mActivity.getResources().getColor(R.color.green));

                mMediaAdpater = new MediaAdapter(mActivity, makeSamplePostsVideo());
                gridView.setAdapter(mMediaAdpater);
            }
        });
        mMediaAdpater = new MediaAdapter(mActivity, makeSamplePostsPhoto());
        gridView.setAdapter(mMediaAdpater);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

    }


    //for test
    private ArrayList<PostModel> makeSamplePostsVideo() {
        ArrayList<PostModel> arrayList = new ArrayList<>();
        for (int i = 0; i < 10; i ++) {
            PostModel postModel = new PostModel();
            postModel.setFavorite("yes");
            arrayList.add(postModel);

        }
        return arrayList;

    }
    //for test
    private ArrayList<PostModel> makeSamplePostsPhoto() {
        ArrayList<PostModel> arrayList = new ArrayList<>();
        for (int i = 0; i < 10; i ++) {
            PostModel postModel = new PostModel();
            postModel.setFavorite("yes");
            arrayList.add(postModel);

        }
        return arrayList;

    }
}
