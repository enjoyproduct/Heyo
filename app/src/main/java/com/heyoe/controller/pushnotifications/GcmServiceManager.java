package com.heyoe.controller.pushnotifications;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.heyoe.R;
import com.heyoe.controller.HomeActivity;
import com.heyoe.controller.UserListActivity;
import com.heyoe.model.Constant;
import com.heyoe.model.Global;
import com.heyoe.model.PushModel;
import com.heyoe.utilities.Utils;

public class GcmServiceManager {
    private static GcmServiceManager gcmServiceManager;

    private Activity activity;
    private String  message;
    private Bundle notificationData;
    public static String PROJECT_NUMBER = "535417836265";
    
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    public static GcmServiceManager getInstance() {
        if (gcmServiceManager == null) {
            gcmServiceManager = new GcmServiceManager();
        }
        

        return gcmServiceManager;
    }

    public void startGcmService(Activity activity) {
        this.activity = activity;

        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(activity, RegistrationIntentService.class);
            activity.startService(intent);
        }
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(activity);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(activity, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                activity.finish();
            }
            return false;
        }
        return true;
    }

    public Activity getActivity() {
        return activity;
    }

    public void setProjectNumber(String PROJECT_NUMBER) {
        this.PROJECT_NUMBER = PROJECT_NUMBER;
    }

    public String getProjectNumber() {
        return PROJECT_NUMBER;
    }

    public void sendNotification() {

        Intent intent = new Intent(activity, activity.getClass());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (notificationData.containsKey("received_invite")) {
            intent.putExtra("page_num", 6);
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(activity, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(activity)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentText(message)
                .setContentTitle(activity.getResources().getString(R.string.app_name))
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }

    private void increaseActivityCount() {
        if (getActivity() != null) {
            int count = Utils.getIntFromPreference(activity, Constant.ACTIVITY_COUNT);
            count ++;
            Utils.saveIntToPreference(activity, Constant.ACTIVITY_COUNT, count);

            Intent intentNewPush = new Intent("pushData");
            PushModel pushModel = new PushModel();
            pushModel.type = "increase_activity_count";
            intentNewPush.putExtra(Constant.PUSH_DATA, pushModel);

            LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intentNewPush);

//            HomeActivity.showActivityBadge();
        }

    }
    private void increaseMsgCount(String user_id) {
        if (getActivity() != null) {

            if (user_id.length() > 0) {
                Intent intentNewPush = new Intent("pushData");
                PushModel pushModel = new PushModel();
                pushModel.user_id = user_id;
                pushModel.type = "increase_message_count";
                intentNewPush.putExtra(Constant.PUSH_DATA, pushModel);
                LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intentNewPush);

//                HomeActivity.showMsgBadge(user_id);
            }

        }

    }
    String id = "";
    private String parseMessage(Bundle data){
        message = "";
        if(data.containsKey("liked_post")){
            message = data.getString("liked_post");
        }else if (data.containsKey("disliked_post")){
            message = data.getString("disliked_post");
        }else if (data.containsKey("commented_post")){
            message = data.getString("commented_post");
        }else if (data.containsKey("shared_post")){
            message = data.getString("shared_post");
        }else if (data.containsKey("received_invite")){
            message = data.getString("received_invite");
        }else if (data.containsKey("accepted_invite")){
            message = data.getString("accepted_invite");
        }else if (data.containsKey("rejected_invite")){
            message = data.getString("rejected_invite");
        } else if (data.containsKey("receive_checkin_chat_request")){

            message = data.getString("receive_checkin_chat_request");

            PushModel pushModel = new PushModel();
            String[] string = message.split("_receive_checkin_chat_request_");
            message = "qb_request_" + "You received checkin chat request from " + string[1];

            pushModel.user_id = string[0];
            pushModel.type = string[2];
            Intent intentNewPush = new Intent("pushData");
            intentNewPush.putExtra(Constant.PUSH_DATA, pushModel);
            LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intentNewPush);

        }else if (data.containsKey("accept_checkin_chat_request")){
            message = data.getString("accept_checkin_chat_request");

            PushModel pushModel = new PushModel();
            String[] string = message.split("_accept_checkin_chat_request_");
            message = "qb_request_" + string[1] + " accepted your checkin chat request";

            pushModel.user_id = string[0];
            pushModel.type = string[2];
            Intent intentNewPush = new Intent("pushData");
            intentNewPush.putExtra(Constant.PUSH_DATA, pushModel);
            LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intentNewPush);

        }else {
            if (data.containsKey("message")) {
                String msg = data.getString("message");
                String[] str = msg.split("_qb_");
                if (str.length > 0) {
                    id = str[0];
                    String name = str[1];
                    message = "qb_msg_You received message from " + name;
                }

            }
        }
        return message;
    }







    public void setNotificationData(Bundle data) {
        notificationData = data;
        parseMessage(data);
        if (!message.equals("")) {

            if (message.contains("qb_msg_")) {
                message = message.replace("qb_msg_", "");
                increaseMsgCount(id);
            } else if (message.contains("qb_request_")){
                message = message.replace("qb_request_", "");

            } else {
                increaseActivityCount();
            }
            sendNotification();
        }

    }

    public Bundle getNotificationData() {
        return notificationData;
    }
}
