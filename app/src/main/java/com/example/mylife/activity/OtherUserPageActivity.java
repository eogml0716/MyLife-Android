package com.example.mylife.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.mylife.R;
import com.example.mylife.adapter.ItemClickListener;
import com.example.mylife.adapter.SquarePostAdapter;
import com.example.mylife.item.Post;
import com.example.mylife.item.User;
import com.example.mylife.util.DialogHelper;
import com.example.mylife.util.InfiniteScrollListener;
import com.example.mylife.util.MethodHelper;
import com.example.mylife.util.NetworkConnection;
import com.example.mylife.util.RetrofitHelper;
import com.google.android.material.snackbar.Snackbar;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.mylife.MyApplication.PROFILE_IMAGE_URL;
import static com.example.mylife.MyApplication.USER_IDX;
import static com.example.mylife.MyApplication.USER_NAME;
import static com.example.mylife.MyApplication.USER_SESSION;
import static com.example.mylife.util.NetworkConnection.TYPE_NOT_CONNECTED;

public class OtherUserPageActivity extends AppCompatActivity implements View.OnClickListener, ItemClickListener, SwipeRefreshLayout.OnRefreshListener {
    private final String TAG = "OtherUserProfileActivity";
    private final RetrofitHelper retrofitHelper = RetrofitHelper.getInstance();
    private final NetworkConnection networkConnection = NetworkConnection.getInstance();
    private final DialogHelper dialogHelper = DialogHelper.getInstance();
    private final MethodHelper methodHelper = MethodHelper.getInstance();

    private SwipeRefreshLayout srRefresh;
    private ProgressBar pbLoading, pbInfiniteScroll; // 프로그래스 바

    private RecyclerView rvSquarePost;
    private ArrayList<Post> posts;
    private SquarePostAdapter squarePostAdapter;
    private InfiniteScrollListener infiniteScrollListener;

    private ImageButton ibBack;

    private int responseCode; // HTTP 응답코드, isLast 대신에 사용할 것임

    // 페이징을 위한 변수 : 마지막 페이지인지 확인
    private final int limit = 9;
    private boolean isLast;
    private boolean isFollow = false;

    private CircleImageView ivProfile;
    private TextView tvPosts, tvPost, tvFollowings, tvFollowing, tvFollowers, tvFollower, tvName, tvAboutMe, tvNoItem;
    private Button btnFollow, btnUnFollow, btnMessage;

