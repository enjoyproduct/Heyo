package com.heyoe_chat.controller.push;

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
import com.heyoe_chat.R;
import com.heyoe_chat.controller.SignActivity;
import com.heyoe_chat.model.Constant;
import com.heyoe_chat.model.PushModel;
import com.heyoe_chat.utilities.Utils;

public class GcmIntentService extends IntentService {

    int i = 0;

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
            } else if (type.equals("enter_checkin")) {

            } else if (type.equals("exit_checkin")) {

            } else {
                if (type.equals("white")) {
                    increaseMsgCount();
                } else if (type.equals("activity") ||
                        type.equals("taged")) {
                    increaseActivityCount();
                }
                if (data.containsKey("send_message")) {
                    if (!conversation_id.equals(Utils.getFromPreference(this, Constant.USER_ID))) {
                        return;
                    }
                }
                sendNotification();
            }
        }
    }

    private void increaseActivityCount() {

        PushModel pushModel = new PushModel();
        pushModel.type = "increase_activity_count";
        localBroadCast(pushModel);

    }
    private void increaseMsgCount() {
        if (id.length() > 0) {
            PushModel pushModel = new PushModel();
            pushModel.user_id = id;
            pushModel.conversation_id = conversation_id;
            if (type.equals("black")) {
                pushModel.type = "increase_black_message_count";
            } else {
                pushModel.type = "increase_message_count";
            }
            localBroadCast(pushModel);
        }
    }
    private void sendNotification() {
        Intent intent = new Intent(this, SignActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (type.equals("activity") ||
                type.equals("receive_invite") ||
                type.equals("accept_friend") ||
                type.equals("taged")) {
//            intent.putExtra("page_num", 6);
            intent.putExtra("type", "activity");
        } else if (type.equals("white")) {
            intent.putExtra("type", "message");
        }

        NotificationManager mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, i++,
                intent,  PendingIntent.FLAG_ONE_SHOT);
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

    String id, type, message, conversation_id;
    private String parseMessage(Bundle data){
        id = "";
        type = "";
        message = "";
        conversation_id = "";
        if(data.containsKey("liked_post")){
            message = data.getString("liked_post");
            type = "activity";
        }else if (data.containsKey("disliked_post")){
            message = data.getString("disliked_post");
            type = "activity";
        }else if (data.containsKey("commented_post")){
            message = data.getString("commented_post");
            type = "activity";
        }else if (data.containsKey("shared_post")){
            message = data.getString("shared_post");
            type = "activity";
        }else if (data.containsKey("received_invite")){
            message = data.getString("received_invite");

            PushModel pushModel = new PushModel();
            String[] string = message.split("_invite_friend_");
            message = string[1];
            pushModel.user_id = string[0];
            pushModel.type = "receive_invite";
            localBroadCast(pushModel);
            type = "receive_invite";
        }else if (data.containsKey("accepted_invite")){
            message = data.getString("accepted_invite");

            PushModel pushModel = new PushModel();
            String[] string = message.split("_accept_invite_");
            message = string[1];
            pushModel.user_id = string[0];
            pushModel.type = "accept_friend";
            localBroadCast(pushModel);
            type = "accept_friend";

        }else if (data.containsKey("rejected_invite")){
            message = data.getString("rejected_invite");
            type = "activity";
        } else if (data.containsKey("receive_checkin_chat_request")){

            message = data.getString("receive_checkin_chat_request");

            PushModel pushModel = new PushModel();
            String[] string = message.split("_receive_checkin_chat_request_");
            message ="You received checkin chat request from " + string[1];

            pushModel.user_id = string[0];
            pushModel.type = string[2];
            localBroadCast(pushModel);
            type = "qb_request";

        }else if (data.containsKey("accept_checkin_chat_request")){
            message = data.getString("accept_checkin_chat_request");

            PushModel pushModel = new PushModel();
            String[] string = message.split("_accept_checkin_chat_request_");
            message = string[1] + " accepted your checkin chat request";

            pushModel.user_id = string[0];
            pushModel.type = string[2];
            localBroadCast(pushModel);
            type = "qb_request";

        }else if (data.containsKey("send_message")){  /// message notification
            message = data.getString("send_message");

            String[] string = message.split("_send_message_");
            if (string.length > 1) {
                id = string[0];
                message = "You received message from " + string[1];
                type = string[2];
                conversation_id = string[3];
            }
        } else if (data.containsKey("enter_checkin")) {
            message = data.getString("enter_checkin");
            String[] string = message.split("_enter_checkin_");
            if (string.length > 1) {
                PushModel pushModel = new PushModel();
                pushModel.user_id = string[3];
                pushModel.fullname = string[1];
                pushModel.friend_status = string[2];
                pushModel.avatar = string[0];
                pushModel.type = "enter_checkin";
                localBroadCast(pushModel);
                type = "enter_checkin";
            }
        } else if (data.containsKey("exit_checkin")) {
            message = data.getString("exit_checkin");
            String[] string = message.split("_exit_checkin_");
            if (string.length > 1) {
                PushModel pushModel = new PushModel();
                pushModel.user_id = string[2];
                pushModel.fullname = string[1];
                pushModel.avatar = string[0];
                pushModel.friend_status = "";
                pushModel.type = "exit_checkin";
                localBroadCast(pushModel);
                type = "exit_checkin";
            }
        } else if (data.containsKey("taged")) {
            message = data.getString("taged");
            type = "activity";
        } else if (data.containsKey("alert")) {
            id = data.getString("alert");
            message = "Heyoe - You received new message";
            type = data.getString("title");

            String[] str1 = data.getString("layer").split(",");
//            String[] m_str2 = str1[0].split(":");
//            String m_id = m_str2[1].substring(1, m_str2[1].length()) + ":" + m_str2[2].substring(0, m_str2[2].length() - 1);
            String[] c_str2 = str1[1].split(":");
            String c_id = c_str2[1].substring(1, c_str2[1].length()) + ":" + c_str2[2].substring(0, c_str2[2].length() - 2);
            conversation_id = c_id;
        }
        return message;
    }
    private void localBroadCast(PushModel pushModel) {
        Intent intentNewPush = new Intent("pushData");
        intentNewPush.putExtra(Constant.PUSH_DATA, pushModel);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intentNewPush);
    }
}