package com.heyoe.controller.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.heyoe.R;
import com.heyoe.model.ActivityModel;
import com.heyoe.model.UserModel;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class FriendFragment extends Fragment {

    private ListView lvHome;
    private PullToRefreshListView mPullRefreshHomeListView;
    private FriendAdapter FriendAdapter;
    private Context mContext;
    static boolean isLast;
    static int offset;
    private ArrayList<UserModel> arrUsers;



    public FriendFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_friend, container, false);
        initVariables();
        initUI(view);
        return view;
    }

    private void initVariables() {
        mContext = getActivity();
        isLast = false;
        offset = 0;
        arrUsers = new ArrayList<>();
    }
    private void initUI(View view) {
///create listview
        mPullRefreshHomeListView = (PullToRefreshListView) view.findViewById(R.id.lv_friend_list);
//        mPullRefreshHomeListView.setVisibility(View.GONE);
        mPullRefreshHomeListView.setOnLastItemVisibleListener(new PullToRefreshBase.OnLastItemVisibleListener() {

            @Override
            public void onLastItemVisible() {
                if (!isLast) {
//                    defaluteFetchTrip(currentCategory, GlobalVariable.getInstance().currentCountryName);
//                    reDrawListView();
                }
                mPullRefreshHomeListView.onRefreshComplete();

            }
        });
        lvHome = mPullRefreshHomeListView.getRefreshableView();
//        for test
        FriendAdapter = new FriendAdapter(makeSampleActivity());

        lvHome.setAdapter(FriendAdapter);
    }
    public class FriendAdapter extends BaseAdapter {

        LayoutInflater mlayoutInflater;
        ArrayList<UserModel> arrFriends;
        public FriendAdapter (ArrayList<UserModel> arrFriends) {
            mlayoutInflater = LayoutInflater.from(mContext);
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
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = mlayoutInflater.inflate(R.layout.item_friend, null);
            }
            UserModel UserModel = arrFriends.get(position);

            return view;
        }
    }

    //    for test
    private ArrayList<UserModel> makeSampleActivity() {
        ArrayList<UserModel> arrayList = new ArrayList<>();
        for (int i = 0; i < 20; i ++) {
            UserModel activityModel = new UserModel();
            arrayList.add(activityModel);
        }
        return arrayList;
    }

}
