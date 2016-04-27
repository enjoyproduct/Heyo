package com.heyoe.controller.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.heyoe.R;
import com.heyoe.model.PostModel;

import java.util.ArrayList;

/**
 * Created by dell17 on 4/18/2016.
 */
public class MediaAdapter extends BaseAdapter {
    LayoutInflater layoutInflater;
    ArrayList<PostModel> arrayList;
    Activity activity ;
    public MediaAdapter (Activity activity, ArrayList<PostModel> arrayList) {
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
            view = layoutInflater.inflate(R.layout.item_media, null);
        }
        ImageView imageView = (ImageView)view.findViewById(R.id.iv_item_media);
        ImageButton imageButton = (ImageButton)view.findViewById(R.id.ib_item_media_play);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        PostModel postModel = arrayList.get(position);
        if (postModel.getFavorite().equals("yes")) {
            imageButton.setVisibility(View.VISIBLE);
        }
        else {
            imageButton.setVisibility(View.GONE);
        }

        return view;
    }
}
