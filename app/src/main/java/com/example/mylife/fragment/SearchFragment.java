package com.example.mylife.fragment;

import android.app.Activity;
import android.content.Context;
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

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.mylife.R;
import com.example.mylife.activity.OnePostActivity;
import com.example.mylife.activity.OtherUserPageActivity;
import com.example.mylife.adapter.ItemClickListener;
import com.example.mylife.adapter.PostAdapter;
import com.example.mylife.adapter.SquarePostAdapter;
import com.example.mylife.adapter.UserAdapter;
import com.example.mylife.item.Post;
import com.example.mylife.item.User;
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

public class SearchFragment extends Fragment implements View.OnClickListener, ItemClickListener, SwipeRefreshLayout.OnRefreshListener {
    private final String TAG = "SearchFragment";
    private final RetrofitHelper retrofitHelper = RetrofitHelper.getInstance();
    private final NetworkConnection networkConnection = NetworkConnection.getInstance();
    private final DialogHelper dialogHelper = DialogHelper.getInstance();
    private final MethodHelper methodHelper = MethodHelper.getInstance();
    private Context mContext;
    private Activity mActivity;

    // TODO: SwipeRefreshLayout안에 Recyclerview를 2개 두는 게 작동이 안되어서 2개로 나누었다. 아마 pbInfiniteScroll도 2개를 만들어야할 거 같다.
    private SwipeRefreshLayout srRefreshPost, srRefreshUser;
    private ProgressBar pbLoading, pbInfiniteScroll; // 프로그래스 바

    private RecyclerView rvSearchUsers, rvSearchPosts;
    private ArrayList<Post> posts;
    private ArrayList<User> users;
    private SquarePostAdapter squarePostAdapter;
    private UserAdapter userAdapter;
    private InfiniteScrollListener infiniteScrollListenerPosts;
    private InfiniteScrollListener infiniteScrollListenerUsers;

    private int responseCode; // HTTP 응답코드, isLast 대신에 사용할 것임

    // 페이징을 위한 변수 : 마지막 페이지인지 확인
    private final int limit = 16;

    private ImageButton ibBack;
    private EditText etSearch;
    private Button btnSearch;
    private TextView tvNoItemUser, tvNoItemPost;

    private String searchWord;

