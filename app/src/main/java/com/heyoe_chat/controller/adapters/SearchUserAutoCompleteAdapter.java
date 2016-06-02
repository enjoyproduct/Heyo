package com.heyoe_chat.controller.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;


import com.heyoe_chat.R;
import com.heyoe_chat.controller.HomeActivity;
import com.heyoe_chat.model.API;
import com.heyoe_chat.model.UserModel;
import com.heyoe_chat.utilities.image_downloader.UrlImageViewCallback;
import com.heyoe_chat.utilities.image_downloader.UrlImageViewHelper;
import com.heyoe_chat.widget.MyCircularImageView;

import java.util.ArrayList;

/**
 * Created by Administrator on 1/20/2016.
 */
public class SearchUserAutoCompleteAdapter extends ArrayAdapter<UserModel> {
    private ArrayList<UserModel> items;
    private ArrayList<UserModel> itemsAll;
    private ArrayList<UserModel> suggestions;
    private int viewResourceId;
    Context mContext;

    public SearchUserAutoCompleteAdapter(Context context, int viewResourceId, ArrayList<UserModel> items) {
        super(context, viewResourceId, items);
        this.mContext = context;
        this.items = items;
        this.itemsAll = (ArrayList<UserModel>) items.clone();
        this.suggestions = new ArrayList<UserModel>();
        this.viewResourceId = viewResourceId;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(viewResourceId, null);
        }
        final UserModel userModel = items.get(position);
        if (userModel != null) {
            TextView name = (TextView) v.findViewById(R.id.item_search_fullname);
            name.setText(userModel.getFullname());
            name.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    Intent intent = new Intent(mContext, ProfileActivity.class);
//                    intent.putExtra("user_id", userModel.getUser_id());
//                    mContext.startActivity(intent);
                    HomeActivity.navigateToProfile(userModel.getUser_id());
                }
            });
            MyCircularImageView circularImageView  = (MyCircularImageView)v.findViewById(R.id.item_search_civ_photo);
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
                circularImageView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.default_user));
            }
            circularImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    Intent intent = new Intent(mContext, ProfileActivity.class);
//                    intent.putExtra("user_id", userModel.getUser_id());
//                    mContext.startActivity(intent);
                    HomeActivity.navigateToProfile(userModel.getUser_id());
                }
            });
            ImageView ibAdd = (ImageView)v.findViewById(R.id.ib_search_add);
//            ibAdd.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    HomeActivity.sendFriendRequest(position);
//                }
//            });
            if (userModel.getFriendStatus().equals("none")) {
                ibAdd.setVisibility(View.VISIBLE);
            } else {
                ibAdd.setVisibility(View.GONE);
            }
        }

        return v;
    }

    @Override
    public Filter getFilter() {
        return nameFilter;
    }

    Filter nameFilter = new Filter() {
        @Override
        public String convertResultToString(Object resultValue) {
            return ((UserModel)(resultValue)).getFullname();
        }
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            if(constraint != null) {
                suggestions.clear();
                for (UserModel customer : itemsAll) {
                    if (customer.getFullname().toLowerCase().contains(constraint.toString())) {
                        suggestions.add(customer);
                    }
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = suggestions;
                filterResults.count = suggestions.size();
                return filterResults;
            } else {
                return new FilterResults();
            }
        }
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            ArrayList<UserModel> filteredList = (ArrayList<UserModel>) results.values;
            if(results != null && results.count > 0) {
                clear();
                for (UserModel c : filteredList) {
                    add(c);
                }
                notifyDataSetChanged();
            }
        }
    };

}
