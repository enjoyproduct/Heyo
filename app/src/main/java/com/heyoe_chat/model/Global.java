package com.heyoe_chat.model;

import com.heyoe_chat.controller.App;
import com.heyoe_chat.utilities.Utils;

import java.util.ArrayList;

/**
 * Created by dell17 on 5/16/2016.
 */
public class Global {
    public boolean isChatting;
    public String currentChattingUserId;
    public String currentChattingPage;
    public ArrayList<UserModel> arrCheckinChatUsers;

    static Global instance;

    private Global () {
        isChatting = false;
        currentChattingUserId = "";
        currentChattingPage = "";
        arrCheckinChatUsers = new ArrayList<>();

    }
    public static Global getInstance() {
        if (instance == null) {
            instance = new Global();
        }
        return instance;
    }

    public void increaseMessageCount() {
        int count = Utils.getIntFromPreference(App.getInstance(), Constant.MSG_COUNT);
        count ++;
        Utils.saveIntToPreference(App.getInstance(), Constant.MSG_COUNT, count);
    }
    public void increaseActivityCount() {
        int count = Utils.getIntFromPreference(App.getInstance(), Constant.ACTIVITY_COUNT);
        count ++;
        Utils.saveIntToPreference(App.getInstance(), Constant.ACTIVITY_COUNT, count);
    }
    public void decreaseMessageCount(int count) {
        int unreadMsgCount = Utils.getIntFromPreference(App.getInstance(), Constant.MSG_COUNT);
        if (count > unreadMsgCount) {
            unreadMsgCount = 0;
        } else {
            unreadMsgCount -= count;
        }
        Utils.saveIntToPreference(App.getInstance(), Constant.MSG_COUNT, unreadMsgCount);
    }

    public ArrayList<UserModel> qsortUsersByMsgDate(ArrayList<UserModel> arrayList){
        QSort qSort = new QSort(arrayList);
        qSort.sort();
        return qSort.getResult();
    }
}
