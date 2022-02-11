package com.example.mylife.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.mylife.R;
import com.example.mylife.adapter.ChatRoomAdapter;
import com.example.mylife.adapter.CommentAdapter;
import com.example.mylife.adapter.ItemClickListener;
import com.example.mylife.item.ChatRoom;
import com.example.mylife.item.Comment;
import com.example.mylife.util.DialogHelper;
import com.example.mylife.util.InfiniteScrollListener;
import com.example.mylife.util.MethodHelper;
import com.example.mylife.util.NetworkConnection;
import com.example.mylife.util.RetrofitHelper;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.mylife.MyApplication.USER_IDX;
import static com.example.mylife.MyApplication.USER_SESSION;
import static com.example.mylife.util.NetworkConnection.TYPE_NOT_CONNECTED;

public class ChatRoomActivity extends AppCompatActivity implements View.OnClickListener, ItemClickListener, SwipeRefreshLayout.OnRefreshListener {
    private final String TAG = "ChatRoomActivity";
    private final RetrofitHelper retrofitHelper = RetrofitHelper.getInstance();
    private final NetworkConnection networkConnection = NetworkConnection.getInstance();
    private final DialogHelper dialogHelper = DialogHelper.getInstance();
    private final MethodHelper methodHelper = MethodHelper.getInstance();

    private SwipeRefreshLayout srRefresh;
    private ProgressBar pbLoading, pbInfiniteScroll; // 프로그래스 바

    private ImageButton ibBack;
    private TextView tvNoItem;

    private RecyclerView rvChatRoom;
    private ArrayList<ChatRoom> chatRooms;
    private ChatRoomAdapter chatRoomAdapter;
    private InfiniteScrollListener infiniteScrollListener;

