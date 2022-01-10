package com.example.mylife.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.mylife.R;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = "SignUpActivity";
    private TextView tvMoveToLogin;
    private EditText etEmail, etName, etPassword;
    private Button btnSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        bindView();
    }

    private void bindView() {
        /* findViewById */
        tvMoveToLogin = findViewById(R.id.tv_move_to_login);
        etEmail = findViewById(R.id.et_email);
        etName = findViewById(R.id.et_name);
        etPassword = findViewById(R.id.et_password);
        btnSignUp = findViewById(R.id.btn_sign_up);

        /* 클릭 이벤트 관련 */
        tvMoveToLogin.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_move_to_login:
                Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
                break;
        }
    }
}