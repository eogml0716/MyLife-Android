package com.example.mylife.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.view.View;

import com.example.mylife.R;
import com.example.mylife.adapter.ItemClickListener;

public class OnePostActivity extends AppCompatActivity implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_one_post);
    }

    /**
     * ------------------------------- category 0. 최초 설정 -------------------------------
     */

    /**
     * ------------------------------- category 1. 뷰 리스너 -------------------------------
     */
    @Override
    public void onClick(View v) {

    }

    @Override
    public void onRefresh() {

    }

    /**
     * ------------------------------- category ?. 서버 통신 -------------------------------
     */

    /**
     * ------------------------------- category ?. 유틸리티 -------------------------------
     */
}