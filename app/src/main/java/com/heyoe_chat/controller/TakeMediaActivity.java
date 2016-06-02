package com.heyoe_chat.controller;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import com.heyoe_chat.R;

public class TakeMediaActivity extends AppCompatActivity {

    private static final int take_photo_from_gallery = 1;
    private static final int take_photo_from_camera = 2;
    private static final int take_video_from_gallery = 3;
    private static final int take_video_from_camera = 4;

    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_media);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        imageView = (ImageView)findViewById(R.id.image);

        int mediaType = getIntent().getIntExtra("mediaType", 0);

        switch (mediaType) {
            case 0:
                takePhotoFromGallery();
                break;
            case 1:
                takePhotoByCamera();
                break;
            case 2:
                takeVideoFromGallery();
                break;
            case 3:
                takeVideoByCamera();
                break;
        }
    }
    private void takePhotoFromGallery() {
        // TODO Auto-generated method stub
        Intent intent = new Intent();
        // call android default gallery
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        // ******** code for crop image
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 0);
        intent.putExtra("aspectY", 0);
        intent.putExtra("outputX", 250);
        intent.putExtra("outputY", 250);

        try {

            intent.putExtra("return-data", true);
            startActivityForResult(Intent.createChooser(intent,
                    "Complete action using"), take_photo_from_gallery);

        } catch (ActivityNotFoundException e) {
            // Do nothing for now
        }
    }
    private void takePhotoByCamera() {
        // call android default camera
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        intent.putExtra(MediaStore.EXTRA_OUTPUT,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString());
        // ******** code for crop image
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 0);
        intent.putExtra("aspectY", 0);
        intent.putExtra("outputX", 250);
        intent.putExtra("outputY", 250);

        try {

            intent.putExtra("return-data", true);
            startActivityForResult(intent, take_photo_from_camera);

        } catch (ActivityNotFoundException e) {
            // Do nothing for now
        }


    }
    private void takeVideoFromGallery() {

    }
    private void takeVideoByCamera() {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == take_photo_from_camera) {
            Bundle extras = data.getExtras();
            if (extras != null) {
                Bitmap photo = extras.getParcelable("data");
                imageView.setImageBitmap(photo);

            }
        }

        if (requestCode == take_photo_from_gallery) {
            Bundle extras2 = data.getExtras();
            if (extras2 != null) {
                Bitmap photo = extras2.getParcelable("data");
                imageView.setImageBitmap(photo);

            }
        }

    }


}
