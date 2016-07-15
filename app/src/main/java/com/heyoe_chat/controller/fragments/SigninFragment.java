package com.heyoe_chat.controller.fragments;


import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.CustomRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.heyoe_chat.R;
import com.heyoe_chat.controller.HomeActivity;
import com.heyoe_chat.controller.SignActivity;
import com.heyoe_chat.model.API;
import com.heyoe_chat.model.Constant;
import com.heyoe_chat.utilities.Utils;
import com.layer.atlas.util.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class SigninFragment extends Fragment implements  GoogleApiClient.OnConnectionFailedListener{

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvForgotPass, tvRegister;
    private ImageButton ibFb, ibGp;
    private Activity mActivity;

    //fb signin
    CallbackManager callbackManager;
    AccessToken fbAccessToken ;
    //google+ sign in
    private GoogleApiClient googleApiClient;

    int GG_SIGN_IN_REQUEST = 101;

    public SigninFragment() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the text_layout for this fragment
        View view = inflater.inflate(R.layout.fragment_signin, container, false);

        initVaraibles();

        initUI(view);
        checkAutoLogin();
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
//        mActivity = activity;

    }
    private void checkAutoLogin() {

        if(Utils.getFromPreference(mActivity, Constant.EMAIL).length() > 0 ) {
            if ( Utils.getFromPreference(mActivity, Constant.PASSWORD).length() > 0) {
                email = Utils.getFromPreference(mActivity, Constant.EMAIL);
                password = Utils.getFromPreference(mActivity, Constant.PASSWORD);
                signin();
            } else {
                socialSignin();
            }

        } else if (Utils.getFromPreference(mActivity, Constant.FB_EMAIL).length() > 0 && Utils.getFromPreference(mActivity, Constant.FB_NAME).length() > 0){

        }
    }
    private void initVaraibles() {
        mActivity = getActivity();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(mActivity)
                    .enableAutoManage(getActivity(), this)
                    .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                    .build();
        }
        callbackManager = CallbackManager.Factory.create();

    }
    private void initUI(View view) {
        etEmail = (EditText)view.findViewById(R.id.et_signin_email);
        etPassword = (EditText)view.findViewById(R.id.et_signin_password);

        btnLogin = (Button)view.findViewById(R.id.btn_signin);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkValue()) {
                    signin();
                }

