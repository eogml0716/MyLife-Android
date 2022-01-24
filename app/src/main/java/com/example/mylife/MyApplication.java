package com.example.mylife;

import android.app.Application;
import android.content.SharedPreferences;

/**
 * 공통 변수, 함수 관리
 */
public class MyApplication extends Application {
    private final String TAG = "MyApplication";
    public static String SERVER_URL; // 서버의 URL

    // 유저 관련 변수
    public static String LOGIN_TYPE, USER_SESSION, USER_NAME, USER_EMAIL, PROFILE_IMAGE_URL;
    public static int USER_IDX;

    @Override
    public void onCreate() {
        super.onCreate();
        SERVER_URL = getString(R.string.default_server_url);
        loadUserSharedPref();
    }

    private void loadUserSharedPref() {
        // TODO: 왜 main_storage_key라고 작명을 했을까?
        SharedPreferences userPref = getSharedPreferences(getString(R.string.main_storage_key), MODE_PRIVATE);
        LOGIN_TYPE = userPref.getString(getString(R.string.login_type), null);
        USER_SESSION = userPref.getString(getString(R.string.user_session), null);
        USER_IDX = userPref.getInt(getString(R.string.user_idx), 0);
        USER_EMAIL = userPref.getString(getString(R.string.email), null);
        USER_NAME = userPref.getString(getString(R.string.email), null);
        PROFILE_IMAGE_URL = userPref.getString(getString(R.string.profile_image_url), null);
    }
}
