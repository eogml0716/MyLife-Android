package com.example.mylife.fragment;

import android.annotation.SuppressLint;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.mylife.R;
import com.example.mylife.activity.CommentActivity;
import com.example.mylife.activity.EditPostActivity;
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
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.mylife.MyApplication.USER_IDX;
import static com.example.mylife.MyApplication.USER_SESSION;
import static com.example.mylife.util.NetworkConnection.TYPE_NOT_CONNECTED;

// TODO: item_post 너무 무식하게 margin 준 거 같은데 비율에 맞게 margin을 주는 방법은 없을까?
public class HomeFragment extends Fragment implements View.OnClickListener, ItemClickListener, SwipeRefreshLayout.OnRefreshListener {
    private final String TAG = "HomeFragment";
    private final RetrofitHelper retrofitHelper = RetrofitHelper.getInstance();
    private final NetworkConnection networkConnection = NetworkConnection.getInstance();
    private final DialogHelper dialogHelper = DialogHelper.getInstance();
    private final MethodHelper methodHelper = MethodHelper.getInstance();
    private Context mContext;
    private Activity mActivity;

    private SwipeRefreshLayout srRefresh;
    private ProgressBar pbLoading, pbInfiniteScroll; // 프로그래스 바

    private RecyclerView rvPost;
    private ArrayList<Post> posts;
    private PostAdapter postAdapter;
    private InfiniteScrollListener infiniteScrollListener;

    private int responseCode; // HTTP 응답코드, isLast 대신에 사용할 것임

    // 페이징을 위한 변수 : 마지막 페이지인지 확인
    private final int limit = 5;
    private boolean isLast;