    public SearchFragment() {
        super(R.layout.fragment_search);
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
            loadRandomPosts(1);
        }
    }

    private void bindView(View v) {
        ibBack = v.findViewById(R.id.ib_back);
        etSearch = v.findViewById(R.id.et_search);
        btnSearch = v.findViewById(R.id.btn_search);
        tvNoItemUser = v.findViewById(R.id.tv_no_item_user);
        tvNoItemPost = v.findViewById(R.id.tv_no_item_post);
        rvSearchUsers = v.findViewById(R.id.rv_search_user);
        rvSearchPosts = v.findViewById(R.id.rv_search_post);
        pbLoading = v.findViewById(R.id.pb_loading);
        pbInfiniteScroll = v.findViewById(R.id.pb_infinite_scroll);
        srRefreshPost = v.findViewById(R.id.sr_refresh_post);
        srRefreshUser = v.findViewById(R.id.sr_refresh_user);

        ibBack.setOnClickListener(this);
        btnSearch.setOnClickListener(this);
        srRefreshPost.setOnRefreshListener(this);
        srRefreshUser.setOnRefreshListener(this);

        ibBack.setVisibility(View.INVISIBLE);
        btnSearch.setVisibility(View.INVISIBLE);
        tvNoItemUser.setVisibility(View.INVISIBLE);
        tvNoItemPost.setVisibility(View.INVISIBLE);
        rvSearchUsers.setVisibility(View.INVISIBLE);
        rvSearchPosts.setVisibility(View.VISIBLE);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                etSearch.requestFocus();
                if (s.toString().trim().length() == 0) {
                    infiniteScrollListenerUsers.resetState();
                    int itemCountUser = users.size();
                    users.clear();
                    userAdapter.notifyItemRangeRemoved(0, itemCountUser);

                    if (posts.size() == 0) {
                        srRefreshPost.setVisibility(View.INVISIBLE);
                        rvSearchPosts.setVisibility(View.INVISIBLE);
                        tvNoItemPost.setVisibility(View.VISIBLE);
                    } else {
                        srRefreshPost.setVisibility(View.VISIBLE);
                        rvSearchPosts.setVisibility(View.VISIBLE);
                        tvNoItemPost.setVisibility(View.INVISIBLE);
                    }
                    srRefreshUser.setVisibility(View.INVISIBLE);
                    rvSearchUsers.setVisibility(View.INVISIBLE);
                    tvNoItemUser.setVisibility(View.INVISIBLE);
                    ibBack.setEnabled(false);
                    ibBack.setVisibility(View.INVISIBLE);
                    btnSearch.setEnabled(false);
                    btnSearch.setVisibility(View.INVISIBLE);
                } else {
                    srRefreshPost.setVisibility(View.INVISIBLE);
                    rvSearchPosts.setVisibility(View.INVISIBLE);
                    tvNoItemPost.setVisibility(View.INVISIBLE);
                    if (users.size() == 0) {
                        srRefreshUser.setVisibility(View.INVISIBLE);
                        rvSearchUsers.setVisibility(View.INVISIBLE);
                        tvNoItemUser.setVisibility(View.VISIBLE);
                    } else {
                        srRefreshUser.setVisibility(View.VISIBLE);
                        rvSearchUsers.setVisibility(View.VISIBLE);
                        tvNoItemUser.setVisibility(View.INVISIBLE);
                    }
                    ibBack.setEnabled(true);
                    ibBack.setVisibility(View.VISIBLE);
                    btnSearch.setEnabled(true);
                    btnSearch.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void buildRecyclerView() {
        // rvSearchPosts 리사이클러뷰
        posts = new ArrayList<>();
        GridLayoutManager gridLayoutManager = new GridLayoutManager(requireView().getContext(), 3);
        gridLayoutManager.setOrientation(RecyclerView.VERTICAL);
        squarePostAdapter = new SquarePostAdapter(requireContext(), posts, this);
        infiniteScrollListenerPosts = new InfiniteScrollListener(gridLayoutManager, 4) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView recyclerView) {
                if (responseCode == 200) {
                    if (networkConnection.checkNetworkConnection(getActivity()) == TYPE_NOT_CONNECTED) {
                        dialogHelper.showConfirmDialog((AppCompatActivity) getActivity(), dialogHelper.ACTIVITY_FINISH_DIALOG_ID, getString(R.string.no_connected_network));
                    } else {
                        loadRandomPosts(page);
                    }
                }
            }

            @Override
            public void onLastVisibleItemPosition(int lastVisibleItemPosition) {
                if (responseCode == 200) {
                    int lastItemPosition = posts.size() - 1;
                    if (srRefreshPost.isRefreshing()) return;
                    if (lastItemPosition == lastVisibleItemPosition)
                        pbInfiniteScroll.setVisibility(View.VISIBLE);
                }
            }
        };
        RecyclerView.ItemAnimator animatorRvSearchPosts = rvSearchPosts.getItemAnimator();
        if (animatorRvSearchPosts instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animatorRvSearchPosts).setSupportsChangeAnimations(false);
        }
        rvSearchPosts.setLayoutManager(gridLayoutManager);
        rvSearchPosts.setAdapter(squarePostAdapter);
        rvSearchPosts.addOnScrollListener(infiniteScrollListenerPosts);

        // rvSearchUser 리사이클러뷰
        users = new ArrayList<>();
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireView().getContext());
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        userAdapter = new UserAdapter(requireContext(), users, this);

        infiniteScrollListenerUsers = new InfiniteScrollListener(layoutManager, 4) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView recyclerView) {
                if (responseCode == 200) {
                    if (networkConnection.checkNetworkConnection(getActivity()) == TYPE_NOT_CONNECTED) {
                        dialogHelper.showConfirmDialog((AppCompatActivity) getActivity(), dialogHelper.ACTIVITY_FINISH_DIALOG_ID, getString(R.string.no_connected_network));
                    } else {
                        loadUsers(page, searchWord);
                    }
                }
            }

            @Override
            public void onLastVisibleItemPosition(int lastVisibleItemPosition) {
                if (responseCode == 200) {
                    int lastItemPosition = users.size() - 1;
                    if (srRefreshUser.isRefreshing()) return;
                    if (lastItemPosition == lastVisibleItemPosition)
                        pbInfiniteScroll.setVisibility(View.VISIBLE);
                }
            }
        };
        RecyclerView.ItemAnimator animatorRvSearchUser = rvSearchUsers.getItemAnimator();
        if (animatorRvSearchUser instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animatorRvSearchUser).setSupportsChangeAnimations(false);
        }
