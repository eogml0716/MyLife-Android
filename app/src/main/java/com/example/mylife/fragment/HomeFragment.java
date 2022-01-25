package com.example.mylife.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mylife.R;
import com.example.mylife.adapter.ItemClickListener;


// TODO: item_post 너무 무식하게 margin 준 거 같은데 비율에 맞게 margin을 주는 방법은 없을까?
public class HomeFragment extends Fragment implements View.OnClickListener, ItemClickListener, SwipeRefreshLayout.OnRefreshListener {
    public HomeFragment() {
    }

    public static HomeFragment newInstance(String param1, String param2) {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onClickItem(View view, int position) {

    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onRefresh() {

    }
}