//                startActivity(new Intent(mActivity, HomeActivity.class));
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

        ibFb = (ImageButton)view.findViewById(R.id.ib_signin_fb);
        ibFb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initFBLogin();
            }
        });
        ibGp = (ImageButton)view.findViewById(R.id.ib_signin_gp);
        ibGp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
                startActivityForResult(intent, GG_SIGN_IN_REQUEST);
            }
        });
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
        //Google signin RESULT
        if (requestCode == GG_SIGN_IN_REQUEST){
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSigninResult(result);
            return;
        }
    }
    ///google sign in
    private void handleSigninResult(GoogleSignInResult result){
        if (result.isSuccess()){
            GoogleSignInAccount account = result.getSignInAccount();
            Utils.saveToPreference(mActivity, Constant.FB_NAME, account.getDisplayName());
            Utils.saveToPreference(mActivity, Constant.FB_EMAIL, account.getEmail());
            Uri avatarUri = account.getPhotoUrl();
            if (avatarUri != null) {
                Utils.saveToPreference(mActivity, Constant.FB_EMAIL, avatarUri.toString());
            }


            socialSignin();
        }else{
            Toast.makeText(mActivity, "Google Signin failed.", Toast.LENGTH_SHORT).show();
        }
    }

    //////////////for fb login
    private void initFBLogin() {
        //////////////////////////////////FACEBOOK LOGIN==start
        FacebookSdk.sdkInitialize(mActivity);
        //get current token

        List<String> permissionNeeds= Arrays.asList("public_profile", "user_birthday","user_about_me","email");
        LoginManager.getInstance().logInWithReadPermissions(this, permissionNeeds);
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(final LoginResult loginResult) {
                Profile.fetchProfileForCurrentAccessToken();
                fbAccessToken = loginResult.getAccessToken();
                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject jsonObject, GraphResponse graphResponse) {
                                try {
                                    ///get facebook profile data
                                    String email = jsonObject.getString("email");
                                    String name = jsonObject.getString("name");
//                                    String id = jsonObject.getString("id");
                                    String photo = jsonObject.getJSONObject("picture").getJSONObject("data").getString("url");
                                    String access_token = loginResult.getAccessToken().toString();

                                    AccessToken currentAccestoken = AccessToken.getCurrentAccessToken();
                                    access_token = currentAccestoken.getToken();

                                    // save fb profile data as preference
                                    Utils.saveToPreference(mActivity, Constant.FB_ACCESS_TOKEN, access_token);
                                    Utils.saveToPreference(mActivity, Constant.FB_NAME, name);
                                    Utils.saveToPreference(mActivity, Constant.FB_EMAIL, email);
                                    Utils.saveToPreference(mActivity, Constant.FB_PHOTO, photo);

                                    socialSignin();

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,email,gender,birthday,first_name,last_name,age_range,picture.type(normal)");
                request.setParameters(parameters);
                request.executeAsync();
            }
            @Override
            public void onCancel() {
//                AccessToken.setCurrentAccessToken(null);
                Utils.showToast(mActivity, "FB login cancel");
            }
            @Override
            public void onError(FacebookException e) {
                Utils.showToast(mActivity, e.toString());
//                AccessToken.setCurrentAccessToken(null);
            }
        });
    }

    //    socail sign in
    private void socialSignin(){
        String email = Utils.getFromPreference(mActivity, Constant.FB_EMAIL);
        String fullname = Utils.getFromPreference(mActivity, Constant.FB_NAME);

        Utils.showProgress(mActivity);

        Map<String, String> params = new HashMap<String, String>();
        params.put(Constant.DEVICE_TYPE, Constant.ANDROID);
        params.put(Constant.DEVICE_TOKEN, Utils.getFromPreference(mActivity, Constant.DEVICE_TOKEN));
        params.put(Constant.DEVICE_ID, Utils.getFromPreference(mActivity, Constant.DEVICE_ID));
        params.put("email", email);
        params.put("fullname", fullname);
        params.put("social_avatar", Utils.getFromPreference(mActivity, Constant.FB_PHOTO));

        CustomRequest signinRequest = new CustomRequest(Request.Method.POST, API.SINGIN_SOCIAL, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Utils.hideProgress();
                        try {
                            String status = response.getString("status");
                            if (status.equals("200")) {

                                JSONObject jsonObject = response.getJSONObject("data");

                                String user_id = jsonObject.getString("user_id");
                                String fullname = jsonObject.getString("fullname");
                                String email = jsonObject.getString("email");
                                String password = jsonObject.getString("password");
                                String black_password = jsonObject.getString("black_password");
                                String city = jsonObject.getString("city");
                                String country = jsonObject.getString("country");
                                String birthday = jsonObject.getString("birthday");
                                String gender = jsonObject.getString("gender");
                                String celebrity = jsonObject.getString("celebrity");
                                String about_me = jsonObject.getString("about_you");
                                String media_count = jsonObject.getString("post_count");
                                String friend_count = jsonObject.getString("friend_count");
                                String avatar = jsonObject.getString("avatar");
                                String header_photo_url = jsonObject.getString("header_photo");
                                String header_video_url = jsonObject.getString("header_video");


                                Utils.saveToPreference(mActivity, Constant.USER_ID, user_id);
                                Utils.saveToPreference(mActivity, Constant.EMAIL, email);
                                Utils.saveToPreference(mActivity, Constant.FULLNAME, fullname);
                                Utils.saveToPreference(mActivity, Constant.PASSWORD, password);
                                Utils.saveToPreference(mActivity, Constant.BLACK_PASSWORD, black_password);
                                Utils.saveToPreference(mActivity, Constant.CITY, city);
                                Utils.saveToPreference(mActivity, Constant.COUNTRY, country);
                                Utils.saveToPreference(mActivity, Constant.BIRTHDAY, birthday);
                                Utils.saveToPreference(mActivity, Constant.GENDER, gender);
                                Utils.saveToPreference(mActivity, Constant.CELEBRITY, celebrity);
                                Utils.saveToPreference(mActivity, Constant.ABOUT_ME, about_me);
                                Utils.saveToPreference(mActivity, Constant.MEDIA_COUNT, media_count);
                                Utils.saveToPreference(mActivity, Constant.FRIEND_COUNT, friend_count);
                                Utils.saveToPreference(mActivity, Constant.AVATAR, avatar);
                                Utils.saveToPreference(mActivity, Constant.HEADER_PHOTO, header_photo_url);
                                Utils.saveToPreference(mActivity, Constant.HEADER_VIDEO, header_video_url);

                                Intent intent = new Intent(mActivity, HomeActivity.class);
                                startActivity(intent);
                                getActivity().finish();

//                                login_to_layer(fullname);

                            } else  if (status.equals("401")) {
                                Utils.showOKDialog(mActivity, getResources().getString(R.string.email_unregistered));
                            } else if (status.equals("402")) {
                                Utils.showOKDialog(mActivity, getResources().getString(R.string.incorrect_password));
                            }else if (status.equals("403")) {
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
    private String email, password;

    private boolean checkValue() {
        email = etEmail.getText().toString();
        password = etPassword.getText().toString();

        if (email.length() == 0 ) {
            Utils.showOKDialog(mActivity, "Please input email");
            return false;
        } else if (!Utils.isEmailValid(email)) {
            Utils.showOKDialog(mActivity, "Please input correct email");
            return false;
        }
        else if (password.length() == 0) {
            Utils.showOKDialog(mActivity, "Please input password");
            return false;
        }
        return true;
    }
    ///Sign in
    private void signin() {
        Utils.showProgress(mActivity);

        Map<String, String> params = new HashMap<String, String>();
        params.put(Constant.DEVICE_TYPE, Constant.ANDROID);
        params.put(Constant.DEVICE_TOKEN, Utils.getFromPreference(mActivity, Constant.DEVICE_TOKEN));
        params.put("email", email);
        params.put("password", password);

        CustomRequest signinRequest = new CustomRequest(Request.Method.POST, API.SINGIN, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Utils.hideProgress();
                        try {
                            String status = response.getString("status");
                            if (status.equals("200")) {
                                JSONObject jsonObject = response.getJSONObject("data");

                                String user_id = jsonObject.getString("user_id");
                                String fullname = jsonObject.getString("fullname");
                                String email = jsonObject.getString("email");
                                String password = jsonObject.getString("password");
                                String black_password = jsonObject.getString("black_password");
                                String city = jsonObject.getString("city");
                                String country = jsonObject.getString("country");
                                String birthday = jsonObject.getString("birthday");
                                String gender = jsonObject.getString("gender");
                                String celebrity = jsonObject.getString("celebrity");
                                String about_me = jsonObject.getString("about_you");
                                String media_count = jsonObject.getString("post_count");
                                String friend_count = jsonObject.getString("friend_count");
                                String avatar = jsonObject.getString("avatar");
                                String header_photo_url = jsonObject.getString("header_photo");
                                String header_video_url = jsonObject.getString("header_video");

                                Utils.saveToPreference(mActivity, Constant.USER_ID, user_id);
                                Utils.saveToPreference(mActivity, Constant.EMAIL, email);
                                Utils.saveToPreference(mActivity, Constant.PASSWORD, password);
                                Utils.saveToPreference(mActivity, Constant.BLACK_PASSWORD, black_password);
                                Utils.saveToPreference(mActivity, Constant.FULLNAME, fullname);
                                Utils.saveToPreference(mActivity, Constant.CITY, city);
                                Utils.saveToPreference(mActivity, Constant.COUNTRY, country);
                                Utils.saveToPreference(mActivity, Constant.BIRTHDAY, birthday);
                                Utils.saveToPreference(mActivity, Constant.GENDER, gender);
                                Utils.saveToPreference(mActivity, Constant.CELEBRITY, celebrity);
                                Utils.saveToPreference(mActivity, Constant.ABOUT_ME, about_me);
                                Utils.saveToPreference(mActivity, Constant.MEDIA_COUNT, media_count);
                                Utils.saveToPreference(mActivity, Constant.FRIEND_COUNT, friend_count);
                                Utils.saveToPreference(mActivity, Constant.AVATAR, avatar);
                                Utils.saveToPreference(mActivity, Constant.HEADER_PHOTO, header_photo_url);
                                Utils.saveToPreference(mActivity, Constant.HEADER_VIDEO, header_video_url);

                                Intent intent = new Intent(mActivity, HomeActivity.class);
                                startActivity(intent);
                                getActivity().finish();
//                                login_to_layer(fullname);
                            } else  if (status.equals("401")) {
                                Utils.showOKDialog(mActivity, getResources().getString(R.string.email_unregistered));
                            } else if (status.equals("402")) {
                                Utils.showOKDialog(mActivity, getResources().getString(R.string.incorrect_password));
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

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

}
