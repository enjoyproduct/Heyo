package com.heyoe.controller.adapters;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.toolbox.Volley;
import com.heyoe.R;
import com.heyoe.controller.DetailPostActivity;
import com.heyoe.controller.MediaPlayActivity;
import com.heyoe.controller.ProfileActivity;
import com.heyoe.controller.UserListActivity;
import com.heyoe.controller.fragments.MainFragment;
import com.heyoe.model.API;
import com.heyoe.model.CommentModel;
import com.heyoe.model.PostModel;
import com.heyoe.utilities.FileUtility;
import com.heyoe.utilities.TimeUtility;
import com.heyoe.utilities.UIUtility;
import com.heyoe.utilities.image_downloader.UrlImageViewCallback;
import com.heyoe.utilities.image_downloader.UrlRectangleImageViewHelper;
import com.heyoe.widget.MyCircularImageView;

import org.w3c.dom.Text;

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
    public View getView(final int position, View convertView, ViewGroup parent) {
        final PostModel postModel = arrayList.get(position);
        View view = convertView;
        if (postModel.getFriendStatus().equals("active")) {
            if (view == null) {
                view = layoutInflater.inflate(R.layout.item_post_for_friend, null);
            }
            ImageButton ibFavorite = (ImageButton)view.findViewById(R.id.iv_ipff_favorite);
            ibFavorite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (postModel.getFavorite().equals("favorite")) {
                        MainFragment.setFavorite(position, false);
                    } else {
                        MainFragment.setFavorite(position, true);
                    }
                }
            });
            ImageView ivLikeCount = (ImageView)view.findViewById(R.id.iv_ipff_like_count);
            ImageView ivDislikeCount = (ImageView)view.findViewById(R.id.iv_ipff_dislike_count);
            ImageView ivComments = (ImageView)view.findViewById(R.id.iv_ipff_comments);

            ivLikeCount.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(activity, UserListActivity.class);
                    intent.putExtra("type", "like");
                    intent.putExtra("post", postModel);
                    activity.startActivity(intent);
                }
            });

            ivDislikeCount.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(activity, UserListActivity.class);
                    intent.putExtra("type", "dislike");
                    intent.putExtra("post", postModel);
                    activity.startActivity(intent);
                }
            });
            ivComments.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(activity, DetailPostActivity.class);
                    intent.putExtra("post", postModel);
                    activity.startActivityForResult(intent, 101);
                }
            });
            TextView tvLikeCount = (TextView)view.findViewById(R.id.tv_ipff_like_count);
            TextView tvDislike = (TextView)view.findViewById(R.id.tv_ipff_dislike_count);

            TextView[] tvNames = new TextView[3];
            tvNames[0] = (TextView)view.findViewById(R.id.tv_ipff_name1);
            tvNames[1] = (TextView)view.findViewById(R.id.tv_ipff_name2);
            tvNames[2] = (TextView)view.findViewById(R.id.tv_ipff_name3);

            TextView[]  tvComments = new TextView[3];
            tvComments[0] = (TextView)view.findViewById(R.id.tv_ipff_comment1);
            tvComments[1] = (TextView)view.findViewById(R.id.tv_ipff_comment2);
            tvComments[2] = (TextView)view.findViewById(R.id.tv_ipff_comment3);

            LinearLayout[] llComments = new LinearLayout[3];
            llComments[0] = (LinearLayout)view.findViewById(R.id.ll_ipff_comment1);
            llComments[1] = (LinearLayout)view.findViewById(R.id.ll_ipff_comment2);
            llComments[2] = (LinearLayout)view.findViewById(R.id.ll_ipff_comment3);

            TextView tvViewedCount = (TextView)view.findViewById(R.id.tv_ipff_viewed_count);
            TextView tvSharedCount = (TextView)view.findViewById(R.id.tv_ipff_shared_count);

            if (postModel.getFavorite().equals("favorite")) {
                ibFavorite.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_favorite_star_white));
            } else {
                ibFavorite.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_favorite_star_empty));
            }
            if (postModel.getLike().equals("like")) {
                ivLikeCount.setImageDrawable(activity.getResources().getDrawable(R.drawable.green_like_count));
                ivDislikeCount.setImageDrawable(activity.getResources().getDrawable(R.drawable.dislike_count_empty));
            } else if (postModel.getLike().equals("dislike")) {
                ivLikeCount.setImageDrawable(activity.getResources().getDrawable(R.drawable.like_count_empty));
                ivDislikeCount.setImageDrawable(activity.getResources().getDrawable(R.drawable.green_dislike_count));
            } else {
                ivLikeCount.setImageDrawable(activity.getResources().getDrawable(R.drawable.like_count_empty));
                ivDislikeCount.setImageDrawable(activity.getResources().getDrawable(R.drawable.dislike_count_empty));
            }

            if (postModel.getCommented().equals("yes")) {
                ivComments.setImageDrawable(activity.getResources().getDrawable(R.drawable.green_comment_count));
            } else {
                ivComments.setImageDrawable(activity.getResources().getDrawable(R.drawable.comment_count_empty));
            }

            tvLikeCount.setText(postModel.getLike_count());
            tvDislike.setText(postModel.getDislike_count());
            tvViewedCount.setText(postModel.getViewed_count());
            tvSharedCount.setText(postModel.getShared_count());

            int commentCount = postModel.getArrComments().size() > 3 ? 3 : postModel.getArrComments().size();
            for (int i = 0; i < commentCount; i ++) {
                CommentModel commentModel = postModel.getArrComments().get(i);
                tvNames[i].setText(commentModel.getFullname());
                tvComments[i].setText(commentModel.getComment());
                llComments[i].setVisibility(View.VISIBLE);
            }

        } else {
            if (view == null) {
                view = layoutInflater.inflate(R.layout.item_post_for_nonfriend, null);
            }
            MyCircularImageView[] civFriends = new MyCircularImageView[3];
            civFriends[0] = (MyCircularImageView)view.findViewById(R.id.civ_ipff_friend_avatar1);
            civFriends[1] = (MyCircularImageView)view.findViewById(R.id.civ_ipff_friend_avatar2);
            civFriends[2] = (MyCircularImageView)view.findViewById(R.id.civ_ipff_friend_avatar3);

            TextView tvBanner = (TextView)view.findViewById(R.id.tv_ipff_banner_text);

            ////make banner text ==start
            int friendCount = postModel.getArrLiked_friends().size() > 3 ? 3 : postModel.getArrLiked_friends().size();

            String strBannerText = "";
            for (int i = 0; i < friendCount; i ++) {
                civFriends[i].setVisibility(View.VISIBLE);
                if (!postModel.getMedia_url().equals("")) {
                    UrlRectangleImageViewHelper.setUrlDrawable(civFriends[i], API.BASE_AVATAR + postModel.getArrLiked_friends().get(i).getAvatar(), R.drawable.default_user, new UrlImageViewCallback() {
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
                } else {
                    civFriends[i].setImageDrawable(activity.getResources().getDrawable(R.drawable.default_user));
                }
                if (friendCount > 1 && i == friendCount - 1) {
                    strBannerText = strBannerText.substring(0, strBannerText.length() - 2) + " "
                            + activity.getResources().getString(R.string.and) + " "
                            + "<b>" + postModel.getArrLiked_friends().get(i).getFullname() + " " + "</b>";
                    continue;
                }
                String str = "<b>" + postModel.getArrLiked_friends().get(i).getFullname() + ", " + "</b>";

            }
            String strLikedMedia = "";
            if (postModel.getMedia_type().equals("post_photo")) {
                strLikedMedia = activity.getResources().getString(R.string.like_a_photo_from);
            } else {
                strLikedMedia = activity.getResources().getString(R.string.like_a_video_from);
            }
            strBannerText = strBannerText + strLikedMedia
                    + "<b>" + postModel.getPoster_fullname() + " * " + "</b>";
            strBannerText = strBannerText + TimeUtility.countTime(activity, Long.parseLong(postModel.getPosted_date()));
            //make banner text == end

            tvBanner.setText(Html.fromHtml(strBannerText));

            //add friend
            ImageButton ibAddFriend = (ImageButton)view.findViewById(R.id.ib_ipff_add_friend);
            if (postModel.getFriendStatus().equals("waiting")) {
                ibAddFriend.setImageDrawable(activity.getResources().getDrawable(R.drawable.sandglass));
            } else {
                ibAddFriend.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_plus_green));
            }
            ibAddFriend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (postModel.getFriendStatus().equals("none")) {
                        MainFragment.addFriend(position);
                    }
                }
            });
        }

        MyCircularImageView myCircularImageView = (MyCircularImageView)view.findViewById(R.id.civ_ipff_avatar);
        myCircularImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.startActivity(new Intent(activity, ProfileActivity.class));
            }
        });
        if (!postModel.getPoster_avatar().equals("")) {
            UrlRectangleImageViewHelper.setUrlDrawable(myCircularImageView, API.BASE_AVATAR + postModel.getPoster_avatar(), R.drawable.default_circular_user_photo, new UrlImageViewCallback() {
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


        final ImageView ivMedia = (ImageView)view.findViewById(R.id.iv_ipff_media);


        String imageUrl = "";
        String videoUrl = "";
        if (postModel.getImageWidth() != 0 && postModel.getImageHeight() != 0) {
            double ratio = ((double)postModel.getImageHeight() / (double)postModel.getImageWidth());
            double height = ratio * UIUtility.getScreenWidth(activity);
            UIUtility.setImageViewSize(ivMedia, UIUtility.getScreenWidth(activity), (int)height);
        } else {
            double height = (int)( UIUtility.getScreenWidth(activity) * 0.75);
            UIUtility.setImageViewSize(ivMedia, UIUtility.getScreenWidth(activity),(int) height);
        }
        if (postModel.getMedia_type().equals("post_photo")) {
            imageUrl = API.BASE_POST_PHOTO + postModel.getMedia_url();

        } else if (postModel.getMedia_type().equals("post_video")){
            imageUrl = API.BASE_THUMBNAIL + postModel.getMedia_url().substring(0, postModel.getMedia_url().length() - 3) + "jpg";
            videoUrl = API.BASE_POST_VIDEO + postModel.getMedia_url();
        } else if (postModel.getMedia_type().equals("youtube")) {
            imageUrl = API.BASE_YOUTUB_PREFIX + FileUtility.getFilenameFromPath(postModel.getMedia_url()) + API.BASE_YOUTUB_SURFIX;
            videoUrl = postModel.getMedia_url();
        }
        if (!postModel.getMedia_url().equals("")) {
            UrlRectangleImageViewHelper.setUrlDrawable(ivMedia, imageUrl, R.drawable.default_tour, new UrlImageViewCallback() {
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

        ImageButton ibPlay = (ImageButton)view.findViewById(R.id.ib_ipff_play);
        if (postModel.getMedia_type().equals("post_photo")) {
            ibPlay.setVisibility(View.GONE);
        } else {
            ibPlay.setVisibility(View.VISIBLE);
        }
        final String finalVideoUrl = videoUrl;
        ibPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, MediaPlayActivity.class);
                intent.putExtra("url", finalVideoUrl);
                intent.putExtra("type", postModel.getMedia_type());
                activity.startActivity(intent);
            }
        });
        TextView tvName = (TextView)view.findViewById(R.id.tv_ipff_fullname);
        tvName.setText(postModel.getPoster_fullname());

        TextView tvPostedDate = (TextView)view.findViewById(R.id.tv_ipff_date);
        tvPostedDate.setText(TimeUtility.countTime(activity, Long.parseLong(postModel.getPosted_date()) ));

        TextView tvDescription = (TextView)view.findViewById(R.id.tv_ipff_description);
        tvDescription.setText(Html.fromHtml(postModel.getDescription()));

        ImageButton ibLike = (ImageButton)view.findViewById(R.id.ib_ipff_like);
        ibLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Long.parseLong(TimeUtility.getCurrentTimeStamp()) - postModel.getClickedTime() > 60 && !postModel.getLike().equals("dislike")) {
                    if (postModel.getLike().equals("like")) {
                        MainFragment.setLike(position, "cancel_like");
                    } else if (postModel.getLike().equals("none")) {
                        MainFragment.setLike(position, "like");
                    }
                }

            }
        });
        ImageButton ibDislike = (ImageButton)view.findViewById(R.id.ib_ipff_dislike);
        ibDislike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Long.parseLong(TimeUtility.getCurrentTimeStamp()) - postModel.getClickedTime() > 60 && !postModel.getLike().equals("like")) {
                    if (postModel.getLike().equals("dislike")) {
                        MainFragment.setLike(position, "cancel_dislike");
                    } else if (postModel.getLike().equals("none")) {
                        MainFragment.setLike(position, "dislike");
                    }
                }

            }
        });
        if (postModel.getLike().equals("like")) {
            ibLike.setImageDrawable(activity.getResources().getDrawable(R.drawable.btn_like_green));
            ibDislike.setImageDrawable(activity.getResources().getDrawable(R.drawable.btn_dislike));
        } else if (postModel.getLike().equals("dislike")) {
            ibLike.setImageDrawable(activity.getResources().getDrawable(R.drawable.btn_like));
            ibDislike.setImageDrawable(activity.getResources().getDrawable(R.drawable.btn_dislike_green));
        } else {
            ibLike.setImageDrawable(activity.getResources().getDrawable(R.drawable.btn_like));
            ibDislike.setImageDrawable(activity.getResources().getDrawable(R.drawable.btn_dislike));
        }


        ImageButton ibComment = (ImageButton)view.findViewById(R.id.ib_ipff_comment);
        ibComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, DetailPostActivity.class);
                intent.putExtra("post", postModel);
                activity.startActivityForResult(intent, 101);
            }
        });
        if (postModel.getCommented().equals("yes")) {
            ibComment.setImageDrawable(activity.getResources().getDrawable(R.drawable.btn_comment_green));
        } else {
            ibComment.setImageDrawable(activity.getResources().getDrawable(R.drawable.btn_comment));
        }

        ImageButton ibShare = (ImageButton)view.findViewById(R.id.ib_ipff_share);
        ibShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainFragment.sharing(position);
            }
        });
        ////



        return view;
    }
}
