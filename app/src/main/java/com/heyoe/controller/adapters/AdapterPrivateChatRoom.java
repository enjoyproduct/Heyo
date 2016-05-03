package com.heyoe.controller.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.heyoe.R;
import com.heyoe.controller.ChatActivity;
import com.heyoe.model.API;
import com.heyoe.model.Constant;
import com.heyoe.utilities.Utils;
import com.heyoe.utilities.image_downloader.UrlImageViewCallback;
import com.heyoe.utilities.image_downloader.UrlRectangleImageViewHelper;
import com.heyoe.widget.MyCircularImageView;
import com.quickblox.chat.model.QBChatMessage;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Jon on 4/22/2016.
 */
public class AdapterPrivateChatRoom extends BaseAdapter {

    private LayoutInflater layoutInflater;
    private Context context;
    private ArrayList<QBChatMessage> arrData;
    private Boolean mineFlag;
    private String opponentAvatar;

    public AdapterPrivateChatRoom(ChatActivity context, ArrayList<QBChatMessage> arr, String avatarURL)
    {
        layoutInflater = LayoutInflater.from(context);
        this.context = context;
        this.arrData = arr;
        this.opponentAvatar = avatarURL;
    }

    public void updateList(ArrayList<QBChatMessage> arr)
    {
        this.arrData = arr;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (arrData == null || arrData.size() == 0)
            return 0;
        return arrData.size();
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        QBChatMessage itemMessage = arrData.get(position);
        int senderID = itemMessage.getSenderId();
        if (senderID == Integer.parseInt(Utils.getFromPreference(context, Constant.QB_ID))){
            mineFlag = true;
            convertView = layoutInflater.inflate(R.layout.cell_my_side, null);
        }else{
            convertView = layoutInflater.inflate(R.layout.cell_opponent_side, null);
            mineFlag = false;
        }

        MyCircularImageView ciUser = (MyCircularImageView)convertView.findViewById(R.id.civUser);
        TextView tvMessage = (TextView)convertView.findViewById(R.id.tvMessage);
        TextView tvTime = (TextView)convertView.findViewById(R.id.tvTime);

        tvMessage.setText(itemMessage.getBody());
        tvTime.setText(DateFormat.format("HH:mm", new Date(itemMessage.getDateSent() * 1000)).toString());
        if (opponentAvatar.length() != 0) {
            String opponentAvatarURL = API.BASE_AVATAR + opponentAvatar;
            UrlRectangleImageViewHelper.setUrlDrawable(ciUser, opponentAvatarURL, R.drawable.default_user, new UrlImageViewCallback() {
                @Override
                public void onLoaded(ImageView imageView, Bitmap loadedBitmap, String url, boolean loadedFromCache) {
                    if (!loadedFromCache) {
                        ScaleAnimation scale = new ScaleAnimation(0, 1, 0, 1, ScaleAnimation.RELATIVE_TO_SELF, .5f, ScaleAnimation.RELATIVE_TO_SELF, .5f);
                        scale.setDuration(10);
                        scale.setInterpolator(new OvershootInterpolator());
                        imageView.startAnimation(scale);
                    }
                }
            });
        }


        return convertView;
    }
}
