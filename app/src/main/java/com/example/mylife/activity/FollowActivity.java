package com.example.mylife.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.mylife.R;
import com.example.mylife.adapter.CommentAdapter;
import com.example.mylife.adapter.FollowAdapter;
import com.example.mylife.adapter.ItemClickListener;
import com.example.mylife.item.Comment;
import com.example.mylife.item.User;
import com.example.mylife.util.DialogHelper;
import com.example.mylife.util.InfiniteScrollListener;
import com.example.mylife.util.MethodHelper;
import com.example.mylife.util.NetworkConnection;
import com.example.mylife.util.RetrofitHelper;
import com.google.android.material.snackbar.Snackbar;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.mylife.MyApplication.USER_IDX;
import static com.example.mylife.MyApplication.USER_SESSION;
import static com.example.mylife.util.NetworkConnection.TYPE_NOT_CONNECTED;

public class FollowActivity extends AppCompatActivity implements View.OnClickListener, ItemClickListener, SwipeRefreshLayout.OnRefreshListener {
    private final String TAG = "FollowActivity";
    private final RetrofitHelper retrofitHelper = RetrofitHelper.getInstance();
    private final NetworkConnection networkConnection = NetworkConnection.getInstance();
    private final DialogHelper dialogHelper = DialogHelper.getInstance();
    private final MethodHelper methodHelper = MethodHelper.getInstance();

    private SwipeRefreshLayout srRefresh;
    private ProgressBar pbLoading, pbInfiniteScroll; // 프로그래스 바

    private ImageButton ibBack;
    private TextView tvFollowing, tvFollower, tvCount, tvNoItemFollowings, tvNoItemFollowers;

    private RecyclerView rvFollow;
    private LinearLayoutManager layoutManager;
    private ArrayList<User> users;
    private FollowAdapter followAdapter;
    private InfiniteScrollListener infiniteScrollListener;

    private int responseCode; // HTTP 응답코드, isLast 대신에 사용할 것임
    // 페이징을 위한 변수 : 마지막 페이지인지 확인
    private final int limit = 5;
    private boolean isLast;
    private String type;
    private int userIdx;

