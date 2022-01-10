package com.example.mylife;

import android.app.Application;

/**
 * 공통 변수, 함수 관리
 */
public class MyApplication extends Application {
    private final String TAG = "MyApplication";
    public static String SERVER_URL; // 서버의 URL

    @Override
    public void onCreate() {
        super.onCreate();
        SERVER_URL = getString(R.string.default_server_url);
    }
}
