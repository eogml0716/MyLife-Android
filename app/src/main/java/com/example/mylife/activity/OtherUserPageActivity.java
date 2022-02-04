package com.example.mylife.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.view.View;

import com.example.mylife.R;
import com.example.mylife.adapter.ItemClickListener;
import com.example.mylife.util.DialogHelper;
import com.example.mylife.util.MethodHelper;
import com.example.mylife.util.NetworkConnection;
import com.example.mylife.util.RetrofitHelper;

public class OtherUserPageActivity extends AppCompatActivity implements View.OnClickListener, ItemClickListener, SwipeRefreshLayout.OnRefreshListener {
    private final String TAG = "OtherUserProfileActivity";
    private final RetrofitHelper retrofitHelper = RetrofitHelper.getInstance();
    private final NetworkConnection networkConnection = NetworkConnection.getInstance();
    private final DialogHelper dialogHelper = DialogHelper.getInstance();
    private final MethodHelper methodHelper = MethodHelper.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_user_page);
    }

    /**
     * ------------------------------- category 0. 최초 설정 -------------------------------
     */

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

    }

    /**
     * ------------------------------- category ?. 서버 통신 -------------------------------
     */

    /**
     * ------------------------------- category ?. 유틸리티 -------------------------------
     */

}