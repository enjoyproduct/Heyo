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

import com.android.volley.Request;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.heyoe.R;
import com.heyoe.model.UserModel;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class RequestFragment extends Fragment {
    private ListView listView;
    private PullToRefreshListView mPullRefreshHomeListView;
    private FriendAdapter friendAdapter;
    private RequestAdapter requestAdapter;
    private Button btnFriend, btnRequest;
    private Activity mActivity;

    private ArrayList<UserModel> arrUsers;

    public RequestFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_request, container, false);
        initVariables();
        initUI(view);
        return view;
    }
    private void initVariables() {
        mActivity = getActivity();
        arrUsers = new ArrayList<>();
    }
    private void initUI(View view) {
        btnFriend = (Button)view.findViewById(R.id.btn_request_friend);
        btnRequest = (Button)view.findViewById(R.id.btn_request_request);

        btnFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnFriend.setBackgroundColor(mActivity.getResources().getColor(R.color.grey));
                btnFriend.setTextColor(mActivity.getResources().getColor(R.color.white));
                btnRequest.setBackgroundColor(mActivity.getResources().getColor(R.color.white));
                btnRequest.setTextColor(mActivity.getResources().getColor(R.color.green));

                friendAdapter = new FriendAdapter(makeSampleActivity());

                listView.setAdapter(friendAdapter);

            }
        });
        btnRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnRequest.setBackgroundColor(mActivity.getResources().getColor(R.color.grey));
                btnRequest.setTextColor(mActivity.getResources().getColor(R.color.white));
                btnFriend.setBackgroundColor(mActivity.getResources().getColor(R.color.white));
                btnFriend.setTextColor(mActivity.getResources().getColor(R.color.green));

                requestAdapter = new RequestAdapter(makeSampleActivity());

                listView.setAdapter(requestAdapter);
            }
        });
        ///create sent listview
        mPullRefreshHomeListView = (PullToRefreshListView) view.findViewById(R.id.plv_request_request);
        mPullRefreshHomeListView.setOnLastItemVisibleListener(new PullToRefreshBase.OnLastItemVisibleListener() {

            @Override
            public void onLastItemVisible() {

                mPullRefreshHomeListView.onRefreshComplete();

            }
        });
        listView = mPullRefreshHomeListView.getRefreshableView();
//        for test
        requestAdapter = new RequestAdapter(makeSampleActivity());
        listView.setAdapter(requestAdapter);

        //=============================================================//


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
                view = mlayoutInflater.inflate(R.layout.item_request_received, null);
            }
            UserModel UserModel = arrFriends.get(position);

            return view;
        }
    }

    public class RequestAdapter extends BaseAdapter {

        LayoutInflater mlayoutInflater;
        ArrayList<UserModel> arrFriends;
        public RequestAdapter (ArrayList<UserModel> arrFriends) {
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
                view = mlayoutInflater.inflate(R.layout.item_request_sent, null);
            }
            UserModel UserModel = arrFriends.get(position);

            return view;
        }
    }

    //    for test
    private ArrayList<UserModel> makeSampleActivity() {
        ArrayList<UserModel> arrayList = new ArrayList<>();
        for (int i = 0; i < 10; i ++) {
            UserModel activityModel = new UserModel();
            arrayList.add(activityModel);
        }
        return arrayList;
    }



}