    private int responseCode; // HTTP 응답코드, isLast 대신에 사용할 것임
    // 페이징을 위한 변수 : 마지막 페이지인지 확인
    private final int limit = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);
        bindView();
        buildRecyclerView();
        setInitData();
    }

    /**
     * ------------------------------- category 0. 최초 설정 -------------------------------
     */
    private void setInitData() {
        if (networkConnection.checkNetworkConnection(this) == TYPE_NOT_CONNECTED) {
            dialogHelper.showConfirmDialog(this, dialogHelper.ACTIVITY_FINISH_DIALOG_ID, getString(R.string.no_connected_network));
        } else {
            loadChatRooms(1);
        }
    }

    private void bindView() {
        ibBack = findViewById(R.id.ib_back);
        tvNoItem = findViewById(R.id.tv_no_item);
        rvChatRoom = findViewById(R.id.rv_chat_room);
        pbLoading = findViewById(R.id.pb_loading);
        pbInfiniteScroll = findViewById(R.id.pb_infinite_scroll);
        srRefresh = findViewById(R.id.sr_refresh);

        ibBack.setOnClickListener(this);
        srRefresh.setOnRefreshListener(this);
    }

    private void buildRecyclerView() {
        chatRooms = new ArrayList<>();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        chatRoomAdapter = new ChatRoomAdapter(this, chatRooms, this);
        infiniteScrollListener = new InfiniteScrollListener(layoutManager, 4) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView recyclerView) {
                if (responseCode == 200) {
                    if (networkConnection.checkNetworkConnection(ChatRoomActivity.this) == TYPE_NOT_CONNECTED) {
                        dialogHelper.showConfirmDialog(ChatRoomActivity.this, dialogHelper.ACTIVITY_FINISH_DIALOG_ID, getString(R.string.no_connected_network));
                    } else {
                        loadChatRooms(page);
                    }
                }
            }

            @Override
            public void onLastVisibleItemPosition(int lastVisibleItemPosition) {
                if (responseCode == 200) {
                    int lastItemPosition = chatRooms.size() - 1;
                    if (srRefresh.isRefreshing()) return;
                    if (lastItemPosition == lastVisibleItemPosition)
                        pbInfiniteScroll.setVisibility(View.VISIBLE);
                }
            }
        };
        RecyclerView.ItemAnimator animator = rvChatRoom.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }
        rvChatRoom.setLayoutManager(layoutManager);
        rvChatRoom.setAdapter(chatRoomAdapter);
        rvChatRoom.addOnScrollListener(infiniteScrollListener);
    }

    /**
     * ------------------------------- category 1. 뷰 리스너 -------------------------------
     */
    @Override
    public void onClickItem(View view, int position) {
        int chatRoomIdx = chatRooms.get(position).getChatRoomIdx();
        Intent toChatIntent = new Intent(this, ChatActivity.class);
        toChatIntent.putExtra("chat_room_idx", chatRoomIdx);
        startActivity(toChatIntent);
    }

    @Override
    public void onClick(View v) {
        if (ibBack.equals(v)) {
            finish();
        }
    }

    @Override
    public void onRefresh() {
        infiniteScrollListener.resetState();
        int itemCount = chatRooms.size();
        chatRooms.clear();
        chatRoomAdapter.notifyItemRangeRemoved(0, itemCount);
        loadChatRooms(1);
    }

    /**
     * ------------------------------- category 3. 서버 통신 -------------------------------
     */
    private void loadChatRooms(int page) {
        Call<ChatRoom> callReadChatRooms = retrofitHelper.getRetrofitInterFace().readChatRooms(USER_SESSION, USER_IDX, page, limit);
        callReadChatRooms.enqueue(new Callback<ChatRoom>() {
            @Override
            public void onResponse(@NotNull Call<ChatRoom> call, @NotNull Response<ChatRoom> response) {
                srRefresh.setRefreshing(false);
                pbLoading.setVisibility(View.GONE);
                pbInfiniteScroll.setVisibility(View.GONE);

                switch (response.code()) {
                    case 500:
                        retrofitHelper.printRetrofitResponse(TAG, response);
                        dialogHelper.showConfirmDialog(ChatRoomActivity.this, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.server_error_message));
                        break;

                    case 400:
                        retrofitHelper.printRetrofitResponse(TAG, response);
                        dialogHelper.showConfirmDialog(ChatRoomActivity.this, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.client_error_message));
                        break;

                    case 204:
                        Log.d(TAG, "loadChatRooms - onResponse : " + responseCode);
                        responseCode = 204;
                        break;

                    case 200:
                        ChatRoom chatRoom = response.body();
                        responseCode = 200;
                        int startPosition = chatRooms.size();
                        assert chatRoom != null;
                        chatRooms.addAll(chatRoom.getChatRooms()); // 서버에서 응답받은 페이지의 리스트에 데이터 추가
                        int totalItemCount = chatRooms.size();
                        chatRoomAdapter.notifyItemRangeInserted(startPosition, totalItemCount - startPosition); // 어뎁터에서 추가된 데이터 업데이트

                        if (chatRooms.size() == 0) {
                            tvNoItem.setVisibility(View.VISIBLE);
                            srRefresh.setVisibility(View.INVISIBLE);
                            rvChatRoom.setVisibility(View.INVISIBLE);
                        } else {
                            tvNoItem.setVisibility(View.INVISIBLE);
                            srRefresh.setVisibility(View.VISIBLE);
                            rvChatRoom.setVisibility(View.VISIBLE);
                        }
                        break;
                }
            }

            @Override
            public void onFailure(@NotNull Call<ChatRoom> call, @NotNull Throwable t) {
                srRefresh.setRefreshing(false);
                pbLoading.setVisibility(View.GONE);
                pbInfiniteScroll.setVisibility(View.GONE);
                dialogHelper.showConfirmDialog(ChatRoomActivity.this, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.network_not_stable));
                Log.d(TAG, "loadChatRooms - onFailure : " + t.getMessage());
            }
        });
    }
}