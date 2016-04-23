package com.heyoe.controller.fragments;


import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.heyoe.R;
import com.heyoe.controller.adapters.PostAdapter;
import com.heyoe.model.PostModel;
import com.heyoe.utilities.Utils;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment {

    private Activity mActivity;
    private ArrayList<PostModel> mArrPost;
    private ArrayList<PostModel> mArrBufferPost;
    private ListView lvMain;
    private PullToRefreshListView mPullRefreshHomeListView;
    private PostAdapter mPostAdapter;

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
        ///create listview
        mPullRefreshHomeListView = (PullToRefreshListView)view.findViewById(R.id.lv_main_wall);
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
}
