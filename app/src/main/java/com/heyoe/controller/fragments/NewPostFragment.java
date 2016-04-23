package com.heyoe.controller.fragments;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

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
import com.android.volley.toolbox.Volley;
import com.commonsware.cwac.richedit.RichEditText;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.heyoe.R;
import com.heyoe.controller.HomeActivity;
import com.heyoe.model.API;
import com.heyoe.model.Constant;
import com.heyoe.utilities.BitmapUtility;
import com.heyoe.utilities.Utils;
import com.heyoe.utilities.VideoPlay;
import com.heyoe.utilities.camera.AlbumStorageDirFactory;
import com.heyoe.utilities.camera.BaseAlbumDirFactory;
import com.heyoe.utilities.camera.FroyoAlbumDirFactory;
import com.heyoe.widget.MyCircularImageView;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 */
public class NewPostFragment extends Fragment implements GoogleApiClient.OnConnectionFailedListener{

    private static final int take_photo_from_gallery = 1;
    private static final int take_photo_from_camera = 2;
    private static final int take_video_from_gallery = 3;
    private static final int take_video_from_camera = 4;

    private static final String JPEG_FILE_PREFIX = "Heyoe_Compose_photo_";
    private static final String JPEG_FILE_SUFFIX = ".jpg";
    private static final String VIDEO_FILE_PREFIX = "Heyoe_Compose_video_";
    private static final String VIDEO_FILE_SUFFIX = ".mp4";

    private ImageButton ibTag, ibPhoto, ibVideo, ibCheckin;
    private Button btnPost;
    private MyCircularImageView myCircularImageView;
    private TextView tvFullname;
    private static RichEditText richEditor;
    private ImageView imageView;
    private VideoView videoView;
    private ImageButton ibPlay;
    private EditText etYotubeUrl;