    public HomeFragment() {
        super(R.layout.fragment_home);
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
            loadPosts(1);
        }
    }

    private void bindView(View v) {
        rvPost = v.findViewById(R.id.rv_post);
        pbLoading = v.findViewById(R.id.pb_loading);
        pbInfiniteScroll = v.findViewById(R.id.pb_infinite_scroll);
        srRefresh = v.findViewById(R.id.sr_refresh);
        srRefresh.setOnRefreshListener(this);
    }

    private void buildRecyclerView() {
        posts = new ArrayList<>();
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireView().getContext());
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        postAdapter = new PostAdapter(requireContext(), posts, this);
        infiniteScrollListener = new InfiniteScrollListener(layoutManager, 4) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView recyclerView) {
                if (responseCode == 200) {
                    if (networkConnection.checkNetworkConnection(getActivity()) == TYPE_NOT_CONNECTED) {
                        dialogHelper.showConfirmDialog((AppCompatActivity) getActivity(), dialogHelper.ACTIVITY_FINISH_DIALOG_ID, getString(R.string.no_connected_network));
                    } else {
                        loadPosts(page);
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
        RecyclerView.ItemAnimator animator = rvPost.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }
//        rvCommunity.setHasFixedSize(true);
        rvPost.setLayoutManager(layoutManager);
        rvPost.setAdapter(postAdapter);
        rvPost.addOnScrollListener(infiniteScrollListener);
    }

    /**
     * ------------------------------- category 1. 뷰 리스너 -------------------------------
     */
    @Override
    public void onClickItem(View view, int position) {
        int boardIdx = posts.get(position).getBoardIdx();

        switch (view.getId()) {
            case R.id.tv_name:

                break;

            case R.id.ib_threedots:
                int userIdx = posts.get(position).getUserIdx();
                // TODO: 수정, 삭제의 경우 클라이언트, 서버에서 둘다 예외 처리 해줄 것
                if (USER_IDX == userIdx) {
                    final List<String> dialogListItems = new ArrayList<>();
                    dialogListItems.add("수정");
                    dialogListItems.add("삭제");
                    dialogListItems.add("닫기");
                    final String[] items = dialogListItems.toArray(new String[dialogListItems.size()]);
                    AlertDialog.Builder postDialogBuilder = new AlertDialog.Builder(mContext);
                    postDialogBuilder.setItems(items, (dialog, pos) -> {
                        String selectedText = items[pos];
                        if (selectedText.equals("수정")) {
                            String imageUrl = posts.get(position).getImageUrl();
                            String contents = posts.get(position).getContents();
                            Intent toEditPostIntent = new Intent(mContext, EditPostActivity.class);
                            toEditPostIntent.putExtra("board_idx", boardIdx);
                            toEditPostIntent.putExtra("image_url", imageUrl);
                            toEditPostIntent.putExtra("contents", contents);
                            // TODO: 댓글 개수 늘어나거나 줄어들면 HomeFragment로 돌아왔을 때 반영해주기
                            startActivity(toEditPostIntent);
                            requireActivity().finish();
                        } else if (selectedText.equals("삭제")) {
                            AlertDialog.Builder deleteDialogBuilder = new AlertDialog.Builder(mContext);
                            deleteDialogBuilder.setTitle("삭제 확인창").setMessage("정말로 삭제하시겠습니까?");
                            deleteDialogBuilder.setPositiveButton("확인", new DialogInterface.OnClickListener(){
                                @Override
                                public void onClick(DialogInterface dialog, int id)
                                {
                                    deletePost(boardIdx, position);
                                }
                            });
                            deleteDialogBuilder.setNegativeButton("취소", (dialog1, id) -> { });
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
                if (networkConnection.checkNetworkConnection(getActivity()) == TYPE_NOT_CONNECTED) {
                    dialogHelper.showConfirmDialog((AppCompatActivity) getActivity(), dialogHelper.ACTIVITY_FINISH_DIALOG_ID, getString(R.string.no_connected_network));
                } else {
                    updateLikePost(posts.get(position).isLike(), position);
                }
                break;

            case R.id.iv_comment:
                Intent toCommentIntent = new Intent(mContext, CommentActivity.class);
                toCommentIntent.putExtra("board_idx", boardIdx);
                // TODO: 댓글 개수 늘어나거나 줄어들면 HomeFragment로 돌아왔을 때 반영해주기
                startActivity(toCommentIntent);
                break;

            case R.id.iv_share:
                // TODO: 구현 보류
                break;
        }
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onRefresh() {
        infiniteScrollListener.resetState();
        int itemCount = posts.size();
        posts.clear();
        postAdapter.notifyItemRangeRemoved(0, itemCount);
        loadPosts(1);
    }

    /**
     * ------------------------------- category 3. 서버 통신 -------------------------------
     */
    private void loadPosts(int page) {
        Call<Post> callReadPosts = retrofitHelper.getRetrofitInterFace().readPosts(USER_SESSION, USER_IDX, page, limit);
        callReadPosts.enqueue(new Callback<Post>() {
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
                        Log.d(TAG, "loadPosts - onResponse : " + responseCode);
                        responseCode = 204;
                        break;

                    case 200:
                        Post post = response.body();
                        responseCode = 200;
                        int startPosition = posts.size();
                        assert post != null;
                        posts.addAll(post.getPosts()); // 서버에서 응답받은 페이지의 리스트에 데이터 추가
                        int totalItemCount = posts.size();
                        postAdapter.notifyItemRangeInserted(startPosition, totalItemCount - startPosition); // 어뎁터에서 추가된 데이터 업데이트
                        break;
                }
            }

            @Override
            public void onFailure(@NotNull Call<Post> call, @NotNull Throwable t) {
                srRefresh.setRefreshing(false);
                pbLoading.setVisibility(View.GONE);
                pbInfiniteScroll.setVisibility(View.GONE);
                dialogHelper.showConfirmDialog((AppCompatActivity) getActivity(), dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.network_not_stable));
                Log.d(TAG, "loadPosts - onFailure : " + t.getMessage());
            }
        });
    }

    private void deletePost(int boardIdx, int position) {
        Call<Void> callDeletePost = retrofitHelper.getRetrofitInterFace().deletePost(USER_SESSION, USER_IDX, boardIdx);
        callDeletePost.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NotNull Call<Void> call, @NotNull Response<Void> response) {
                switch (response.code()) {
                    case 500:
                        retrofitHelper.printRetrofitResponse(TAG, response);
                        dialogHelper.showConfirmDialog((AppCompatActivity) getActivity(), dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.server_error_message));
                        break;

                    case 400:
                        retrofitHelper.printRetrofitResponse(TAG, response);
                        dialogHelper.showConfirmDialog((AppCompatActivity) getActivity(), dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.client_error_message));
                        break;

                    case 200:
                        posts.remove(position);
                        postAdapter.notifyItemRemoved(position);
                        postAdapter.notifyItemRangeChanged(position, posts.size());
                        break;
                }
            }

            @Override
            public void onFailure(@NotNull Call<Void> call, @NotNull Throwable t) {
                dialogHelper.showConfirmDialog((AppCompatActivity) getActivity(), dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.network_not_stable));
                Log.d(TAG, "deletePost - onFailure : " + t.getMessage());
            }
        });
    }

    // 좋아요 상태에 따라 좋아요 업데이트 해주는 메소드 : 중복되는 메소드<-???
    private void updateLikePost(boolean isLike, int position) {
        String type = "POST";
        int boardIdx = posts.get(position).getBoardIdx();
        Call<Post> callUpdateLike = retrofitHelper.getRetrofitInterFace().updateLikePost(USER_SESSION, USER_IDX, type, boardIdx, isLike);
        callUpdateLike.enqueue(new Callback<Post>() {
            @Override
            public void onResponse(@NotNull Call<Post> call, @NotNull Response<Post> response) {
                switch (response.code()) {
                    case 500:
                        retrofitHelper.printRetrofitResponse(TAG, response);

                        // 에러 발생 시 좋아요를 반영하지 않음
                        posts.get(position).setLike(!isLike);
                        postAdapter.notifyItemChanged(position);
                        postAdapter.notifyItemRangeChanged(position, posts.size());

                        dialogHelper.showConfirmDialog((AppCompatActivity) getActivity(), dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.server_error_message));
                        break;

                    case 400:
                        retrofitHelper.printRetrofitResponse(TAG, response);

                        // 에러 발생 시 좋아요를 반영하지 않음
                        posts.get(position).setLike(!isLike);
                        postAdapter.notifyItemChanged(position);
                        postAdapter.notifyItemRangeChanged(position, posts.size());

                        dialogHelper.showConfirmDialog((AppCompatActivity) getActivity(), dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.client_error_message));
                        break;

                    case 204:
                        retrofitHelper.printRetrofitResponse(TAG, response);
                        break;

                    case 200:
                        Log.d(TAG, "updateLikePost - onResponse : " + response);
                        assert response.body() != null;
                        boolean isLike = response.body().isLike();
                        int likes = response.body().getLikes();

                        posts.get(position).setLike(isLike);
                        posts.get(position).setLikes(likes);
                        postAdapter.notifyItemChanged(position);
                        postAdapter.notifyItemRangeChanged(position, posts.size());

                        if (isLike) {
                            Snackbar.make(getActivity().getWindow().getDecorView().getRootView(), R.string.like_plus, Snackbar.LENGTH_LONG).show();
                        } else {
                            Snackbar.make(getActivity().getWindow().getDecorView().getRootView(), R.string.like_minus, Snackbar.LENGTH_LONG).show();
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