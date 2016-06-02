package com.heyoe_chat.controller.layer_chat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.CustomRequest;
import com.android.volley.toolbox.Volley;
import com.heyoe_chat.R;
import com.heyoe_chat.controller.HomeActivity;
import com.heyoe_chat.controller.layer_chat.util.AuthenticationProvider;
import com.heyoe_chat.controller.layer_chat.util.Log;
import com.heyoe_chat.model.API;
import com.heyoe_chat.model.Constant;
import com.heyoe_chat.utilities.Utils;
import com.layer.sdk.LayerClient;
import com.layer.sdk.exceptions.LayerException;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static com.heyoe_chat.controller.layer_chat.util.Util.streamToString;


public class DemoAuthenticationProvider implements AuthenticationProvider<DemoAuthenticationProvider.Credentials> {
    private final SharedPreferences mPreferences;
    private Callback mCallback;
    private Context mContext;
    public DemoAuthenticationProvider(Context context) {
        mPreferences = context.getSharedPreferences(DemoAuthenticationProvider.class.getSimpleName(), Context.MODE_PRIVATE);
        mContext = context;
    }

    @Override
    public AuthenticationProvider<Credentials> setCredentials(Credentials credentials) {
        if (credentials == null) {
            mPreferences.edit().clear().commit();
            return this;
        }
        mPreferences.edit()
                .putString("appId", credentials.getLayerAppId())
                .putString("name", credentials.getUserName())
                .commit();
        return this;
    }

    @Override
    public boolean hasCredentials() {
        return mPreferences.contains("appId");
    }

    @Override
    public AuthenticationProvider<Credentials> setCallback(Callback callback) {
        mCallback = callback;
        return this;
    }

    @Override
    public void onAuthenticated(LayerClient layerClient, String userId) {
        if (Log.isLoggable(Log.VERBOSE)) Log.v("Authenticated with Layer, user ID: " + userId);
        layerClient.connect();
        if (mCallback != null) {
            mCallback.onSuccess(this, userId);
        }
    }

    @Override
    public void onDeauthenticated(LayerClient layerClient) {
        if (Log.isLoggable(Log.VERBOSE)) Log.v("Deauthenticated with Layer");
    }

    @Override
    public void onAuthenticationChallenge(LayerClient layerClient, String nonce) {
        if (Log.isLoggable(Log.VERBOSE)) Log.v("Received challenge: " + nonce);
        respondToChallenge(layerClient, nonce);
    }

    @Override
    public void onAuthenticationError(LayerClient layerClient, LayerException e) {
        String error = "Failed to authenticate with Layer: " + e.getMessage();
        if (Log.isLoggable(Log.ERROR)) Log.e(error, e);
        if (mCallback != null) {
            mCallback.onError(this, error);
        }
    }

    @Override
    public boolean routeLogin(LayerClient layerClient, String layerAppId, Activity from) {

        if ((layerClient != null) && layerClient.isAuthenticated()) {
            // The LayerClient is authenticated: no action required.
            if (Log.isLoggable(Log.VERBOSE)) Log.v("No authentication routing required");
            return false;
        }

        if ((layerClient != null) && hasCredentials()) {
            // With a LayerClient and cached provider credentials, we can resume.
            if (Log.isLoggable(Log.VERBOSE)) {
                Log.v("Routing to resume Activity using cached credentials");
            }
//            Intent intent = new Intent(from, ResumeActivity.class);
//            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//            intent.putExtra(ResumeActivity.EXTRA_LOGGED_IN_ACTIVITY_CLASS_NAME, from.getClass().getName());
//            intent.putExtra(ResumeActivity.EXTRA_LOGGED_OUT_ACTIVITY_CLASS_NAME, DemoLoginActivity.class.getName());
//            from.startActivity(intent);
            return true;
        }

        // We have a Layer App ID but no cached provider credentials: routing to Login required.
        if (Log.isLoggable(Log.VERBOSE)) Log.v("Routing to login Activity");
//        Intent intent = new Intent(from, HomeActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//        from.startActivity(intent);
        return true;
    }

