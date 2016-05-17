package com.heyoe.controller;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.CustomRequest;
import com.android.volley.toolbox.Volley;
import com.heyoe.R;
import com.heyoe.controller.adapters.AdapterPrivateChatRoom;
import com.heyoe.model.API;
import com.heyoe.model.Constant;
import com.heyoe.model.UserModel;
import com.heyoe.utilities.BitmapUtility;
import com.heyoe.utilities.FileUtility;
import com.heyoe.utilities.UIUtility;
import com.heyoe.utilities.Utils;
import com.heyoe.utilities.camera.AlbumStorageDirFactory;
import com.heyoe.utilities.camera.BaseAlbumDirFactory;
import com.heyoe.utilities.camera.FroyoAlbumDirFactory;
import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBSession;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBPrivateChat;
import com.quickblox.chat.QBPrivateChatManager;
import com.quickblox.chat.exception.QBChatException;
import com.quickblox.chat.listeners.QBIsTypingListener;
import com.quickblox.chat.listeners.QBMessageListener;
import com.quickblox.chat.listeners.QBPrivateChatManagerListener;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.QBSettings;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.helper.StringifyArrayList;
import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.messages.QBPushNotifications;
import com.quickblox.messages.model.QBEnvironment;
import com.quickblox.messages.model.QBEvent;
import com.quickblox.messages.model.QBNotificationType;
import com.quickblox.messages.model.QBPushType;
import com.quickblox.users.model.QBUser;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class QBChatActivity extends AppCompatActivity {

    private ImageView ivSendMessage;
    private EditText etMessage;
    private ListView lvContent;
    private TextView tvName, tvOnlineStatus;
    private LinearLayout llChat;

    RequestQueue requestQueue;
    private String opponentID;
    public static UserModel opponentUserModel;


    private Timer timer;
    private TimerTask timerTask;
    private int timeCounter = 1;
    Activity mActivity;
    boolean isBlackFriend;
    int blacker_id ;

    //QB
    private QBChatService chatService;
    private QBDialog PrivateDialog;
    private QBPrivateChatManager privateChatManager;
    private ArrayList<QBChatMessage> arrHIstoryMessages;
    private AdapterPrivateChatRoom adapterPrivateChat;
    private QBUser me;


    QBPrivateChat privateChat;
    private QBPrivateChat initQBPrivateChat() {
        privateChat= privateChatManager.getChat(Integer.parseInt(opponentID));
        if (privateChat == null) {
            privateChat = privateChatManager.createChat(Integer.parseInt(opponentID), privateChatQBMessageListener);
        }
        privateChat.addIsTypingListener(privateChatIsTypingListener);
        return privateChat;
    }
    //QB listener
    QBIsTypingListener<QBPrivateChat> privateChatIsTypingListener = new QBIsTypingListener<QBPrivateChat>() {
        @Override
        public void processUserIsTyping(QBPrivateChat privateChat, Integer userId) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvOnlineStatus.setText(getResources().getString(R.string.typing));
                }
            });

        }

        @Override
        public void processUserStopTyping(QBPrivateChat privateChat, Integer userId) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateStatus(opponentUserModel.isOnline());
                }
            });

        }
    };

    ConnectionListener connectionListener = new ConnectionListener() {
        @Override
        public void connected(XMPPConnection xmppConnection) {

        }

        @Override
        public void authenticated(XMPPConnection xmppConnection, boolean b) {

        }

        @Override
        public void connectionClosed() {

        }

        @Override
        public void connectionClosedOnError(Exception e) {

        }

        @Override
        public void reconnectionSuccessful() {

        }

        @Override
        public void reconnectingIn(int i) {

        }

        @Override
        public void reconnectionFailed(Exception e) {

        }
    };

    QBMessageListener<QBPrivateChat> privateChatQBMessageListener = new QBMessageListener<QBPrivateChat>() {
        @Override
        public void processMessage(QBPrivateChat qbPrivateChat, final QBChatMessage qbChatMessage) {

            String strReceiveMessage = qbChatMessage.getBody();
            Log.d("receive", strReceiveMessage);
            if (qbChatMessage.getDialogId().equals(PrivateDialog.getDialogId())){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        arrHIstoryMessages.add(qbChatMessage);
                        adapterPrivateChat.updateList(arrHIstoryMessages);
                    }
                });
            }
        }

        @Override
        public void processError(QBPrivateChat qbPrivateChat, QBChatException e, QBChatMessage qbChatMessage) {

        }
    };

    QBPrivateChatManagerListener privateChatManagerListener = new QBPrivateChatManagerListener() {
        @Override
        public void chatCreated(QBPrivateChat qbPrivateChat, boolean b) {
            qbPrivateChat.addMessageListener(privateChatQBMessageListener);
        }
    };

    private void sendTypingNotification() {
        if (privateChat != null) {
            try {
                privateChat.sendIsTypingNotification();
            } catch (XMPPException e) {
                e.printStackTrace();
            } catch (SmackException.NotConnectedException e) {
                e.printStackTrace();
            }
        }

    }
    private void sendStopTypingNotification() {
        if (privateChat != null) {
            try {
                privateChat.sendStopTypingNotification();
            } catch (XMPPException e) {
                e.printStackTrace();
            } catch (SmackException.NotConnectedException e) {
                e.printStackTrace();
            }
        }

    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qb_chat);

        initVariables();
        initUI();

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        loginChatService();

        initTimer();
    }
    private void initVariables() {
        mActivity = this;
        requestQueue = Volley.newRequestQueue(mActivity);
        blacker_id = getIntent().getIntExtra("blacker_id", 0);
        isBlackFriend = getIntent().getBooleanExtra("is_black_chat", false);
        arrHIstoryMessages = new ArrayList<>();
        opponentUserModel = (UserModel) getIntent().getSerializableExtra("userModel");

        opponentID = opponentUserModel.getQb_id();
    }
    private void initUI() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ImageButton ibBack = (ImageButton)toolbar.findViewById(R.id.ib_back);
        ibBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UIUtility.hideSoftKeyboard(QBChatActivity.this);
                finish();
            }
        });

        tvName = (TextView)toolbar.findViewById(R.id.tv_chat_fullname);
        tvOnlineStatus = (TextView)toolbar.findViewById(R.id.tv_chat_online);

        ivSendMessage = (ImageView) findViewById(R.id.iv_sendmessage);
        ivSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etMessage.getText().toString().trim() == null ||
                        etMessage.getText().toString().trim() == "")
                    return;
                sendMessage();
                etMessage.setText("");
            }
        });

        etMessage = (EditText) findViewById(R.id.et_message_content);
        etMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (timeCounter > 4) {
                    timeCounter = 0;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
//                sendStopTypingNotification();
            }
        });
        llChat = (LinearLayout)findViewById(R.id.ll_chat);

        if (isBlackFriend) {
            llChat.setBackgroundColor(getResources().getColor(R.color.black));
            etMessage.setTextColor(getResources().getColor(R.color.white));
        }


        tvName.setText(opponentUserModel.getFullname());
        updateStatus(opponentUserModel.isOnline());

        lvContent = (ListView) findViewById(R.id.lv_messages);
        lvContent.setStackFromBottom(true);
        lvContent.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        adapterPrivateChat = new AdapterPrivateChatRoom(QBChatActivity.this, arrHIstoryMessages, opponentUserModel.getAvatar());
        lvContent.setAdapter(adapterPrivateChat);



    }

    private void updateStatus(boolean state) {
        if (state) {
            tvOnlineStatus.setText(getResources().getString(R.string.online));
        } else {
            tvOnlineStatus.setText(getResources().getString(R.string.offline));
        }
    }

    private void loginChatService() {
        Utils.showProgress(this);
        me = new QBUser(Utils.getFromPreference(this, Constant.EMAIL), Constant.DEFAULT_PASSWORD);

        chatService = QBChatService.getInstance();
        chatService.addConnectionListener(connectionListener);

        if (chatService.isLoggedIn()) {
            try {
                chatService.startAutoSendPresence(30);
                privateChatManager = chatService.getPrivateChatManager();
                privateChatManager.addPrivateChatManagerListener(privateChatManagerListener);
                initQBPrivateChat();
                createDialog();
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            QBSettings.getInstance().fastConfigInit(Constant.APP_ID, Constant.AUTH_KEY, Constant.AUTH_SECRET);
            QBAuth.createSession(me, new QBEntityCallback<QBSession>() {
                @Override
                public void onSuccess(QBSession qbSession, Bundle bundle) {
                    me.setId(qbSession.getUserId());
                    chatService.login(me, new QBEntityCallback() {
                        @Override
                        public void onSuccess(Object o, Bundle bundle) {

                            try {
                                chatService.startAutoSendPresence(30);
                                privateChatManager = chatService.getPrivateChatManager();
                                privateChatManager.addPrivateChatManagerListener(privateChatManagerListener);

                                initQBPrivateChat();
                                createDialog();

                            } catch (SmackException.NotLoggedInException e) {
                                e.printStackTrace();
                                finish();
                            }
                        }

                        @Override
                        public void onError(QBResponseException e) {
                            Utils.hideProgress();

                            showAlert(e.getLocalizedMessage());
                            finish();
                        }
                    });
                }

                @Override
                public void onError(QBResponseException e) {
                    Utils.hideProgress();
                    showAlert(e.getLocalizedMessage());
                    finish();
                }
            });
        }

    }
    ///creat private dialog
    private void createDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                privateChatManager.createDialog(Integer.parseInt(opponentID), new QBEntityCallback<QBDialog>() {
                    @Override
                    public void onSuccess(QBDialog dialog, Bundle bundle) {
                        PrivateDialog = dialog;
                        getChatHistory();
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        showAlert(e.getLocalizedMessage());
                        Utils.hideProgress();
                    }
                });
            }
        });
    }
    private void getChatHistory() {
        QBRequestGetBuilder requestGetBuilder = new QBRequestGetBuilder();
        requestGetBuilder.setLimit(1000);
        requestGetBuilder.sortAsc("date_sent");
        QBChatService.getDialogMessages(PrivateDialog, requestGetBuilder, new QBEntityCallback<ArrayList<QBChatMessage>>() {
            @Override
            public void onSuccess(ArrayList<QBChatMessage> qbChatMessages, Bundle bundle) {
                Utils.hideProgress();
                arrHIstoryMessages = qbChatMessages;
                adapterPrivateChat.updateList(arrHIstoryMessages);

            }

            @Override
            public void onError(QBResponseException e) {
                showAlert(e.getLocalizedMessage());

            }
        });
    }
    private void scrollMessageListDown() {
        lvContent.setSelection(lvContent.getCount() - 1);
    }


    private void initTimer() {
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                if (timeCounter == 0) {
                    sendTypingNotification();
                }
                if (timeCounter == 4) {
                    sendStopTypingNotification();
                }
                if (timeCounter % 4 == 0) {
                    getOnlineStatus();
                }
                timeCounter += 2;
            }
        };
        timer.schedule(timerTask, 0, 2000);
    }
    private void getOnlineStatus() {


        Map<String, String> params = new HashMap<String, String>();
        params.put(Constant.DEVICE_TYPE, Constant.ANDROID);
        params.put(Constant.DEVICE_TOKEN, Utils.getFromPreference(mActivity, Constant.DEVICE_TOKEN));
        params.put("my_id", Utils.getFromPreference(mActivity, Constant.USER_ID));
        params.put("user_id", opponentUserModel.getUser_id());

        CustomRequest signinRequest = new CustomRequest(Request.Method.POST, API.GET_ONLINE_STATUS, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Utils.hideProgress();
                        try {
                            String status = response.getString("status");
                            if (status.equals("200")) {
                                JSONObject jsonObject = response.getJSONObject("data");
                                String online_status = jsonObject.getString("online_status");
                                if (online_status.equals("on")) {
                                    if (!opponentUserModel.isOnline()) {
                                        opponentUserModel.setOnline(true);
                                        updateStatus(opponentUserModel.isOnline());
                                    }
                                } else {
                                    if (opponentUserModel.isOnline()) {
                                        opponentUserModel.setOnline(false);
                                        updateStatus(opponentUserModel.isOnline());
                                    }
                                }
                                requestQueue.getCache().clear();
                            } else  if (status.equals("400")) {
                                Utils.showOKDialog(mActivity, mActivity.getResources().getString(R.string.access_denied));
                            } else if (status.equals("402")) {
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

        requestQueue.add(signinRequest);
    }

    private void sendPush(String id, String name) {
        // recipients
        StringifyArrayList<Integer> userIds = new StringifyArrayList<Integer>();
//        userIds.add(Integer.valueOf(id));
        userIds.add(Integer.valueOf(opponentID));

        QBEvent event = new QBEvent();
        event.setUserIds(userIds);
        event.setEnvironment(QBEnvironment.DEVELOPMENT);
        event.setNotificationType(QBNotificationType.PUSH);
        event.setPushType(QBPushType.GCM);
//        HashMap<String, String> data = new HashMap<String, String>();
//        data.put("user_id", id);
//        data.put("message", "You received message from " + name);
        event.setMessage(id + "_qb_" + name);
//        event.setUserId(Integer.parseInt(id));
//        event.setId(Integer.parseInt(id));

        QBPushNotifications.createEvent(event, new QBEntityCallback<QBEvent>() {
            @Override
            public void onSuccess(QBEvent qbEvent, Bundle args) {
                // sent
            }

            @Override
            public void onError(QBResponseException errors) {

            }
        });
    }
    private void sendMessage() {
        String content = etMessage.getText().toString().trim();
        if (content.length() == 0)
            return;
        try{
            QBChatMessage chatMessage = new QBChatMessage();
            chatMessage.setBody(content);
            chatMessage.setProperty("save_to_history", "1");

            QBPrivateChat privateChat = privateChatManager.getChat(Integer.parseInt(opponentID));
            if (privateChat == null){
                privateChat = privateChatManager.createChat(Integer.parseInt(opponentID), privateChatQBMessageListener);
            }

            privateChat.sendMessage(chatMessage);
            if (blacker_id == 0 || blacker_id == Integer.parseInt(Utils.getFromPreference(mActivity, Constant.USER_ID))) {
                sendPush(Utils.getFromPreference(this, Constant.QB_ID), Utils.getFromPreference(this, Constant.FULLNAME));
            }


            chatMessage.setDateSent(System.currentTimeMillis() / 1000);
            chatMessage.setSenderId(Integer.parseInt(Utils.getFromPreference(this, Constant.QB_ID)));
            arrHIstoryMessages.add(chatMessage);
            adapterPrivateChat.updateList(arrHIstoryMessages);

        }catch (Exception e){
            showAlert(e.getLocalizedMessage());
        }
    }

    private void sendPhoto() {
    }
    private void sendVidoe() {

    }























    public void showAlert(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void transferAnimation(int i) {
        switch (i){
            case 1:
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                break;
            case 2:
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                break;
        }
    }
    @Override
    protected void onDestroy() {

//        chatService.removeConnectionListener(connectionListener);
//        chatService.logout(new QBEntityCallback<Void>() {
//            @Override
//            public void onSuccess(Void aVoid, Bundle bundle) {
//                chatService.destroy();
//            }
//
//            @Override
//            public void onError(QBResponseException e) {
//
//                showAlert(e.getLocalizedMessage());
//            }
//        });
        timer.purge();
        UIUtility.hideSoftKeyboard(this);
        super.onDestroy();
    }

    private static MenuItem photo, video;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_chat, menu);
        photo = menu.findItem(R.id.ic_photo);
        video = menu.findItem(R.id.ic_video);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.ic_photo) {
            showPictureChooseDialog();
            return true;
        } else if (id == R.id.ic_video) {
            showVideoChooseDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case take_photo_from_gallery:
                if (resultCode == Activity.RESULT_OK) {

                    Uri selectedImage = data.getData();
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};

                    Cursor cursor = mActivity.getContentResolver().query(
                            selectedImage, filePathColumn, null, null, null);
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    initMediaPath();
                    photoPath = cursor.getString(columnIndex);
                    cursor.close();

                    Bitmap bitmap = BitmapUtility.adjustBitmap(photoPath);


                    imageWidth = bitmap.getWidth();
                    imageHeight = bitmap.getHeight();

                    photoPath = BitmapUtility.saveBitmap(bitmap, Constant.MEDIA_PATH + "heyoe", FileUtility.getFilenameFromPath(photoPath));

                    if (photoPath.length() > 0) {
                        sendPhoto();
                    }

                }
                break;
            case take_photo_from_camera: {
                if (resultCode == Activity.RESULT_OK) {
                    setPic();
                }
                break;
            }
            case take_video_from_gallery:
                if (resultCode == Activity.RESULT_OK) {
                    Uri selectedVideoUri = data.getData();
                    videoPath = getVideoPath(selectedVideoUri);
                    if (videoPath.length() > 0) {
                        sendVidoe();
                    }
                }
                break;
            case take_video_from_camera:
                if (resultCode == Activity.RESULT_OK) {
                    if (videoPath.length() > 0) {
                        sendVidoe();
                    }
                }
                break;
        }
    }

    public String getVideoPath(Uri uri) {

        String path = "";
        try {
            Cursor cursor = mActivity.getContentResolver().query(uri, null, null, null, null);
            cursor.moveToFirst();
            String document_id = cursor.getString(0);
            document_id = document_id.substring(document_id.lastIndexOf(":")+1);
            cursor.close();

            cursor = mActivity.getContentResolver().query(android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    null, MediaStore.Video.Media._ID + " = ? ", new String[]{document_id}, null);
            cursor.moveToFirst();
            path = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
            cursor.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return path;
    }

    private static final int take_photo_from_gallery = 1;
    private static final int take_photo_from_camera = 2;
    private static final int take_video_from_gallery = 3;
    private static final int take_video_from_camera = 4;

    private static final String JPEG_FILE_PREFIX = "Heyoe_Compose_photo_";
    private static final String JPEG_FILE_SUFFIX = ".jpg";
    private static final String VIDEO_FILE_PREFIX = "Heyoe_Compose_video_";
    private static final String VIDEO_FILE_SUFFIX = ".mp4";

    private AlbumStorageDirFactory mAlbumStorageDirFactory = null;

    private String photoPath, videoPath;
    private void initMediaPath() {
        photoPath = "";
        videoPath = "";

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
                        captureVideoFromCamera();
                        dialog.cancel();
                    }
                });
        builder.setNegativeButton("Gallery",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        takeVideoFromGallery();
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
    private void takeVideoFromGallery() {
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

        // set max time limit
        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 20);
//        or
//        intent.putExtra("android.intent.extra.durationLimit", 30000);

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
        builder.setMessage(getResources().getString(R.string.choose_photo));
        builder.setCancelable(true);
        builder.setPositiveButton("Camera",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dispatchTakePictureIntent();
//                        deleteMessage(opponentUserModel.getDialog_id());
                        dialog.cancel();
                    }
                });
        builder.setNegativeButton("Gallery",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
//                        deleteMessage(opponentUserModel.getDialog_id());
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
        AlertDialog alert = builder.create();
        alert.show();
    }
    //take a picture from gallery
    private void takePictureFromGallery() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, take_photo_from_gallery);
    }
    //capture photo
    public void dispatchTakePictureIntent() {
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            mAlbumStorageDirFactory = new FroyoAlbumDirFactory();
        } else {
            mAlbumStorageDirFactory = new BaseAlbumDirFactory();
        }
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
    private void setPic() {
        if (photoPath == null) {
            return;
        }

		/* There isn't enough memory to open up more than a couple camera photos */
		/* So pre-scale the target bitmap into which the file is decoded */

		/* Get the size of the ImageView */
        int targetW = 150;
        int targetH = 150;

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

        Bitmap bitmap = BitmapUtility.adjustBitmap(photoPath);

//        imageView.setImageBitmap(bitmap);

        imageWidth = bitmap.getWidth();
        imageHeight = bitmap.getHeight();

        photoPath = BitmapUtility.saveBitmap(bitmap, Constant.MEDIA_PATH + "heyoe", FileUtility.getFilenameFromPath(photoPath));

        if (photoPath.length() > 0) {
            sendPhoto();
        }

        //crop thumbnail
//        Bitmap cropBitmap = BitmapUtility.cropBitmapCenter(bitmap);
//        Bitmap cropBitmap = BitmapUtility.cropBitmapAnySize(bitmap, bitmap.getWidth(), bitmap.getWidth());
        // save croped thumbnail
//        thumbPath = BitmapUtility.saveBitmap(cropBitmap, Constant.MEDIA_PATH, "heyoe_thumb");
    }
    int imageWidth = 0 , imageHeight = 0;



}
