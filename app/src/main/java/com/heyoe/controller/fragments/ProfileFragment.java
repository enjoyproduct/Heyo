package com.heyoe.controller.fragments;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.heyoe.R;
import com.heyoe.controller.ProfileActivity;
import com.heyoe.controller.adapters.PostAdapter;
import com.heyoe.model.PostModel;
import com.heyoe.model.UserModel;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment implements View.OnClickListener{
    private Activity mActivity;
    private ArrayList<PostModel> mArrPost;
    private ArrayList<PostModel> mArrBufferPost;
    private ListView lvMain;
    private PullToRefreshListView mPullRefreshHomeListView;
    private PostAdapter mPostAdapter;

    private RelativeLayout rlInfo, rlMedia, rlFriend;

    int offset;
    boolean isLast;

    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
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

        rlInfo = (RelativeLayout)view.findViewById(R.id.rl_profile_info);
        rlMedia = (RelativeLayout)view.findViewById(R.id.rl_profile_media);
        rlFriend = (RelativeLayout)view.findViewById(R.id.rl_profile_friend);
        rlInfo.setOnClickListener(this);
        rlMedia.setOnClickListener(this);
        rlFriend.setOnClickListener(this);

        ///create listview
        mPullRefreshHomeListView = (PullToRefreshListView)view.findViewById(R.id.lv_profile_myposts);
        mPullRefreshHomeListView.setOnLastItemVisibleListener(new PullToRefreshBase.OnLastItemVisibleListener() {

            @Override
            public void onLastItemVisible() {
                if (!isLast) {
//                    defaluteFetchTrip(currentCategory, GlobalVariable.getInstance().currentCountryName);
                }
                mPullRefreshHomeListView.onRefreshComplete();

            }
        });
        lvMain = mPullRefreshHomeListView.getRefreshableView();


        mPostAdapter = new PostAdapter(getActivity(), makeSamplePosts());
        lvMain.setAdapter(mPostAdapter);

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

    }


    //for test
    private ArrayList<PostModel> makeSamplePosts() {
        ArrayList<PostModel> arrayList = new ArrayList<>();
        for (int i = 0; i < 10; i ++) {
            PostModel postModel = new PostModel();
            arrayList.add(postModel);

        }
        return arrayList;

    }

    @Override
    public void onClick(View v) {
        if (v == rlInfo) {
            ProfileActivity.navigateTo(1);
        }
        if (v == rlMedia) {
            ProfileActivity.navigateTo(2);
        }
        if (v == rlFriend) {
            ProfileActivity.navigateTo(3);
        }

    }
}