    private int clickedPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow);
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
            if (type.equals("following")) {
                tvFollowing.setTextColor(Color.parseColor("#000000"));
                tvFollower.setTextColor(Color.parseColor("#808080"));
                loadFollowings(1);
            } else if (type.equals("follower")) {
                tvFollowing.setTextColor(Color.parseColor("#808080"));
                tvFollower.setTextColor(Color.parseColor("#000000"));
                loadFollowers(1);
            }
        }
    }

    private void bindView() {
        // TODO: 데이터를 먼저 꼭 먼저 받아와야만 하는 예외 상황이 발생해서 메소드 이름에 맞지 않는 위치에 데이터가 놓이게 된다. 그냥 이렇게 할 바엔 분리 안하는 게 낫지 않나? 그런데 그러면 너무 코드가 더러워지긴함
        userIdx = getIntent().getIntExtra("user_idx", 0);
        type = getIntent().getStringExtra("type");

        ibBack = findViewById(R.id.ib_back);
        tvFollowing = findViewById(R.id.tv_following);
        tvFollower = findViewById(R.id.tv_follower);
        tvCount = findViewById(R.id.tv_count);
        tvNoItemFollowings = findViewById(R.id.tv_no_item_followings);
        tvNoItemFollowers = findViewById(R.id.tv_no_item_followers);
        rvFollow = findViewById(R.id.rv_follow);
        pbLoading = findViewById(R.id.pb_loading);
        pbInfiniteScroll = findViewById(R.id.pb_infinite_scroll);
        srRefresh = findViewById(R.id.sr_refresh);

        ibBack.setOnClickListener(this);
        tvFollowing.setOnClickListener(this);
        tvFollower.setOnClickListener(this);
        srRefresh.setOnRefreshListener(this);

        tvNoItemFollowings.setVisibility(View.INVISIBLE);
        tvNoItemFollowers.setVisibility(View.INVISIBLE);
    }

    private void buildRecyclerView() {
        if (type.equals("following")) {
            users = new ArrayList<>();
            layoutManager = new LinearLayoutManager(this);
            layoutManager.setOrientation(RecyclerView.VERTICAL);
            layoutManager.setReverseLayout(false);
            layoutManager.setStackFromEnd(false);
            followAdapter = new FollowAdapter(this, users, this);
            infiniteScrollListener = new InfiniteScrollListener(layoutManager, 4) {
                @Override
                public void onLoadMore(int page, int totalItemsCount, RecyclerView recyclerView) {
                    if (responseCode == 200) {
                        if (networkConnection.checkNetworkConnection(FollowActivity.this) == TYPE_NOT_CONNECTED) {
                            dialogHelper.showConfirmDialog(FollowActivity.this, dialogHelper.ACTIVITY_FINISH_DIALOG_ID, getString(R.string.no_connected_network));
                        } else {
                            loadFollowings(page);
                        }
                    }
                }

                @Override
                public void onLastVisibleItemPosition(int lastVisibleItemPosition) {
                    if (responseCode == 200) {
                        int lastItemPosition = users.size() - 1;
                        if (srRefresh.isRefreshing()) return;
                        if (lastItemPosition == lastVisibleItemPosition)
                            pbInfiniteScroll.setVisibility(View.VISIBLE);
                    }
                }
            };
            RecyclerView.ItemAnimator animator = rvFollow.getItemAnimator();
            if (animator instanceof SimpleItemAnimator) {
                ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
            }
            rvFollow.setLayoutManager(layoutManager);
            rvFollow.setAdapter(followAdapter);
            rvFollow.addOnScrollListener(infiniteScrollListener);
        } else if (type.equals("follower")) {
            users = new ArrayList<>();
            layoutManager = new LinearLayoutManager(this);
            layoutManager.setOrientation(RecyclerView.VERTICAL);
            layoutManager.setReverseLayout(false);
            layoutManager.setStackFromEnd(false);
            followAdapter = new FollowAdapter(this, users, this);
            infiniteScrollListener = new InfiniteScrollListener(layoutManager, 4) {
                @Override
                public void onLoadMore(int page, int totalItemsCount, RecyclerView recyclerView) {
                    if (responseCode == 200) {
                        if (networkConnection.checkNetworkConnection(FollowActivity.this) == TYPE_NOT_CONNECTED) {
                            dialogHelper.showConfirmDialog(FollowActivity.this, dialogHelper.ACTIVITY_FINISH_DIALOG_ID, getString(R.string.no_connected_network));
                        } else {
                            loadFollowers(page);
                        }
                    }
                }

                @Override
                public void onLastVisibleItemPosition(int lastVisibleItemPosition) {
                    if (responseCode == 200) {
                        int lastItemPosition = users.size() - 1;
                        if (srRefresh.isRefreshing()) return;
                        if (lastItemPosition == lastVisibleItemPosition)
                            pbInfiniteScroll.setVisibility(View.VISIBLE);
                    }
                }
            };
            RecyclerView.ItemAnimator animator = rvFollow.getItemAnimator();
            if (animator instanceof SimpleItemAnimator) {
                ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
            }
            rvFollow.setLayoutManager(layoutManager);
            rvFollow.setAdapter(followAdapter);
            rvFollow.addOnScrollListener(infiniteScrollListener);
        }
    }

    /**
     * ------------------------------- category 1. 뷰 리스너 -------------------------------
     */
    @Override
    public void onClickItem(View view, int position) {
        switch (view.getId()) {
            case R.id.btn_follow:
                if (networkConnection.checkNetworkConnection(this) == TYPE_NOT_CONNECTED) {
                    dialogHelper.showConfirmDialog(this, dialogHelper.ACTIVITY_FINISH_DIALOG_ID, getString(R.string.no_connected_network));
                } else {
                    updateFollow(!users.get(position).isFollow(), position);
                }
                break;

            case R.id.btn_unfollow:
                if (networkConnection.checkNetworkConnection(this) == TYPE_NOT_CONNECTED) {
                    dialogHelper.showConfirmDialog(this, dialogHelper.ACTIVITY_FINISH_DIALOG_ID, getString(R.string.no_connected_network));
                } else {
                    updateFollow(!users.get(position).isFollow(), position);
                }
                break;
        }
    }

    @Override
    public void onClick(View v) {
        if (ibBack.equals(v)) {
            finish();
        } else if (tvFollowing.equals(v)) {
            type = "following";
            tvFollowing.setTextColor(Color.parseColor("#000000"));
            tvFollower.setTextColor(Color.parseColor("#808080"));
            buildRecyclerView(); // TODO: 리사이클러뷰를 초기화 시켜주려고 적었는데 옳은 방법인 지 생각해보기
            loadFollowings(1);
        } else if (tvFollower.equals(v)) {
            type = "follower";
            tvFollowing.setTextColor(Color.parseColor("#808080"));
            tvFollower.setTextColor(Color.parseColor("#000000"));
            buildRecyclerView(); // TODO: 리사이클러뷰를 초기화 시켜주려고 적었는데 옳은 방법인 지 생각해보기
            loadFollowers(1);
        }
    }

    @Override
    public void onRefresh() {
        if (type.equals("following")) {
            tvFollowing.setTextColor(Color.parseColor("#000000"));
            tvFollower.setTextColor(Color.parseColor("#808080"));
            infiniteScrollListener.resetState();
            int itemCount = users.size();
            users.clear();
            followAdapter.notifyItemRangeRemoved(0, itemCount);
            loadFollowings(1);
        } else if (type.equals("follower")) {
            tvFollowing.setTextColor(Color.parseColor("#808080"));
            tvFollower.setTextColor(Color.parseColor("#000000"));
            infiniteScrollListener.resetState();
            int itemCount = users.size();
            users.clear();
            followAdapter.notifyItemRangeRemoved(0, itemCount);
            loadFollowers(1);
        }
    }

    /**
     * ------------------------------- category 3. 서버 통신 -------------------------------
     */
    private void loadFollowings(int page) {
        Call<User> callReadFollowings = retrofitHelper.getRetrofitInterFace().readFollowings(USER_SESSION, USER_IDX, userIdx, page, limit);
        callReadFollowings.enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NotNull Call<User> call, @NotNull Response<User> response) {
                srRefresh.setRefreshing(false);
                pbLoading.setVisibility(View.GONE);
                pbInfiniteScroll.setVisibility(View.GONE);

                switch (response.code()) {
                    case 500:
                        retrofitHelper.printRetrofitResponse(TAG, response);
                        dialogHelper.showConfirmDialog(FollowActivity.this, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.server_error_message));
                        break;

                    case 400:
                        retrofitHelper.printRetrofitResponse(TAG, response);
                        dialogHelper.showConfirmDialog(FollowActivity.this, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.client_error_message));
                        break;

                    case 204:
                        Log.d(TAG, "loadFollowings - onResponse : " + responseCode);
                        responseCode = 204;
                        break;

                    case 200:
                        User user = response.body();
                        responseCode = 200;
                        int startPosition = users.size();
                        assert user != null;
                        users.addAll(user.getFollowings()); // 서버에서 응답받은 페이지의 리스트에 데이터 추가
                        int totalItemCount = users.size();
                        followAdapter.notifyItemRangeInserted(startPosition, totalItemCount - startPosition); // 어뎁터에서 추가된 데이터 업데이트

