package com.example.mylife.util;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

// TODO: 어떻게 구현이 되어있는건지 제대로 확인을 더 해보면 좋을 듯
public abstract class InfiniteScrollListener extends RecyclerView.OnScrollListener {
    private static final String TAG = "InfiniteScrollListener";
    private int visibleThreshold;

    // 현재 페이지
    private int currentPage = 1;

    // 전체 아이템 갯수
    private int previousTotalItemCount = 0;

    // 마지막 데이터가 로드되기를 기다리는지 여부
    private boolean loading = true;

    // 처음 페이지
//    private int startingPageIndex = 1;
    private final RecyclerView.LayoutManager layoutManager;

    public InfiniteScrollListener(LinearLayoutManager layoutManager, int visibleThreshold) {
        this.layoutManager = layoutManager;
        this.visibleThreshold = visibleThreshold;
    }

    public InfiniteScrollListener(GridLayoutManager layoutManager, int visibleThreshold) {
        this.layoutManager = layoutManager;
        this.visibleThreshold = visibleThreshold;
        this.visibleThreshold *= layoutManager.getSpanCount();
    }

    public InfiniteScrollListener(StaggeredGridLayoutManager layoutManager, int visibleThreshold) {
        this.layoutManager = layoutManager;
        this.visibleThreshold = visibleThreshold;
        this.visibleThreshold *= layoutManager.getSpanCount();
    }

    private int getLastVisibleItem(int[] lastVisibleItemPositions) {
        int maxSize = 0;
        for (int i = 0; i < lastVisibleItemPositions.length; i++) {
            if (i == 0) {
                maxSize = lastVisibleItemPositions[i];
            } else if (lastVisibleItemPositions[i] > maxSize) {
                maxSize = lastVisibleItemPositions[i];
            }
        }
        return maxSize;
    }

    @Override
    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        int lastVisibleItemPosition = 0;
        int totalItemCount = layoutManager.getItemCount();

        // TODO: 변경하기
        switch (layoutManager.getClass().getName()) {
            case "androidx.recyclerview.widget.StaggeredGridLayoutManager":
                int[] lastVisibleItemPositions = ((StaggeredGridLayoutManager) layoutManager).findLastVisibleItemPositions(null);
                lastVisibleItemPosition = getLastVisibleItem(lastVisibleItemPositions);
                break;

            case "androidx.recyclerview.widget.GridLayoutManager":
                lastVisibleItemPosition = ((GridLayoutManager) layoutManager).findLastVisibleItemPosition();
                break;

            case "androidx.recyclerview.widget.LinearLayoutManager":
                lastVisibleItemPosition = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
                break;

            default:
                Log.e(TAG, "onScrolled: wrong layout manager type");
                break;
        }

        // 이전 항목 수 보다 총 항목 수가 적고
        if (totalItemCount < previousTotalItemCount) {
//            currentPage = startingPageIndex;
            previousTotalItemCount = totalItemCount;
            if (totalItemCount == 0) loading = true;
        }

        // 로딩 중이고, 이전 전체 아이템 갯수 보다 전체 갯수가 많아지면 -> 데이터가 업데이트가 된 것이기 때문에 로딩 false
        if (loading) {
            if (totalItemCount > previousTotalItemCount) {
                previousTotalItemCount = totalItemCount;
                loading = false;
            }
        }

        // 하단 로딩을 위해 마지막 아이템 position 관찰
        onLastVisibleItemPosition(lastVisibleItemPosition);
        // 아이템 하단 겟수가 4개 이하이면 데이터를 더 부를 수 있도록 onLoadMore 을 실행
        if (!loading) {
            if (lastVisibleItemPosition + visibleThreshold > totalItemCount) {
                currentPage++;
                Log.i(TAG, "onRefresh: " + currentPage);
                loading = true;
                onLoadMore(currentPage, totalItemCount, recyclerView);
            }
        }
    }

    // 페이징 값을 모두 초기화하기 위해 사용하는 메소드
    public void resetState() {
        currentPage = 1;
        previousTotalItemCount = 0;
        loading = true;
    }

    // 액티비티에서 추상 메소드 구현하기
    public abstract void onLoadMore(int page, int totalItemsCount, RecyclerView recyclerView);

    public abstract void onLastVisibleItemPosition(int lastVisibleItemPosition);
}

