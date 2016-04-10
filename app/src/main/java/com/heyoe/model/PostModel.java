package com.heyoe.model;

import java.util.ArrayList;

/**
 * Created by dell17 on 4/10/2016.
 */
public class PostModel {
    String post_id, posted_date, poster_id, poster_avatar, poster_celebrity, media_type,
        media_url, like_count, unlike_count, comment_count, shared_count, viewed_count,
        is_liked, is_unliked, is_favorite, is_commented;
    ArrayList<UserModel> arrLiked_friends = new ArrayList<>();

    public String getPost_id() {
        return post_id;
    }

    public void setPost_id(String post_id) {
        this.post_id = post_id;
    }

    public String getPosted_date() {
        return posted_date;
    }

    public void setPosted_date(String posted_date) {
        this.posted_date = posted_date;
    }

    public String getPoster_id() {
        return poster_id;
    }

    public void setPoster_id(String poster_id) {
        this.poster_id = poster_id;
    }

    public String getPoster_avatar() {
        return poster_avatar;
    }

    public void setPoster_avatar(String poster_avatar) {
        this.poster_avatar = poster_avatar;
    }

    public String getPoster_celebrity() {
        return poster_celebrity;
    }

    public void setPoster_celebrity(String poster_celebrity) {
        this.poster_celebrity = poster_celebrity;
    }

    public String getMedia_type() {
        return media_type;
    }

    public void setMedia_type(String media_type) {
        this.media_type = media_type;
    }

    public String getMedia_url() {
        return media_url;
    }

    public void setMedia_url(String media_url) {
        this.media_url = media_url;
    }

    public String getLike_count() {
        return like_count;
    }

    public void setLike_count(String like_count) {
        this.like_count = like_count;
    }

    public String getUnlike_count() {
        return unlike_count;
    }

    public void setUnlike_count(String unlike_count) {
        this.unlike_count = unlike_count;
    }

    public String getComment_count() {
        return comment_count;
    }

    public void setComment_count(String comment_count) {
        this.comment_count = comment_count;
    }

    public String getShared_count() {
        return shared_count;
    }

    public void setShared_count(String shared_count) {
        this.shared_count = shared_count;
    }

    public String getViewed_count() {
        return viewed_count;
    }

    public void setViewed_count(String viewed_count) {
        this.viewed_count = viewed_count;
    }

    public String getIs_liked() {
        return is_liked;
    }

    public void setIs_liked(String is_liked) {
        this.is_liked = is_liked;
    }

    public String getIs_unliked() {
        return is_unliked;
    }

    public void setIs_unliked(String is_unliked) {
        this.is_unliked = is_unliked;
    }

    public String getIs_favorite() {
        return is_favorite;
    }

    public void setIs_favorite(String is_favorite) {
        this.is_favorite = is_favorite;
    }

    public String getIs_commented() {
        return is_commented;
    }

    public void setIs_commented(String is_commented) {
        this.is_commented = is_commented;
    }

    public ArrayList<UserModel> getArrLiked_friends() {
        return arrLiked_friends;
    }

    public void setArrLiked_friends(ArrayList<UserModel> arrLiked_friends) {
        this.arrLiked_friends = arrLiked_friends;
    }

    public ArrayList<CommentModel> getArrComments() {
        return arrComments;
    }

    public void setArrComments(ArrayList<CommentModel> arrComments) {
        this.arrComments = arrComments;
    }

    ArrayList<CommentModel> arrComments = new ArrayList<>();
}
