package com.example.mylife.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.mylife.R;
import com.example.mylife.util.DialogHelper;
import com.example.mylife.util.NetworkConnection;
import com.example.mylife.util.RetrofitHelper;

import static com.example.mylife.util.NetworkConnection.TYPE_NOT_CONNECTED;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = "LoginActivity";
    private TextView tvMoveToSignUp;
    private RetrofitHelper retrofitHelper = RetrofitHelper.getInstance();
    private final NetworkConnection networkConnection = NetworkConnection.getInstance();
    private DialogHelper dialogHelper = DialogHelper.getInstance();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setInitData();
        bindView();

//        int networkType = networkConnection.checkNetworkConnection(this);
//        if (networkType == TYPE_NOT_CONNECTED) {
//        } else { }
    }

    /**
     * ------------------------------- category 0. 최초 설정 -------------------------------
     */
    private void setInitData() {
    }

    private void bindView() {
        /* findViewById */
        tvMoveToSignUp = findViewById(R.id.tv_move_to_sign_up);

        /* 클릭 이벤트 관련 */
        tvMoveToSignUp.setOnClickListener(this);
    }

    /**
     * ------------------------------- category 1. 뷰 리스너 -------------------------------
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_move_to_sign_up:
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);
                finish();
                break;
        }
    }
}