//                        int followingCount = user.getFollowingCount();
//                        tvCount.setText(followingCount + "명");

                        if (users.size() == 0) {
                            tvNoItemFollowings.setVisibility(View.VISIBLE);
                            tvNoItemFollowers.setVisibility(View.INVISIBLE);
                            srRefresh.setVisibility(View.INVISIBLE);
                            rvFollow.setVisibility(View.INVISIBLE);
                        } else {
                            tvNoItemFollowings.setVisibility(View.INVISIBLE);
                            tvNoItemFollowers.setVisibility(View.INVISIBLE);
                            srRefresh.setVisibility(View.VISIBLE);
                            rvFollow.setVisibility(View.VISIBLE);
                        }
                        break;
                }
            }

            @Override
            public void onFailure(@NotNull Call<User> call, @NotNull Throwable t) {
                srRefresh.setRefreshing(false);
                pbLoading.setVisibility(View.GONE);
                pbInfiniteScroll.setVisibility(View.GONE);
                dialogHelper.showConfirmDialog(FollowActivity.this, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.network_not_stable));
                Log.d(TAG, "loadFollowings - onFailure : " + t.getMessage());
            }
        });
    }

    private void loadFollowers(int page) {
        Call<User> callReadFollowers = retrofitHelper.getRetrofitInterFace().readFollowers(USER_SESSION, USER_IDX, userIdx, page, limit);
        callReadFollowers.enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NotNull Call<User> call, @NotNull Response<User> response) {
                srRefresh.setRefreshing(false);
                pbLoading.setVisibility(View.GONE);
                pbInfiniteScroll.setVisibility(View.GONE);

                switch (response.code()) {
                    case 500:
                        retrofitHelper.printRetrofitResponse(TAG, response);
                        dialogHelper.showConfirmDialog(FollowActivity.this, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.server_error_message));
                        break;

                    case 400:
                        retrofitHelper.printRetrofitResponse(TAG, response);
                        dialogHelper.showConfirmDialog(FollowActivity.this, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.client_error_message));
                        break;

                    case 204:
                        Log.d(TAG, "loadFollowers - onResponse : " + responseCode);
                        responseCode = 204;
                        break;

                    case 200:
                        User user = response.body();
                        responseCode = 200;
                        int startPosition = users.size();
                        assert user != null;
                        users.addAll(user.getFollowers()); // 서버에서 응답받은 페이지의 리스트에 데이터 추가
                        int totalItemCount = users.size();
                        followAdapter.notifyItemRangeInserted(startPosition, totalItemCount - startPosition); // 어뎁터에서 추가된 데이터 업데이트

