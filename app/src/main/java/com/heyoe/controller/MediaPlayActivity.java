package com.heyoe.controller;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.heyoe.R;
import com.heyoe.utilities.FileUtility;
import com.heyoe.utilities.VideoPlay;

public class MediaPlayActivity extends YouTubeBaseActivity implements YouTubePlayer.OnInitializedListener {

    private static final int RECOVERY_DIALOG_REQUEST = 1;

    private YouTubePlayerView youTubeView;
    private VideoView videoView;
    String url;
    String type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_media_play);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        ImageButton ibBack = (ImageButton)toolbar.findViewById(R.id.ib_back);
        ibBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        url = getIntent().getStringExtra("url");
        type = getIntent().getStringExtra("type");
        if (type.equals("youtube")) {
            initYoutube();
        } else {
            initVideoview();
        }


    }
    private void initVideoview() {
        videoView = (VideoView)findViewById(R.id.videoview);
        videoView.setVisibility(View.VISIBLE);
        VideoPlay videoPlay = new VideoPlay(this, videoView, url);
        videoPlay.playVideo();
    }




    private void initYoutube() {
        youTubeView = (YouTubePlayerView) findViewById(R.id.youtube_view);
        youTubeView.setVisibility(View.VISIBLE);
        // Initializing video player with developer key
        youTubeView.initialize(getResources().getString(R.string.google_android_key), this);
    }
    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean wasRestored) {
        if (!wasRestored) {

            // loadVideo() will auto play video
            // Use cueVideo() method, if you don't want to play it automatically
            youTubePlayer.loadVideo(FileUtility.getFilenameFromPath(url));

            // Hiding player controls
            youTubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.DEFAULT);

        }

    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
        if (youTubeInitializationResult.isUserRecoverableError()) {
            youTubeInitializationResult.getErrorDialog(this, RECOVERY_DIALOG_REQUEST).show();
        } else {
            String errorMessage = String.format(
                    getString(R.string.Youtube_play_error), youTubeInitializationResult.toString());
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RECOVERY_DIALOG_REQUEST) {
            // Retry initialization if user performed a recovery action
            getYouTubePlayerProvider().initialize(getResources().getString(R.string.google_android_key), this);
        }
    }

    private YouTubePlayer.Provider getYouTubePlayerProvider() {
        return (YouTubePlayerView) findViewById(R.id.youtube_view);
    }

}
