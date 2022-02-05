package com.example.mylife.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.mylife.R;
import com.example.mylife.activity.EditPostActivity;
import com.example.mylife.activity.EditProfileActivity;
import com.example.mylife.activity.FollowActivity;
import com.example.mylife.activity.LoginActivity;
import com.example.mylife.activity.OnePostActivity;
import com.example.mylife.adapter.ItemClickListener;
import com.example.mylife.adapter.PostAdapter;
import com.example.mylife.adapter.SquarePostAdapter;
import com.example.mylife.item.Post;
import com.example.mylife.item.User;
import com.example.mylife.util.DialogHelper;
import com.example.mylife.util.InfiniteScrollListener;
import com.example.mylife.util.MethodHelper;
import com.example.mylife.util.NetworkConnection;
import com.example.mylife.util.RetrofitHelper;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;
import static com.example.mylife.MyApplication.PROFILE_IMAGE_URL;
import static com.example.mylife.MyApplication.USER_ABOUT_ME;
import static com.example.mylife.MyApplication.USER_IDX;
import static com.example.mylife.MyApplication.USER_NAME;
import static com.example.mylife.MyApplication.USER_SESSION;
import static com.example.mylife.util.NetworkConnection.TYPE_NOT_CONNECTED;

public class MyPageFragment extends Fragment implements View.OnClickListener, ItemClickListener, SwipeRefreshLayout.OnRefreshListener {
    private final String TAG = "MyPageFragment";
    private final RetrofitHelper retrofitHelper = RetrofitHelper.getInstance();
    private final NetworkConnection networkConnection = NetworkConnection.getInstance();
    private final DialogHelper dialogHelper = DialogHelper.getInstance();
    private final MethodHelper methodHelper = MethodHelper.getInstance();
    private Context mContext;
    private Activity mActivity;

    private SwipeRefreshLayout srRefresh;
    private ProgressBar pbLoading, pbInfiniteScroll; // 프로그래스 바

    private RecyclerView rvSquarePost;
    private ArrayList<Post> posts;
    private SquarePostAdapter squarePostAdapter;
    private InfiniteScrollListener infiniteScrollListener;

    private int responseCode; // HTTP 응답코드, isLast 대신에 사용할 것임

    // 페이징을 위한 변수 : 마지막 페이지인지 확인
    private final int limit = 9;
    private boolean isLast;

    private CircleImageView ivProfile;
    private TextView tvPosts, tvPost, tvFollowings, tvFollowing, tvFollowers, tvFollower, tvName, tvAboutMe;
    private Button btnProfile, btnLogout;

    public MyPageFragment() {
        super(R.layout.fragment_my_page);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindView(view);
        buildRecyclerView();
        setInitData();
    }

    /**
     * ------------------------------- category 0. 최초 설정 -------------------------------
     */
    private void setInitData() {
        if (networkConnection.checkNetworkConnection(getActivity()) == TYPE_NOT_CONNECTED) {
            dialogHelper.showConfirmDialog((AppCompatActivity) getActivity(), dialogHelper.ACTIVITY_FINISH_DIALOG_ID, getString(R.string.no_connected_network));
        } else {
            loadMyPosts(1);
        }
        tvName.setText(USER_NAME);
        // TODO: 유저의 자기소개까지 SharedPreference에 저장을 해야하나? 어디까지 저장을 해야하지, 그렇다고 이거 하나 때문에 서버 요청 보내는 것도 이상하고...
        tvAboutMe.setText(USER_ABOUT_ME);
        ivProfile.post(() -> {
            ivProfile.setBackground(null); // 배경 이미지 변경
        });
        Glide.with(this).load(PROFILE_IMAGE_URL).into(ivProfile);
    }

    private void bindView(View v) {
        ivProfile = v.findViewById(R.id.iv_profile);
        tvPosts = v.findViewById(R.id.tv_posts);
        tvPost = v.findViewById(R.id.tv_post);
        tvFollowings = v.findViewById(R.id.tv_followings);
        tvFollowing = v.findViewById(R.id.tv_following);
        tvFollowers = v.findViewById(R.id.tv_followers);
        tvFollower = v.findViewById(R.id.tv_follower);
        tvName = v.findViewById(R.id.tv_name);
        tvAboutMe = v.findViewById(R.id.tv_about_me);
        btnProfile = v.findViewById(R.id.btn_profile);
        btnLogout = v.findViewById(R.id.btn_logout);
        rvSquarePost = v.findViewById(R.id.rv_my_post);
        pbLoading = v.findViewById(R.id.pb_loading);
        pbInfiniteScroll = v.findViewById(R.id.pb_infinite_scroll);
        srRefresh = v.findViewById(R.id.sr_refresh);

        tvPosts.setOnClickListener(this);
        tvFollowings.setOnClickListener(this);
        tvFollowers.setOnClickListener(this);
        btnProfile.setOnClickListener(this);
        btnLogout.setOnClickListener(this);
        srRefresh.setOnRefreshListener(this);
    }

