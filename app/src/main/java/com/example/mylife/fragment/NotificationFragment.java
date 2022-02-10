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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.mylife.R;
import com.example.mylife.activity.CommentActivity;
import com.example.mylife.activity.EditPostActivity;
import com.example.mylife.activity.OtherUserPageActivity;
import com.example.mylife.adapter.ItemClickListener;
import com.example.mylife.adapter.NotificationAdapter;
import com.example.mylife.adapter.PostAdapter;
import com.example.mylife.item.Notification;
import com.example.mylife.item.Post;
import com.example.mylife.util.DialogHelper;
import com.example.mylife.util.InfiniteScrollListener;
import com.example.mylife.util.MethodHelper;
import com.example.mylife.util.NetworkConnection;
import com.example.mylife.util.RetrofitHelper;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.mylife.MyApplication.USER_IDX;
import static com.example.mylife.MyApplication.USER_SESSION;
import static com.example.mylife.util.NetworkConnection.TYPE_NOT_CONNECTED;

public class NotificationFragment extends Fragment implements View.OnClickListener, ItemClickListener, SwipeRefreshLayout.OnRefreshListener {
    private final String TAG = "NotificationFragment";
    private final RetrofitHelper retrofitHelper = RetrofitHelper.getInstance();
    private final NetworkConnection networkConnection = NetworkConnection.getInstance();
    private final DialogHelper dialogHelper = DialogHelper.getInstance();
    private final MethodHelper methodHelper = MethodHelper.getInstance();
    private Context mContext;
    private Activity mActivity;

    private SwipeRefreshLayout srRefresh;
    private ProgressBar pbLoading, pbInfiniteScroll; // 프로그래스 바
    private TextView tvNoItem;

    private RecyclerView rvNotification;
    private ArrayList<Notification> notifications;
    private NotificationAdapter notificationAdapter;
    private InfiniteScrollListener infiniteScrollListener;

    private int responseCode; // HTTP 응답코드, isLast 대신에 사용할 것임

    // 페이징을 위한 변수 : 마지막 페이지인지 확인
    private final int limit = 5;
    private boolean isLast;

    public NotificationFragment() {
        super(R.layout.fragment_notification);
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
            loadNotifications(1);
        }
    }

    private void bindView(View v) {
        tvNoItem = v.findViewById(R.id.tv_no_item);
        rvNotification = v.findViewById(R.id.rv_notification);
        pbLoading = v.findViewById(R.id.pb_loading);
        pbInfiniteScroll = v.findViewById(R.id.pb_infinite_scroll);
        srRefresh = v.findViewById(R.id.sr_refresh);
        srRefresh.setOnRefreshListener(this);
    }

    private void buildRecyclerView() {
        notifications = new ArrayList<>();
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireView().getContext());
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        notificationAdapter = new NotificationAdapter(requireContext(), notifications, this);
        infiniteScrollListener = new InfiniteScrollListener(layoutManager, 4) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView recyclerView) {
                if (responseCode == 200) {
                    if (networkConnection.checkNetworkConnection(getActivity()) == TYPE_NOT_CONNECTED) {
                        dialogHelper.showConfirmDialog((AppCompatActivity) getActivity(), dialogHelper.ACTIVITY_FINISH_DIALOG_ID, getString(R.string.no_connected_network));
                    } else {
                        loadNotifications(page);
                    }
                }
            }

            @Override
            public void onLastVisibleItemPosition(int lastVisibleItemPosition) {
                if (responseCode == 200) {
                    int lastItemPosition = notifications.size() - 1;
                    if (srRefresh.isRefreshing()) return;
                    if (lastItemPosition == lastVisibleItemPosition)
                        pbInfiniteScroll.setVisibility(View.VISIBLE);
                }
            }
        };
        RecyclerView.ItemAnimator animator = rvNotification.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }
        rvNotification.setLayoutManager(layoutManager);
        rvNotification.setAdapter(notificationAdapter);
        rvNotification.addOnScrollListener(infiniteScrollListener);
    }

    /**
     * ------------------------------- category 1. 뷰 리스너 -------------------------------
     */
    @Override
    public void onClickItem(View view, int position) {

    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onRefresh() {
        infiniteScrollListener.resetState();
        int itemCount = notifications.size();
        notifications.clear();
        notificationAdapter.notifyItemRangeRemoved(0, itemCount);
        loadNotifications(1);
    }

    /**
     * ------------------------------- category 3. 서버 통신 -------------------------------
     */
    private void loadNotifications(int page) {
        Call<Notification> callReadNotifications = retrofitHelper.getRetrofitInterFace().readNotifications(USER_SESSION, USER_IDX, page, limit);
        callReadNotifications.enqueue(new Callback<Notification>() {
            @Override
            public void onResponse(@NotNull Call<Notification> call, @NotNull Response<Notification> response) {
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
                        Log.d(TAG, "loadNotifications - onResponse : " + responseCode);
                        responseCode = 204;
                        break;

                    case 200:
                        Notification notification = response.body();
                        responseCode = 200;
                        int startPosition = notifications.size();
                        assert notification != null;
                        notifications.addAll(notification.getNotifications()); // 서버에서 응답받은 페이지의 리스트에 데이터 추가
                        int totalItemCount = notifications.size();
                        notificationAdapter.notifyItemRangeInserted(startPosition, totalItemCount - startPosition); // 어뎁터에서 추가된 데이터 업데이트
                        Log.d(TAG, "loadNotifications - posts.size() : " + notifications.size());
                        if (notifications.size() == 0) {
                            tvNoItem.setVisibility(View.VISIBLE);
                            srRefresh.setVisibility(View.INVISIBLE);
                            rvNotification.setVisibility(View.INVISIBLE);
                        } else {
                            tvNoItem.setVisibility(View.INVISIBLE);
                            srRefresh.setVisibility(View.VISIBLE);
                            rvNotification.setVisibility(View.VISIBLE);
                        }
                        break;
                }
            }

            @Override
            public void onFailure(@NotNull Call<Notification> call, @NotNull Throwable t) {
                srRefresh.setRefreshing(false);
                pbLoading.setVisibility(View.GONE);
                pbInfiniteScroll.setVisibility(View.GONE);
                dialogHelper.showConfirmDialog((AppCompatActivity) getActivity(), dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.network_not_stable));
                Log.d(TAG, "loadNotifications - onFailure : " + t.getMessage());
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