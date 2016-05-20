package com.heyoe.controller.push;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.heyoe.R;
import com.heyoe.controller.HomeActivity;
import com.heyoe.model.Constant;
import com.heyoe.model.PushModel;
import com.heyoe.utilities.Utils;

public class GcmIntentService extends IntentService {

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty() && GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
            setNotificationData(extras);
        }
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }
    public void setNotificationData(Bundle data) {
        parseMessage(data);
        if (!message.equals("")) {

            if (type.equals("black")) {
                increaseMsgCount();
            } else {
                if (type.equals("white")) {
                    increaseMsgCount();
                } else if (message.contains("qb_request_")){
                    message = message.replace("qb_request_", "");

                } else {
                    increaseActivityCount();
                }
                sendNotification();
            }

        }

    }

    private void increaseActivityCount() {

        Intent intentNewPush = new Intent("pushData");
        PushModel pushModel = new PushModel();
        pushModel.type = "increase_activity_count";
        intentNewPush.putExtra(Constant.PUSH_DATA, pushModel);

        LocalBroadcastManager.getInstance(this).sendBroadcast(intentNewPush);


    }
    private void increaseMsgCount() {
        if (id.length() > 0) {
            Intent intentNewPush = new Intent("pushData");
            PushModel pushModel = new PushModel();
            pushModel.user_id = id;
            if (type.equals("black")) {
                pushModel.type = "increase_black_message_count";
            } else {
                pushModel.type = "increase_message_count";
            }

            intentNewPush.putExtra(Constant.PUSH_DATA, pushModel);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intentNewPush);
        }
    }
    private void sendNotification() {
        NotificationManager mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, HomeActivity.class), 0);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(getResources().getString(R.string.app_name))
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(message))
                        .setSound(defaultSoundUri)
                        .setContentText(message);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(0, mBuilder.build());
    }

    String id, type, message;
    private String parseMessage(Bundle data){
        id = "";
        type = "";
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
            LocalBroadcastManager.getInstance(this).sendBroadcast(intentNewPush);

        }else if (data.containsKey("accept_checkin_chat_request")){
            message = data.getString("accept_checkin_chat_request");

            PushModel pushModel = new PushModel();
            String[] string = message.split("_accept_checkin_chat_request_");
            message = "qb_request_" + string[1] + " accepted your checkin chat request";

            pushModel.user_id = string[0];
            pushModel.type = string[2];
            Intent intentNewPush = new Intent("pushData");
            intentNewPush.putExtra(Constant.PUSH_DATA, pushModel);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intentNewPush);

        }else if (data.containsKey("send_message")){  /// message notification
            message = data.getString("send_message");

            String[] string = message.split("_send_message_");
            if (string.length > 1) {
                id = string[0];
                message = "You received message from " + string[1];
                type = string[2];
            }


//            if (data.containsKey("message")) {
//                String msg = data.getString("message");
//                msg = msg.substring(1, msg.length() - 1);
//                String[] str = msg.split(", ");
//                if (str.length > 0) {
//                    for (int i = 0; i < str.length; i ++) {
//                        String[] strData = str[i].split("=");
//                        if (strData.length == 0) {
//                            break;
//                        }
//                        if (strData[0].equals("user_id")) {
//                            id = strData[1];
//                        }
//                        if (strData[0].equals("message")) {
//                            message = strData[1];
//                        }
//                        if (strData[0].equals("type")) {
//                            type = strData[1];
//                        }
//                    }
//
//                }
//
//            }
        }
        return message;
    }
}