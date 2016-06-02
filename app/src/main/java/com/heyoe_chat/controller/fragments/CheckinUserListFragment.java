package com.heyoe_chat.controller.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.heyoe_chat.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class CheckinUserListFragment extends Fragment {


    public CheckinUserListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the text_layout for this fragment
        return inflater.inflate(R.layout.fragment_checkin_user_list, container, false);
    }

}
