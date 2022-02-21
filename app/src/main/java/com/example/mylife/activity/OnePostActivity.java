package com.example.mylife.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.mylife.R;
import com.example.mylife.adapter.ItemClickListener;
import com.example.mylife.item.Post;
import com.example.mylife.item.User;
import com.example.mylife.util.DialogHelper;
import com.example.mylife.util.MethodHelper;
import com.example.mylife.util.NetworkConnection;
import com.example.mylife.util.RetrofitHelper;
import com.google.android.material.snackbar.Snackbar;

import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.mylife.MyApplication.LOGIN_TYPE;
import static com.example.mylife.MyApplication.PROFILE_IMAGE_URL;
import static com.example.mylife.MyApplication.USER_EMAIL;
import static com.example.mylife.MyApplication.USER_IDX;
import static com.example.mylife.MyApplication.USER_NAME;
import static com.example.mylife.MyApplication.USER_SESSION;
import static com.example.mylife.util.NetworkConnection.TYPE_NOT_CONNECTED;

public class OnePostActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = "OnePostActivity";
    private final RetrofitHelper retrofitHelper = RetrofitHelper.getInstance();
    private final NetworkConnection networkConnection = NetworkConnection.getInstance();
    private final DialogHelper dialogHelper = DialogHelper.getInstance();
    private final MethodHelper methodHelper = MethodHelper.getInstance();

    private CircleImageView ivProfile;
    private TextView tvName, tvUploadDate, tvLike, tvLikeCount, tvCommentCount, tvContents;
    private ImageButton ibBack, ibThreeDots;
    private ImageView ivPost, ivComment, ivShare;
    private CheckBox cbHeart;
    private ProgressBar pbLoading;

    private int boardIdx, userIdx, likes, comments;
    private String name, profileImageUrl, imageUrl, contents, createDate;
    private boolean isLike;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_one_post);
        setInitData();
        bindView();
    }

    /**
     * ------------------------------- category 0. 최초 설정 -------------------------------
     */
    private void setInitData() {
        userIdx = getIntent().getIntExtra("user_idx", 0);
        boardIdx = getIntent().getIntExtra("board_idx", 0);

        if (networkConnection.checkNetworkConnection(this) == TYPE_NOT_CONNECTED) {
            dialogHelper.showConfirmDialog(this, dialogHelper.ACTIVITY_FINISH_DIALOG_ID, getString(R.string.no_connected_network));
        } else {
            loadPost();
        }
    }

    private void bindView() {
        /* 뷰 바인드 */
        ibBack = findViewById(R.id.ib_back);
        ivProfile = findViewById(R.id.iv_profile);
        tvName = findViewById(R.id.tv_name);
        tvUploadDate = findViewById(R.id.tv_upload_date);
//        tvLike = findViewById(R.id.tv_like);
        tvLikeCount = findViewById(R.id.tv_like_count);
        tvCommentCount = findViewById(R.id.tv_comment_count);
        ibThreeDots = findViewById(R.id.ib_threedots);
        ivPost = findViewById(R.id.iv_post);
        ivComment = findViewById(R.id.iv_comment);
        ivShare = findViewById(R.id.iv_share);
        cbHeart = findViewById(R.id.cb_heart);
        tvContents = findViewById(R.id.tv_contents);
        pbLoading = findViewById(R.id.pb_loading);

        /* 리스너 관련 */
        ibBack.setOnClickListener(this);
        tvName.setOnClickListener(this);
//        tvLike.setOnClickListener(this);
        ibThreeDots.setOnClickListener(this);
        ivComment.setOnClickListener(this);
        ivShare.setOnClickListener(this);
        cbHeart.setOnClickListener(this);
        cbHeart.setOnCheckedChangeListener(null);
    }

    /**
     * ------------------------------- category 1. 뷰 리스너 -------------------------------
     */
    @Override
    public void onClick(View v) {
        if (tvName.equals(v)) {
            if (USER_IDX == userIdx) {
                // TODO: 나의 이름을 눌렀으면 MyFragment로 이동한다? -> 인스타그램 보니까 마이페이지로 와도 뒤로가기도 되고 잘 되던데, Fragment로 조작하는건가?
                // TODO: 이거는 OtherUserPageActivity나 기타 Fragment로 구현될만한 것들은 Fragment로 구현되도록 바꿔야할듯?
            } else {
                // TODO: 다른 사람의 이름을 눌렀으면 OtherUserPageActivity로 이동한다.
                Intent toOtherUserPageIntent = new Intent(this, OtherUserPageActivity.class);
                toOtherUserPageIntent.putExtra("user_idx", userIdx);
                startActivity(toOtherUserPageIntent);
            }
        } else if (ivComment.equals(v)) {
            Intent toCommentIntent = new Intent(this, CommentActivity.class);
            toCommentIntent.putExtra("board_idx", boardIdx);
            // TODO: 댓글 개수 늘어나거나 줄어들면 HomeFragment로 돌아왔을 때 반영해주기, 근데 이거 유튜브에서도 매번 새로고침해서 고치는건데 되긴하는건가
            startActivity(toCommentIntent);
        } else if (ibThreeDots.equals(v)) {
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
                        Intent toEditPostIntent = new Intent(this, EditPostActivity.class);
                        toEditPostIntent.putExtra("board_idx", boardIdx);
                        toEditPostIntent.putExtra("image_url", imageUrl);
                        toEditPostIntent.putExtra("contents", contents);
                        // TODO: 댓글 개수 늘어나거나 줄어들면 OnePostActivity로 돌아왔을 때 반영해주기
                        startActivity(toEditPostIntent);
                    } else if (selectedText.equals("삭제")) {
                        AlertDialog.Builder deleteDialogBuilder = new AlertDialog.Builder(this);
                        deleteDialogBuilder.setTitle("삭제 확인창").setMessage("정말로 삭제하시겠습니까?");
                        deleteDialogBuilder.setPositiveButton("확인", new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialog, int id)
                            {
                                deletePost(boardIdx);
                            }
                        });
                        deleteDialogBuilder.setNegativeButton("취소", (dialog1, id) -> { });
                        AlertDialog deleteDialog = deleteDialogBuilder.create();
                        deleteDialog.show();
                    }
                });
                AlertDialog postDialog = postDialogBuilder.create();
                postDialog.show();
            }
        } else if (cbHeart.equals(v)) {
            if (networkConnection.checkNetworkConnection(this) == TYPE_NOT_CONNECTED) {
                dialogHelper.showConfirmDialog(this, dialogHelper.ACTIVITY_FINISH_DIALOG_ID, getString(R.string.no_connected_network));
            } else {
                if (isLike) {
                    isLike = false;
                    updateLikePost(isLike);
                } else {
                    isLike = true;
                    updateLikePost(isLike);
                }
            }
        } else if (ibBack.equals(v)) {
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    /**
     * ------------------------------- category ?. 서버 통신 -------------------------------
     */
    private void loadPost() {
        Call<Post> callReadPost = retrofitHelper.getRetrofitInterFace().readPost(USER_SESSION, USER_IDX, boardIdx);
        callReadPost.enqueue(new Callback<Post>() {
            @Override
            public void onResponse(@NotNull Call<Post> call, @NotNull Response<Post> response) {
                pbLoading.setVisibility(View.GONE);
                switch (response.code()) {
                    case 400:
                        retrofitHelper.printRetrofitResponse(TAG, response);
                        dialogHelper.showConfirmDialog(OnePostActivity.this, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.client_error_message));
                        break;

                    case 500:
                        retrofitHelper.printRetrofitResponse(TAG, response);
                        dialogHelper.showConfirmDialog(OnePostActivity.this, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.server_error_message));
                        break;

                    case 200:
                        assert response.body() != null;
                        boardIdx = response.body().getBoardIdx();
                        userIdx = response.body().getUserIdx(); // 글을 작성한 유저의 인덱스
                        name = response.body().getName();
                        profileImageUrl = response.body().getProfileImageUrl();
                        imageUrl = response.body().getImageUrl();
                        likes = response.body().getLikes();
                        comments = response.body().getComments();
                        contents = response.body().getContents();
                        isLike = response.body().isLike();
                        try {
                            createDate = response.body().getCreateDate();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        Log.d(TAG, "loadPost - boardIdx : " + boardIdx);
                        Log.d(TAG, "loadPost - userIdx : " + userIdx);
                        Log.d(TAG, "loadPost - name : " + name);
                        Log.d(TAG, "loadPost - profileImageUrl : " + profileImageUrl);
                        Log.d(TAG, "loadPost - imageUrl : " + imageUrl);
                        Log.d(TAG, "loadPost - contents : " + contents);
                        Log.d(TAG, "loadPost - likes : " + likes);
                        Log.d(TAG, "loadPost - comments : " + comments);
                        Log.d(TAG, "loadPost - isLike : " + isLike);
                        Log.d(TAG, "loadPost - createDate : " + createDate);

                        // TODO: 이미지 묶는 거랑, 그냥 이렇게 데이터 받아와서 세팅해주는 게 약간 줏대가 없다. 밑에 코드를 setInitData에 보내면 View가 Bind가 안되어있고, bindView를 먼저하면 데이터가 안 들어와있고...
                        Glide.with(OnePostActivity.this).load(profileImageUrl).into(ivProfile);
                        tvName.setText(name);
                        tvUploadDate.setText(createDate);
                        Glide.with(OnePostActivity.this).load(imageUrl).into(ivPost);
                        tvLikeCount.setText(String.valueOf(likes));
                        tvCommentCount.setText(String.valueOf(comments));
                        tvContents.setText(contents);
                        cbHeart.setChecked(isLike);
                        break;
                }
            }

            @Override
            public void onFailure(@NotNull Call<Post> call, @NotNull Throwable t) {
                Log.e(TAG, "loadPost - onFailure : " + t.getMessage());
                pbLoading.setVisibility(View.GONE);
                dialogHelper.showConfirmDialog(OnePostActivity.this, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.network_not_stable));
            }
        });
    }

    private void deletePost(int boardIdx) {
        Call<Void> callDeletePost = retrofitHelper.getRetrofitInterFace().deletePost(USER_SESSION, USER_IDX, boardIdx);
        callDeletePost.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NotNull Call<Void> call, @NotNull Response<Void> response) {
                switch (response.code()) {
                    case 500:
                        retrofitHelper.printRetrofitResponse(TAG, response);
                        dialogHelper.showConfirmDialog(OnePostActivity.this, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.server_error_message));
                        break;

                    case 400:
                        retrofitHelper.printRetrofitResponse(TAG, response);
                        dialogHelper.showConfirmDialog(OnePostActivity.this, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.client_error_message));
                        break;

                    case 200:
                        finish();
                        break;
                }
            }

            @Override
            public void onFailure(@NotNull Call<Void> call, @NotNull Throwable t) {
                dialogHelper.showConfirmDialog(OnePostActivity.this, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.network_not_stable));
                Log.d(TAG, "deletePost - onFailure : " + t.getMessage());
            }
        });
    }

    // 좋아요 상태에 따라 좋아요 업데이트 해주는 메소드 : 중복되는 메소드<-???
    private void updateLikePost(boolean isLike) {
        String type = "POST";
        Call<Post> callUpdateLikePost = retrofitHelper.getRetrofitInterFace().updateLikePost(USER_SESSION, USER_IDX, type, boardIdx, isLike);
        callUpdateLikePost.enqueue(new Callback<Post>() {
            @Override
            public void onResponse(@NotNull Call<Post> call, @NotNull Response<Post> response) {
                switch (response.code()) {
                    case 500:
                        retrofitHelper.printRetrofitResponse(TAG, response);

                        // 에러 발생 시 좋아요를 반영하지 않음
                        cbHeart.setChecked(!isLike);

                        dialogHelper.showConfirmDialog(OnePostActivity.this, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.server_error_message));
                        break;

                    case 400:
                        retrofitHelper.printRetrofitResponse(TAG, response);

                        // 에러 발생 시 좋아요를 반영하지 않음
                        cbHeart.setChecked(!isLike);

                        dialogHelper.showConfirmDialog(OnePostActivity.this, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.client_error_message));
                        break;

                    case 204:
                        retrofitHelper.printRetrofitResponse(TAG, response);
                        break;

                    case 200:
                        Log.d(TAG, "updateLikePost - onResponse : " + response);
                        assert response.body() != null;
                        boolean isLike = response.body().isLike();
                        int likes = response.body().getLikes();

                        cbHeart.setChecked(isLike);
                        tvLikeCount.setText(String.valueOf(likes));
                        if (isLike) {
                            Snackbar.make(getWindow().getDecorView().getRootView(), R.string.like_plus, Snackbar.LENGTH_LONG).show();
                        } else {
                            Snackbar.make(getWindow().getDecorView().getRootView(), R.string.like_minus, Snackbar.LENGTH_LONG).show();
                        }

                        break;
                }
            }

            @Override
            public void onFailure(@NotNull Call<Post> call, @NotNull Throwable t) {
                Log.d(TAG, "updateLikePost - onFailure : " + t.getMessage());
            }
        });
    }
}