    private void respondToChallenge(final LayerClient layerClient, String nonce) {
        Credentials credentials = new Credentials(mPreferences.getString("appId", null), mPreferences.getString("name", null));
        if (credentials.getUserName() == null || credentials.getLayerAppId() == null) {
            if (Log.isLoggable(Log.WARN)) {
                Log.w("No stored credentials to respond to challenge with");
            }
            return;
        }
//        Utils.showProgress(mContext);

        Map<String, String> params = new HashMap<String, String>();
        params.put(Constant.DEVICE_TYPE, Constant.ANDROID);
        params.put(Constant.DEVICE_TOKEN, Utils.getFromPreference(mContext, Constant.DEVICE_TOKEN));
        params.put("nonce", nonce);
        params.put("name", credentials.getUserName());

        CustomRequest signinRequest = new CustomRequest(Request.Method.POST, API.GET_LAYER_TOKEN, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
//                        Utils.hideProgress();
                        try {
                            String status = response.getString("status");
                            if (status.equals("200")) {
                                String layer_token = response.getString("layer_token");
                                layerClient.answerAuthenticationChallenge(layer_token);
                            } else  if (status.equals("400")) {
                                Utils.showOKDialog(mContext, mContext.getResources().getString(R.string.access_denied));
                            } else if (status.equals("401")) {
                                Utils.showOKDialog(mContext, "Layer auth failed");
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
                        Toast.makeText(mContext, error.toString(), Toast.LENGTH_LONG).show();
                    }
                });
        RequestQueue requestQueue = Volley.newRequestQueue(mContext);
        requestQueue.add(signinRequest);

//        try {
//            // Post request
//            String url = "https://layer-identity-provider.herokuapp.com/apps/" + credentials.getLayerAppId() + "/atlas_identities";
//            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
//            connection.setDoInput(true);
//            connection.setDoOutput(true);
//            connection.setRequestMethod("POST");
//            connection.setRequestProperty("Content-Type", "application/json");
//            connection.setRequestProperty("Accept", "application/json");
//            connection.setRequestProperty("X_LAYER_APP_ID", credentials.getLayerAppId());
//
//            // Credentials
//            JSONObject rootObject = new JSONObject()
//                    .put("nonce", nonce)
//                    .put("name", credentials.getUserName());
//
//            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
//
//            OutputStream os = connection.getOutputStream();
//            os.write(rootObject.toString().getBytes("UTF-8"));
//            os.close();
//
//            // Handle failure
//            int statusCode = connection.getResponseCode();
//            if (statusCode != HttpURLConnection.HTTP_OK && statusCode != HttpURLConnection.HTTP_CREATED) {
//                String error = String.format("Got status %d when requesting authentication for '%s' with nonce '%s' from '%s'",
//                        statusCode, credentials.getUserName(), nonce, url);
//                if (Log.isLoggable(Log.ERROR)) Log.e(error);
//                if (mCallback != null) mCallback.onError(this, error);
//                return;
//            }
//
//            // Parse response
//            InputStream in = new BufferedInputStream(connection.getInputStream());
//            String result = streamToString(in);
//            in.close();
//            connection.disconnect();
//            JSONObject json = new JSONObject(result);
//            if (json.has("error")) {
//                String error = json.getString("error");
//                if (Log.isLoggable(Log.ERROR)) Log.e(error);
//                if (mCallback != null) mCallback.onError(this, error);
//                return;
//            }
//
//            // Answer authentication challenge.
//            String identityToken = json.optString("identity_token", null);
//            if (Log.isLoggable(Log.VERBOSE)) Log.v("Got identity token: " + identityToken);
//            layerClient.answerAuthenticationChallenge(identityToken);
//        } catch (Exception e) {
//            String error = "Error when authenticating with provider: " + e.getMessage();
//            if (Log.isLoggable(Log.ERROR)) Log.e(error, e);
//            if (mCallback != null) mCallback.onError(this, error);
//        }
    }

    public static class Credentials {
        private final String mLayerAppId;
        private final String mUserName;

        public Credentials(Uri layerAppId, String userName) {
            this(layerAppId == null ? null : layerAppId.getLastPathSegment(), userName);
        }

        public Credentials(String layerAppId, String userName) {
            mLayerAppId = layerAppId == null ? null : (layerAppId.contains("/") ? layerAppId.substring(layerAppId.lastIndexOf("/") + 1) : layerAppId);
            mUserName = userName;
        }

        public String getUserName() {
            return mUserName;
        }

        public String getLayerAppId() {
            return mLayerAppId;
        }
    }
}

