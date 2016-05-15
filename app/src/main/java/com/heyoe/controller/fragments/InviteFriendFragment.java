package com.heyoe.controller.fragments;


import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.FacebookSdk;
import com.facebook.applinks.AppLinkData;
import com.facebook.share.model.AppInviteContent;
import com.facebook.share.widget.AppInviteDialog;
import com.heyoe.R;
import com.heyoe.model.API;

import bolts.AppLinks;

/**
 * A simple {@link Fragment} subclass.
 */
public class InviteFriendFragment extends Fragment {


    Activity mActivity;

    public InviteFriendFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the text_layout for this fragment
        View view = inflater.inflate(R.layout.fragment_invite_friend, container, false);

        initVariable();
        initUI(view);
        fbInviteDlg();
        return view;
    }
    private void initVariable() {
        mActivity = getActivity();
        FacebookSdk.sdkInitialize(mActivity);


    }
    private void initUI(View view) {

    }

    public void fbInviteDlg() {
        String appLinkUrl, previewImageUrl;

        appLinkUrl = "http://www.heyoe.com/";
        previewImageUrl = API.BASE_APP;

        if (AppInviteDialog.canShow()) {
            AppInviteContent content = new AppInviteContent.Builder()
                    .setApplinkUrl(appLinkUrl)
//                    .setPreviewImageUrl(previewImageUrl)
                    .build();
            AppInviteDialog.show(mActivity, content);
        }
    }
}
