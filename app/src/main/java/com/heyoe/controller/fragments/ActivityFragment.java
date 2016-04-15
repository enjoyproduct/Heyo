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

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class ActivityFragment extends Fragment {

    private ListView lvHome;
    private PullToRefreshListView mPullRefreshHomeListView;
    private ActivityAdapter activityAdapter;
    private Context mContext;
    static boolean isLast;
    static int offset;
    private ArrayList<ActivityModel> arrActivities;


    public ActivityFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_activity, container, false);
        return view;
    }

    private void initVariables() {
        mContext = getActivity();
        isLast = false;
        offset = 0;
        arrActivities = new ArrayList<>();
    }
    private void initUI(View view) {
///create listview
        mPullRefreshHomeListView = (PullToRefreshListView) view.findViewById(R.id.lv_activity);
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
        activityAdapter = new ActivityAdapter(arrActivities);
        lvHome.setAdapter(activityAdapter);
    }
    public class ActivityAdapter extends BaseAdapter {

        LayoutInflater mlayoutInflater;
        ArrayList<ActivityModel> arrActivity;
        public ActivityAdapter (ArrayList<ActivityModel> arrActivity) {
            mlayoutInflater = LayoutInflater.from(mContext);
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
            View view = convertView;
            if (view == null) {
                view = mlayoutInflater.inflate(R.layout.item_activity, null);
            }
            ActivityModel activityModel = arrActivity.get(position);

            return view;
        }
    }
}
