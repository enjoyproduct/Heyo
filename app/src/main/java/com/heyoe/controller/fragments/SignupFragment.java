package com.heyoe.controller.fragments;


import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.transition.CircularPropagation;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.heyoe.R;
import com.heyoe.controller.SignActivity;
import com.heyoe.widget.MyCircularImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class SignupFragment extends Fragment {

    private ImageView ivDefaultAvatar, ivWhiteCircle, ivBanner;
    private MyCircularImageView civAvatar;
    private EditText etFirstname, etLastname, etEmail, etPassword, etConfirmPassword;
    private Button btnSignup;
    private TextView tvTerms, tvPolicy;

    private Activity mActivity;

    public SignupFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_signup, container, false);
        initVariables();
        initUI(view);
        return view;
    }
    private void initVariables() {

    }
    private void initUI(View view) {
        ivDefaultAvatar = (ImageView)view.findViewById(R.id.iv_signup_default_avatar);
        ivWhiteCircle = (ImageView)view.findViewById(R.id.iv_signup_white_circle);
        ivBanner = (ImageView)view.findViewById(R.id.iv_signup_banner);

        civAvatar = (MyCircularImageView)view.findViewById(R.id.civ_signup_avatar);

        etFirstname = (EditText)view.findViewById(R.id.et_signup_firstname);
        etLastname = (EditText)view.findViewById(R.id.et_signup_lastname);
        etEmail = (EditText)view.findViewById(R.id.et_signup_email);
        etPassword = (EditText)view.findViewById(R.id.et_signup_password);
        etConfirmPassword = (EditText)view.findViewById(R.id.et_signup_confirm_pass);

        btnSignup = (Button)view.findViewById(R.id.btn_signup);
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        tvTerms = (TextView)view.findViewById(R.id.tv_signup_termsofuse);
        tvTerms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SignActivity.pushFragment(2);
            }
        });
        tvPolicy = (TextView)view.findViewById(R.id.tv_signup_privacypolicy);
        tvPolicy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SignActivity.pushFragment(3);
            }
        });

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }
}
