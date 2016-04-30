package com.heyoe.controller.fragments;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.error.AuthFailureError;
import com.android.volley.error.NetworkError;
import com.android.volley.error.NoConnectionError;
import com.android.volley.error.ParseError;
import com.android.volley.error.ServerError;
import com.android.volley.error.TimeoutError;
import com.android.volley.error.VolleyError;
import com.android.volley.request.CustomMultipartRequest;
import com.android.volley.request.CustomRequest;
import com.android.volley.toolbox.Volley;
import com.heyoe.R;
import com.heyoe.controller.HomeActivity;
import com.heyoe.controller.SignActivity;
import com.heyoe.model.API;
import com.heyoe.model.Constant;
import com.heyoe.model.UserModel;
import com.heyoe.utilities.BitmapUtility;
import com.heyoe.utilities.FileUtility;
import com.heyoe.utilities.Utils;
import com.heyoe.utilities.camera.AlbumStorageDirFactory;
import com.heyoe.utilities.camera.BaseAlbumDirFactory;
import com.heyoe.utilities.camera.FroyoAlbumDirFactory;
import com.heyoe.widget.MyCircularImageView;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class SignupFragment extends Fragment {

    private static final int from_gallery = 1;
    private static final int from_camera = 2;

    private static final String JPEG_FILE_PREFIX = "Heyoe__";
    private static final String JPEG_FILE_SUFFIX = ".jpg";

    private ImageView ivDefaultAvatar,  ivBanner;
    private MyCircularImageView ivWhiteCircle, civAvatar;
    private EditText etFirstname, etLastname, etEmail, etPassword, etConfirmPassword;
    private Button btnSignup;
    private TextView tvTerms, tvPolicy;

    private Activity mActivity;
    private UserModel userModel;
    private String avatarPath;
    private AlbumStorageDirFactory mAlbumStorageDirFactory = null;
    
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

        userModel = new UserModel();
        avatarPath = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            mAlbumStorageDirFactory = new FroyoAlbumDirFactory();
        } else {
            mAlbumStorageDirFactory = new BaseAlbumDirFactory();
        }
    }

    private void initUI(View view) {

        ivWhiteCircle = (MyCircularImageView)view.findViewById(R.id.iv_signup_white_circle);
        ivBanner = (ImageView)view.findViewById(R.id.iv_signup_banner);

        civAvatar = (MyCircularImageView)view.findViewById(R.id.civ_signup_avatar);
        civAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChooseDialog(mActivity, getResources().getString(R.string.choose_avatar));
            }
        });

        etFirstname = (EditText)view.findViewById(R.id.et_signup_firstname);
        etLastname = (EditText)view.findViewById(R.id.et_signup_lastname);
        etEmail = (EditText)view.findViewById(R.id.et_signup_email);
        etPassword = (EditText)view.findViewById(R.id.et_signup_password);
        etConfirmPassword = (EditText)view.findViewById(R.id.et_signup_confirm_pass);

        btnSignup = (Button)view.findViewById(R.id.btn_signup);
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkValue()) {
                    signup();
                }
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

    private boolean checkValue() {

        String fistname = etFirstname.getText().toString().trim();
        String lastname = etLastname.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (fistname.length() == 0 || lastname.length() == 0) {
            Utils.showOKDialog(mActivity, getResources().getString(R.string.Please_input_name));
            return false;
        }
        if (email.length() == 0 ) {
            Utils.showOKDialog(mActivity, getResources().getString(R.string.input_email));
            return false;
        }
        if (!Utils.isEmailValid(email)) {
            Utils.showOKDialog(mActivity, getResources().getString(R.string.Invalid_email));
            return false;
        }
        if (password.length() == 0 ) {
            Utils.showOKDialog(mActivity, getResources().getString(R.string.Input_password));
            return false;
        }

        if (!password.equals(confirmPassword) ) {
            Utils.showOKDialog(mActivity, getResources().getString(R.string.confirm_password));
            return false;
        }
        userModel.setFullname(fistname + " " + lastname);
        userModel.setEmail(email);
        userModel.setPassword(password);
        return true;
    }

    ///customer Sign up
    private void signup() {

        Utils.showProgress(mActivity);
        CustomMultipartRequest customMultipartRequest = new CustomMultipartRequest(API.SINGUP,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Utils.hideProgress();
                        try {
                            String status = response.getString("status");
                            if (status.equals("200")) {
                                signin();
                            } else  if (status.equals("400")) {
                                Utils.showOKDialog(mActivity, getResources().getString(R.string.post_failed));
                            }else  if (status.equals("401")) {
                                Utils.showOKDialog(mActivity, getResources().getString(R.string.email_registerd));
                            }else  if (status.equals("402")) {
                                Utils.showOKDialog(mActivity, getResources().getString(R.string.email_registerd));
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
                        if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                            Toast.makeText(mActivity, "TimeoutError", Toast.LENGTH_LONG).show();
                        } else if (error instanceof AuthFailureError) {
                            //TODO
                            Toast.makeText(mActivity, "AuthFailureError", Toast.LENGTH_LONG).show();
                        } else if (error instanceof ServerError) {
                            //TODO
                            Toast.makeText(mActivity, "ServerError", Toast.LENGTH_LONG).show();
                        } else if (error instanceof NetworkError) {
                            //TODO
                            Toast.makeText(mActivity, "NetworkError", Toast.LENGTH_LONG).show();
                        } else if (error instanceof ParseError) {
                            //TODO
                            Toast.makeText(mActivity, "ParseError", Toast.LENGTH_LONG).show();
                        } else {
                            //TODO
                            Toast.makeText(mActivity, "UnknownError", Toast.LENGTH_LONG).show();
                        }
                    }
                });
        customMultipartRequest
                .addStringPart(Constant.DEVICE_TYPE, Constant.ANDROID)
                .addStringPart(Constant.DEVICE_TOKEN, Utils.getFromPreference(mActivity, Constant.DEVICE_TOKEN))
                .addStringPart(Constant.DEVICE_ID, Utils.getFromPreference(mActivity, Constant.DEVICE_ID))
                .addStringPart(Constant.FULLNAME, userModel.getFullname())
                .addStringPart(Constant.EMAIL, userModel.getEmail())
                .addStringPart(Constant.PASSWORD, userModel.getPassword());

        if (avatarPath.length() > 0) {
            customMultipartRequest
                    .addStringPart(Constant.ADD_AVATAR, "yes")
                    .addImagePart(Constant.AVATAR, avatarPath);
        } else {
            customMultipartRequest.addStringPart(Constant.ADD_AVATAR, "no");
        }

        RequestQueue requestQueue = Volley.newRequestQueue(mActivity);
        requestQueue.add(customMultipartRequest);

    }
    ///Sign in
    private void signin() {

//        Utils.showProgress(mActivity);

        Map<String, String> params = new HashMap<String, String>();
        params.put(Constant.DEVICE_TYPE, Constant.ANDROID);
        params.put(Constant.DEVICE_TOKEN, Utils.getFromPreference(mActivity, Constant.DEVICE_TOKEN));
        params.put(Constant.EMAIL, userModel.getEmail());
        params.put(Constant.PASSWORD, userModel.getPassword());

        CustomRequest signinRequest = new CustomRequest(Request.Method.POST, API.SINGIN, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
//                        Utils.hideProgress();
                        try {
                            String status = response.getString("status");
                            if (status.equals("200")) {
                                JSONObject jsonObject = response.getJSONObject("data");

                                String user_id = jsonObject.getString("user_id");
                                String fullname = jsonObject.getString("fullname");
                                String email = jsonObject.getString("email");
                                String password = jsonObject.getString("password");
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
                                Utils.saveToPreference(mActivity, Constant.PASSWORD, userModel.getPassword());
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


                                startActivity(new Intent(mActivity, HomeActivity.class));
                                getActivity().finish();
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case from_gallery:
                if (resultCode == Activity.RESULT_OK) {
                    if (data != null) {
                        Uri selectedImage = data.getData();
                        String[] filePathColumn = {MediaStore.Images.Media.DATA};

                        Cursor cursor = mActivity.getContentResolver().query(
                                selectedImage, filePathColumn, null, null, null);
                        cursor.moveToFirst();

                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                        avatarPath = cursor.getString(columnIndex);
                        cursor.close();

//
//                    //convert bitmap to drawable
//                    Drawable d = Drawable.createFromPath(avatarPath);
////                    ImageView ivUser = (ImageView)findViewById(R.id.iv_register_user);
//                    Drawable drawable = new BitmapDrawable(getResources(), BitmapUtility.adjustBitmap(avatarPath));

                        Bitmap bitmap = BitmapUtility.adjustBitmap(avatarPath);
                        FileUtility.deleteFile(avatarPath);
                        avatarPath = BitmapUtility.saveBitmap(bitmap, Constant.MEDIA_PATH + "heyoe", FileUtility.getFilenameFromPath(avatarPath));

                        civAvatar.setImageBitmap(bitmap);
                    }
                }
                break;

           

            case from_camera: {

                if (resultCode == Activity.RESULT_OK) {
                    handleBigCameraPhoto();
                }
                break;
            }
        }
    }

    ///photo choose dialog
    public void showChooseDialog(Context context, String message){

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(Constant.INDECATOR);
        builder.setMessage(message);
        builder.setCancelable(true);
        builder.setPositiveButton("Camera",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dispatchTakePictureIntent();
                        dialog.cancel();
                    }
                });
        builder.setNegativeButton("Gallery",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        takePictureFromGallery();
                        dialog.cancel();
                    }
                });
        builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int arg1) {
                dialog.cancel();

            }
        });
