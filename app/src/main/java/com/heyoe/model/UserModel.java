package com.heyoe.model;

import java.io.Serializable;

/**
 * Created by dell17 on 4/10/2016.
 */
public class UserModel implements Serializable {
    String user_id;
    String fullname;
    String email;
    String city;
    String country;
    String birthday;
    String about_you;
    String celebrity;
    String media_count;
    String friend_count;
    String avatar;
    String header_photo;
    String header_video;
    String gender;
    String time;
    String friendStatus;

    public String getFriendStatus() {
        return friendStatus;
    }

    public void setFriendStatus(String friendStatus) {
        this.friendStatus = friendStatus;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    String password;

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getAbout_you() {
        return about_you;
    }

    public void setAbout_you(String about_you) {
        this.about_you = about_you;
    }

    public String getCelebrity() {
        return celebrity;
    }

    public void setCelebrity(String celebrity) {
        this.celebrity = celebrity;
    }

    public String getMedia_count() {
        return media_count;
    }

    public void setMedia_count(String media_count) {
        this.media_count = media_count;
    }

    public String getFriend_count() {
        return friend_count;
    }

    public void setFriend_count(String friend_count) {
        this.friend_count = friend_count;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getHeader_photo() {
        return header_photo;
    }

    public void setHeader_photo(String header_photo) {
        this.header_photo = header_photo;
    }

    public String getHeader_video() {
        return header_video;
    }

    public void setHeader_video(String header_video) {
        this.header_video = header_video;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }
}
