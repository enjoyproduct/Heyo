package com.heyoe.controller;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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

import com.heyoe.R;
import com.heyoe.controller.adapters.AdapterPrivateChatRoom;
import com.heyoe.model.Constant;
import com.heyoe.model.UserModel;
import com.heyoe.utilities.UIUtility;
import com.heyoe.utilities.Utils;
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
import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.users.model.QBUser;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class ChatActivity extends AppCompatActivity {

    private ImageView ivSendMessage;
    private EditText etMessage;
    private ListView lvContent;
    private TextView tvName, tvOnlineStatus;
    private LinearLayout llChat;

    private String opponentID;
//    private String dialogId;
    public static UserModel opponentUserModel;
    private QBChatService chatService;
    private QBDialog PrivateDialog;
    private QBPrivateChatManager privateChatManager;
    private ArrayList<QBChatMessage> arrHIstoryMessages;
    private AdapterPrivateChatRoom adapterPrivateChat;
    private QBUser me;

    private Timer timer;
    private TimerTask timerTask;
    private int timeCounter = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        initUI();

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        loginChatService();

        initTimer();
    }

    private void initUI() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ImageButton ibBack = (ImageButton)toolbar.findViewById(R.id.ib_back);
        ibBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UIUtility.hideSoftKeyboard(ChatActivity.this);
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
                if (timeCounter > 5) {
                    timeCounter = 0;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
//                sendStopTypingNotification();
            }
        });
        llChat = (LinearLayout)findViewById(R.id.ll_chat);

        if (getIntent().getBooleanExtra("is_black_chat", false)) {
            llChat.setBackgroundColor(getResources().getColor(R.color.black));
            etMessage.setTextColor(getResources().getColor(R.color.white));
        }
        arrHIstoryMessages = new ArrayList<>();
        opponentUserModel = (UserModel) getIntent().getSerializableExtra("userModel");

        opponentID = opponentUserModel.getQb_id();

//        me = (QBUser)getIntent().getSerializableExtra("me");

        tvName.setText(opponentUserModel.getFullname());
        if (opponentUserModel.isOnline()) {
            tvOnlineStatus.setText(getResources().getString(R.string.online));
        } else {
            tvOnlineStatus.setText(getResources().getString(R.string.offline));
        }

        lvContent = (ListView) findViewById(R.id.lv_messages);
        lvContent.setStackFromBottom(true);
        lvContent.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        adapterPrivateChat = new AdapterPrivateChatRoom(ChatActivity.this, arrHIstoryMessages, opponentUserModel.getAvatar());
        lvContent.setAdapter(adapterPrivateChat);


    }

    private void loginChatService()
    {
        Utils.showProgress(this);
        me = new QBUser(Utils.getFromPreference(this, Constant.EMAIL), Constant.DEFAULT_PASSWORD);

        chatService = QBChatService.getInstance();
        chatService.addConnectionListener(connectionListener);

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

    //        /creat private dialog
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

    private void getChatHistory()
    {
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
    private void initTimer() {
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                if (timeCounter == 0) {
                    sendTypingNotification();
                }
                if (timeCounter == 5) {
                    sendStopTypingNotification();
                }
                timeCounter ++;
            }
        };
        timer.schedule(timerTask, 0, 1000);
    }

    private void sendMessage()
    {
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
            chatMessage.setDateSent(System.currentTimeMillis() / 1000);
            chatMessage.setSenderId(Integer.parseInt(Utils.getFromPreference(this, Constant.QB_ID)));
            arrHIstoryMessages.add(chatMessage);
            adapterPrivateChat.updateList(arrHIstoryMessages);

        }catch (Exception e){
            showAlert(e.getLocalizedMessage());
        }
    }

    QBPrivateChat privateChat;
    private QBPrivateChat initQBPrivateChat() {
        privateChat= privateChatManager.getChat(Integer.parseInt(opponentID));
        if (privateChat == null) {
            privateChat = privateChatManager.createChat(Integer.parseInt(opponentID), privateChatQBMessageListener);
        }
        privateChat.addIsTypingListener(privateChatIsTypingListener);
        return privateChat;
    }

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
                    if (opponentUserModel.isOnline()) {
                        tvOnlineStatus.setText(getResources().getString(R.string.online));
                    } else {
                        tvOnlineStatus.setText(getResources().getString(R.string.offline));
                    }
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

    public void showAlert(String message)
    {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void transferAnimation(int i)
    {
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

        chatService.removeConnectionListener(connectionListener);
        chatService.logout(new QBEntityCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid, Bundle bundle) {
                chatService.destroy();
            }

            @Override
            public void onError(QBResponseException e) {

                showAlert(e.getLocalizedMessage());
            }
        });
        UIUtility.hideSoftKeyboard(this);
        super.onDestroy();
    }

}
