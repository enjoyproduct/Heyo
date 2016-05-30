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
        ArrayList<Long> msgDate = new ArrayList<>();

        for (UserModel userModel : arrayList) {
            if (userModel.getQbLastMsgSentTime() > 0) {
                msgDate.add(userModel.getQbLastMsgSentTime());
            }
        }
        //sort by date(timestamp)
        QSort qSort = new QSort(msgDate);
        qSort.sort();
        msgDate.clear();
        msgDate.addAll(qSort.getResult());
        //make new user arraylist
        ArrayList<UserModel> sortedArray = new ArrayList<>();
        for (int i = msgDate.size() - 1; i >= 0; i --) {
            for (int k = 0; k < arrayList.size(); k ++) {
                if( msgDate.get(i) == arrayList.get(k).getQbLastMsgSentTime()) {
                    sortedArray.add(arrayList.get(k));
                    break;
                }
            }
        }
        for (int j = 0; j < arrayList.size(); j ++) {
            if (arrayList.get(j).getQbLastMsgSentTime() == 0) {
                sortedArray.add(arrayList.get(j));
            }
        }
        return sortedArray;
    }
}
