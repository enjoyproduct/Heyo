package com.heyoe.model;

import com.heyoe.controller.App;
import com.heyoe.utilities.Utils;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;

/**
 * Created by dell17 on 5/16/2016.
 */
public class Global {
    public  QBUser qbUser;
    public boolean isChatting;
    public String currentChattingUser;
    public String currentChattingPage;
    public ArrayList<UserModel> arrCheckinChatUsers;

    static Global instance;

    private Global () {
        isChatting = false;
        currentChattingUser = "";
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
        ArrayList<UserModel> arrUsers = new ArrayList<>();
        return arrayList;
    }
}
