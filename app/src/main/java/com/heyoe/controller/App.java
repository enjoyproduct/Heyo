package com.heyoe.controller;

import android.app.Application;

import com.heyoe.model.Constant;
import com.layer.sdk.LayerClient;
import com.quickblox.core.QBSettings;

/**
 * App provides static access to a LayerClient and other Atlas and Messenger context, including
 * AuthenticationProvider, ParticipantProvider, Participant, and Picasso.
 *
 * App.Flavor allows build variants to target different environments, such as the Atlas Demo and the
 * open source Rails Identity Provider.  Switch flavors with the Android Studio `Build Variant` tab.
 * When using a flavor besides the Atlas Demo you must manually set your Layer App ID and GCM Sender
 * ID in that flavor's Flavor.java.
 *
 * @see LayerClient
 */
public class App extends Application {

    private static Application sInstance;
    @Override
    public void onCreate() {
        super.onCreate();

        // Allow the LayerClient to track app state
//        LayerClient.applicationCreated(this);
        // Create a LayerClient ready to receive push notifications through GCM
        sInstance = this;
    }
    public static Application getInstance() {
        return sInstance;
    }
}
