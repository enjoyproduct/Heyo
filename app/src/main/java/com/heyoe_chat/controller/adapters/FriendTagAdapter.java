package com.heyoe_chat.controller.adapters;

import android.app.Activity;
import android.graphics.Bitmap;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.heyoe_chat.R;
import com.heyoe_chat.model.API;
import com.heyoe_chat.model.UserModel;
import com.heyoe_chat.utilities.image_downloader.UrlImageViewCallback;
import com.heyoe_chat.utilities.image_downloader.UrlImageViewHelper;
import com.heyoe_chat.widget.MyCircularImageView;

import java.util.ArrayList;

/**
 * Created by dell17 on 4/18/2016.
 */
public class FriendTagAdapter extends BaseAdapter {
    LayoutInflater layoutInflater;
    ArrayList<UserModel> arrayList;
    Activity activity ;
    public FriendTagAdapter(Activity activity, ArrayList<UserModel> arrayList) {
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
        View view = layoutInflater.inflate(R.layout.item_search, null);

        final UserModel userModel = arrayList.get(position);
        if (userModel != null) {
            TextView name = (TextView) view.findViewById(R.id.item_search_fullname);
            name.setText(Html.fromHtml("<b>" +  userModel.getFullname() + "</b>"));

            MyCircularImageView circularImageView = (MyCircularImageView) view.findViewById(R.id.item_search_civ_photo);
            if (!userModel.getAvatar().equals("")) {
                UrlImageViewHelper.setUrlDrawable(circularImageView, API.BASE_AVATAR + userModel.getAvatar(), R.drawable.default_user, new UrlImageViewCallback() {
                    @Override
                    public void onLoaded(ImageView imageView, Bitmap loadedBitmap, String url, boolean loadedFromCache) {
                        if (!loadedFromCache) {
                            ScaleAnimation scale = new ScaleAnimation(0, 1, 0, 1, ScaleAnimation.RELATIVE_TO_SELF, .5f, ScaleAnimation.RELATIVE_TO_SELF, .5f);
                            scale.setDuration(100);
                            scale.setInterpolator(new OvershootInterpolator());
                            imageView.startAnimation(scale);
                        }
                    }
                });
            } else {
                circularImageView.setImageDrawable(activity.getResources().getDrawable(R.drawable.default_user));
            }

            ImageView ibAdd = (ImageView) view.findViewById(R.id.ib_search_add);
            ibAdd.setVisibility(View.GONE);
        }
        return view;
    }

}
