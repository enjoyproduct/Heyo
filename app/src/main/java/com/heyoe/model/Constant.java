package com.heyoe.model;

import android.os.Environment;

/**
 * Created by Administrator on 2/9/2016.
 */
public class Constant {

    public static String MEDIA_PATH = Environment.getExternalStorageDirectory().toString() + "/";
    //////////string parameters

    public static String INDECATOR = "Heyoe";
    public static String ANDROID = "android";
    public static String DEVICE_TYPE = "device_type";
    public static String DEVICE_TOKEN = "device_token";
    public static String DEVICE_ID = "device_id";

    public static String USER_ID = "user_id";
    public static String EMAIL = "email";
    public static String PASSWORD = "password";
    public static String FIRST_NAME = "firstname";
    public static String LAST_NAME = "lastname";
    public static String FULLNAME = "fullname";
    public static String BIRTHDAY = "birthday";
    public static String GENDER = "gender";
    public static String AVATAR = "avatar";
    public static String ADD_AVATAR = "add_avatar";
    public static String CITY = "city";
    public static String COUNTRY = "country";
    public static String VERIFY_CODE = "verify_code";
    public static String CELEBRITY = "celebrity";
    public static String ABOUT_ME = "gender";
    public static String MEDIA_COUNT = "media_count";
    public static String FRIEND_COUNT = "friend_count";
    public static String HEADER_PHOTO = "header_photo";
    public static String HEADER_VIDEO = "header_video";


    ///for facebook login
    public static String FB_EMAIL = INDECATOR + "_fb_email";
    public static String FB_ID = INDECATOR + "_fb_id";
    public static String FB_ACCESS_TOKEN = INDECATOR + "_fb_access_token";
    public static String FB_NAME = INDECATOR + "_fb_name";
    public static String FB_PHOTO = INDECATOR + "_fb_photo";

    public static int PLACE_AUTOCOMPLETE_REQUEST_CODE = 101;
    public static int REQUEST_PLACE_PICKER = 102;


    public static String APP_ID = "39876";
    public static String AUTH_KEY = "zYac8X4A2Zs3z9y";
    public static String AUTH_SECRET = "OxhcUz3rrnSaeVJ";
    public static String ACCOUNT_KEY = "5Vv8WGbhPpqHqtyoXs7i";

    public static String DEFAULT_PASSWORD = "135792468";

    public static String QB_ID = "qb_id";
}
