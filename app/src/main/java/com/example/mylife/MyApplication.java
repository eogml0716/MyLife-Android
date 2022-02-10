package com.example.mylife;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import androidx.annotation.Nullable;

/**
 * 공통 변수, 함수 관리
 */
public class MyApplication extends Application {
    private final String TAG = "MyApplication";
    public static final String CHANNEL_ID = "RtY9ty36WT0bjpIj29G9u1gJfZhNh9";
    public static String SERVER_URL; // 서버의 URL

    // 유저 관련 변수
    public static String LOGIN_TYPE, USER_SESSION, USER_NAME, USER_EMAIL, PROFILE_IMAGE_URL, FIREBASE_TOKEN;
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
        LOGIN_TYPE = userPref.getString(getString(R.string.login_type), "general");
        USER_SESSION = userPref.getString(getString(R.string.user_session), null);
        USER_IDX = userPref.getInt(getString(R.string.user_idx), 0);
        USER_EMAIL = userPref.getString(getString(R.string.email), null);
        USER_NAME = userPref.getString(getString(R.string.email), null);
        PROFILE_IMAGE_URL = userPref.getString(getString(R.string.profile_image_url), null);
        FIREBASE_TOKEN = userPref.getString(getString(R.string.key_firebase_token), null);
    }

    public static String getAppName(Context context) {
        String appName = "";
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo i = pm.getPackageInfo(context.getPackageName(), 0);
            appName = i.applicationInfo.loadLabel(pm) + "";
        } catch(PackageManager.NameNotFoundException e) { }
        return appName;
    }
}
