package com.heyoe_chat.controller.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.TextView;

import com.heyoe_chat.R;
import com.heyoe_chat.controller.SignActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class TermsOfUseFragment extends Fragment {


    public TermsOfUseFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the text_layout for this fragment
        View view = inflater.inflate(R.layout.fragment_terms_of_use, container, false);
        Toolbar toolbar = (Toolbar)view.findViewById(R.id.toolbar);
        ImageButton ibBack = (ImageButton)toolbar.findViewById(R.id.ib_back);
        ibBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((SignActivity)getActivity()).popFragment();
            }
        });
        TextView tvTitle = (TextView)toolbar.findViewById(R.id.tv_home_title);
        tvTitle.setText(getResources().getString(R.string.user_agreement));

        WebView webView = (WebView)view.findViewById(R.id.webview);
        webView.loadUrl("file:///android_asset/term_nl.html");
        return view;
    }

}
