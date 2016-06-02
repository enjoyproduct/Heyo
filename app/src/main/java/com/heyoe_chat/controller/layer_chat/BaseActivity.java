package com.heyoe_chat.controller.layer_chat;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.heyoe_chat.controller.App;
import com.layer.atlas.provider.ParticipantProvider;
import com.layer.sdk.LayerClient;
import com.squareup.picasso.Picasso;

public abstract class BaseActivity extends AppCompatActivity {
    private final int mLayoutResId;
    private final boolean mMenuBackEnabled;

    public BaseActivity(int layoutResId, boolean menuBackEnabled) {
        mLayoutResId = layoutResId;
        mMenuBackEnabled = menuBackEnabled;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(mLayoutResId);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) return;
        if (mMenuBackEnabled) actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void setTitle(CharSequence title) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) {
            super.setTitle(title);
        } else {
            actionBar.setTitle(title);
        }
    }

    @Override
    public void setTitle(int titleId) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) {
            super.setTitle(titleId);
        } else {
            actionBar.setTitle(titleId);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        LayerClient client = App.getLayerClient();
        if (client == null) return;
        if (client.isAuthenticated()) {
            client.connect();
        } else {
            client.authenticate();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Menu "Navigate Up" acts like hardware back button
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected LayerClient getLayerClient() {
        return App.getLayerClient();
    }

    protected ParticipantProvider getParticipantProvider() {
        return App.getParticipantProvider();
    }

    protected Picasso getPicasso() {
        return App.getPicasso();
    }
}
