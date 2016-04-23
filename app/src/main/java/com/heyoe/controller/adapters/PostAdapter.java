package com.heyoe.controller.adapters;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.android.volley.toolbox.Volley;
import com.heyoe.R;
import com.heyoe.controller.ProfileActivity;
import com.heyoe.model.PostModel;
import com.heyoe.widget.MyCircularImageView;

import java.util.ArrayList;

/**
 * Created by dell17 on 4/18/2016.
 */
public class PostAdapter extends BaseAdapter {
    LayoutInflater layoutInflater;
    ArrayList<PostModel> arrayList;
    Activity activity ;
    public PostAdapter (Activity activity, ArrayList<PostModel> arrayList) {
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
        MyCircularImageView myCircularImageView = (MyCircularImageView)view.findViewById(R.id.civ_ipff_avatar);
        myCircularImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.startActivity(new Intent(activity, ProfileActivity.class));
            }
        });
        return view;
    }
}