    private Activity mActivity;
    private String photoPath, videoPath, thumbPath, youtubePath;
    private AlbumStorageDirFactory mAlbumStorageDirFactory = null;
    public NewPostFragment() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_new_post, container, false);
        initVariable();
        initUI(view);
        return view;
    }

    private void initVariable() {
        mActivity = getActivity();
        initMediaPath();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            mAlbumStorageDirFactory = new FroyoAlbumDirFactory();
        } else {
            mAlbumStorageDirFactory = new BaseAlbumDirFactory();
        }
    }
    private void initMediaPath() {
        photoPath = "";
        youtubePath = "";
        videoPath = "";
    }
    private void initUI(View view) {
        myCircularImageView = (MyCircularImageView)view.findViewById(R.id.civ_compose_avatar);
        tvFullname = (TextView)view.findViewById(R.id.tv_compose_fullname);

        etYotubeUrl = (EditText)view.findViewById(R.id.et_compose_youtube_url);
        etYotubeUrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String textToPaste = "";

                ClipboardManager clipboard = (ClipboardManager)mActivity.getSystemService(Context.CLIPBOARD_SERVICE);

                if (clipboard.hasPrimaryClip()) {
                    ClipData clip = clipboard.getPrimaryClip();

                    // if you need text data only, use:
                    if (clip.getDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN))
                        // WARNING: The item could cantain URI that points to the text data.
                        // In this case the getText() returns null and this code fails!
                        textToPaste = clip.getItemAt(0).getText().toString();

                    // or you may coerce the data to the text representation:
                    textToPaste = clip.getItemAt(0).coerceToText(mActivity).toString();
                }
                if (!TextUtils.isEmpty(textToPaste)) {
                    etYotubeUrl.setText(textToPaste);
                    initMediaPath();
                    youtubePath = etYotubeUrl.getText().toString();
                }
                else {
                    etYotubeUrl.setVisibility(View.GONE);
                }
            }
        });
        ibTag = (ImageButton)view.findViewById(R.id.ib_compose_tag);
        ibCheckin = (ImageButton)view.findViewById(R.id.ib_compose_checkin);
        ibPhoto = (ImageButton)view.findViewById(R.id.ib_compose_photo);
        ibVideo = (ImageButton)view.findViewById(R.id.ib_compose_video);
        ibPlay =  (ImageButton)view.findViewById(R.id.ib_compose_play);

        btnPost = (Button)view.findViewById(R.id.btn_compose_post);


        ibTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTagDialog();

            }
        });
        ibCheckin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCheckinClick();
            }
        });
        ibPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showPictureChooseDialog();
            }
        });
        ibVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showVideoChooseDialog();
            }
        });
        btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkValue()) {
                    postMedia();
                }
            }
        });
        ibPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (videoPath.length() > 0) {
                    videoView.setVisibility(View.VISIBLE);
//                    imageView.setVisibility(View.GONE);
//                    ibPlay.setVisibility(View.GONE);
                    VideoPlay videoPlay = new VideoPlay(mActivity, videoView, videoPath);
                    videoPlay.playVideo();
                }
            }
        });
        richEditor=(RichEditText)view.findViewById(R.id.editor);
        richEditor.enableActionModes(true);

        imageView = (ImageView)view.findViewById(R.id.iv_compose);
        videoView = (VideoView)view.findViewById(R.id.vv_compose);

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                videoView.setVisibility(View.GONE);
                ibPlay.setVisibility(View.VISIBLE);
            }
        });

    }
    String mediaType;
    String description;
    private boolean checkValue() {
        description = richEditor.getText().toString();
        if (photoPath.length() > 0) {
            mediaType = "post_photo";
            return true;
        }
        if (videoPath.length() > 0) {
            if (thumbPath.length() == 0) {
                Utils.showToast(mActivity, "Cannot get thumbnail");
                return false;
            }
            mediaType = "post_video";
            return true;
        }
        if (youtubePath.length() > 0) {
            mediaType = "youtube";
            return true;
        }
        Utils.showOKDialog(mActivity, getResources().getString(R.string.take_more_media));
        return false;
    }
    private void postMedia() {
        Utils.showProgress(mActivity);
        CustomMultipartRequest customMultipartRequest = new CustomMultipartRequest(API.NEW_POST,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Utils.hideProgress();
                        try {
                            String status = response.getString("status");
                            if (status.equals("200")) {
                                Utils.showToast(mActivity, getResources().getString(R.string.post_success));
                                HomeActivity.navigateTo(0);
                            } else  if (status.equals("400")) {
                                Utils.showOKDialog(mActivity, getResources().getString(R.string.access_denied));
                            }else  if (status.equals("401")) {
                                Utils.showOKDialog(mActivity, getResources().getString(R.string.post_failed));
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
                .addStringPart("my_id", Utils.getFromPreference(mActivity, Constant.USER_ID))
                .addStringPart("media_type", String.valueOf(mediaType))
                .addStringPart("description", description);

        if (mediaType.equals("post_video")) {
            customMultipartRequest.addVideoPart("post_video", videoPath);
            customMultipartRequest.addImagePart("thumb", thumbPath);

        } else if (mediaType.equals("post_photo")) {
            customMultipartRequest.addImagePart("post_photo", photoPath);

        } else if (mediaType.equals("youtube")) {
            customMultipartRequest.addStringPart("media", youtubePath);

        }


        RequestQueue requestQueue = Volley.newRequestQueue(mActivity);
        requestQueue.add(customMultipartRequest);

    }
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
    public static void inputTag(String strTag) {
        String str = "<b>" + strTag + "</b>";
        richEditor.append(Html.fromHtml(str));

    }

    public void showTagDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        LayoutInflater inflater = getActivity().getLayoutInflater();

        final View dialogView = inflater.inflate(R.layout.dlg_tag_selection, null);
//        AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView)dialogView.findViewById(R.id.actv_tag_selection);
//        ListView listView = (ListView)dialogView.findViewById(R.id.lv_tag_selection);
        final ArrayList<String> arrayList = makeSampleData();
        ArrayAdapter<String> searchAgentAdapter = new ArrayAdapter<String>(mActivity, android.R.layout.simple_list_item_1,arrayList);
//        TagSelectionAutoCompleteAdapter searchAgentAdapter = new TagSelectionAutoCompleteAdapter(mActivity, R.layout.item_tag_selection, arrayList);
//        listView.setAdapter(searchAgentAdapter);
//        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                inputTag(arrayList.get(position));
//
//            }
//        });
        builder.setCancelable(true);
        builder.setView(dialogView);
        builder.setAdapter(searchAgentAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                inputTag(arrayList.get(which));
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }
    //    for test
    private ArrayList<String> makeSampleData() {
        ArrayList<String> arrayList  = new ArrayList<>();
        for (int i = 0; i < 30; i ++) {
            String str = "@" +  "Test User - " + String.valueOf(i);
            arrayList.add(str);
        }
        return arrayList;
    }
//    get google place
    public void onCheckinClick() {
        // Construct an intent for the place picker
        try {
            PlacePicker.IntentBuilder intentBuilder = new PlacePicker.IntentBuilder();
            Intent intent = intentBuilder.build(mActivity);
            // Start the intent by requesting a result,
            // identified by a request code.
            startActivityForResult(intent, Constant.REQUEST_PLACE_PICKER);

        } catch (GooglePlayServicesRepairableException e) {
            GooglePlayServicesUtil.getErrorDialog(e.getConnectionStatusCode(), getActivity(), 0);
        } catch (GooglePlayServicesNotAvailableException e) {
            Toast.makeText(getActivity(), "Google Play Services is not available.",
                    Toast.LENGTH_LONG)
                    .show();
        }
    }


    // A place has been received; use requestCode to track the request.
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == Constant.REQUEST_PLACE_PICKER
                && resultCode == Activity.RESULT_OK) {

            // The user has selected a place. Extract the name and address.
            final Place place = PlacePicker.getPlace(data, mActivity);

            final CharSequence name = place.getName();
            final CharSequence address = place.getAddress();
            String attributions = PlacePicker.getAttributions(data);

            String str = "<b>" + name + "</b>";
            richEditor.append(Html.fromHtml(str));
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }

        switch (requestCode) {
            case take_photo_from_gallery:
                if (resultCode == Activity.RESULT_OK) {

                    etYotubeUrl.setVisibility(View.GONE);
                    imageView.setVisibility(View.VISIBLE);

                    Uri selectedImage = data.getData();
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};

                    Cursor cursor = mActivity.getContentResolver().query(
                            selectedImage, filePathColumn, null, null, null);
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    initMediaPath();
                    photoPath = cursor.getString(columnIndex);
                    cursor.close();


                    //convert bitmap to drawable
                    Drawable d = Drawable.createFromPath(photoPath);
//                    ImageView ivUser = (ImageView)findViewById(R.id.iv_register_user);
                    Drawable drawable = new BitmapDrawable(getResources(), BitmapUtility.adjustBitmap(photoPath));
                    imageView.setImageDrawable(drawable);

                }
                break;
            case take_photo_from_camera: {
                if (resultCode == Activity.RESULT_OK) {

                    etYotubeUrl.setVisibility(View.GONE);
                    imageView.setVisibility(View.VISIBLE);

                    handleBigCameraPhoto();
                }
                break;
            }
            case take_video_from_gallery:
                if (resultCode == Activity.RESULT_OK) {

                    etYotubeUrl.setVisibility(View.GONE);
                    imageView.setVisibility(View.VISIBLE);

                    ///////  1  ////////
                    Uri selectedVideoUri = data.getData();
                    String[] filePathColumn = {MediaStore.Video.VideoColumns.DATA};
//
//                    Cursor cursor = mActivity.getContentResolver().query(
//                            selectedVideoUri, filePathColumn, null, null, null);
//                    cursor.moveToFirst();
//
//                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
//                    videoPath = cursor.getString(columnIndex);
//                    cursor.close();

                    // MEDIA GALLERY
                    videoPath = getVideoPath(selectedVideoUri);
//                    videoView.setVideoURI(selectedVideoUri);
//                    videoView.setVisibility(View.VISIBLE);

                    //////////////////   2   //////////////
//                    Cursor cursorq = mActivity.getContentResolver().query(
//                            selectedVideoUri, filePathColumn, null, null, null);
//                    if (cursorq == null) {
//                        initMediaPath();
//                        videoPath = selectedVideoUri.getPath();
//                    } else {
//                        cursorq.moveToFirst();
//                        int idx = cursorq.getColumnIndex(MediaStore.Video.VideoColumns.DATA);
//                        initMediaPath();
//                        videoPath  = cursorq.getString(idx);
//                    }
                    if (videoPath != null) {
                        Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(videoPath, MediaStore.Video.Thumbnails.MINI_KIND);

                        imageView.setImageBitmap(thumbnail);
                        ibPlay.setVisibility(View.VISIBLE);
                        Bitmap cropBitmap = BitmapUtility.cropBitmapAnySize(thumbnail, thumbnail.getWidth(), thumbnail.getWidth());
                        thumbPath = BitmapUtility.saveBitmap(cropBitmap, Constant.MEDIA_PATH, "heyoe_thumb");

                    }

                }
                break;
            case take_video_from_camera:
                if (resultCode == Activity.RESULT_OK) {

                    etYotubeUrl.setVisibility(View.GONE);
                    imageView.setVisibility(View.VISIBLE);

                    if (videoPath.length() > 0) {
                        Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(videoPath, MediaStore.Video.Thumbnails.MINI_KIND);
                        imageView.setImageBitmap(thumbnail);
                        ibPlay.setVisibility(View.VISIBLE);
                        Bitmap cropBitmap = BitmapUtility.cropBitmapAnySize(thumbnail, thumbnail.getWidth(), thumbnail.getWidth());
                        thumbPath = BitmapUtility.saveBitmap(cropBitmap, Constant.MEDIA_PATH, "heyoe_thumb");

                    }
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    // User cancelled the video capture
                    Toast.makeText(mActivity, "User cancelled the video capture.",Toast.LENGTH_LONG).show();
                } else {
                    // Video capture failed, advise user
                    Toast.makeText(mActivity, "Video capture failed.",Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    public String getVideoPath(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = mActivity.managedQuery(uri, projection, null, null, null);
        if (cursor != null) {
            // HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
            // THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } else
            return null;
    }


    //    choose video
    private void showVideoChooseDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle(Constant.INDECATOR);
        builder.setMessage(getResources().getString(R.string.choose_video));
        builder.setCancelable(true);
        builder.setPositiveButton("Camera",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        etYotubeUrl.setVisibility(View.GONE);
                        imageView.setVisibility(View.VISIBLE);
                        captureVideoFromCamera();
                        dialog.cancel();
                    }
                });
        builder.setNegativeButton("gallery",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        etYotubeUrl.setVisibility(View.GONE);
                        imageView.setVisibility(View.VISIBLE);
                        takeVideoFromGallery();
                        dialog.cancel();
                    }
                });
        builder.setNeutralButton("Youtube", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int arg1) {
                takeYoutubeVideoUrl();
                dialog.cancel();

            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void takeYoutubeVideoUrl() {
        etYotubeUrl.setVisibility(View.VISIBLE);
        imageView.setVisibility(View.GONE);

        Intent videoClient = new Intent(Intent.ACTION_VIEW);
//        videoClient.setData(Uri.parse("http://m.youtube.com/watch?v="+videoId));
        videoClient.setData(Uri.parse("http://m.youtube.com/"));
        startActivityForResult(videoClient, 1234);
    }

    private void takeVideoFromGallery()
    {
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.putExtra("return-data", true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Video"), take_video_from_gallery);
    }





    private Uri fileUri;
    public static final int MEDIA_TYPE_VIDEO = 2;

    private void captureVideoFromCamera() {
// create new Intentwith with Standard Intent action that can be
        // sent to have the camera application capture an video and return it.
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

        // create a file to save the video
        fileUri = getOutputMediaFileUri(MEDIA_TYPE_VIDEO);
        initMediaPath();
        videoPath = fileUri.getPath();
        // set the image file name
        intent.putExtra(MediaStore.EXTRA_OUTPUT,fileUri);

        // set the video image quality to high
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);

        // start the Video Capture Intent
        startActivityForResult(intent, take_video_from_camera);
    }
    /** Create a file Uri for saving an image or video */
    private Uri getOutputMediaFileUri(int type){

        return Uri.fromFile(getOutputMediaFile(type));
    }

    /** Create a File for saving an image or video */
    private File getOutputMediaFile(int type){

        // Check that the SDCard is mounted
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "HeyoeVideo");


        // Create the storage directory(MyCameraVideo) if it does not exist
        if (! mediaStorageDir.exists()){

            if (! mediaStorageDir.mkdirs()){


                Toast.makeText(mActivity, "Failed to create directory HeyoeVideo.",
                        Toast.LENGTH_LONG).show();

                Log.d("MyCameraVideo", "Failed to create directory HeyoeVideo.");
                return null;
            }
        }


        // Create a media file name

        // For unique file name appending current timeStamp with file name
        java.util.Date date= new java.util.Date();
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(date.getTime());

        File mediaFile;

        if(type == MEDIA_TYPE_VIDEO) {

            // For unique video file name appending current timeStamp with file name
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    VIDEO_FILE_PREFIX + timeStamp + VIDEO_FILE_SUFFIX);

        } else {
            return null;
        }

        return mediaFile;
    }







    ///photo choose dialog
    public void showPictureChooseDialog(){

        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle(Constant.INDECATOR);
        builder.setMessage(getResources().getString(R.string.choose_avatar));
        builder.setCancelable(true);
        builder.setPositiveButton("Camera",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dispatchTakePictureIntent();
                        dialog.cancel();
                    }
                });
        builder.setNegativeButton("gallery",
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
        startActivityForResult(photoPickerIntent, take_photo_from_gallery);

//        Intent intent = new Intent(mActivity, TakeMediaActivity.class);
//        intent.putExtra("mediaType", 0);
//        startActivity(intent);
    }
    /////////////capture photo
    public void dispatchTakePictureIntent() {
//        Intent intent = new Intent(mActivity, TakeMediaActivity.class);
//        intent.putExtra("mediaType", 1);
//        startActivity(intent);

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File f = null;
        try {
            f = setUpPhotoFile();
            initMediaPath();
            photoPath = f.getAbsolutePath();
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
        } catch (IOException e) {
            e.printStackTrace();
            f = null;
            photoPath = "";
        }
        startActivityForResult(takePictureIntent, take_photo_from_camera);
    }
    private File setUpPhotoFile() throws IOException {

        File f = createImageFile();
        initMediaPath();
        photoPath = f.getAbsolutePath();
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

        if (photoPath != null) {
            setPic();
//            galleryAddPic();
        }
    }

    private void setPic() {

		/* There isn't enough memory to open up more than a couple camera photos */
		/* So pre-scale the target bitmap into which the file is decoded */

		/* Get the size of the ImageView */
        int targetW = imageView.getWidth();
        int targetH = imageView.getWidth();

		/* Get the size of the image */
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(photoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

		/* Figure out which way needs to be reduced less */
        int scaleFactor = 1;
        if ((targetW > 0) && (targetH > 0)) {
            scaleFactor = Math.min(photoW/targetW, photoH/targetH);
        }

		/* Set bitmap options to scale the image decode target */
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = null;

		/* Associate the Bitmap to the ImageView */
        /* Decode the JPEG file into a Bitmap */
        bitmap = BitmapFactory.decodeFile(photoPath, bmOptions);
        bitmap = BitmapUtility.rotateImage(bitmap, 90);
        imageView.setImageBitmap(bitmap);

    }



}
