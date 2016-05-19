package com.heyoe.controller.qb_chat;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
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
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.CustomRequest;
import com.android.volley.toolbox.Volley;
import com.heyoe.R;
import com.heyoe.controller.App;
import com.heyoe.controller.HomeActivity;
import com.heyoe.controller.adapters.AdapterPrivateChatRoom;
import com.heyoe.controller.qb_chat.chat.Chat;
import com.heyoe.controller.qb_chat.chat.ChatHelper;
import com.heyoe.controller.qb_chat.chat.GroupChatImpl;
import com.heyoe.controller.qb_chat.chat.PrivateChatImpl;
import com.heyoe.controller.qb_chat.chat.QBChatMessageListener;
import com.heyoe.controller.qb_chat.qb.PaginationHistoryListener;
import com.heyoe.controller.qb_chat.qb.QbDialogUtils;
import com.heyoe.controller.qb_chat.qb.VerboseQbChatConnectionListener;
import com.heyoe.controller.qb_chat.qb_adapters.AttachmentPreviewAdapter;
import com.heyoe.controller.qb_chat.qb_adapters.ChatAdapter;
import com.heyoe.model.API;
import com.heyoe.model.Constant;
import com.heyoe.model.Global;
import com.heyoe.model.UserModel;
import com.heyoe.utilities.BitmapUtility;
import com.heyoe.utilities.FileUtility;
import com.heyoe.utilities.UIUtility;
import com.heyoe.utilities.Utils;
import com.heyoe.utilities.camera.AlbumStorageDirFactory;
import com.heyoe.utilities.camera.BaseAlbumDirFactory;
import com.heyoe.utilities.camera.FroyoAlbumDirFactory;
import com.heyoe.widget.AttachmentPreviewAdapterView;
import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBSession;
import com.quickblox.chat.QBChat;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBPrivateChat;
import com.quickblox.chat.QBPrivateChatManager;
import com.quickblox.chat.exception.QBChatException;
import com.quickblox.chat.listeners.QBIsTypingListener;
import com.quickblox.chat.listeners.QBMessageListener;
import com.quickblox.chat.listeners.QBPrivateChatManagerListener;
import com.quickblox.chat.model.QBAttachment;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.QBSettings;
import com.quickblox.core.exception.QBResponseException;

import com.quickblox.core.helper.StringifyArrayList;
import com.quickblox.messages.QBPushNotifications;
import com.quickblox.messages.model.QBEnvironment;
import com.quickblox.messages.model.QBEvent;
import com.quickblox.messages.model.QBNotificationType;
import com.quickblox.messages.model.QBPushType;
import com.quickblox.users.model.QBUser;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;


public class ChatActivity extends BaseActivity  {
    private static final String TAG = ChatActivity.class.getSimpleName();
    private static final int REQUEST_CODE_ATTACHMENT = 721;
    private static final int REQUEST_CODE_SELECT_PEOPLE = 752;

    private static final String EXTRA_DIALOG = "dialog";
    private static final String PROPERTY_SAVE_TO_HISTORY = "save_to_history";

    public static final String EXTRA_MARK_READ = "markRead";
    public static final String EXTRA_DIALOG_ID = "dialogId";

//    private ProgressBar progressBar;
    private StickyListHeadersListView messagesListView;
    private EditText messageEditText;

    private LinearLayout attachmentPreviewContainerLayout;
    private Snackbar snackbar;

    private ChatAdapter chatAdapter;
    private AttachmentPreviewAdapter attachmentPreviewAdapter;
    private ConnectionListener chatConnectionListener;

    private Chat chat;
    private QBDialog qbDialog;
    public static UserModel opponentUserModel;
//    private ArrayList<String> chatMessageIds;
    private ArrayList<QBChatMessage> unShownMessages;
    private int skipPagination = 0;

    private TextView tvName, tvOnlineStatus;

    RequestQueue requestQueue;
    private String opponentID;

    private Timer timer;
    private TimerTask timerTask;
    private int timeCounter = 1;
    boolean isBlackFriend;
    int blacker_id ;
    String currentChatPage;

    private QBChatService chatService;
//    private QBPrivateChatManager privateChatManager;
    private QBPrivateChat privateChat;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        initVariables();

