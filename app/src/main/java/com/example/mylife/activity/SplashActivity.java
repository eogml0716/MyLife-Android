package com.example.mylife.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.example.mylife.R;

/**
 * 로딩화면
 *
 * 기능
 * 1. TODO : 네트워크 연결 체크
 * 2. TODO : 자동 로그인
 */
public class SplashActivity extends AppCompatActivity {
    private final String TAG = "SplashActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        moveToLogin();
    }

    // 로딩 화면에서 로그인 화면으로 넘기기
    private void moveToLogin() {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        }, 1000);
    }
}