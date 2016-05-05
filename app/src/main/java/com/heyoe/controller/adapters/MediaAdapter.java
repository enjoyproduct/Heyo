package com.heyoe.controller.adapters;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.heyoe.R;
import com.heyoe.controller.MediaPlayActivity;
import com.heyoe.model.API;
import com.heyoe.model.PostModel;
import com.heyoe.utilities.FileUtility;
import com.heyoe.utilities.UIUtility;
import com.heyoe.utilities.image_downloader.UrlImageViewCallback;
import com.heyoe.utilities.image_downloader.UrlRectangleImageViewHelper;

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
        final PostModel postModel = arrayList.get(position);

        ImageView imageView = (ImageView)view.findViewById(R.id.iv_item_media);

        //set imageview size
        UIUtility.setImageViewSize(imageView, UIUtility.getScreenWidth(activity)/3, UIUtility.getScreenWidth(activity)/3);
//        if (postModel.getImageWidth() != 0 && postModel.getImageHeight() != 0) {
//            double ratio = ((double)postModel.getImageHeight() / (double)postModel.getImageWidth());
//            double height = ratio * UIUtility.getScreenWidth(activity) / 2;
//            UIUtility.setImageViewSize(imageView, UIUtility.getScreenWidth(activity), (int)height);
//        } else {
//            int height = (int)( UIUtility.getScreenWidth(activity) * 0.75  / 2);
//            UIUtility.setImageViewSize(imageView, UIUtility.getScreenWidth(activity), height);
//        }

        String thumbnailUrl = "";
        String imagUrl = "";
        String videoUrl = "";
        if (postModel.getMedia_type().equals("post_photo")) {
            thumbnailUrl = API.BASE_THUMBNAIL + postModel.getMedia_url().substring(0, postModel.getMedia_url().length() - 3) + "jpg";
            imagUrl = API.BASE_POST_PHOTO + postModel.getMedia_url();
        } else if (postModel.getMedia_type().equals("post_video")){
            thumbnailUrl = API.BASE_THUMBNAIL + postModel.getMedia_url().substring(0, postModel.getMedia_url().length() - 3) + "jpg";
            videoUrl = API.BASE_POST_VIDEO + postModel.getMedia_url();
        } else if (postModel.getMedia_type().equals("youtube")) {
            thumbnailUrl = API.BASE_YOUTUB_PREFIX + FileUtility.getFilenameFromPath(postModel.getMedia_url()) + API.BASE_YOUTUB_SURFIX;
            videoUrl = postModel.getMedia_url();
        }

        if (!postModel.getMedia_url().equals("")) {
            UrlRectangleImageViewHelper.setUrlDrawable(imageView, thumbnailUrl, R.drawable.default_tour, new UrlImageViewCallback() {
                @Override
                public void onLoaded(ImageView imageView, Bitmap loadedBitmap, String url, boolean loadedFromCache) {
                    if (!loadedFromCache) {

                        ScaleAnimation scale = new ScaleAnimation(0, 1, 0, 1, ScaleAnimation.RELATIVE_TO_SELF, .5f, ScaleAnimation.RELATIVE_TO_SELF, .5f);
                        scale.setDuration(500);
                        scale.setInterpolator(new OvershootInterpolator());
                        imageView.startAnimation(scale);


                    }
                }
            });
        }

        ImageButton imageButton = (ImageButton)view.findViewById(R.id.ib_item_media_play);
        final String finalVideoUrl = videoUrl;
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, MediaPlayActivity.class);
                intent.putExtra("url", finalVideoUrl);
                if (postModel.getMedia_type().equals("post_video")) {
                    intent.putExtra("type", "video");
                } else {
                    intent.putExtra("type", "youtube");
                }
                activity.startActivity(intent);
            }
        });
        final String finalImagUrl = imagUrl;
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, MediaPlayActivity.class);
                if (postModel.getMedia_type().equals("post_photo")) {
                    intent.putExtra("type", "photo");
                    intent.putExtra("url", finalImagUrl);
                } else {
                    if (postModel.getMedia_type().equals("post_video")) {
                        intent.putExtra("type", "video");
                    } else {
                        intent.putExtra("type", "youtube");
                    }

                    intent.putExtra("url", finalVideoUrl);
                }
                activity.startActivity(intent);
            }
        });
        if (!postModel.getMedia_type().equals("post_photo")) {
            imageButton.setVisibility(View.VISIBLE);
        }
        else {
            imageButton.setVisibility(View.GONE);
        }

        return view;
    }
}
