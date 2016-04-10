package com.heyoe.controller.fragments;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.heyoe.R;
import com.heyoe.controller.HomeActivity;
import com.heyoe.controller.SignActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class SigninFragment extends Fragment {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvForgotPass, tvRegister;

    private Context mContext ;
    private Activity mActivity;

    public SigninFragment() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_signin, container, false);
        initVaraibles();
        initUI(view);
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

    private void initVaraibles() {
//        mContext = getActivity();
    }
    private void initUI(View view) {
        etEmail = (EditText)view.findViewById(R.id.et_signin_email);
        etPassword = (EditText)view.findViewById(R.id.et_signin_password);

        btnLogin = (Button)view.findViewById(R.id.btn_signin);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mActivity, HomeActivity.class));
            }
        });

        tvForgotPass = (TextView)view.findViewById(R.id.tv_signin_forgot_password);
        tvForgotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        tvRegister = (TextView)view.findViewById(R.id.tv_signin_register);
        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SignActivity.pushFragment(1);
            }
        });
    }

}
