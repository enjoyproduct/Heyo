package com.heyoe_chat.controller.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.heyoe_chat.R;
import com.heyoe_chat.model.API;
import com.heyoe_chat.model.CommentModel;
import com.heyoe_chat.utilities.TimeUtility;
import com.heyoe_chat.utilities.image_downloader.UrlImageViewCallback;
import com.heyoe_chat.utilities.image_downloader.UrlRectangleImageViewHelper;
import com.heyoe_chat.widget.MyCircularImageView;

import java.util.List;

/**
 * Created by Sagar on 6/14/2015.
 */
public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

    public List<CommentModel> commentModel;
    private Context mContext;
    public RecyclerAdapter(Context context,  List<CommentModel> commentModel){
        this.commentModel = commentModel;
        this.mContext = context;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView fullname;
        TextView comment, date;
        MyCircularImageView myCircularImageView;
        public ViewHolder(View itemView) {
            super(itemView);
            this.fullname = (TextView)itemView.findViewById(R.id.tv_ic_fullname);
            this.comment = (TextView)itemView.findViewById(R.id.tv_ic_comment);
            this.date = (TextView)itemView.findViewById(R.id.tv_ic_date);
            this.myCircularImageView = (MyCircularImageView)itemView.findViewById(R.id.civ_ic_avatar);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.fullname.setText(commentModel.get(position).getFullname());
        holder.comment.setText(commentModel.get(position).getComment());
        holder.date.setText(TimeUtility.countTime(mContext, Long.parseLong(commentModel.get(position).getTime())));
        if (!commentModel.get(position).getAvatar().equals("")) {
            UrlRectangleImageViewHelper.setUrlDrawable(holder.myCircularImageView, commentModel.get(position).getAvatar(), R.drawable.default_circular_user_photo, new UrlImageViewCallback() {
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
    }

    @Override
    public int getItemCount() {
        return commentModel.size();
    }
}
