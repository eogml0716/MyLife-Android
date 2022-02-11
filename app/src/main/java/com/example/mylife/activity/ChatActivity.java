package com.example.mylife.activity;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.mylife.R;
import com.example.mylife.adapter.ItemClickListener;
import com.example.mylife.adapter.MessageAdapter;
import com.example.mylife.item.ChatRoom;
import com.example.mylife.item.Message;
import com.example.mylife.item.MessageLocation;
import com.example.mylife.util.DialogHelper;
import com.example.mylife.util.InfiniteScrollListener;
import com.example.mylife.util.MethodHelper;
import com.example.mylife.util.NetworkConnection;
import com.example.mylife.util.RetrofitHelper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.mylife.MyApplication.PROFILE_IMAGE_URL;
import static com.example.mylife.MyApplication.USER_IDX;
import static com.example.mylife.MyApplication.USER_NAME;
import static com.example.mylife.MyApplication.USER_SESSION;
import static com.example.mylife.util.NetworkConnection.TYPE_NOT_CONNECTED;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener, ItemClickListener, SwipeRefreshLayout.OnRefreshListener {
    private final String TAG = "ChatActivity";
    private final RetrofitHelper retrofitHelper = RetrofitHelper.getInstance();
    private final NetworkConnection networkConnection = NetworkConnection.getInstance();
    private final DialogHelper dialogHelper = DialogHelper.getInstance();
    private final MethodHelper methodHelper = MethodHelper.getInstance();

    private SwipeRefreshLayout srRefresh;
    private ProgressBar pbLoading, pbInfiniteScroll; // 프로그래스 바

    private RecyclerView rvMessage;
    private LinearLayoutManager layoutManager;
    private ArrayList<Message> messages;
    private MessageAdapter messageAdapter;

    private ImageButton ibBack, ibImage;
    private TextView tvChatRoomName, tvNoItem;
    private EditText etMessage;
    private Button btnSend;

    private int responseCode; // HTTP 응답코드, isLast 대신에 사용할 것임

    // 페이징을 위한 변수 : 마지막 페이지인지 확인
    private final int limit = 10;

    private int chatRoomIdx, userIdx;
    private String type, chatRoomImageUrl, chatRoomName, openType, lastMessage, lastMessageDate;

    private final int port = 1755;
    private String host;

    private OutputStream outputStream = null;
    private DataOutputStream dataOutputStream = null;
    private InputStream inputStream = null;
    private DataInputStream dataInputStream = null;

    private Thread connectionThread = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        bindView();
        buildRecyclerView();
        setInitData();
    }

    /**
     * ------------------------------- category 0. 최초 설정 -------------------------------
     */
    private void setInitData() {
        String index = Build.ID;
        if (index.equals("RSR1.210210.001.A1")) {
            // 애뮬레이터일 때
            host = "10.0.2.2";
        } else if (index.equals("RP1A.200720.012")) {
            // 기존 디바이스 계정일 때
            host = "192.168.0.21";
        }

        // 1:1 채팅방 구현 관련 내용
        chatRoomIdx = getIntent().getIntExtra("chat_room_idx", 0);
        userIdx = getIntent().getIntExtra("user_idx", 0); // 1:1 채팅방에서 상대방의 유저 인덱스

        if (networkConnection.checkNetworkConnection(this) == TYPE_NOT_CONNECTED) {
            dialogHelper.showConfirmDialog(this, dialogHelper.ACTIVITY_FINISH_DIALOG_ID, getString(R.string.no_connected_network));
        } else {
            if (chatRoomIdx == 0 && userIdx != 0) {
                // 채팅방이 존재하지 않는 경우 - 채팅방을 만든다.
                createChatPersonalRoom();
            } else {
                // 채팅방이 존재하는 경우 - 채팅방 정보를 가져온다.
                loadChatRoomInfo();
                loadMessages(1);
            }
        }

        //상대방의 대화를 받기 위한 스레트를 생성 및 실행
        connectionThread = new Thread(new ConnectionThread());
        connectionThread.start();
        // TODO: 최초 연결 시 chatRoomIdx 값을 보내야함, 그래야지 처음 메시지를 받을 수 있음
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("message_idx", 0);
        jsonObject.addProperty("chat_room_idx", chatRoomIdx);
        jsonObject.addProperty("message_type", "CONNECT");

        String connectJsonString = new Gson().toJson(jsonObject);
        new Thread(new SenderThread(connectJsonString)).start();
        // TODO: 그룹 채팅방 구현 관련 내용 (채팅방 멤버들은 list에 담아서 처리를 해주어야할 거 같다. 채팅방 type값을 가져와서 처리를 해주는 건 필요없는 게 어차피 상대 프로필 들어가서 누르는 건 무조건 1:1 채팅이니까
    }

    private void bindView() {
        ibBack = findViewById(R.id.ib_back);
        ibImage = findViewById(R.id.ib_image);
        tvChatRoomName = findViewById(R.id.tv_chat_room_name);
        tvNoItem = findViewById(R.id.tv_no_item);
        etMessage = findViewById(R.id.et_message);
        btnSend = findViewById(R.id.btn_send);
        rvMessage = findViewById(R.id.rv_message);
        pbLoading = findViewById(R.id.pb_loading);
        pbInfiniteScroll = findViewById(R.id.pb_infinite_scroll);
        srRefresh = findViewById(R.id.sr_refresh);

        ibBack.setOnClickListener(this);
        ibImage.setOnClickListener(this);
        btnSend.setOnClickListener(this);
        srRefresh.setOnRefreshListener(this);

        // messageEdit에 글자가 입력되었을 때에만 전송 버튼 활성화
        etMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                btnSend.setEnabled(s.toString().length() > 0);
            }
        });
    }

    private void buildRecyclerView() {
        messages = new ArrayList<>();
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        messageAdapter = new MessageAdapter(this, messages, this);
        RecyclerView.ItemAnimator animator = rvMessage.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }
        rvMessage.setLayoutManager(layoutManager);
        rvMessage.setAdapter(messageAdapter);
        rvMessage.smoothScrollToPosition(messages.size());
    }

    /**
     * ------------------------------- category 1. 뷰 리스너 -------------------------------
     */
    @Override
    public void onClick(View v) {
        if (ibBack.equals(v)) {
            connectionThread.interrupt();
            finish(); // TODO: finish()가 아니라 아예 activity 끄고 키는 걸로 해야할 거 같은데 chat_room_idx가 다른 사람 프로필 들어갔을 때 새로고침 안하면 안 불러와짐
        } else if (ibImage.equals(v)) {
            // 이미지 메시지 전송
            Intent toPick = new Intent(Intent.ACTION_PICK);
            toPick.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
            imagePickResultLauncher.launch(toPick);
        } else if (btnSend.equals(v)) {
            // 텍스트 메시지 전송
            String contents = etMessage.getText().toString();
            Log.e(TAG, "onClick - btnSend.equals(v) - contents : " + contents);
            if (!contents.isEmpty()) {
                createTextMessage(contents);
            }
        }
    }

    @Override
    public void onRefresh() {
        int itemCount = messages.size();
        messages.clear();
        messageAdapter.notifyItemRangeRemoved(0, itemCount);
        loadMessages(1);
    }

    @Override
    public void onClickItem(View view, int position) {

    }

    /**
     * ------------------------------- category 3. 서버 통신 -------------------------------
     */
    private void loadChatRoomInfo() {
        Call<ChatRoom> callReadChatRoomInfo = retrofitHelper.getRetrofitInterFace().readChatRoomInfo(USER_SESSION, USER_IDX, chatRoomIdx);
        callReadChatRoomInfo.enqueue(new Callback<ChatRoom>() {
            @Override
            public void onResponse(@NotNull Call<ChatRoom> call, @NotNull Response<ChatRoom> response) {
                pbLoading.setVisibility(View.GONE);
                switch (response.code()) {
                    case 400:
                        retrofitHelper.printRetrofitResponse(TAG + "loadChatRoomInfo", response);
                        dialogHelper.showConfirmDialog(ChatActivity.this, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.client_error_message));
                        break;

                    case 500:
                        retrofitHelper.printRetrofitResponse(TAG + "loadChatRoomInfo", response);
                        dialogHelper.showConfirmDialog(ChatActivity.this, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.server_error_message));
                        break;

                    case 200:
                        assert response.body() != null;
                        chatRoomIdx = response.body().getChatRoomIdx();
                        type = response.body().getType();
                        chatRoomImageUrl = response.body().getChatRoomImageUrl();
                        chatRoomName = response.body().getChatRoomName();
                        openType = response.body().getOpenType();
                        lastMessage = response.body().getLastMessage();
                        lastMessageDate = response.body().getLastMessageDate();

                        tvChatRoomName.setText(chatRoomName);
                        break;
                }
            }

            @Override
            public void onFailure(@NotNull Call<ChatRoom> call, @NotNull Throwable t) {
                Log.e(TAG, "loadChatRoomInfo - onFailure : " + t.getMessage());
                pbLoading.setVisibility(View.GONE);
                dialogHelper.showConfirmDialog(ChatActivity.this, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.network_not_stable));
            }
        });
    }

    private void loadMessages(int page) {
        Call<Message> callReadChatMessages = retrofitHelper.getRetrofitInterFace().readChatMessages(USER_SESSION, USER_IDX, page, limit, chatRoomIdx);
        callReadChatMessages.enqueue(new Callback<Message>() {
            @Override
            public void onResponse(@NotNull Call<Message> call, @NotNull Response<Message> response) {
                srRefresh.setRefreshing(false);
                pbLoading.setVisibility(View.GONE);
                pbInfiniteScroll.setVisibility(View.GONE);

                switch (response.code()) {
                    case 500:
                        retrofitHelper.printRetrofitResponse(TAG + "loadMessages", response);
                        dialogHelper.showConfirmDialog(ChatActivity.this, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.server_error_message));
                        break;

                    case 400:
                        retrofitHelper.printRetrofitResponse(TAG + "loadMessages", response);
                        dialogHelper.showConfirmDialog(ChatActivity.this, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.client_error_message));
                        break;

                    case 204:
                        Log.d(TAG, "loadMessages - onResponse : " + responseCode);
                        responseCode = 204;
                        break;

                    case 200:
                        Message message = response.body();
                        responseCode = 200;
                        assert message != null;
                        for (int index = 0; index < message.getMessages().size(); index++) {
                            if (message.getMessages().get(index).getUserIdx() == USER_IDX) {
                                if (message.getMessages().get(index).getMessageType().equals("TEXT")) {
                                    message.getMessages().get(index).setMessageLocation(MessageLocation.RIGHT_CONTENTS);
                                } else if (message.getMessages().get(index).getMessageType().equals("IMAGE")) {
                                    message.getMessages().get(index).setMessageLocation(MessageLocation.RIGHT_IMAGE);
                                }
                            } else {
                                if (message.getMessages().get(index).getMessageType().equals("TEXT")) {
                                    message.getMessages().get(index).setMessageLocation(MessageLocation.LEFT_CONTENTS);
                                } else if (message.getMessages().get(index).getMessageType().equals("IMAGE")) {
                                    message.getMessages().get(index).setMessageLocation(MessageLocation.LEFT_IMAGE);
                                }
                            }
                        }
                        messages.addAll(message.getMessages()); // 서버에서 응답받은 페이지의 리스트에 데이터 추가
                        messageAdapter.notifyItemRangeInserted(0, messages.size()); // 어뎁터에서 추가된 데이터 업데이트

                        // TODO: 새로 아이템 갯수가 갱신 되어도 계속해서 tvNoItem이 뜨는 경우가 있어서 해놓음, 코드 간소화 시키기
                        if (messages.size() == 0) {
                            tvNoItem.setVisibility(View.VISIBLE);
                            srRefresh.setVisibility(View.INVISIBLE);
                            rvMessage.setVisibility(View.INVISIBLE);
                        } else {
                            tvNoItem.setVisibility(View.INVISIBLE);
                            srRefresh.setVisibility(View.VISIBLE);
                            rvMessage.setVisibility(View.VISIBLE);
                        }
                        break;
                }
            }

            @Override
            public void onFailure(@NotNull Call<Message> call, @NotNull Throwable t) {
                srRefresh.setRefreshing(false);
                pbLoading.setVisibility(View.GONE);
                pbInfiniteScroll.setVisibility(View.GONE);
                dialogHelper.showConfirmDialog(ChatActivity.this, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.network_not_stable));
                Log.d(TAG, "loadMessages - onFailure : " + t.getMessage());
            }
        });
    }

    private void createChatPersonalRoom() {
        Call<ChatRoom> callCreateChatPersonalRoom = retrofitHelper.getRetrofitInterFace().createChatPersonalRoom(USER_SESSION, USER_IDX, userIdx);
        callCreateChatPersonalRoom.enqueue(new Callback<ChatRoom>() {
            @Override
            public void onResponse(@NotNull Call<ChatRoom> call, @NotNull Response<ChatRoom> response) {
                srRefresh.setRefreshing(false);
                pbLoading.setVisibility(View.GONE);
                pbInfiniteScroll.setVisibility(View.GONE);

                switch (response.code()) {
                    case 400:
                        retrofitHelper.printRetrofitResponse(TAG + "createChatPersonalRoom", response);
                        dialogHelper.showConfirmDialog(ChatActivity.this, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.client_error_message));
                        break;

                    case 500:
                        retrofitHelper.printRetrofitResponse(TAG + "createChatPersonalRoom", response);
                        dialogHelper.showConfirmDialog(ChatActivity.this, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.server_error_message));
                        break;

                    case 200:
                        assert response.body() != null;
                        chatRoomIdx = response.body().getChatRoomIdx();
                        type = response.body().getType();
                        chatRoomImageUrl = response.body().getChatRoomImageUrl();
                        chatRoomName = response.body().getChatRoomName();
                        openType = response.body().getOpenType();
                        lastMessage = response.body().getLastMessage();
                        lastMessageDate = response.body().getLastMessageDate();

                        tvChatRoomName.setText(chatRoomName);
                        break;
                }
            }

            @Override
            public void onFailure(@NotNull Call<ChatRoom> call, @NotNull Throwable t) {
                Log.e(TAG, "uploadPost - callCreatePost failed : " + t.getMessage());
                dialogHelper.showConfirmDialog(ChatActivity.this, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.network_not_stable));
                srRefresh.setRefreshing(false);
                pbLoading.setVisibility(View.GONE);
                pbInfiniteScroll.setVisibility(View.GONE);
            }
        });
    }

    private void createTextMessage(String contents) {
        Call<Message> callCreateComment = retrofitHelper.getRetrofitInterFace().createChatTextMessage(USER_SESSION, USER_IDX, chatRoomIdx, contents);
        callCreateComment.enqueue(new Callback<Message>() {
            @Override
            public void onResponse(@NotNull Call<Message> call, @NotNull Response<Message> response) {
                switch (response.code()) {
                    case 500:
                        retrofitHelper.printRetrofitResponse(TAG + "createTextMessage", response);
                        dialogHelper.showConfirmDialog(ChatActivity.this, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.server_error_message));
                        break;

                    case 400:
                        retrofitHelper.printRetrofitResponse(TAG + "createTextMessage", response);
                        dialogHelper.showConfirmDialog(ChatActivity.this, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.client_error_message));
                        break;

                    case 200:
                        Message message = response.body();
                        assert message != null;
                        message.setMessageLocation(MessageLocation.RIGHT_CONTENTS);
                        JsonObject jsonObject = new JsonObject();
                        jsonObject.addProperty("message_idx", message.getMessageIdx());
                        jsonObject.addProperty("chat_room_idx", message.getChatRoomIdx());
                        jsonObject.addProperty("user_idx", message.getUserIdx());
                        jsonObject.addProperty("name", message.getName());
                        jsonObject.addProperty("profile_image_url", message.getProfileImageUrl());
                        jsonObject.addProperty("message_type", message.getMessageType());
                        jsonObject.addProperty("contents", message.getContents());
                        jsonObject.addProperty("create_date", message.getCreateDate());
                        jsonObject.addProperty("update_date", message.getUpdateDate());

                        String jsonString = new Gson().toJson(jsonObject);
                        new Thread(new SenderThread(jsonString)).start();
                        messages.add(message);
                        messageAdapter.notifyDataSetChanged();
                        rvMessage.smoothScrollToPosition(messages.size());
                        etMessage.setText("");

                        // TODO: 새로 아이템 갯수가 갱신 되어도 계속해서 tvNoItem이 뜨는 경우가 있어서 해놓음, 코드 간소화 시키기
                        if (messages.size() == 0) {
                            tvNoItem.setVisibility(View.VISIBLE);
                            srRefresh.setVisibility(View.INVISIBLE);
                            rvMessage.setVisibility(View.INVISIBLE);
                        } else {
                            tvNoItem.setVisibility(View.INVISIBLE);
                            srRefresh.setVisibility(View.VISIBLE);
                            rvMessage.setVisibility(View.VISIBLE);
                        }
                        break;
                }
            }

            @Override
            public void onFailure(@NotNull Call<Message> call, @NotNull Throwable t) {
                dialogHelper.showConfirmDialog(ChatActivity.this, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.network_not_stable));
                Log.d(TAG, "createTextMessage - onFailure : " + t.getMessage());
            }
        });
    }

    private void createImageMessage(Bitmap bitmap) {
        // TODO: Bitmap을 Base64로 인코딩해서 이미지를 서버로 전송하는건데 이거 메소드로 바꿀까...
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        String image = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);
        String imageName = String.valueOf(Calendar.getInstance().getTimeInMillis());

        Call<Message> callCreateChatImageMessage = retrofitHelper.getRetrofitInterFace().createChatImageMessage(USER_SESSION, USER_IDX, chatRoomIdx, image, imageName);
        Log.d(TAG, "createImageMessage - USER_SESSION : " + USER_SESSION);
        Log.d(TAG, "createImageMessage - USER_IDX : " + USER_IDX);
        Log.d(TAG, "createImageMessage - chatRoomIdx : " + chatRoomIdx);
        Log.d(TAG, "createImageMessage - image : " + image);
        Log.d(TAG, "createImageMessage - imageName " + imageName);

        callCreateChatImageMessage.enqueue(new Callback<Message>() {
            @Override
            public void onResponse(@NotNull Call<Message> call, @NotNull Response<Message> response) {
                switch (response.code()) {
                    case 400:
                        retrofitHelper.printRetrofitResponse(TAG + "createImageMessage", response);
                        dialogHelper.showConfirmDialog(ChatActivity.this, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.client_error_message));
                        break;

                    case 500:
                        retrofitHelper.printRetrofitResponse(TAG + "createImageMessage", response);
                        dialogHelper.showConfirmDialog(ChatActivity.this, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.server_error_message));
                        break;

                    case 200:
                        Message message = response.body();
                        assert message != null;
                        message.setMessageLocation(MessageLocation.RIGHT_IMAGE);
                        JsonObject jsonObject = new JsonObject();
                        jsonObject.addProperty("message_idx", message.getMessageIdx());
                        jsonObject.addProperty("chat_room_idx", message.getChatRoomIdx());
                        jsonObject.addProperty("user_idx", message.getUserIdx());
                        jsonObject.addProperty("name", message.getName());
                        jsonObject.addProperty("profile_image_url", message.getProfileImageUrl());
                        jsonObject.addProperty("message_type", message.getMessageType());
                        jsonObject.addProperty("contents", message.getContents());
                        jsonObject.addProperty("create_date", message.getCreateDate());
                        jsonObject.addProperty("update_date", message.getUpdateDate());

                        String jsonString = new Gson().toJson(jsonObject);
                        new Thread(new SenderThread(jsonString)).start();
                        messages.add(message);
                        messageAdapter.notifyDataSetChanged();
                        rvMessage.smoothScrollToPosition(messages.size());

                        if (messages.size() == 0) {
                            tvNoItem.setVisibility(View.VISIBLE);
                            srRefresh.setVisibility(View.INVISIBLE);
                            rvMessage.setVisibility(View.INVISIBLE);
                        } else {
                            tvNoItem.setVisibility(View.INVISIBLE);
                            srRefresh.setVisibility(View.VISIBLE);
                            rvMessage.setVisibility(View.VISIBLE);
                        }
                        break;
                }
            }

            @Override
            public void onFailure(@NotNull Call<Message> call, @NotNull Throwable t) {
                Log.e(TAG, "createImageMessage - callCreatePost failed : " + t.getMessage());
                dialogHelper.showConfirmDialog(ChatActivity.this, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.network_not_stable));
            }
        });
    }

    /**
     * ------------------------------- category 3. 소켓 관련 -------------------------------
     */
    class ConnectionThread implements Runnable {
        public void run() {
            Socket socket;
            try {
                socket = new Socket(host, port);
                socket.setTcpNoDelay(true);
                outputStream = socket.getOutputStream();
                dataOutputStream = new DataOutputStream(outputStream);

                inputStream = socket.getInputStream();
                dataInputStream = new DataInputStream(inputStream);

                runOnUiThread(() -> {
                    Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_LONG).show();
                });
                new Thread(new ReceiverThread()).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class ReceiverThread implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    final String jsonMessage = dataInputStream.readUTF();
                    Log.e(TAG, "서버에서 받은 메시지 : " + jsonMessage);
                    if (!jsonMessage.isEmpty()) {
                        Gson gson = new Gson();
                        Message message = gson.fromJson(jsonMessage, Message.class);
                        runOnUiThread(() -> {
                            if (message.getUserIdx() == USER_IDX) {
                                if (message.getMessageType().equals("TEXT")) {
                                    message.setMessageLocation(MessageLocation.RIGHT_CONTENTS);
                                } else if (message.getMessageType().equals("IMAGE")) {
                                    message.setMessageLocation(MessageLocation.RIGHT_IMAGE);
                                }
                            } else {
                                if (message.getMessageType().equals("TEXT")) {
                                    message.setMessageLocation(MessageLocation.LEFT_CONTENTS);
                                } else if (message.getMessageType().equals("IMAGE")) {
                                    message.setMessageLocation(MessageLocation.LEFT_IMAGE);
                                }
                            }
                            messages.add(message);
                            messageAdapter.notifyDataSetChanged();
                            rvMessage.smoothScrollToPosition(messages.size());
                        });
                    } else {
                        Log.e(TAG, "ReceiverThread - else :  connectionThread = new Thread(new ConnectionThread());");
                        connectionThread = new Thread(new ConnectionThread());
                        connectionThread.start();
                        return;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class SenderThread implements Runnable {
        private String jsonMessage;

        SenderThread(String jsonMessage) {
            this.jsonMessage = jsonMessage;
        }

        @Override
        public void run() {
            try {
                dataOutputStream.writeUTF(jsonMessage);
                dataOutputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            runOnUiThread(() -> {
                Log.e(TAG, "클라이언트가 보낸 메시지 : " + jsonMessage);
                etMessage.setText("");
            });
        }
    }

    /**
     * ------------------------------- category ?. 유틸리티 -------------------------------
     */
    private final ActivityResultLauncher<Intent> imagePickResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
            Intent data = result.getData();
            Uri imageUri = data.getData();
            Log.d(TAG, "registerForActivityResult - result : " + result);
            Log.d(TAG, "registerForActivityResult - data : " + data);
            Log.d(TAG, "registerForActivityResult - imageUri : " + imageUri);
            Bitmap uploadImage = null;
            try {
                ImageDecoder.Source source = ImageDecoder.createSource(this.getContentResolver(), imageUri);
                uploadImage = ImageDecoder.decodeBitmap(source);
            } catch (IOException e) {
                e.printStackTrace();
            }
            assert uploadImage != null;
            createImageMessage(uploadImage);
        }
    });
}