//                        int followerCount = user.getFollowerCount();
//                        tvCount.setText(String.valueOf(followerCount) + "명");

                        if (users.size() == 0) {
                            tvNoItemFollowers.setVisibility(View.VISIBLE);
                            tvNoItemFollowings.setVisibility(View.INVISIBLE);
                            srRefresh.setVisibility(View.INVISIBLE);
                            rvFollow.setVisibility(View.INVISIBLE);
                        } else {
                            tvNoItemFollowers.setVisibility(View.INVISIBLE);
                            tvNoItemFollowings.setVisibility(View.INVISIBLE);
                            srRefresh.setVisibility(View.VISIBLE);
                            rvFollow.setVisibility(View.VISIBLE);
                        }
                        break;
                }
            }

            @Override
            public void onFailure(@NotNull Call<User> call, @NotNull Throwable t) {
                srRefresh.setRefreshing(false);
                pbLoading.setVisibility(View.GONE);
                pbInfiniteScroll.setVisibility(View.GONE);
                dialogHelper.showConfirmDialog(FollowActivity.this, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.network_not_stable));
                Log.d(TAG, "loadFollowers - onFailure : " + t.getMessage());
            }
        });
    }

    private void updateFollow(boolean isFollow, int position) {
        if (type.equals("following")) {
            int userIdx = users.get(position).getToUserIdx();
            Call<User> callUpdateFollow = retrofitHelper.getRetrofitInterFace().updateFollow(USER_SESSION, USER_IDX, userIdx, isFollow);
            callUpdateFollow.enqueue(new Callback<User>() {
                @Override
                public void onResponse(@NotNull Call<User> call, @NotNull Response<User> response) {
                    switch (response.code()) {
                        case 500:
                            retrofitHelper.printRetrofitResponse(TAG, response);

                            // 에러 발생 시 팔로우를 반영하지 않음
                            users.get(position).setFollow(!isFollow);
                            followAdapter.notifyItemChanged(position);
                            followAdapter.notifyItemRangeChanged(position, users.size());

                            dialogHelper.showConfirmDialog(FollowActivity.this, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.server_error_message));
                            break;

                        case 400:
                            retrofitHelper.printRetrofitResponse(TAG, response);

                            // 에러 발생 시 팔로우를 반영하지 않음
                            users.get(position).setFollow(!isFollow);
                            followAdapter.notifyItemChanged(position);
                            followAdapter.notifyItemRangeChanged(position, users.size());

                            dialogHelper.showConfirmDialog(FollowActivity.this, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.client_error_message));
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

                            users.get(position).setFollow(isFollow);
                            users.get(position).setFollowingCount(followingCount);
                            users.get(position).setFollowerCount(followerCount);
                            followAdapter.notifyItemChanged(position);
                            followAdapter.notifyItemRangeChanged(position, users.size());

                            if (type.equals("following")) {
                                tvCount.setText(String.valueOf(followingCount) + "명");
                            } else if (type.equals("followers")) {
                                tvCount.setText(String.valueOf(followerCount) + "명");
                            }

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
        } else if (type.equals("follower")) {
            int userIdx = users.get(position).getFromUserIdx();
            Call<User> callUpdateFollow = retrofitHelper.getRetrofitInterFace().updateFollow(USER_SESSION, USER_IDX, userIdx, isFollow);
            callUpdateFollow.enqueue(new Callback<User>() {
                @Override
                public void onResponse(@NotNull Call<User> call, @NotNull Response<User> response) {
                    switch (response.code()) {
                        case 500:
                            retrofitHelper.printRetrofitResponse(TAG, response);

                            // 에러 발생 시 팔로우를 반영하지 않음
                            users.get(position).setFollow(!isFollow);
                            followAdapter.notifyItemChanged(position);
                            followAdapter.notifyItemRangeChanged(position, users.size());

                            dialogHelper.showConfirmDialog(FollowActivity.this, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.server_error_message));
                            break;

                        case 400:
                            retrofitHelper.printRetrofitResponse(TAG, response);

                            // 에러 발생 시 팔로우를 반영하지 않음
                            users.get(position).setFollow(!isFollow);
                            followAdapter.notifyItemChanged(position);
                            followAdapter.notifyItemRangeChanged(position, users.size());

                            dialogHelper.showConfirmDialog(FollowActivity.this, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.client_error_message));
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

                            users.get(position).setFollow(isFollow);
                            users.get(position).setFollowingCount(followingCount);
                            users.get(position).setFollowerCount(followerCount);
                            followAdapter.notifyItemChanged(position);
                            followAdapter.notifyItemRangeChanged(position, users.size());

                            if (type.equals("following")) {
                                tvCount.setText(String.valueOf(followingCount) + "명");
                            } else if (type.equals("followers")) {
                                tvCount.setText(String.valueOf(followerCount) + "명");
                            }

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
    }
}