//        rvCommunity.setHasFixedSize(true);
        rvSearchUsers.setLayoutManager(layoutManager);
        rvSearchUsers.setAdapter(userAdapter);
        rvSearchUsers.addOnScrollListener(infiniteScrollListenerUsers);
    }

    /**
     * ------------------------------- category 1. 뷰 리스너 -------------------------------
     */
    @Override
    public void onClickItem(View view, int position) {
        switch (view.getId()) {
            // 게시글 리사이클러뷰
            case R.id.iv_square_post:
                int boardIdx = posts.get(position).getBoardIdx();
                Intent toOnePostIntent = new Intent(mContext, OnePostActivity.class);
                toOnePostIntent.putExtra("board_idx", boardIdx);
                startActivity(toOnePostIntent);
                break;
            // 유저 리사이클러뷰
            case R.id.tv_name:
                int userIdx = users.get(position).getUserIdx();
                if (USER_IDX == userIdx) {
                    // TODO: 나의 이름을 눌렀으면 MyFragment로 이동한다? -> 인스타그램 보니까 마이페이지로 와도 뒤로가기도 되고 잘 되던데, Fragment로 조작하는건가?
                    // TODO: 이거는 OtherUserPageActivity나 기타 Fragment로 구현될만한 것들은 Fragment로 구현되도록 바꿔야할듯?
                } else {
                    // TODO: 다른 사람의 이름을 눌렀으면 OtherUserPageActivity로 이동한다.
                    Intent toOtherUserPageIntent = new Intent(mContext, OtherUserPageActivity.class);
                    toOtherUserPageIntent.putExtra("user_idx", userIdx);
                    startActivity(toOtherUserPageIntent);
                }
                break;
        }
    }

    @Override
    public void onClick(View v) {
        if (ibBack.equals(v)) {
            onRefresh();
        } else if (btnSearch.equals(v)) {
            searchWord = etSearch.getText().toString();
            loadUsers(1, searchWord);
        }
    }

    @Override
    public void onRefresh() {
        etSearch.setText(null);

        if (posts.size() == 0) {
            srRefreshPost.setVisibility(View.INVISIBLE);
            rvSearchPosts.setVisibility(View.INVISIBLE);
            tvNoItemPost.setVisibility(View.VISIBLE);
        } else {
            srRefreshPost.setVisibility(View.VISIBLE);
            rvSearchPosts.setVisibility(View.VISIBLE);
            tvNoItemPost.setVisibility(View.INVISIBLE);
        }
        srRefreshUser.setVisibility(View.INVISIBLE);
        rvSearchUsers.setVisibility(View.INVISIBLE);
        tvNoItemUser.setVisibility(View.INVISIBLE);
        ibBack.setEnabled(false);
        ibBack.setVisibility(View.INVISIBLE);
        btnSearch.setEnabled(false);
        btnSearch.setVisibility(View.INVISIBLE);

        infiniteScrollListenerPosts.resetState();
        int itemCountPost = posts.size();
        posts.clear();
        squarePostAdapter.notifyItemRangeRemoved(0, itemCountPost);
        loadRandomPosts(1);

        infiniteScrollListenerUsers.resetState();
        int itemCountUser = users.size();
        users.clear();
        userAdapter.notifyItemRangeRemoved(0, itemCountUser);
    }


    /**
     * ------------------------------- category 3. 서버 통신 -------------------------------
     */
    private void loadRandomPosts(int page) {
        Call<Post> callReadSearchPosts = retrofitHelper.getRetrofitInterFace().readSearchPosts(USER_SESSION, USER_IDX, page, limit);
        callReadSearchPosts.enqueue(new Callback<Post>() {
            @Override
            public void onResponse(@NotNull Call<Post> call, @NotNull Response<Post> response) {
                srRefreshPost.setRefreshing(false);
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
                        Log.d(TAG, "loadRandomPosts - onResponse : " + responseCode);
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

                        if (posts.size() == 0) {
                            tvNoItemPost.setVisibility(View.VISIBLE);
                            srRefreshPost.setVisibility(View.INVISIBLE);
                            rvSearchPosts.setVisibility(View.INVISIBLE);
                        } else {
                            tvNoItemPost.setVisibility(View.INVISIBLE);
                            srRefreshPost.setVisibility(View.VISIBLE);
                            rvSearchPosts.setVisibility(View.VISIBLE);
                        }
                        break;
                }
            }

            @Override
            public void onFailure(@NotNull Call<Post> call, @NotNull Throwable t) {
                srRefreshPost.setRefreshing(false);
                pbLoading.setVisibility(View.GONE);
                pbInfiniteScroll.setVisibility(View.GONE);
                dialogHelper.showConfirmDialog((AppCompatActivity) getActivity(), dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.network_not_stable));
                Log.d(TAG, "loadRandomPosts - onFailure : " + t.getMessage());
            }
        });
    }

    private void loadUsers(int page, String searchWord) {
        Call<User> callReadSearchUsers = retrofitHelper.getRetrofitInterFace().readSearchUsers(USER_SESSION, USER_IDX, page, limit, searchWord);
        callReadSearchUsers.enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NotNull Call<User> call, @NotNull Response<User> response) {
                srRefreshUser.setRefreshing(false);
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
                        Log.d(TAG, "loadUsers - onResponse : " + responseCode);
                        responseCode = 204;
                        break;

                    case 200:


                        User user = response.body();
                        responseCode = 200;
                        int startPosition = users.size();
                        assert user != null;
                        users.addAll(user.getUsers()); // 서버에서 응답받은 페이지의 리스트에 데이터 추가
                        int totalItemCount = users.size();
                        userAdapter.notifyItemRangeInserted(startPosition, totalItemCount - startPosition); // 어뎁터에서 추가된 데이터 업데이트

                        if (users.size() == 0) {
                            tvNoItemUser.setVisibility(View.VISIBLE);
                            srRefreshUser.setVisibility(View.INVISIBLE);
                            rvSearchUsers.setVisibility(View.INVISIBLE);
                        } else {
                            tvNoItemUser.setVisibility(View.INVISIBLE);
                            srRefreshUser.setVisibility(View.VISIBLE);
                            rvSearchUsers.setVisibility(View.VISIBLE);
                        }
                        hideKeyboard();
                        break;
                }
            }

            @Override
            public void onFailure(@NotNull Call<User> call, @NotNull Throwable t) {
                srRefreshUser.setRefreshing(false);
                pbLoading.setVisibility(View.GONE);
                pbInfiniteScroll.setVisibility(View.GONE);
                dialogHelper.showConfirmDialog((AppCompatActivity) getActivity(), dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.network_not_stable));
                Log.d(TAG, "loadUsers - onFailure : " + t.getMessage());
            }
        });
    }

    /**
     * ------------------------------- category ?. 유틸리티 -------------------------------
     */
    private void showKeyboard() {
        etSearch.post(() -> {
            InputMethodManager inputMethodManager = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            etSearch.requestFocus();
            if (etSearch != null)
                // TODO: 'toggleSoftInput(int, int)' is deprecated 에 의해서 변경한 함수, 제대로 동작하는 지 확인하기
                inputMethodManager.showSoftInput(etSearch, InputMethodManager.SHOW_FORCED);
        });
    }

    private void hideKeyboard() {
        etSearch.post(() -> {
            InputMethodManager inputMethodManager = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            etSearch.requestFocus();
            if (etSearch != null)
                // TODO: 'toggleSoftInput(int, int)' is deprecated 에 의해서 변경한 함수, 제대로 동작하는 지 확인하기
                inputMethodManager.showSoftInput(etSearch, InputMethodManager.HIDE_IMPLICIT_ONLY);
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