//        dialog.setCancelable(true);
//        dialog.setCanceledOnTouchOutside(false);
        AlertDialog alert = builder.create();
        alert.show();
    }
    //////////////////take a picture from gallery
    private void takePictureFromGallery()
    {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, from_gallery);
    }
    /////////////capture photo
    public void dispatchTakePictureIntent() {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File f = null;
        try {
            f = setUpPhotoFile();
            avatarPath = f.getAbsolutePath();
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
        } catch (IOException e) {
            e.printStackTrace();
            f = null;
            avatarPath = "";
        }
        startActivityForResult(takePictureIntent, from_camera);
    }
    private File setUpPhotoFile() throws IOException {

        File f = createImageFile();
        avatarPath = f.getAbsolutePath();
        return f;
    }
    private File createImageFile() throws IOException {

        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_";
        File albumF = getAlbumDir();
        File imageF = File.createTempFile(imageFileName, JPEG_FILE_SUFFIX, albumF);
        return imageF;
    }
    private File getAlbumDir() {

        File storageDir = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            storageDir = mAlbumStorageDirFactory.getAlbumStorageDir("AllyTours");
            if (storageDir != null) {
                if (! storageDir.mkdirs()) {
                    if (! storageDir.exists()){
                        Log.d("CameraSample", "failed to create directory");
                        return null;
                    }
                }
            }
        } else {
            Log.v(getString(R.string.app_name), "External storage is not mounted READ/WRITE.");
        }
        return storageDir;
    }

    ///process result of captured photo
    private void handleBigCameraPhoto() {

        if (avatarPath != null) {
            setPic();
//            galleryAddPic();
        }
    }

    private void setPic() {

		/* There isn't enough memory to open up more than a couple camera photos */
		/* So pre-scale the target bitmap into which the file is decoded */

		/* Get the size of the ImageView */
        int targetW = civAvatar.getWidth();
        int targetH = civAvatar.getHeight();

		/* Get the size of the image */
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(avatarPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

		/* Figure out which way needs to be reduced less */
        int scaleFactor = 1;
        if ((targetW > 0) || (targetH > 0)) {
            scaleFactor = Math.min(photoW/targetW, photoH/targetH);
        }

		/* Set bitmap options to scale the image decode target */
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapUtility.adjustBitmapForAvatar(avatarPath);
        civAvatar.setImageBitmap(bitmap);

//        FileUtility.deleteFile(avatarPath);
        avatarPath = BitmapUtility.saveBitmap(bitmap, Constant.MEDIA_PATH + "heyoe", FileUtility.getFilenameFromPath(avatarPath));


    }

    private void galleryAddPic() {

        Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
        File f = new File(avatarPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        mActivity.sendBroadcast(mediaScanIntent);
    }
}