    private void buildRecyclerView() {
        posts = new ArrayList<>();
        GridLayoutManager layoutManager = new GridLayoutManager(requireView().getContext(), 3);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        squarePostAdapter = new SquarePostAdapter(requireContext(), posts, this);
        infiniteScrollListener = new InfiniteScrollListener(layoutManager, 4) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView recyclerView) {
                if (responseCode == 200) {
                    if (networkConnection.checkNetworkConnection(getActivity()) == TYPE_NOT_CONNECTED) {
                        dialogHelper.showConfirmDialog((AppCompatActivity) getActivity(), dialogHelper.ACTIVITY_FINISH_DIALOG_ID, getString(R.string.no_connected_network));
                    } else {
                        loadMyPosts(page);
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
                Intent toOnePostIntent = new Intent(mContext, OnePostActivity.class);
                toOnePostIntent.putExtra("board_idx", boardIdx);
                startActivity(toOnePostIntent);
        }
    }

    @Override
    public void onClick(View v) {
        if (tvFollowings.equals(v)) {
            Intent toFollowIntent = new Intent(mContext, FollowActivity.class);
            toFollowIntent.putExtra("type", "following");
            startActivity(toFollowIntent);
        } else if (tvFollowers.equals(v)) {
            Intent toFollowIntent = new Intent(mContext, FollowActivity.class);
            toFollowIntent.putExtra("type", "follower");
            startActivity(toFollowIntent);
        } else if (btnProfile.equals(v)) {
            Intent toEditProfileIntent = new Intent(mContext, EditProfileActivity.class);
            startActivity(toEditProfileIntent);
        } else if (btnLogout.equals(v)) {
            AlertDialog.Builder logoutDialogBuilder = new AlertDialog.Builder(mContext);
            logoutDialogBuilder.setTitle("로그아웃 확인창").setMessage("정말로 로그아웃 하시겠습니까?");
            logoutDialogBuilder.setPositiveButton("확인", new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int id)
                {
                    logout();
                }
            });
            logoutDialogBuilder.setNegativeButton("취소", (dialog1, id) -> { });
            AlertDialog logoutDialog = logoutDialogBuilder.create();
            logoutDialog.show();
        }
    }

    @Override
    public void onRefresh() {
        infiniteScrollListener.resetState();
        int itemCount = posts.size();
        posts.clear();
        squarePostAdapter.notifyItemRangeRemoved(0, itemCount);
        loadMyPosts(1);
    }

    /**
     * ------------------------------- category 3. 서버 통신 -------------------------------
     */
    private void loadMyPosts(int page) {
        Call<Post> callReadMyPosts = retrofitHelper.getRetrofitInterFace().readMyPosts(USER_SESSION, USER_IDX, page, limit);
        callReadMyPosts.enqueue(new Callback<Post>() {
            @Override
            public void onResponse(@NotNull Call<Post> call, @NotNull Response<Post> response) {
                srRefresh.setRefreshing(false);
                pbLoading.setVisibility(View.GONE);
                pbInfiniteScroll.setVisibility(View.GONE);

                switch (response.code()) {
                    case 500:
                        retrofitHelper.printRetrofitResponse(TAG, response);
                        dialogHelper.showConfirmDialog((AppCompatActivity) getActivity(), dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.server_error_message));
                        break;

                    case 400:
                        retrofitHelper.printRetrofitResponse(TAG, response);
                        dialogHelper.showConfirmDialog((AppCompatActivity) getActivity(), dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.client_error_message));
                        break;

                    case 204:
                        Log.d(TAG, "loadMyPosts - onResponse : " + responseCode);
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
                dialogHelper.showConfirmDialog((AppCompatActivity) getActivity(), dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.network_not_stable));
                Log.d(TAG, "loadMyPosts - onFailure : " + t.getMessage());
            }
        });
    }

    private void logout() {
        Call<User> callLogout = retrofitHelper.getRetrofitInterFace().logout(USER_SESSION, USER_IDX);
        callLogout.enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NotNull Call<User> call, @NotNull Response<User> response) {
                switch (response.code()) {
                    case 500:
                        retrofitHelper.printRetrofitResponse(TAG, response);
                        dialogHelper.showConfirmDialog((AppCompatActivity) mContext, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.server_error_message));
                        break;

                    case 400:
                        retrofitHelper.printRetrofitResponse(TAG, response);
                        dialogHelper.showConfirmDialog((AppCompatActivity) mContext, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.client_error_message));
                        break;

                    case 200:
                        requireActivity().getSharedPreferences(getString(R.string.main_storage_key), MODE_PRIVATE).edit().clear().apply();
                        Intent toLoginIntent = new Intent(mContext, LoginActivity.class);
                        toLoginIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(toLoginIntent);
                        requireActivity().finish();
                        break;
                }
            }

            @Override
            public void onFailure(@NotNull Call<User> call, @NotNull Throwable t) {
                Log.d(TAG, "logout - onFailure : " + t.getMessage());
                dialogHelper.showConfirmDialog((AppCompatActivity) mContext, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.network_not_stable));
            }
        });
    }

    /**
     * ------------------------------- category ?. 생명주기 -------------------------------
     */
    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        mContext = context;
        if (context instanceof Activity) mActivity = (Activity) mContext;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActivity = null;
        mContext = null;
    }
}