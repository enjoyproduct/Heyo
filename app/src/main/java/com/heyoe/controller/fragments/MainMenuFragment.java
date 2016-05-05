package com.heyoe.controller.fragments;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.heyoe.R;
import com.heyoe.controller.HomeActivity;
import com.heyoe.controller.ProfileActivity;
import com.heyoe.model.API;
import com.heyoe.model.Constant;
import com.heyoe.utilities.Utils;
import com.heyoe.utilities.image_downloader.UrlImageViewCallback;
import com.heyoe.utilities.image_downloader.UrlRectangleImageViewHelper;
import com.heyoe.widget.MyCircularImageView;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class MainMenuFragment extends Fragment {

    private ImageView ivDefaultAvatar, ivWhiteCircle, ivCheckCelibrity;
    private MyCircularImageView myCircularImageView;
    private TextView tvFullname;
    private ListView listView;

    private Activity mActivity;
    LayoutInflater mLayoutInflator;
    public MainMenuFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_main_menu, container, false);

        mLayoutInflator = getLayoutInflater(savedInstanceState);

        initVariables();
        initUI(view);
        return view;
    }

    private void initVariables() {

    }
    private void initUI(View view) {
//        ivDefaultAvatar = (ImageView)view.findViewById(R.id.iv_menu_default_avatar);
        ivWhiteCircle = (ImageView)view.findViewById(R.id.iv_menu_white_circle);
        ivCheckCelibrity = (ImageView)view.findViewById(R.id.iv_menu_celebrity);
        if (Utils.getFromPreference(mActivity, Constant.CELEBRITY).equals("yes")) {
            ivCheckCelibrity.setVisibility(View.VISIBLE);
        } else {
            ivCheckCelibrity.setVisibility(View.INVISIBLE);
        }
        tvFullname = (TextView)view.findViewById(R.id.tv_menu_fullname);
        tvFullname.setText(Utils.getFromPreference(mActivity, Constant.FULLNAME));

        myCircularImageView = (MyCircularImageView)view.findViewById(R.id.civ_menu_avatar);
        myCircularImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mActivity, ProfileActivity.class);
                intent.putExtra("user_id", Utils.getFromPreference(mActivity, Constant.USER_ID));
                mActivity.startActivity(intent);
                HomeActivity.mDrawerLayout.closeDrawers();
            }
        });
        if (!Utils.getFromPreference(mActivity, Constant.AVATAR).equals("")) {
            UrlRectangleImageViewHelper.setUrlDrawable(myCircularImageView, API.BASE_AVATAR + Utils.getFromPreference(mActivity, Constant.AVATAR), R.drawable.default_circular_user_photo, new UrlImageViewCallback() {
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

        listView = (ListView)view.findViewById(R.id.lv_menu_listview);
        MenuAdapter menuAdapter = new MenuAdapter(generateMenuModel());
        listView.setAdapter(menuAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HomeActivity.menuNavigateTo(position);
            }
        });

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }
    private ArrayList<MenuModel> generateMenuModel() {
        int[] images = {
                R.drawable.ic_menu_favorites,
//                R.drawable.ic_menu_friends,
                R.drawable.ic_menu_requests,
                R.drawable.ic_menu_more_friends,
                R.drawable.ic_menu_invite_friends,
//                R.drawable.ic_menu_groups,
//                R.drawable.ic_menu_calender,
                R.drawable.ic_menu_faq,
                R.drawable.ic_menu_logout
        };

        int[] titles = {
                R.string.FAVORITES,
//                R.string.FRIENDS,
                R.string.REQUESTS,
                R.string.MORE_FRIENDS,
                R.string.INVITE_FRIENDS,
//                R.string.GROUPS,
//                R.string.CALENDAR,
                R.string.FAQ,
                R.string.LOG_OUT,
        };
        ArrayList<MenuModel> arrayList = new ArrayList<>();
        for (int i = 0; i < images.length; i ++) {
            MenuModel menuModel = new MenuModel();
            menuModel.imageId = images[i];
            menuModel.title = titles[i];
            arrayList.add(menuModel);
        }
        return arrayList;
    }
    public class MenuModel {
        int  title;
        int imageId;

        public int getTitle() {
            return title;
        }

        public void setTitle(int title) {
            this.title = title;
        }

        public int getImageId() {
            return imageId;
        }

        public void setImageId(int imageId) {
            this.imageId = imageId;
        }
    }
    private class MenuAdapter extends BaseAdapter {

        ArrayList<MenuModel> arrMenus;

        private MenuAdapter (ArrayList<MenuModel> arrayList) {
            arrMenus = arrayList;

        }
        @Override
        public int getCount() {
            return arrMenus.size();
        }

        @Override
        public Object getItem(int position) {
            return arrMenus.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = mLayoutInflator.inflate(R.layout.item_menu_list, null);
            }
            ImageView imageView = (ImageView)view.findViewById(R.id.iv_item_menu);
            TextView textView = (TextView)view.findViewById(R.id.tv_item_menu);

            MenuModel  menuModel = arrMenus.get(position);
            imageView.setImageDrawable(getResources().getDrawable(menuModel.getImageId()));
            textView.setText(getResources().getString(menuModel.getTitle()));

            return view;
        }
    }
}
