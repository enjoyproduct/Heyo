package com.heyoe.controller.fragments;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.heyoe.R;
import com.heyoe.controller.adapters.MediaAdapter;
import com.heyoe.model.UserModel;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class MyFriendFragment extends Fragment {
    private ListView lvHome;
    private PullToRefreshListView mPullRefreshHomeListView;
    private FriendAdapter friendAdapter;
    private Activity mActivity;
    static boolean isLast;
    static int offset;
    private ArrayList<UserModel> arrUsers;
    private Button btnFriend, btnBlocked;

    public MyFriendFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_my_friend, container, false);
        initVariables();
        initUI(view);
        return view;
    }
    private void initVariables() {
        mActivity = getActivity();
        isLast = false;
        offset = 0;
        arrUsers = new ArrayList<>();
    }
    private void initUI(View view) {
        btnFriend = (Button)view.findViewById(R.id.btn_my_friend);
        btnBlocked = (Button)view.findViewById(R.id.btn_my_friend_blocked);

        btnFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnFriend.setBackgroundColor(mActivity.getResources().getColor(R.color.grey));
                btnFriend.setTextColor(mActivity.getResources().getColor(R.color.white));
                btnBlocked.setBackgroundColor(mActivity.getResources().getColor(R.color.white));
                btnBlocked.setTextColor(mActivity.getResources().getColor(R.color.green));

                friendAdapter = new FriendAdapter(makeSampleActivity());

                lvHome.setAdapter(friendAdapter);

            }
        });
        btnBlocked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnBlocked.setBackgroundColor(mActivity.getResources().getColor(R.color.grey));
                btnBlocked.setTextColor(mActivity.getResources().getColor(R.color.white));
                btnFriend.setBackgroundColor(mActivity.getResources().getColor(R.color.white));
                btnFriend.setTextColor(mActivity.getResources().getColor(R.color.green));

                friendAdapter = new FriendAdapter(arrUsers);

                lvHome.setAdapter(friendAdapter);
            }
        });

        ///create listview
        mPullRefreshHomeListView = (PullToRefreshListView) view.findViewById(R.id.lv_my_friend_list);
        mPullRefreshHomeListView.setOnLastItemVisibleListener(new PullToRefreshBase.OnLastItemVisibleListener() {

            @Override
            public void onLastItemVisible() {
                if (!isLast) {
                }
                mPullRefreshHomeListView.onRefreshComplete();

            }
        });
        lvHome = mPullRefreshHomeListView.getRefreshableView();
//        for test
        friendAdapter = new FriendAdapter(makeSampleActivity());

        lvHome.setAdapter(friendAdapter);
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
