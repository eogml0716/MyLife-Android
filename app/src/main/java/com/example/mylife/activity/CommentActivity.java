package com.example.mylife.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mylife.R;
import com.example.mylife.adapter.CommentAdapter;
import com.example.mylife.adapter.ItemClickListener;
import com.example.mylife.adapter.PostAdapter;
import com.example.mylife.item.Comment;
import com.example.mylife.item.Post;
import com.example.mylife.util.DialogHelper;
import com.example.mylife.util.InfiniteScrollListener;
import com.example.mylife.util.MethodHelper;
import com.example.mylife.util.NetworkConnection;
import com.example.mylife.util.RetrofitHelper;
import com.google.android.material.snackbar.Snackbar;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.mylife.MyApplication.USER_IDX;
import static com.example.mylife.MyApplication.USER_SESSION;
import static com.example.mylife.util.NetworkConnection.TYPE_NOT_CONNECTED;

public class CommentActivity extends AppCompatActivity implements View.OnClickListener, ItemClickListener, SwipeRefreshLayout.OnRefreshListener {
    private final String TAG = "CommentActivity";
    private final RetrofitHelper retrofitHelper = RetrofitHelper.getInstance();
    private final NetworkConnection networkConnection = NetworkConnection.getInstance();
    private final DialogHelper dialogHelper = DialogHelper.getInstance();
    private final MethodHelper methodHelper = MethodHelper.getInstance();

    private SwipeRefreshLayout srRefresh;
    private ProgressBar pbLoading, pbInfiniteScroll; // 프로그래스 바

    private ImageButton ibBack;
    private TextView tvLatestOrder, tvRegistrationOrder, tvNoItem;
    private EditText etComment;
    private Button btnSend;

    private RecyclerView rvComment;
    private LinearLayoutManager layoutManager;
    private ArrayList<Comment> comments;
    private CommentAdapter commentAdapter;
    private InfiniteScrollListener infiniteScrollListener;

    private int responseCode; // HTTP 응답코드, isLast 대신에 사용할 것임
    // 페이징을 위한 변수 : 마지막 페이지인지 확인
    private final int limit = 5;
    private boolean isLast;
    private int boardIdx;

    private boolean isEdit = false;
    private long backKeyPresseTime = 0; // 뒤로가기 버튼 클릭 시간
    private int clickedPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
        bindView();
        buildRecyclerView();
        setInitData();
    }

    /**
     * ------------------------------- category 0. 최초 설정 -------------------------------
     */
    private void setInitData() {
        boardIdx = getIntent().getIntExtra("board_idx", 0);

        if (networkConnection.checkNetworkConnection(this) == TYPE_NOT_CONNECTED) {
            dialogHelper.showConfirmDialog(this, dialogHelper.ACTIVITY_FINISH_DIALOG_ID, getString(R.string.no_connected_network));
        } else {
            loadComments(1);
        }
    }

    private void bindView() {
        ibBack = findViewById(R.id.ib_back);
        tvLatestOrder = findViewById(R.id.tv_latest_order);
        tvRegistrationOrder = findViewById(R.id.tv_registration_order);
        tvNoItem = findViewById(R.id.tv_no_item);
        etComment = findViewById(R.id.et_comment);
        btnSend = findViewById(R.id.btn_send);
        rvComment = findViewById(R.id.rv_comment);
        pbLoading = findViewById(R.id.pb_loading);
        pbInfiniteScroll = findViewById(R.id.pb_infinite_scroll);
        srRefresh = findViewById(R.id.sr_refresh);

        ibBack.setOnClickListener(this);
        tvLatestOrder.setOnClickListener(this);
        tvRegistrationOrder.setOnClickListener(this);
        btnSend.setOnClickListener(this);
        srRefresh.setOnRefreshListener(this);

        tvLatestOrder.setTextColor(Color.parseColor("#000000"));
        tvRegistrationOrder.setTextColor(Color.parseColor("#808080"));

        etComment.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() == 0) {
                    btnSend.setEnabled(false);
                    btnSend.setVisibility(View.INVISIBLE);
                } else {
                    btnSend.setEnabled(true);
                    btnSend.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                etComment.requestFocus();
                if (s.toString().trim().length() == 0) {
                    btnSend.setEnabled(false);
                    btnSend.setVisibility(View.INVISIBLE);
                } else {
                    btnSend.setEnabled(true);
                    btnSend.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void buildRecyclerView() {
        comments = new ArrayList<>();
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        layoutManager.setReverseLayout(false);
        layoutManager.setStackFromEnd(false);
        commentAdapter = new CommentAdapter(this, comments, this);
        infiniteScrollListener = new InfiniteScrollListener(layoutManager, 4) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView recyclerView) {
                if (responseCode == 200) {
                    if (networkConnection.checkNetworkConnection(CommentActivity.this) == TYPE_NOT_CONNECTED) {
                        dialogHelper.showConfirmDialog(CommentActivity.this, dialogHelper.ACTIVITY_FINISH_DIALOG_ID, getString(R.string.no_connected_network));
                    } else {
                        loadComments(page);
                    }
                }
            }

            @Override
            public void onLastVisibleItemPosition(int lastVisibleItemPosition) {
                if (responseCode == 200) {
                    int lastItemPosition = comments.size() - 1;
                    if (srRefresh.isRefreshing()) return;
                    if (lastItemPosition == lastVisibleItemPosition)
                        pbInfiniteScroll.setVisibility(View.VISIBLE);
                }
            }
        };
        RecyclerView.ItemAnimator animator = rvComment.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }
//        rvCommunity.setHasFixedSize(true);
        rvComment.setLayoutManager(layoutManager);
        rvComment.setAdapter(commentAdapter);
        rvComment.addOnScrollListener(infiniteScrollListener);
    }

    /**
     * ------------------------------- category 1. 뷰 리스너 -------------------------------
     */
    @Override
    public void onClickItem(View view, int position) {
        int commentIdx = comments.get(position).getCommentIdx();
        clickedPosition = position;

        switch (view.getId()) {
            case R.id.tv_name:
                // TODO: OtherUserPageActivity로 넘기기
                break;

            case R.id.ib_threedots:
                int userIdx = comments.get(position).getUserIdx();
                // TODO: 수정, 삭제의 경우 클라이언트, 서버에서 둘다 예외 처리 해줄 것
                if (USER_IDX == userIdx) {
                    final List<String> dialogListItems = new ArrayList<>();
                    dialogListItems.add("수정");
                    dialogListItems.add("삭제");
                    dialogListItems.add("닫기");
                    final String[] items = dialogListItems.toArray(new String[dialogListItems.size()]);
                    AlertDialog.Builder postDialogBuilder = new AlertDialog.Builder(this);
                    postDialogBuilder.setItems(items, (dialog, pos) -> {
                        String selectedText = items[pos];
                        if (selectedText.equals("수정")) {
                            isEdit = true;
                            String contents = comments.get(position).getContents().trim();
                            etComment.setText(contents);
                            btnSend.post(() -> btnSend.setText("수정"));
                            showKeyboard();
                        } else if (selectedText.equals("삭제")) {
                            AlertDialog.Builder deleteDialogBuilder = new AlertDialog.Builder(this);
                            deleteDialogBuilder.setTitle("삭제 확인창").setMessage("정말로 삭제하시겠습니까?");
                            deleteDialogBuilder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    deleteComment(commentIdx, position);
                                }
                            });
                            deleteDialogBuilder.setNegativeButton("취소", (dialog1, id) -> {
                            });
                            AlertDialog deleteDialog = deleteDialogBuilder.create();
                            deleteDialog.show();
                        }
                    });
                    AlertDialog postDialog = postDialogBuilder.create();
                    postDialog.show();
                } else {
                    return;
                }

                break;

            case R.id.cb_heart:
                if (networkConnection.checkNetworkConnection(this) == TYPE_NOT_CONNECTED) {
                    dialogHelper.showConfirmDialog(this, dialogHelper.ACTIVITY_FINISH_DIALOG_ID, getString(R.string.no_connected_network));
                } else {
                    updateLikeComment(comments.get(position).isLike(), position);
                }
                break;
        }
    }

    @Override
    public void onClick(View v) {
        if (ibBack.equals(v)) {
            finish();
        } else if (btnSend.equals(v)) {
            if (isEdit) {
                String contents = etComment.getText().toString().trim();
                int commentIdx = comments.get(clickedPosition).getCommentIdx();
                updateComment(commentIdx, contents, clickedPosition);
            } else {
                String contents = etComment.getText().toString().trim();
                uploadComment(contents);
            }
        } else if (tvLatestOrder.equals(v)) {
            runOnUiThread(() -> {
                tvLatestOrder.setTextColor(Color.parseColor("#000000"));
                tvRegistrationOrder.setTextColor(Color.parseColor("#808080"));
                layoutManager.setReverseLayout(false);
                layoutManager.setStackFromEnd(false);
                rvComment.setLayoutManager(layoutManager);
            });
        } else if (tvRegistrationOrder.equals(v)) {
            runOnUiThread(() -> {
                tvLatestOrder.setTextColor(Color.parseColor("#808080"));
                tvRegistrationOrder.setTextColor(Color.parseColor("#000000"));
                layoutManager.setReverseLayout(true);
                layoutManager.setStackFromEnd(true);
                rvComment.setLayoutManager(layoutManager);
            });
        }
    }

    @Override
    public void onRefresh() {
        infiniteScrollListener.resetState();
        int itemCount = comments.size();
        comments.clear();
        commentAdapter.notifyItemRangeRemoved(0, itemCount);
        loadComments(1);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // 댓글 수정하기 작업 중 일 경우
        if (isEdit) {
            // 1번째 백버튼 클릭
            if (System.currentTimeMillis() > backKeyPresseTime + 2000) {
                backKeyPresseTime = System.currentTimeMillis();
                Toast.makeText(this, R.string.update_comment_backpressed, Toast.LENGTH_SHORT).show();
            }
            // 2초 이내로 백버튼 한번 더 클릭시 수정하기 종료
            else {
                finish();
                overridePendingTransition(0, 0);
                startActivity(getIntent());
                overridePendingTransition(0, 0);
            }
        }

        if (!isEdit) {
            // 댓글 수정하기 작업 중이 아닐 경우
            finish();
        }
    }

    /**
     * ------------------------------- category 3. 서버 통신 -------------------------------
     */
    private void loadComments(int page) {
        Call<Comment> callReadComments = retrofitHelper.getRetrofitInterFace().readComments(USER_SESSION, USER_IDX, page, limit, boardIdx);
        callReadComments.enqueue(new Callback<Comment>() {
            @Override
            public void onResponse(@NotNull Call<Comment> call, @NotNull Response<Comment> response) {
                srRefresh.setRefreshing(false);
                pbLoading.setVisibility(View.GONE);
                pbInfiniteScroll.setVisibility(View.GONE);

                switch (response.code()) {
                    case 500:
                        retrofitHelper.printRetrofitResponse(TAG, response);
                        dialogHelper.showConfirmDialog(CommentActivity.this, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.server_error_message));
                        break;

                    case 400:
                        retrofitHelper.printRetrofitResponse(TAG, response);
                        dialogHelper.showConfirmDialog(CommentActivity.this, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.client_error_message));
                        break;

                    case 204:
                        Log.d(TAG, "loadComments - onResponse : " + responseCode);
                        responseCode = 204;
                        break;

                    case 200:
                        Comment comment = response.body();
                        responseCode = 200;
                        int startPosition = comments.size();
                        assert comment != null;
                        comments.addAll(comment.getComments()); // 서버에서 응답받은 페이지의 리스트에 데이터 추가
                        int totalItemCount = comments.size();
                        commentAdapter.notifyItemRangeInserted(startPosition, totalItemCount - startPosition); // 어뎁터에서 추가된 데이터 업데이트

                        if (comments.size() == 0) {
                            tvNoItem.setVisibility(View.VISIBLE);
                            rvComment.setVisibility(View.INVISIBLE);
                        } else {
                            tvNoItem.setVisibility(View.INVISIBLE);
                            rvComment.setVisibility(View.VISIBLE);
                        }
                        break;
                }
            }

            @Override
            public void onFailure(@NotNull Call<Comment> call, @NotNull Throwable t) {
                srRefresh.setRefreshing(false);
                pbLoading.setVisibility(View.GONE);
                pbInfiniteScroll.setVisibility(View.GONE);
                dialogHelper.showConfirmDialog(CommentActivity.this, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.network_not_stable));
                Log.d(TAG, "loadComments - onFailure : " + t.getMessage());
            }
        });
    }

    private void uploadComment(String contents) {
        Call<Void> callCreateComment = retrofitHelper.getRetrofitInterFace().createComment(USER_SESSION, USER_IDX, boardIdx, contents);
        callCreateComment.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NotNull Call<Void> call, @NotNull Response<Void> response) {
                switch (response.code()) {
                    case 500:
                        retrofitHelper.printRetrofitResponse(TAG, response);
                        dialogHelper.showConfirmDialog(CommentActivity.this, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.server_error_message));
                        break;

                    case 400:
                        retrofitHelper.printRetrofitResponse(TAG, response);
                        dialogHelper.showConfirmDialog(CommentActivity.this, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.client_error_message));
                        break;

                    case 200:
                        // TODO: 리사이클러뷰 아이템 추가 관련 메소드로 변경하기
                        onRefresh();
                        break;
                }
            }

            @Override
            public void onFailure(@NotNull Call<Void> call, @NotNull Throwable t) {
                dialogHelper.showConfirmDialog(CommentActivity.this, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.network_not_stable));
                Log.d(TAG, "uploadComment - onFailure : " + t.getMessage());
            }
        });
    }

    private void updateComment(int commentIdx, String contents, int position) {
        Call<Comment> callUpdateComment = retrofitHelper.getRetrofitInterFace().updateComment(USER_SESSION, USER_IDX, boardIdx, commentIdx, contents);
        callUpdateComment.enqueue(new Callback<Comment>() {
            @Override
            public void onResponse(@NotNull Call<Comment> call, @NotNull Response<Comment> response) {
                switch (response.code()) {
                    case 500:
                        retrofitHelper.printRetrofitResponse(TAG, response);
                        dialogHelper.showConfirmDialog(CommentActivity.this, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.server_error_message));
                        break;

                    case 400:
                        retrofitHelper.printRetrofitResponse(TAG, response);
                        dialogHelper.showConfirmDialog(CommentActivity.this, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.client_error_message));
                        break;

                    case 200:
                        Snackbar.make(getWindow().getDecorView().getRootView(), R.string.update_comment, Snackbar.LENGTH_LONG).show();
                        isEdit = true;
                        etComment.setText("");
                        btnSend.setText("등록");
                        // TODO: 이게 굳이 필요할까?, 클라이언트에서 일단 바꿔두고 나중에 새로고침하면 서버꺼 받아오고 대충 이런식인가
                        comments.get(position).setContents(contents);
                        commentAdapter.notifyItemChanged(position);
                        commentAdapter.notifyItemRangeChanged(position, comments.size());
                        hideKeyboard();

                        if (comments.size() == 0) {
                            tvNoItem.setVisibility(View.VISIBLE);
                            rvComment.setVisibility(View.INVISIBLE);
                        } else {
                            tvNoItem.setVisibility(View.INVISIBLE);
                            rvComment.setVisibility(View.VISIBLE);
                        }
                        break;
                }
            }

            @Override
            public void onFailure(@NotNull Call<Comment> call, @NotNull Throwable t) {
                dialogHelper.showConfirmDialog(CommentActivity.this, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.network_not_stable));
                Log.d(TAG, "updateComment - onFailure : " + t.getMessage());
            }
        });
    }

    private void updateLikeComment(boolean isLike, int position) {
        String type = "COMMENT";
        int commentIdx = comments.get(position).getCommentIdx();
        Call<Comment> callUpdateLikeComment = retrofitHelper.getRetrofitInterFace().updateLikeComment(USER_SESSION, USER_IDX, type, commentIdx, isLike);
        callUpdateLikeComment.enqueue(new Callback<Comment>() {
            @Override
            public void onResponse(@NotNull Call<Comment> call, @NotNull Response<Comment> response) {
                switch (response.code()) {
                    case 500:
                        retrofitHelper.printRetrofitResponse(TAG, response);

                        // 에러 발생 시 좋아요를 반영하지 않음
                        comments.get(position).setLike(!isLike);
                        commentAdapter.notifyItemChanged(position);
                        commentAdapter.notifyItemRangeChanged(position, comments.size());

                        dialogHelper.showConfirmDialog(CommentActivity.this, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.server_error_message));
                        break;

                    case 400:
                        retrofitHelper.printRetrofitResponse(TAG, response);

                        // 에러 발생 시 좋아요를 반영하지 않음
                        comments.get(position).setLike(!isLike);
                        commentAdapter.notifyItemChanged(position);
                        commentAdapter.notifyItemRangeChanged(position, comments.size());

                        dialogHelper.showConfirmDialog(CommentActivity.this, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.client_error_message));
                        break;

                    case 204:
                        retrofitHelper.printRetrofitResponse(TAG, response);
                        break;

                    case 200:
                        Log.d(TAG, "updateLikeComment - onResponse : " + response);
                        assert response.body() != null;
                        boolean isLike = response.body().isLike();
                        int likes = response.body().getLikes();

                        comments.get(position).setLike(isLike);
                        comments.get(position).setLikes(likes);
                        commentAdapter.notifyItemChanged(position);
                        commentAdapter.notifyItemRangeChanged(position, comments.size());

                        if (isLike) {
                            Snackbar.make(getWindow().getDecorView().getRootView(), R.string.like_plus, Snackbar.LENGTH_LONG).show();
                        } else {
                            Snackbar.make(getWindow().getDecorView().getRootView(), R.string.like_minus, Snackbar.LENGTH_LONG).show();
                        }

                        break;
                }
            }

            @Override
            public void onFailure(@NotNull Call<Comment> call, @NotNull Throwable t) {
                Log.d(TAG, "updateLikeComment - onFailure : " + t.getMessage());
            }
        });
    }

    private void deleteComment(int commentIdx, int position) {
        Call<Void> callDeleteComment = retrofitHelper.getRetrofitInterFace().deleteComment(USER_SESSION, USER_IDX, commentIdx);
        callDeleteComment.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NotNull Call<Void> call, @NotNull Response<Void> response) {
                switch (response.code()) {
                    case 500:
                        retrofitHelper.printRetrofitResponse(TAG, response);
                        dialogHelper.showConfirmDialog(CommentActivity.this, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.server_error_message));
                        break;

                    case 400:
                        retrofitHelper.printRetrofitResponse(TAG, response);
                        dialogHelper.showConfirmDialog(CommentActivity.this, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.client_error_message));
                        break;

                    case 200:
                        comments.remove(position);
                        commentAdapter.notifyItemRemoved(position);
                        commentAdapter.notifyItemRangeChanged(position, comments.size());
                        Snackbar.make(getWindow().getDecorView().getRootView(), R.string.delete_comment, Snackbar.LENGTH_LONG).show();
                        if (comments.size() == 0) {
                            tvNoItem.setVisibility(View.VISIBLE);
                            rvComment.setVisibility(View.INVISIBLE);
                        } else {
                            tvNoItem.setVisibility(View.INVISIBLE);
                            rvComment.setVisibility(View.VISIBLE);
                        }
                        break;
                }
            }

            @Override
            public void onFailure(@NotNull Call<Void> call, @NotNull Throwable t) {
                dialogHelper.showConfirmDialog(CommentActivity.this, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.network_not_stable));
                Log.d(TAG, "deleteComment - onFailure : " + t.getMessage());
            }
        });
    }

    /**
     * ------------------------------- category ?. 유틸리티 -------------------------------
     */
    private void showKeyboard() {
        etComment.post(() -> {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            etComment.requestFocus();
            if (etComment != null)
                // TODO: 'toggleSoftInput(int, int)' is deprecated 에 의해서 변경한 함수, 제대로 동작하는 지 확인하기
                inputMethodManager.showSoftInput(etComment, InputMethodManager.SHOW_FORCED);
        });
    }

    private void hideKeyboard() {
        etComment.post(() -> {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            etComment.requestFocus();
            if (etComment != null)
                // TODO: 'toggleSoftInput(int, int)' is deprecated 에 의해서 변경한 함수, 제대로 동작하는 지 확인하기
                inputMethodManager.showSoftInput(etComment, InputMethodManager.HIDE_IMPLICIT_ONLY);
        });
    }
}