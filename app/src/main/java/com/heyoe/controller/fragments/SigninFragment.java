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
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.CustomRequest;
import com.android.volley.toolbox.Volley;
import com.heyoe.R;
import com.heyoe.controller.HomeActivity;
import com.heyoe.controller.SignActivity;
import com.heyoe.model.API;
import com.heyoe.model.Constant;
import com.heyoe.utilities.Utils;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class SigninFragment extends Fragment {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvForgotPass, tvRegister;

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
//        mActivity = getActivity();
    }
    private void initUI(View view) {
        etEmail = (EditText)view.findViewById(R.id.et_signin_email);
        etPassword = (EditText)view.findViewById(R.id.et_signin_password);

        btnLogin = (Button)view.findViewById(R.id.btn_signin);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                signin();
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
    ///Sign in
    private void signin() {
        String email = etEmail.getText().toString();
        final String password = etPassword.getText().toString();

        if (email.length() == 0 ) {
            Utils.showOKDialog(mActivity, "Please input email");
            return;
        } else if (!Utils.isEmailValid(email)) {
            Utils.showOKDialog(mActivity, "Please input correct email");
            return;
        }
        else if (password.length() == 0) {
            Utils.showOKDialog(mActivity, "Please input password");
            return;
        }
        {

            Utils.showProgress(mActivity);

            Map<String, String> params = new HashMap<String, String>();
            params.put("email", email);
            params.put("password", password);

            CustomRequest signinRequest = new CustomRequest(Request.Method.POST, API.SINGIN, params,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Utils.hideProgress();
                            try {
                                String success = response.getString("result");
                                if (success.equals("success")) {

                                    JSONObject jsonObject = response.getJSONObject("data");

                                    String user_id = jsonObject.getString("user_id");
                                    String fullname = jsonObject.getString("fullname");
                                    String email = jsonObject.getString("email");
                                    String city = jsonObject.getString("city");
                                    String country = jsonObject.getString("country");
                                    String birthday = jsonObject.getString("birthday");
                                    String gender = jsonObject.getString("gender");
                                    String celebrity = jsonObject.getString("celebrity");
                                    String about_me = jsonObject.getString("about_me");
                                    String media_count = jsonObject.getString("media_count");
                                    String friend_count = jsonObject.getString("friend_count");
                                    String avatar = jsonObject.getString("avatar");
                                    String header_photo_url = jsonObject.getString("header_photo_url");
                                    String header_video_url = jsonObject.getString("header_video_url");

                                    Utils.setOnPreference(mActivity, Constant.USER_ID, user_id);
                                    Utils.setOnPreference(mActivity, Constant.EMAIL, email);
                                    Utils.setOnPreference(mActivity, Constant.PASSWORD, password);
                                    Utils.setOnPreference(mActivity, Constant.FULLNAME, fullname);
                                    Utils.setOnPreference(mActivity, Constant.CITY, city);
                                    Utils.setOnPreference(mActivity, Constant.COUNTRY, country);
                                    Utils.setOnPreference(mActivity, Constant.BIRTHDAY, birthday);
                                    Utils.setOnPreference(mActivity, Constant.GENDER, gender);
                                    Utils.setOnPreference(mActivity, Constant.CELEBRITY, celebrity);
                                    Utils.setOnPreference(mActivity, Constant.ABOUT_ME, about_me);
                                    Utils.setOnPreference(mActivity, Constant.MEDIA_COUNT, media_count);
                                    Utils.setOnPreference(mActivity, Constant.FRIEND_COUNT, friend_count);
                                    Utils.setOnPreference(mActivity, Constant.AVATAR, avatar);
                                    Utils.setOnPreference(mActivity, Constant.HEADER_PHOTO, header_photo_url);
                                    Utils.setOnPreference(mActivity, Constant.HEADER_VIDEO, header_video_url);

                                   
                                    startActivity(new Intent(mActivity, HomeActivity.class));
                                    getActivity().finish();
                                } else {
                                    String reason = response.getString("reason");
                                    if (reason.equals("401")) {
                                        Utils.showOKDialog(mActivity, "Email is unregistered");
                                    } else if (reason.equals("402")) {
                                        Utils.showOKDialog(mActivity, "Password incorrect");
                                    }
                                }
                            }catch (Exception e) {
                                e.printStackTrace();
                            }

                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Utils.hideProgress();
                            Toast.makeText(mActivity, error.toString(), Toast.LENGTH_LONG).show();
                        }
                    });
            RequestQueue requestQueue = Volley.newRequestQueue(mActivity);
            requestQueue.add(signinRequest);
        }

    }
}
