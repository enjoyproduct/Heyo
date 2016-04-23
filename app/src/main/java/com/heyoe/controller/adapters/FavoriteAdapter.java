package com.heyoe.controller.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.heyoe.R;
import com.heyoe.model.PostModel;

import java.util.ArrayList;

/**
 * Created by dell17 on 4/18/2016.
 */
public class FavoriteAdapter extends BaseAdapter {
    LayoutInflater layoutInflater;
    ArrayList<PostModel> arrayList;
    Activity activity ;
    public FavoriteAdapter (Activity activity, ArrayList<PostModel> arrayList) {
        this.activity = activity;
        this.arrayList = arrayList;
        layoutInflater = activity.getLayoutInflater();
    }
    @Override
    public int getCount() {
        return arrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return arrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = layoutInflater.inflate(R.layout.item_post_for_friend, null);
        }
        return view;
    }
}
