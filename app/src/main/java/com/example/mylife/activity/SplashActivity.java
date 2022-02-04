package com.example.mylife.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.mylife.R;
import com.example.mylife.item.User;
import com.example.mylife.util.DialogHelper;
import com.example.mylife.util.MethodHelper;
import com.example.mylife.util.NetworkConnection;
import com.example.mylife.util.RetrofitHelper;

import org.jetbrains.annotations.NotNull;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.mylife.MyApplication.LOGIN_TYPE;
import static com.example.mylife.MyApplication.PROFILE_IMAGE_URL;
import static com.example.mylife.MyApplication.USER_EMAIL;
import static com.example.mylife.MyApplication.USER_IDX;
import static com.example.mylife.MyApplication.USER_NAME;
import static com.example.mylife.MyApplication.USER_SESSION;
import static com.example.mylife.util.NetworkConnection.TYPE_NOT_CONNECTED;

/**
 * 로딩화면
 *
 * 기능
 * 1. TODO : 네트워크 연결 체크
 * 2. TODO : 자동 로그인
 */
public class SplashActivity extends AppCompatActivity {
    private final String TAG = "SplashActivity";
    private final RetrofitHelper retrofitHelper = RetrofitHelper.getInstance();
    private final NetworkConnection networkConnection = NetworkConnection.getInstance();
    private final DialogHelper dialogHelper = DialogHelper.getInstance();
    private final MethodHelper methodHelper = MethodHelper.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        moveToMain();
    }

    /**
     * ------------------------------- category 0. 최초 설정 -------------------------------
     */
    private void moveToMain() {
        if (networkConnection.checkNetworkConnection(SplashActivity.this) == TYPE_NOT_CONNECTED) {
            dialogHelper.showConfirmDialog(this, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.no_connected_network));
            return;
        }
        // 사용자 세션이 없으면 로그인 화면으로 넘기기
        if (USER_SESSION == null) {
            moveToLogin();
            return;
        }
        if (LOGIN_TYPE.equals("auto")) {
            autoLogin(); // 자동 로그인
        } else {
            moveToLogin();
        }
    }

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

    /**
     * ------------------------------- category ?. 서버 통신 -------------------------------
     */
    private void autoLogin() {
        Call<User> callSigninAuto = retrofitHelper.getRetrofitInterFace().signinAuto(USER_SESSION, USER_IDX);
        Log.d(TAG, "autoLogin - USER_SESSION : " + USER_SESSION);

        callSigninAuto.enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NotNull Call<User> call, @NotNull Response<User> response) {
                switch (response.code()) {
                    case 400:
                        retrofitHelper.printRetrofitResponse(TAG, response);
                        dialogHelper.showConfirmDialog(SplashActivity.this, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.client_error_message));
                        break;

                    case 500:
                        retrofitHelper.printRetrofitResponse(TAG, response);
                        dialogHelper.showConfirmDialog(SplashActivity.this, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.server_error_message));
                        break;

                    case 200:
                        // 회원인 경우. 유저 정보를 SharedPreference에 저장한 후 메인 화면으로 이동한다.
                        // SharedPreference에 저장하는 유저 정보 : 로그인 타입, 유저 세션, 유저 인덱스, 유저 이메일, 유저 닉네임, 유저 프로필 이미지 URL
                        String userSession = response.headers().get("Set-Cookie");
                        assert response.body() != null;
                        int userIdx = response.body().getUserIdx();
                        String email = response.body().getEmail();
                        String name = response.body().getName();
                        String profileImageUrl = response.body().getProfileImageUrl();

                        // TODO: 유저 로그인 타입은 자동 로그인, 네이버 로그인, 카카오 로그인 구현하면 다시 건드리기
                        LOGIN_TYPE = "auto";
                        USER_SESSION = userSession;
                        USER_IDX = userIdx;
                        USER_EMAIL = email;
                        USER_NAME = name;
                        PROFILE_IMAGE_URL = profileImageUrl;

                        SharedPreferences.Editor editor = getSharedPreferences("auto", Activity.MODE_PRIVATE).edit();
                        editor.putString(getString(R.string.login_type), LOGIN_TYPE);
                        editor.putString(getString(R.string.user_session), userSession);
                        editor.putInt(getString(R.string.user_idx), userIdx);
                        editor.putString(getString(R.string.email), email);
                        editor.putString(getString(R.string.name), name);
                        editor.putString(getString(R.string.profile_image_url), profileImageUrl);
                        editor.apply();

                        Log.d(TAG, "autoLogin - loginType : " + LOGIN_TYPE);
                        Log.d(TAG, "autoLogin - userSession : " + userSession);
                        Log.d(TAG, "autoLogin - userIdx : " + userIdx);
                        Log.d(TAG, "autoLogin - email : " + email);
                        Log.d(TAG, "autoLogin - name : " + name);
                        Log.d(TAG, "autoLogin - profileImageUrl : " + profileImageUrl);

                        // TODO: 스낵바 메시지 안 뜸
                        methodHelper.showSnackBar(TAG, SplashActivity.this, R.string.login_success);

                        // 메인 화면으로 이동
                        Intent toMainIntent = new Intent(SplashActivity.this, MainActivity.class);
                        startActivity(toMainIntent);
                        finish();
                        break;
                }
            }

            @Override
            public void onFailure(@NotNull Call<User> call, @NotNull Throwable t) {
                Log.e(TAG, "autoLogin - callSigninAuto failed : " + t.getMessage());
                dialogHelper.showConfirmDialog(SplashActivity.this, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.network_not_stable));
            }
        });
    }
}