    private int userIdx, postCount, followerCount, followingCount, chatRoomIdx;
    private String profileImageUrl, name, aboutMe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_user_page);
        bindView();
        buildRecyclerView();
        setInitData();
    }

    /**
     * ------------------------------- category 0. 최초 설정 -------------------------------
     */
    private void setInitData() {
        userIdx = getIntent().getIntExtra("user_idx", 0);

        if (networkConnection.checkNetworkConnection(this) == TYPE_NOT_CONNECTED) {
            dialogHelper.showConfirmDialog(this, dialogHelper.ACTIVITY_FINISH_DIALOG_ID, getString(R.string.no_connected_network));
        } else {
            loadInfo(userIdx);
            loadInfoPosts(1);
        }
    }

    private void bindView() {
        ibBack = findViewById(R.id.ib_back);
        ivProfile = findViewById(R.id.iv_profile);
        tvPosts = findViewById(R.id.tv_posts);
        tvPost = findViewById(R.id.tv_post);
        tvFollowings = findViewById(R.id.tv_followings);
        tvFollowing = findViewById(R.id.tv_following);
        tvFollowers = findViewById(R.id.tv_followers);
        tvFollower = findViewById(R.id.tv_follower);
        tvName = findViewById(R.id.tv_name);
        tvAboutMe = findViewById(R.id.tv_about_me);
        tvNoItem = findViewById(R.id.tv_no_item);
        btnFollow = findViewById(R.id.btn_follow);
        btnUnFollow = findViewById(R.id.btn_unfollow);
        btnMessage = findViewById(R.id.btn_message);
        rvSquarePost = findViewById(R.id.rv_my_post);
        pbLoading = findViewById(R.id.pb_loading);
        pbInfiniteScroll = findViewById(R.id.pb_infinite_scroll);
        srRefresh = findViewById(R.id.sr_refresh);

        ibBack.setOnClickListener(this);
        tvPosts.setOnClickListener(this);
        tvFollowings.setOnClickListener(this);
        tvFollowers.setOnClickListener(this);
        btnFollow.setOnClickListener(this);
        btnUnFollow.setOnClickListener(this);
        btnMessage.setOnClickListener(this);
        srRefresh.setOnRefreshListener(this);

        // 팔로우, 언팔로우 기본값, 팔로우 여부에 따라서 변경됨
        btnFollow.setVisibility(View.VISIBLE);
        btnUnFollow.setVisibility(View.INVISIBLE);
    }

    private void buildRecyclerView() {
        posts = new ArrayList<>();
        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        squarePostAdapter = new SquarePostAdapter(this, posts, this);
        infiniteScrollListener = new InfiniteScrollListener(layoutManager, 4) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView recyclerView) {
                if (responseCode == 200) {
                    if (networkConnection.checkNetworkConnection(OtherUserPageActivity.this) == TYPE_NOT_CONNECTED) {
                        dialogHelper.showConfirmDialog(OtherUserPageActivity.this, dialogHelper.ACTIVITY_FINISH_DIALOG_ID, getString(R.string.no_connected_network));
                    } else {
                        loadInfoPosts(page);
                    }
                }
            }

            @Override
            public void onLastVisibleItemPosition(int lastVisibleItemPosition) {
                if (responseCode == 200) {
                    int lastItemPosition = posts.size() - 1;
                    if (srRefresh.isRefreshing()) return;
                    if (lastItemPosition == lastVisibleItemPosition)
                        pbInfiniteScroll.setVisibility(View.VISIBLE);
                }
            }
        };
        RecyclerView.ItemAnimator animator = rvSquarePost.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }
        rvSquarePost.setLayoutManager(layoutManager);
        rvSquarePost.setAdapter(squarePostAdapter);
        rvSquarePost.addOnScrollListener(infiniteScrollListener);
    }

    /**
     * ------------------------------- category 1. 뷰 리스너 -------------------------------
     */
    @Override
    public void onClickItem(View view, int position) {
        int boardIdx = posts.get(position).getBoardIdx();

        switch (view.getId()) {
            case R.id.iv_square_post:
                Intent toOnePostIntent = new Intent(this, OnePostActivity.class);
                toOnePostIntent.putExtra("board_idx", boardIdx);
                startActivity(toOnePostIntent);
        }
    }

    @Override
    public void onClick(View v) {
        if (ibBack.equals(v)) {
            finish();
        } else if (tvFollowings.equals(v)) {
            Intent toFollowIntent = new Intent(this, FollowActivity.class);
            toFollowIntent.putExtra("user_idx", userIdx);
            toFollowIntent.putExtra("type", "following");
            startActivity(toFollowIntent);
        } else if (tvFollowers.equals(v)) {
            Intent toFollowIntent = new Intent(this, FollowActivity.class);
            toFollowIntent.putExtra("user_idx", userIdx);
            toFollowIntent.putExtra("type", "follower");
            startActivity(toFollowIntent);
        } else if (btnFollow.equals(v) || btnUnFollow.equals(v)) {
            if (networkConnection.checkNetworkConnection(this) == TYPE_NOT_CONNECTED) {
                dialogHelper.showConfirmDialog(this, dialogHelper.ACTIVITY_FINISH_DIALOG_ID, getString(R.string.no_connected_network));
            } else {
                if (isFollow) {
                    isFollow = false;
                    updateFollow(isFollow);
                } else {
                    isFollow = true;
                    updateFollow(isFollow);
                }
            }
        } else if (btnMessage.equals(v)) {
            Intent toChatIntent = new Intent(this, ChatActivity.class);
            toChatIntent.putExtra("chat_room_idx", chatRoomIdx);
            toChatIntent.putExtra("user_idx", userIdx);
            startActivity(toChatIntent);
        }
    }

    @Override
    public void onRefresh() {
        infiniteScrollListener.resetState();
        int itemCount = posts.size();
        posts.clear();
        squarePostAdapter.notifyItemRangeRemoved(0, itemCount);
        loadInfo(userIdx);
        loadInfoPosts(1);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    /**
     * ------------------------------- category ?. 서버 통신 -------------------------------
     */
    private void loadInfo(int userIdx) {
        Call<User> callReadInfo = retrofitHelper.getRetrofitInterFace().readInfo(USER_SESSION, USER_IDX, userIdx);
        callReadInfo.enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NotNull Call<User> call, @NotNull Response<User> response) {
                pbLoading.setVisibility(View.GONE);
                switch (response.code()) {
                    case 400:
                        retrofitHelper.printRetrofitResponse(TAG, response);
                        dialogHelper.showConfirmDialog(OtherUserPageActivity.this, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.client_error_message));
                        break;

                    case 500:
                        retrofitHelper.printRetrofitResponse(TAG, response);
                        dialogHelper.showConfirmDialog(OtherUserPageActivity.this, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.server_error_message));
                        break;

                    case 200:
                        assert response.body() != null;
                        name = response.body().getName();
                        profileImageUrl = response.body().getProfileImageUrl();
                        aboutMe = response.body().getAboutMe();
                        postCount = response.body().getPostCount();
                        followerCount = response.body().getFollowerCount();
                        followingCount = response.body().getFollowingCount();
                        isFollow = response.body().isFollow();
                        chatRoomIdx = response.body().getChatRoomIdx();

                        Log.d(TAG, "loadInfo - userIdx : " + userIdx);
                        Log.d(TAG, "loadInfo - name : " + name);
                        Log.d(TAG, "loadInfo - profileImageUrl : " + profileImageUrl);
                        Log.d(TAG, "loadInfo - aboutMe : " + aboutMe);
                        Log.d(TAG, "loadInfo - postCount : " + postCount);
                        Log.d(TAG, "loadInfo - followerCount : " + followerCount);
                        Log.d(TAG, "loadInfo - followingCount : " + followingCount);
                        Log.d(TAG, "loadInfo - isFollow : " + isFollow);
                        Log.d(TAG, "loadInfo - chatRoomIdx : " + chatRoomIdx);

                        tvName.setText(name);
                        ivProfile.post(() -> {
                            ivProfile.setBackground(null); // 배경 이미지 변경
                        });
                        Glide.with(OtherUserPageActivity.this).load(profileImageUrl).into(ivProfile);
                        tvAboutMe.setText(aboutMe);
                        tvPosts.setText(String.valueOf(postCount));
                        tvFollowers.setText(String.valueOf(followerCount));
                        tvFollowings.setText(String.valueOf(followingCount));
                        if (isFollow) {
                            btnFollow.setVisibility(View.INVISIBLE);
                            btnUnFollow.setVisibility(View.VISIBLE);
                        } else {
                            btnFollow.setVisibility(View.VISIBLE);
                            btnUnFollow.setVisibility(View.INVISIBLE);
                        }
                        if (postCount == 0) {
                            tvNoItem.setVisibility(View.VISIBLE);
                            rvSquarePost.setVisibility(View.INVISIBLE);
                        } else {
                            tvNoItem.setVisibility(View.INVISIBLE);
                            rvSquarePost.setVisibility(View.VISIBLE);
                        }
                        break;
                }
            }

            @Override
            public void onFailure(@NotNull Call<User> call, @NotNull Throwable t) {
                Log.e(TAG, "loadInfo - onFailure : " + t.getMessage());
                pbLoading.setVisibility(View.GONE);
                dialogHelper.showConfirmDialog(OtherUserPageActivity.this, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.network_not_stable));
            }
        });
    }

    private void loadInfoPosts(int page) {
        Call<Post> callReadInfoPosts = retrofitHelper.getRetrofitInterFace().readInfoPosts(USER_SESSION, USER_IDX, userIdx, page, limit);
        callReadInfoPosts.enqueue(new Callback<Post>() {
            @Override
            public void onResponse(@NotNull Call<Post> call, @NotNull Response<Post> response) {
                srRefresh.setRefreshing(false);
                pbLoading.setVisibility(View.GONE);
                pbInfiniteScroll.setVisibility(View.GONE);

                switch (response.code()) {
                    case 500:
                        retrofitHelper.printRetrofitResponse(TAG, response);
                        dialogHelper.showConfirmDialog(OtherUserPageActivity.this, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.server_error_message));
                        break;

                    case 400:
                        retrofitHelper.printRetrofitResponse(TAG, response);
                        dialogHelper.showConfirmDialog(OtherUserPageActivity.this, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.client_error_message));
                        break;

                    case 204:
                        Log.d(TAG, "loadOtherPosts - onResponse : " + responseCode);
                        responseCode = 204;
                        break;

                    case 200:
                        Post post = response.body();
                        responseCode = 200;
                        int startPosition = posts.size();
                        assert post != null;
                        posts.addAll(post.getPosts()); // 서버에서 응답받은 페이지의 리스트에 데이터 추가
                        int totalItemCount = posts.size();
                        squarePostAdapter.notifyItemRangeInserted(startPosition, totalItemCount - startPosition); // 어뎁터에서 추가된 데이터 업데이트
                        break;
                }
            }

            @Override
            public void onFailure(@NotNull Call<Post> call, @NotNull Throwable t) {
                srRefresh.setRefreshing(false);
                pbLoading.setVisibility(View.GONE);
                pbInfiniteScroll.setVisibility(View.GONE);
                dialogHelper.showConfirmDialog(OtherUserPageActivity.this, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.network_not_stable));
                Log.d(TAG, "loadOtherPosts - onFailure : " + t.getMessage());
            }
        });
    }

    private void updateFollow(boolean isFollow) {
        Call<User> callUpdateFollow = retrofitHelper.getRetrofitInterFace().updateFollow(USER_SESSION, USER_IDX, userIdx, isFollow);
        callUpdateFollow.enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NotNull Call<User> call, @NotNull Response<User> response) {
                switch (response.code()) {
                    case 500:
                        retrofitHelper.printRetrofitResponse(TAG, response);

                        // 에러 발생 시 좋아요를 반영하지 않음
                        if (isFollow) {
                            btnFollow.setVisibility(View.VISIBLE);
                            btnUnFollow.setVisibility(View.INVISIBLE);
                        } else {
                            btnFollow.setVisibility(View.INVISIBLE);
                            btnUnFollow.setVisibility(View.VISIBLE);
                        }

                        dialogHelper.showConfirmDialog(OtherUserPageActivity.this, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.server_error_message));
                        break;

                    case 400:
                        retrofitHelper.printRetrofitResponse(TAG, response);

                        // 에러 발생 시 좋아요를 반영하지 않음
                        if (isFollow) {
                            btnFollow.setVisibility(View.VISIBLE);
                            btnUnFollow.setVisibility(View.INVISIBLE);
                        } else {
                            btnFollow.setVisibility(View.INVISIBLE);
                            btnUnFollow.setVisibility(View.VISIBLE);
                        }

                        dialogHelper.showConfirmDialog(OtherUserPageActivity.this, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.client_error_message));
                        break;

                    case 204:
                        retrofitHelper.printRetrofitResponse(TAG, response);
                        break;

                    case 200:
                        Log.d(TAG, "updateFollow - onResponse : " + response);
                        assert response.body() != null;
                        boolean isFollow = response.body().isFollow();
                        int followingCount = response.body().getFollowingCount();
                        int followerCount = response.body().getFollowerCount();

                        if (isFollow) {
                            btnFollow.setVisibility(View.INVISIBLE);
                            btnUnFollow.setVisibility(View.VISIBLE);
                        } else {
                            btnFollow.setVisibility(View.VISIBLE);
                            btnUnFollow.setVisibility(View.INVISIBLE);
                        }
                        tvFollowings.setText(String.valueOf(followingCount));
                        tvFollowers.setText(String.valueOf(followerCount));
                        if (isFollow) {
                            Snackbar.make(getWindow().getDecorView().getRootView(), R.string.follow, Snackbar.LENGTH_LONG).show();
                        } else {
                            Snackbar.make(getWindow().getDecorView().getRootView(), R.string.unfollow, Snackbar.LENGTH_LONG).show();
                        }
                        break;
                }
            }

            @Override
            public void onFailure(@NotNull Call<User> call, @NotNull Throwable t) {
                Log.d(TAG, "updateFollow - onFailure : " + t.getMessage());
            }
        });
    }

    /**
     * ------------------------------- category ?. 유틸리티 -------------------------------
     */
}