package com.heyoe_chat.controller;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;

import com.heyoe_chat.R;
import com.heyoe_chat.controller.fragments.PrivacyPolicyFragment;
import com.heyoe_chat.controller.fragments.SigninFragment;
import com.heyoe_chat.controller.fragments.SignupFragment;
import com.heyoe_chat.controller.fragments.TermsOfUseFragment;
import com.heyoe_chat.controller.push.GetNotificationRegID;
import com.heyoe_chat.model.Constant;
import com.heyoe_chat.model.Global;
import com.heyoe_chat.utilities.Utils;

public class SignActivity extends AppCompatActivity {

    public static FragmentManager fragmentManager;
    public static Fragment[] fragments;
    private static int currentPageNum;
    public static String type;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign);

        if (Utils.getFromPreference(this, Constant.DEVICE_TOKEN).length() == 0) {
            GetNotificationRegID getNotificationRegID = new GetNotificationRegID(this);
            getNotificationRegID.registerInBackground();
        }
        if (Utils.getFromPreference(this, Constant.DEVICE_ID).length() == 0) {
            String deviceID = ((TelephonyManager)getBaseContext().getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
            Utils.saveToPreference(this, Constant.DEVICE_ID, deviceID);
        }



        initVariable();
        initUI();

    }
    private void initVariable() {
        fragmentManager = getSupportFragmentManager();
        fragments = new Fragment[4];
        type = getIntent().getStringExtra("type");
        if (type != null) {
            if (type.equals("activity")) {
                Global.getInstance().increaseActivityCount();
            } else if (type.equals("message")) {
                Global.getInstance().increaseMessageCount();
            }
        }

    }
    private void initUI() {
        pushFragment(0);
    }
    public static void pushFragment(int pageNum) {
        //set animation
        android.support.v4.app.FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
        switch (pageNum) {

            case 0:
                fragments[pageNum] = new SigninFragment();
                transaction
                        .replace(R.id.fragment_container, fragments[pageNum])
                        .commit();
                currentPageNum = 0;
                break;
            case 1:
                fragments[pageNum] = new SignupFragment();
                transaction
                        .replace(R.id.fragment_container, fragments[pageNum])
                        .commit();
                currentPageNum = 1;
                break;
            case 2:
                fragments[pageNum] = new TermsOfUseFragment();
                transaction
                        .replace(R.id.fragment_container, fragments[pageNum])
                        .commit();
                currentPageNum = 2;
                break;

            case 3:
                fragments[pageNum] = new PrivacyPolicyFragment();
                transaction
                        .replace(R.id.fragment_container, fragments[pageNum])
                        .commit();
                currentPageNum = 3;
                break;

        }
    }
    public void popFragment() {

        if (currentPageNum > 0) {
            //set animation
            android.support.v4.app.FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_left);
            if (currentPageNum == 3) {
                transaction.replace(R.id.fragment_container, fragments[currentPageNum - 2]).commit();
                currentPageNum -= 2;
            } else {
                transaction.replace(R.id.fragment_container, fragments[currentPageNum - 1]).commit();
                currentPageNum -= 1;
            }

        } else {
            SignActivity.this.finish();
        }

    }

    @Override
    public void onBackPressed() {
        popFragment();
    }
}
