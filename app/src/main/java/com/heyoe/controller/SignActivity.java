package com.heyoe.controller;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.heyoe.R;
import com.heyoe.controller.fragments.PrivacyPolicyFragment;
import com.heyoe.controller.fragments.SigninFragment;
import com.heyoe.controller.fragments.SignupFragment;
import com.heyoe.controller.fragments.TermsOfUseFragment;

public class SignActivity extends AppCompatActivity {

    public static FragmentManager fragmentManager;
    public static Fragment[] fragments;
    private static int currentPageNum;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign);

        initVariable();
        initUI();

    }
    private void initVariable() {
        fragmentManager = getSupportFragmentManager();
        fragments = new Fragment[4];

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
