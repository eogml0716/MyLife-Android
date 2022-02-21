package com.example.mylife.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.mylife.R;
import com.example.mylife.util.DialogHelper;
import com.example.mylife.util.MethodHelper;
import com.example.mylife.util.NetworkConnection;
import com.example.mylife.util.RetrofitHelper;

// 통화 대기화면
public class CallActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = "ChatActivity";
    private final RetrofitHelper retrofitHelper = RetrofitHelper.getInstance();
    private final NetworkConnection networkConnection = NetworkConnection.getInstance();
    private final DialogHelper dialogHelper = DialogHelper.getInstance();
    private final MethodHelper methodHelper = MethodHelper.getInstance();

    private TextView tvName;
    private ImageButton ibCall, ibEndCall;

    private String name;
    private int userIdx, chatRoomIdx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);
        bindView();
        setInitData();
    }

    /**
     * ------------------------------- category 0. 최초 설정 -------------------------------
     */
    private void setInitData() {
        chatRoomIdx = getIntent().getIntExtra("chat_room_idx", 0);
        userIdx = getIntent().getIntExtra("user_idx", 0);
        name = getIntent().getStringExtra("name");

        tvName.setText(name);
    }

    private void bindView() {
        tvName = findViewById(R.id.tv_name);
        ibCall = findViewById(R.id.ib_call);
        ibEndCall = findViewById(R.id.ib_end_call);

        ibCall.setOnClickListener(this);
        ibEndCall.setOnClickListener(this);
    }


    /**
     * ------------------------------- category 1. 뷰 리스너 -------------------------------
     */
    @Override
    public void onClick(View v) {
        if (ibCall.equals(v)) {
            Intent toConnectIntent = new Intent(this,ConnectActivity.class);
            toConnectIntent.putExtra("chat_room_idx", chatRoomIdx);
            toConnectIntent.putExtra("user_idx", userIdx);
            startActivity(toConnectIntent);
            finish();
        } else if (ibEndCall.equals(v)) {
            finish();
        }
    }
}