        initViews();
        initTimer();
    }

    private void initVariables() {
        requestQueue = Volley.newRequestQueue(this);
        blacker_id = getIntent().getIntExtra("blacker_id", 0);
        isBlackFriend = getIntent().getBooleanExtra("is_black_chat", false);
        opponentUserModel = (UserModel) getIntent().getSerializableExtra("userModel");
        qbDialog = opponentUserModel.getQbDialog();
        opponentID = opponentUserModel.getQb_id();
        currentChatPage = getIntent().getStringExtra("page");
        Global.getInstance().currentChattingPage = currentChatPage;

        skipPagination = 0;
        chatAdapter = null;

        Global.getInstance().isChatting = true;
        Global.getInstance().currentChattingUser = opponentID;

        Utils.showProgress(this);
//        if (qbDialog == null) {
//            createDialog();
//        } else {
//            init();
//        }

    }

    private QBChatMessageListener chatMessageListener = new QBChatMessageListener() {
        @Override
        public void onQBChatMessageReceived(QBChat chat, QBChatMessage message) {
            showMessage(message);
//            Global.getInstance().decreaseMessageCount(1);
//            HomeActivity.showMsgBadge(opponentID);
            int unreadMsgcount = qbDialog.getUnreadMessageCount();
            unreadMsgcount ++;
            qbDialog.setUnreadMessageCount(unreadMsgcount);
//            Utils.showToast(ChatActivity.this, "Message2");
        }
    };

    @Override
    public void onSessionCreated(boolean success) {
        if (success) {
            initChat();
        }
    }
    private void initChat() {
        if (qbDialog != null) {
            chat = new PrivateChatImpl(chatMessageListener, QbDialogUtils.getOpponentIdForPrivateDialog(qbDialog));
            init();
        } else {
            createDialog();
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        if (qbDialog != null) {
            outState.putSerializable(EXTRA_DIALOG, qbDialog);
        }
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (qbDialog == null) {
            qbDialog = (QBDialog) savedInstanceState.getSerializable(EXTRA_DIALOG);
        }
    }



    private void createDialog() {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                QBChatService.getInstance().getPrivateChatManager().createDialog(Integer.parseInt(opponentID), new QBEntityCallback<QBDialog>() {
                    @Override
                    public void onSuccess(QBDialog dialog, Bundle bundle) {
                        qbDialog = dialog;
                        init();
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        Utils.hideProgress();
                        Utils.showToast(ChatActivity.this, e.getLocalizedMessage());
                    }
                });
            }
        });
    }
    private void initChatConnectionListener() {
        chatConnectionListener = new VerboseQbChatConnectionListener(getSnackbarAnchorView()) {
            @Override
            public void connectionClosedOnError(final Exception e) {
                super.connectionClosedOnError(e);
                Utils.hideProgress();
                qb_login();
            }
            @Override
            public void reconnectionSuccessful() {
                super.reconnectionSuccessful();
                skipPagination = 0;
                chatAdapter = null;
                switch (qbDialog.getType()) {
                    case PRIVATE:
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                loadChatHistory();

                            }
                        });
                        break;

                }
            }
        };
    }
    private void qb_login() {
        QBUser user = new QBUser(Utils.getFromPreference(this, Constant.EMAIL), Constant.DEFAULT_PASSWORD);
        ChatHelper.getInstance().login(user, new QBEntityCallback<Void>() {
            @Override
            public void onSuccess(Void result, Bundle bundle) {
//                            Utils.showToast(App.getInstance(), "QB qb_login success");
                createDialog();
            }

            @Override
            public void onError(QBResponseException e) {
                Utils.showToast(App.getInstance(), "QB qb_login failed");
            }
        });

//        QBUser user = new QBUser(Utils.getFromPreference(this, Constant.EMAIL), Constant.DEFAULT_PASSWORD);
//        chatService = QBChatService.getInstance();
//        if (!chatService.isLoggedIn()) {
//            QBSettings.getInstance().fastConfigInit(Constant.APP_ID, Constant.AUTH_KEY, Constant.AUTH_SECRET);
//            final QBUser finalUser = user;
//            QBAuth.createSession(user, new QBEntityCallback<QBSession>() {
//                @Override
//                public void onSuccess(QBSession qbSession, Bundle bundle) {
//
//                    finalUser.setId(qbSession.getUserId());
//                    Global.getInstance().qbUser = finalUser;
//                    ChatHelper.getInstance().login(finalUser, new QBEntityCallback<Void>() {
//                        @Override
//                        public void onSuccess(Void result, Bundle bundle) {
////                            Utils.showToast(App.getInstance(), "QB qb_login success");
//                        }
//
//                        @Override
//                        public void onError(QBResponseException e) {
//                            Utils.showToast(App.getInstance(), "QB qb_login failed");
//                        }
//                    });
//                }
//
//                @Override
//                public void onError(QBResponseException e) {
//                    Utils.showToast(mActivity, e.getLocalizedMessage());
//                }
//            });
//
//        }
    }

    private void init() {

//        QBChatService.getInstance().getPrivateChatManager().addPrivateChatManagerListener(privateChatManagerListener);
        initChatConnectionListener();
        loadChatHistory();
        initQBPrivateChat();
    }

    private void loadChatHistory() {
        ChatHelper.getInstance().loadChatHistory(qbDialog, skipPagination, new QBEntityCallback<ArrayList<QBChatMessage>>() {
            @Override
            public void onSuccess(ArrayList<QBChatMessage> messages, Bundle args) {


                // The newest messages should be in the end of list,
                // so we need to reverse list to show messages in the right order
                Collections.reverse(messages);
                if (chatAdapter == null) {
                    chatAdapter = new ChatAdapter(ChatActivity.this, messages, opponentUserModel.getAvatar());
                    chatAdapter.setPaginationHistoryListener(new PaginationHistoryListener() {
                        @Override
                        public void downloadMore() {
                            loadChatHistory();
                        }
                    });
                    chatAdapter.setOnItemInfoExpandedListener(new ChatAdapter.OnItemInfoExpandedListener() {
                        @Override
                        public void onItemInfoExpanded(final int position) {
                            if (isLastItem(position)) {
                                // HACK need to allow info textview visibility change so posting it via handler
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        messagesListView.setSelection(position);
                                    }
                                });
                            } else {
                                messagesListView.smoothScrollToPosition(position);
                            }
                        }

                        private boolean isLastItem(int position) {
                            return position == chatAdapter.getCount() - 1;
                        }
                    });
                    if (unShownMessages != null && !unShownMessages.isEmpty()) {
                        List<QBChatMessage> chatList = chatAdapter.getList();
                        for (QBChatMessage message : unShownMessages) {
                            if (!chatList.contains(message)) {
                                chatAdapter.add(message);
                            }
                        }
                    }
                    messagesListView.setAdapter(chatAdapter);
                    messagesListView.setAreHeadersSticky(false);
                    messagesListView.setDivider(null);

                } else {
                    chatAdapter.addList(messages);
                    messagesListView.setSelection(messages.size());
                }
                Utils.hideProgress();
            }

            @Override
            public void onError(QBResponseException e) {
                Utils.hideProgress();
                skipPagination -= ChatHelper.CHAT_HISTORY_ITEMS_PER_PAGE;
                snackbar = showErrorSnackbar(R.string.connection_error, e, null);
            }
        });
        skipPagination += ChatHelper.CHAT_HISTORY_ITEMS_PER_PAGE;
    }


    @Override
    protected View getSnackbarAnchorView() {
        return findViewById(R.id.list_chat_messages);
    }



    private void initViews() {
//        actionBar.setDisplayHomeAsUpEnabled(true);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ImageButton ibBack = (ImageButton)toolbar.findViewById(R.id.ib_back);
        ibBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UIUtility.hideSoftKeyboard(ChatActivity.this);
                setDataAndFinish();
            }
        });
        tvName = (TextView)toolbar.findViewById(R.id.tv_chat_fullname);
        tvOnlineStatus = (TextView)toolbar.findViewById(R.id.tv_chat_online);

        tvName.setText(opponentUserModel.getFullname());
        updateStatus(opponentUserModel.isOnline());

        messagesListView = _findViewById(R.id.list_chat_messages);
        messagesListView.setStackFromBottom(true);
        messagesListView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);

        messageEditText = _findViewById(R.id.edit_chat_message);
        messageEditText.addTextChangedListener(new TextWatcher() {
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
        LinearLayout llChat = (LinearLayout)findViewById(R.id.ll_chat);
        if (isBlackFriend) {
            llChat.setBackgroundColor(getResources().getColor(R.color.black));
            messageEditText.setTextColor(getResources().getColor(R.color.white));
        }
//        progressBar = _findViewById(R.id.progress_chat);
        attachmentPreviewContainerLayout = _findViewById(R.id.layout_attachment_preview_container);

        attachmentPreviewAdapter = new AttachmentPreviewAdapter(this,
                new AttachmentPreviewAdapter.OnAttachmentCountChangedListener() {
                    @Override
                    public void onAttachmentCountChanged(int count) {
                        attachmentPreviewContainerLayout.setVisibility(count == 0 ? View.GONE : View.VISIBLE);
                    }
                },
                new AttachmentPreviewAdapter.OnAttachmentUploadErrorListener() {
                    @Override
                    public void onAttachmentUploadError(QBResponseException e) {
                        showErrorSnackbar(0, e, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                            }
                        });
                    }
                });
        AttachmentPreviewAdapterView previewAdapterView = (AttachmentPreviewAdapterView)this.findViewById(R.id.adapter_view_attachment_preview);
        previewAdapterView.setAdapter(attachmentPreviewAdapter);
    }


    public void showMessage(QBChatMessage message) {
        if (chatAdapter != null) {
            chatAdapter.add(message);
            scrollMessageListDown();
        } else {
            if (unShownMessages == null) {
                unShownMessages = new ArrayList<>();
            }
            unShownMessages.add(message);
        }
    }

    public void onSendChatClick(View view) {
        int totalAttachmentsCount = attachmentPreviewAdapter.getCount();
        Collection<QBAttachment> uploadedAttachments = attachmentPreviewAdapter.getUploadedAttachments();
        if (!uploadedAttachments.isEmpty()) {
            if (uploadedAttachments.size() == totalAttachmentsCount) {
                for (QBAttachment attachment : uploadedAttachments) {
                    attachment.getType();
                    sendChatMessage(null, attachment);
                }
            } else {
                Utils.showToast(this, getResources().getString(R.string.chat_wait_for_attachments_to_upload));
            }
        }

        String text = messageEditText.getText().toString().trim();
        if (!TextUtils.isEmpty(text)) {
            sendChatMessage(text, null);
        }
    }

    private void sendChatMessage(String text, QBAttachment attachment) {
        QBChatMessage chatMessage = new QBChatMessage();
        if (attachment != null) {
            chatMessage.addAttachment(attachment);
        } else {
            chatMessage.setBody(text);
        }
        chatMessage.setProperty(PROPERTY_SAVE_TO_HISTORY, "1");
        chatMessage.setDateSent(System.currentTimeMillis() / 1000);

        try {
            privateChat.sendMessage(chatMessage);
            if (blacker_id == 0 || blacker_id == Integer.parseInt(Utils.getFromPreference(this, Constant.USER_ID))) {
                sendPush(Utils.getFromPreference(this, Constant.QB_ID), Utils.getFromPreference(this, Constant.FULLNAME), "white");
            } else {
                sendPush(Utils.getFromPreference(this, Constant.QB_ID), Utils.getFromPreference(this, Constant.FULLNAME), "black");
            }
            if (qbDialog.getType() == QBDialogType.PRIVATE) {
                showMessage(chatMessage);
            }

            if (attachment != null) {
                attachmentPreviewAdapter.remove(attachment);
            } else {
                messageEditText.setText("");
            }
        } catch ( SmackException e) {
            Log.e(TAG, "Failed to send a message", e);
            Utils.showToast(this, getResources().getString(R.string.chat_send_message_error));
        }
    }

    private void scrollMessageListDown() {
        messagesListView.setSelection(messagesListView.getCount() - 1);
    }

    private void sendPush(String id, String name, String type) {
        // recipients
        StringifyArrayList<Integer> userIds = new StringifyArrayList<Integer>();
//        userIds.add(Integer.valueOf(id));
        userIds.add(Integer.valueOf(opponentID));

        QBEvent event = new QBEvent();
        event.setUserIds(userIds);
        event.setEnvironment(QBEnvironment.DEVELOPMENT);
        event.setNotificationType(QBNotificationType.PUSH);
        event.setPushType(QBPushType.GCM);
        HashMap<String, String> data = new HashMap<String, String>();
        data.put("user_id", id);
        data.put("message", "You received message from " + name);
        data.put("type", type);
        event.setMessage(data.toString());

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



























    QBMessageListener<QBPrivateChat> privateChatQBMessageListener = new QBMessageListener<QBPrivateChat>() {
        @Override
        public void processMessage(QBPrivateChat qbPrivateChat, final QBChatMessage qbChatMessage) {
//            showMessage(qbChatMessage);
//            Utils.showToast(ChatActivity.this, "Received message");

        }

        @Override
        public void processError(QBPrivateChat qbPrivateChat, QBChatException e, QBChatMessage qbChatMessage) {
            Utils.showToast(ChatActivity.this, "Failed in receiving message");
        }
    };
    QBPrivateChatManagerListener privateChatManagerListener = new QBPrivateChatManagerListener() {
        @Override
        public void chatCreated(QBPrivateChat qbPrivateChat, boolean b) {
            qbPrivateChat.addMessageListener(privateChatQBMessageListener);
        }
    };
    private QBPrivateChat initQBPrivateChat() {
        privateChat= ChatHelper.getInstance().getPrivateChat(Integer.parseInt(opponentID));
        if (privateChat == null) {
            privateChat = ChatHelper.getInstance().createChat(Integer.parseInt(opponentID), privateChatQBMessageListener);
        }
        privateChat.addIsTypingListener(privateChatIsTypingListener);
        return privateChat;
    }
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
    private void updateStatus(boolean state) {
        if (state) {
            tvOnlineStatus.setText(getResources().getString(R.string.online));
        } else {
            tvOnlineStatus.setText(getResources().getString(R.string.offline));
        }
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
        params.put(Constant.DEVICE_TOKEN, Utils.getFromPreference(this, Constant.DEVICE_TOKEN));
        params.put("my_id", Utils.getFromPreference(this, Constant.USER_ID));
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
                                Utils.showOKDialog(ChatActivity.this, getResources().getString(R.string.access_denied));
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
                        Toast.makeText(ChatActivity.this, error.toString(), Toast.LENGTH_LONG).show();
                    }
                });

        requestQueue.add(signinRequest);
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        setDataAndFinish();
    }

    private void setDataAndFinish() {
        Global.getInstance().isChatting = false;
        Global.getInstance().currentChattingUser = "";
        Global.getInstance().currentChattingPage = "";

//        if (currentChatPage.equals("checkin_chat")) {
//        }
        Intent intent = getIntent();
        intent.putExtra("dialog", qbDialog);
        setResult(RESULT_OK, intent);

        finish();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
            timer.purge();
        }

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

                    Cursor cursor = getContentResolver().query(
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
                        File file = new File(photoPath);
                        attachmentPreviewAdapter.add(file);
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
                        File file = new File(videoPath);
                        attachmentPreviewAdapter.add(file);

                    }
                }
                break;
            case take_video_from_camera:
                if (resultCode == Activity.RESULT_OK) {
                    if (videoPath.length() > 0) {
                        File file = new File(videoPath);
                        attachmentPreviewAdapter.add(file);


                    }
                }
                break;
        }
    }

    public String getVideoPath(Uri uri) {

        String path = "";
        try {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            cursor.moveToFirst();
            String document_id = cursor.getString(0);
            document_id = document_id.substring(document_id.lastIndexOf(":")+1);
            cursor.close();

            cursor = getContentResolver().query(android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
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

    private String photoPath, videoPath, thumbPath;
    private void initMediaPath() {
        photoPath = "";
        videoPath = "";
        thumbPath = "";
    }

    //    choose video
    private void showVideoChooseDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
                Toast.makeText(this, "Failed to create directory HeyoeVideo.",
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

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
            File file = new File(photoPath);
            attachmentPreviewAdapter.add(file);
        }

        //crop thumbnail
//        Bitmap cropBitmap = BitmapUtility.cropBitmapCenter(bitmap);
//        Bitmap cropBitmap = BitmapUtility.cropBitmapAnySize(bitmap, bitmap.getWidth(), bitmap.getWidth());
        // save croped thumbnail
//        thumbPath = BitmapUtility.saveBitmap(cropBitmap, Constant.MEDIA_PATH, "heyoe_thumb");
    }
    int imageWidth = 0 , imageHeight = 0;
}
