package com.heyoe_chat.controller.fragments;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.CustomRequest;
import com.android.volley.toolbox.Volley;
import com.heyoe_chat.R;
import com.heyoe_chat.controller.HomeActivity;
import com.heyoe_chat.controller.ProfileActivity;
import com.heyoe_chat.model.API;
import com.heyoe_chat.model.Constant;
import com.heyoe_chat.model.UserModel;
import com.heyoe_chat.utilities.LocaleHelper;
import com.heyoe_chat.utilities.SelectDateFragment;
import com.heyoe_chat.utilities.UIUtility;
import com.heyoe_chat.utilities.Utils;
import com.layer.atlas.util.Util;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileInfoFragment extends Fragment implements View.OnClickListener{

    private ImageButton ibEditContact, ibEditAboutMe, ibEditPassword;
    private EditText etFullname, etCity, etEmail, etAboutMe, etOldPass, etNewPass, etConfirmPass;
    private TextView tvCountry, tvGender, tvBirthday, tvLanguage, tvPrivateStatus;
    private Switch aSwitchCelebrity;
    private LinearLayout llPassword;
    private RelativeLayout rlEmail, rlCelebrity, rlLanguage;

    private UserModel userModel;
    private Activity mActivity;

    private boolean editContactStatus, editPasswordStatus, editAboutMeStatus;

    public ProfileInfoFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the text_layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile_info, container, false);
        initVariables();
        initUI(view);
        return view;
    }
    private void initVariables() {
        mActivity  = getActivity();
        userModel  = ProfileActivity.userModel;

        editContactStatus  = false;
        editPasswordStatus = false;
        editAboutMeStatus  = false;
    }
    private void initUI(View view) {

        rlCelebrity = (RelativeLayout)view.findViewById(R.id.rl_profile_info_celebrity);
        rlEmail = (RelativeLayout)view.findViewById(R.id.rl_profile_info_email);
        rlLanguage = (RelativeLayout)view.findViewById(R.id.rl_info_language);

        if (userModel.getUser_id().equals(Utils.getFromPreference(mActivity, Constant.USER_ID))) {
//            rlCelebrity.setVisibility(View.VISIBLE);
            rlEmail.setVisibility(View.VISIBLE);
            rlLanguage.setVisibility(View.VISIBLE);
        }
        ibEditAboutMe  = (ImageButton)view.findViewById(R.id.ib_info_edit_about_me);
        ibEditContact  = (ImageButton)view.findViewById(R.id.ib_info_edit_contact);
        ibEditPassword = (ImageButton)view.findViewById(R.id.ib_info_edit_password);

        etFullname       = (EditText)view.findViewById(R.id.et_info_name);
        etCity           = (EditText)view.findViewById(R.id.et_info_city);
        etEmail          = (EditText)view.findViewById(R.id.et_info_email);
        etAboutMe        = (EditText)view.findViewById(R.id.et_info_about_me);
        etOldPass        = (EditText)view.findViewById(R.id.et_info_old_password);
        etNewPass        = (EditText)view.findViewById(R.id.et_info_new_password);
        etConfirmPass    = (EditText)view.findViewById(R.id.et_info_confirm_password);

        aSwitchCelebrity = (Switch)view.findViewById(R.id.switch_info_celebrity);

        tvBirthday       = (TextView)view.findViewById(R.id.et_info_birthday);
        tvGender         = (TextView)view.findViewById(R.id.et_info_gender);
        tvPrivateStatus  = (TextView)view.findViewById(R.id.et_private_status);
        tvCountry        = (TextView)view.findViewById(R.id.et_info_country);
        tvLanguage       = (TextView)view.findViewById(R.id.et_info_language);

        llPassword = (LinearLayout)view.findViewById(R.id.ll_info_password);

        if (userModel != null) {
            if (userModel.getUser_id().equals(Utils.getFromPreference(mActivity, Constant.USER_ID))) {
                ibEditAboutMe .setVisibility(View.VISIBLE);
                ibEditContact .setVisibility(View.VISIBLE);
                ibEditPassword.setVisibility(View.VISIBLE);

                llPassword.setVisibility(View.VISIBLE);

                ibEditAboutMe .setOnClickListener(this);
                ibEditContact .setOnClickListener(this);
                ibEditPassword.setOnClickListener(this);

                tvCountry       .setOnClickListener(this);
                tvLanguage      .setOnClickListener(this);
                tvBirthday      .setOnClickListener(this);
                tvGender        .setOnClickListener(this);
                tvPrivateStatus .setOnClickListener(this);

            }
            if (userModel.getUser_id().equals(Utils.getFromPreference(mActivity, Constant.USER_ID))) {
                etFullname.setText(Utils.getFromPreference(mActivity, Constant.FULLNAME));
                etCity    .setText(Utils.getFromPreference(mActivity, Constant.CITY));
                tvCountry .setText(Utils.getFromPreference(mActivity, Constant.COUNTRY));
                String countryCode = Utils.getFromPreference(mActivity, Constant.LANGUAGE_CODE);
                if (countryCode.equals("en")) {
                    tvLanguage.setText(getResources().getString(R.string.English));
                } else if (countryCode.equals("nl")) {
                    tvLanguage.setText(getResources().getString(R.string.Netherland));
                }else if (countryCode.equals("tr")) {
                    tvLanguage.setText(getResources().getString(R.string.Turkish));
                }else if (countryCode.equals("de")) {
                    tvLanguage.setText(getResources().getString(R.string.Germany));
                }
                etEmail   .setText(Utils.getFromPreference(mActivity, Constant.EMAIL));
                tvBirthday.setText(Utils.getFromPreference(mActivity, Constant.BIRTHDAY));
                String gender = Utils.getFromPreference(mActivity, Constant.GENDER);
                if (gender.length() > 0) {
                    if (gender.equals("male")) {
                        tvGender  .setText(getResources().getString(R.string.Male));
                    } else {
                        tvGender  .setText(getResources().getString(R.string.Female));

                    }
                }
                private_status = Utils.getIntFromPreference(mActivity, Constant.PRIVATE_STATUS);
                if (private_status == 0) {
                    tvPrivateStatus  .setText(getResources().getString(R.string.Public));
                } else if (private_status == 1){
                    tvPrivateStatus  .setText(getResources().getString(R.string.Friend_Only));
                } else if (private_status == 2){
                    tvPrivateStatus  .setText(getResources().getString(R.string.Private));

                }

                etAboutMe .setText(Utils.getFromPreference(mActivity, Constant.ABOUT_ME));

                if (Utils.getFromPreference(mActivity, Constant.CELEBRITY).equals("yes")) {
                    aSwitchCelebrity.setChecked(true);
                }
            } else {
                etFullname.setText(userModel.getFullname());
                etCity    .setText(userModel.getCity());
                tvCountry .setText(userModel.getCountry());
                etEmail   .setText(userModel.getEmail());
                tvBirthday.setText(userModel.getBirthday());

                if (userModel.getGender().equals("male")) {
                    tvGender  .setText(getResources().getString(R.string.Male));
                } else {
                    tvGender  .setText(getResources().getString(R.string.Female));

                }
                private_status = (userModel.getPrivate_status());
                if (private_status == 0) {
                    tvPrivateStatus  .setText(getResources().getString(R.string.Public));
                } else if (private_status == 1){
                    tvPrivateStatus  .setText(getResources().getString(R.string.Friend_Only));
                } else if (private_status == 2){
                    tvPrivateStatus  .setText(getResources().getString(R.string.Private));

                }

                etAboutMe .setText(userModel.getAbout_you());

                if (userModel.getCelebrity().equals("yes")) {
                    aSwitchCelebrity.setChecked(true);
                }
            }

        }

    }

    @Override
    public void onClick(View v) {
        if (!(v instanceof EditText)) {
            UIUtility.hideSoftKeyboard(mActivity);
        }
        if (v == ibEditContact) {
            editContact();
        }
        if (v == ibEditAboutMe) {
            editAboutme();
        }
        if (v == ibEditPassword) {
            editPassword();
        }
        if (v == tvCountry){
            showCountryDlg();
        }
        if (v == tvLanguage) {
            setLanguage();
        }
        if (v == tvGender){
            showGenderDlg();
        }
        if (v == tvBirthday){
            showDateDlg();
        }
        if (v == tvPrivateStatus){
            showPrivateStatusDlg();
        }
    }
    private void editContact() {
        if (editContactStatus) {
            UIUtility.hideSoftKeyboard(mActivity);
            editContactStatus = false;
            ibEditContact   .setImageDrawable(getResources().getDrawable(R.drawable.edit_pen));
            etFullname      .setEnabled(false);
            etCity          .setEnabled(false);
            tvCountry       .setEnabled(false);
            tvLanguage      .setEnabled(false);
            etEmail         .setEnabled(false);
            tvBirthday      .setEnabled(false);
            tvGender        .setEnabled(false);
            tvPrivateStatus .setEnabled(false);
            aSwitchCelebrity.setEnabled(false);
            if (checkContact()) {
                updateContact();
            }

        } else {
            editContactStatus = true;
            ibEditContact   .setImageDrawable(getResources().getDrawable(R.drawable.check_green));
            etFullname      .setEnabled(true);
            etCity          .setEnabled(true);
            tvCountry       .setEnabled(true);
            tvLanguage      .setEnabled(true);
//            etEmail         .setEnabled(true);
            tvBirthday      .setEnabled(true);
            tvGender        .setEnabled(true);
            tvPrivateStatus .setEnabled(true);
            aSwitchCelebrity.setEnabled(true);

            etFullname.requestFocus();
            UIUtility.showSoftKeyboard(mActivity, etFullname);
        }
    }
    private void editAboutme() {
        if (editAboutMeStatus) {
            UIUtility.hideSoftKeyboard(mActivity);
            editAboutMeStatus = false;
            ibEditAboutMe.setImageDrawable(getResources().getDrawable(R.drawable.edit_pen));
            etAboutMe    .setEnabled(false);
            if (checkAboutMe()) {
                updateAboutMe();
            }
        } else {
            editAboutMeStatus = true;
            ibEditAboutMe.setImageDrawable(getResources().getDrawable(R.drawable.check_green));
            etAboutMe    .setEnabled(true);
            etAboutMe    .requestFocus();
            UIUtility.showSoftKeyboard(mActivity, etAboutMe);
        }
    }
    private void editPassword() {
        if (editPasswordStatus) {
            UIUtility.hideSoftKeyboard(mActivity);
            editPasswordStatus = false;
            ibEditPassword.setImageDrawable(getResources().getDrawable(R.drawable.edit_pen));
            etOldPass     .setEnabled(false);
            etNewPass     .setEnabled(false);
            etConfirmPass .setEnabled(false);
            if (checkPassword()) {
                updatePassword();
            }
        } else {
            editPasswordStatus = true;
            ibEditPassword.setImageDrawable(getResources().getDrawable(R.drawable.check_green));
            etOldPass     .setEnabled(true);
            etNewPass     .setEnabled(true);
            etConfirmPass .setEnabled(true);

            etOldPass.requestFocus();
            UIUtility.showSoftKeyboard(mActivity, etOldPass);
        }
    }
    private String fullname, email, city, country, gender, birthday, celebrity;
    int private_status = 0;
    private boolean checkContact() {
        fullname  = etFullname.getText().toString().trim();
        email     = etEmail.getText().toString().trim();
        city      = etCity.getText().toString().trim();
        country   = tvCountry.getText().toString().trim();
        String gen = tvGender.getText().toString();
        if (gen.equals(getResources().getString(R.string.Male))) {
            gender = "male";
        } else {
            gender = "female";
        }
        birthday  = tvBirthday.getText().toString().trim();

        if (aSwitchCelebrity.isChecked()) {
            celebrity = "yes";
        } else {
            celebrity = "no";
        }
        if (fullname.length() == 0) {
            Utils.showOKDialog(mActivity, getResources().getString(R.string.input_fullname));
            return false;
        }
        if (email.length() == 0) {
            Utils.showOKDialog(mActivity, getResources().getString(R.string.input_email));
            return false;
        }
        if (!Utils.isEmailValid(email)) {
            Utils.showOKDialog(mActivity, getResources().getString(R.string.Invalid_email));
            return false;
        }

        return true;
    }
    private boolean checkAboutMe() {
        if (etAboutMe.getText().toString().trim().length() == 0) {
            return false;
        }
        return true;
    }
    private boolean checkPassword() {
        if (!etOldPass.getText().toString().trim().equals(Utils.getFromPreference(mActivity, Constant.PASSWORD))) {
            Utils.showOKDialog(mActivity, getResources().getString(R.string.incorrect_password));
            return false;
        }
        if (etNewPass.getText().toString().trim().length() == 0) {
            Utils.showOKDialog(mActivity, getResources().getString(R.string.input_new_password));
            return false;
        }
        if (!etNewPass.getText().toString().trim().equals(etConfirmPass.getText().toString().trim())) {
            Utils.showOKDialog(mActivity, getResources().getString(R.string.input_confirm_password_again));
            return false;
        }

        return true;
    }
    private void updateContact() {

        Map<String, String> params = new HashMap<String, String>();
        params.put(Constant.DEVICE_TYPE, Constant.ANDROID);
        params.put(Constant.DEVICE_TOKEN, Utils.getFromPreference(mActivity, Constant.DEVICE_TOKEN));
        params.put("my_id", Utils.getFromPreference(mActivity, Constant.USER_ID));
        params.put("fullname", fullname);
        params.put("city", city);
        params.put("country", country);
        params.put("email", email);
        params.put("birthday", birthday);
        params.put("gender", gender);
        params.put("private_status", String.valueOf(private_status));
        params.put("celebrity", celebrity);

        CustomRequest signinRequest = new CustomRequest(Request.Method.POST, API.EDIT_CONTACT, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String status = response.getString("status");
                            if (status.equals("200")) {
                                Utils.saveToPreference(mActivity, Constant.FULLNAME, fullname);
                                Utils.saveToPreference(mActivity, Constant.CITY, city);
                                Utils.saveToPreference(mActivity, Constant.COUNTRY, country);
                                Utils.saveToPreference(mActivity, Constant.EMAIL, email);
                                Utils.saveToPreference(mActivity, Constant.BIRTHDAY, birthday);
                                Utils.saveToPreference(mActivity, Constant.GENDER, gender);
                                Utils.saveToPreference(mActivity, Constant.CELEBRITY, celebrity);

                                ProfileActivity.userModel.setFullname(fullname);
                                ProfileActivity.userModel.setCity(city);
                                ProfileActivity.userModel.setCountry(country);
                                ProfileActivity.userModel.setEmail(email);
                                ProfileActivity.userModel.setBirthday(birthday);
                                ProfileActivity.userModel.setGender(gender);
                                ProfileActivity.userModel.setCelebrity(celebrity);


                            } else  if (status.equals("400")) {
                                Utils.showOKDialog(mActivity, getResources().getString(R.string.access_denied));
                            } else if (status.equals("402")) {
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
    private void updateAboutMe() {
        Map<String, String> params = new HashMap<String, String>();
        params.put(Constant.DEVICE_TYPE, Constant.ANDROID);
        params.put(Constant.DEVICE_TOKEN, Utils.getFromPreference(mActivity, Constant.DEVICE_TOKEN));
        params.put("my_id", Utils.getFromPreference(mActivity, Constant.USER_ID));
        params.put("about_me", etAboutMe.getText().toString().trim());

        CustomRequest signinRequest = new CustomRequest(Request.Method.POST, API.EDIT_ABOUTME, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String status = response.getString("status");
                            if (status.equals("200")) {
                                ProfileActivity.userModel.setAbout_you(etAboutMe.getText().toString().trim());
                                Utils.saveToPreference(mActivity, Constant.ABOUT_ME, etAboutMe.getText().toString().trim());
                            } else  if (status.equals("400")) {
                                Utils.showOKDialog(mActivity, getResources().getString(R.string.access_denied));
                            } else if (status.equals("402")) {

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
    private void updatePassword() {
        Map<String, String> params = new HashMap<String, String>();
        params.put(Constant.DEVICE_TYPE, Constant.ANDROID);
        params.put(Constant.DEVICE_TOKEN, Utils.getFromPreference(mActivity, Constant.DEVICE_TOKEN));
        params.put("my_id", Utils.getFromPreference(mActivity, Constant.USER_ID));
        params.put("new_password", etNewPass.getText().toString());

        CustomRequest signinRequest = new CustomRequest(Request.Method.POST, API.CHANGE_PASSWORD, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String status = response.getString("status");
                            if (status.equals("200")) {
                                ProfileActivity.userModel.setPassword(etNewPass.getText().toString());
                                Utils.saveToPreference(mActivity, Constant.PASSWORD, etNewPass.getText().toString());
                            } else  if (status.equals("400")) {
                                Utils.showOKDialog(mActivity, getResources().getString(R.string.access_denied));
                            } else if (status.equals("402")) {
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





    private void showDateDlg() {
        SelectDateFragment selectDateFragment = new SelectDateFragment(tvBirthday);
        selectDateFragment.show(getFragmentManager(), "Birthday");
    }
    private void showGenderDlg() {
        final String[] strGenders = getResources().getStringArray(R.array.gender);
        new AlertDialog.Builder(mActivity)
                .setSingleChoiceItems(strGenders, 0, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                                     /* User clicked on a radio button do some stuff */

                        tvGender.setText(strGenders[whichButton]);
                        dialog.dismiss();
                    }
                }).show();
    }
    private void showPrivateStatusDlg() {
        final String[] arrPrivateStatus = getResources().getStringArray(R.array.private_status);
        new AlertDialog.Builder(mActivity)
                .setSingleChoiceItems(arrPrivateStatus, 0, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                                     /* User clicked on a radio button do some stuff */
                        private_status = whichButton;
                        Utils.saveIntToPreference(mActivity, Constant.PRIVATE_STATUS, private_status);
                        tvPrivateStatus.setText(arrPrivateStatus[whichButton]);
                        dialog.dismiss();
                    }
                }).show();
    }
    private void showCountryDlg() {
        final String[] strCountry = getResources().getStringArray(R.array.country);
        ArrayAdapter arrayAdapter = new ArrayAdapter(mActivity, android.R.layout.simple_list_item_1, strCountry);
        new AlertDialog.Builder(mActivity)
                .setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        tvCountry.setText(strCountry[which]);
                        dialog.dismiss();
                    }
                })
                .show();
    }
    private void setLanguage() {

        String[] languages = getResources().getStringArray(R.array.languages);
        final List<String> arrCountries = Arrays.asList(languages);

        ArrayAdapter arrayAdapter = new ArrayAdapter(mActivity, android.R.layout.simple_list_item_1, arrCountries);


        Dialog dialog = new android.app.AlertDialog.Builder(mActivity)
                .setCancelable(true)
                .setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (arrCountries.get(which).equals(getResources().getString(R.string.English))) {
                            Utils.saveToPreference(mActivity, Constant.LANGUAGE_CODE, "en");
                            tvLanguage.setText(getResources().getString(R.string.English));
                            LocaleHelper.setLocale(mActivity, "en");
                        } else if (arrCountries.get(which).equals(getResources().getString(R.string.Netherland))) {
                            Utils.saveToPreference(mActivity, Constant.LANGUAGE_CODE, "nl");
                            tvLanguage.setText(getResources().getString(R.string.Netherland));
                            LocaleHelper.setLocale(mActivity, "nl");
                        } else if (arrCountries.get(which).equals(getResources().getString(R.string.Turkish))) {
                            Utils.saveToPreference(mActivity, Constant.LANGUAGE_CODE, "tr");
                            tvLanguage.setText(getResources().getString(R.string.Turkish));
                            LocaleHelper.setLocale(mActivity, "tr");
                        } else if (arrCountries.get(which).equals(getResources().getString(R.string.Germany))) {
                            LocaleHelper.setLocale(mActivity, "de");
                            Utils.saveToPreference(mActivity, Constant.LANGUAGE_CODE, "de");
                            tvLanguage.setText(getResources().getString(R.string.Germany));
                        }
//                        startActivity(new Intent(mActivity, HomeActivity.class));
                        mActivity.setResult(50);
                        mActivity.finish();
                        dialog.cancel();

                    }
                })

                .setNegativeButton(getResources().getString(R.string.dlg_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();

                    }
                })
                .show();
//        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }
}
