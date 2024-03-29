package com.example.mylife.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.example.mylife.R;
import com.example.mylife.item.User;
import com.example.mylife.util.DialogHelper;
import com.example.mylife.util.MethodHelper;
import com.example.mylife.util.NetworkConnection;
import com.example.mylife.util.RetrofitHelper;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import org.jetbrains.annotations.NotNull;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.mylife.MyApplication.LOGIN_TYPE;
import static com.example.mylife.MyApplication.USER_SESSION;
import static com.example.mylife.MyApplication.USER_IDX;
import static com.example.mylife.MyApplication.USER_EMAIL;
import static com.example.mylife.MyApplication.USER_NAME;
import static com.example.mylife.MyApplication.PROFILE_IMAGE_URL;
import static com.example.mylife.util.NetworkConnection.TYPE_NOT_CONNECTED;

/**
 * 로그인 화면
 *
 * 기능
 * 1. 일반 로그인
 * 2. TODO : 네이버 로그인
 * 3. TODO : 카카오 로그인
 */
public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = "LoginActivity";
    private TextView tvMoveToSignUp;
    private final RetrofitHelper retrofitHelper = RetrofitHelper.getInstance();
    private final NetworkConnection networkConnection = NetworkConnection.getInstance();
    private final DialogHelper dialogHelper = DialogHelper.getInstance();
    private final MethodHelper methodHelper = MethodHelper.getInstance();

    private TextInputLayout tilEmail, tilPassword;
    private Button btnLogin;
    private CheckBox cbAutoLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setInitData();
        bindView();
    }

    /**
     * ------------------------------- category 0. 최초 설정 -------------------------------
     */
    private void setInitData() { }

    private void bindView() {
        /* 뷰 바인드 */
        tvMoveToSignUp = findViewById(R.id.tv_move_to_sign_up);
        tilEmail = findViewById(R.id.til_email);
        tilPassword = findViewById(R.id.til_password);
        btnLogin = findViewById(R.id.btn_login);
        cbAutoLogin = findViewById(R.id.cb_auto_login);

        /* 리스너 관련 */
        tvMoveToSignUp.setOnClickListener(this);
        btnLogin.setOnClickListener(this);
        cbAutoLogin.setOnClickListener(this);

        cbAutoLogin.setChecked(LOGIN_TYPE.equals("auto"));
    }

    /**
     * ------------------------------- category 1. 뷰 리스너 -------------------------------
     */
    @Override
    public void onClick(View v) {
        if (tvMoveToSignUp.equals(v)) {
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
            finish();
        } else if (btnLogin.equals(v)) {
            String email = methodHelper.getTextInputLayoutString(TAG, tilEmail);
            String password = methodHelper.getTextInputLayoutString(TAG, tilPassword);

            if (networkConnection.checkNetworkConnection(LoginActivity.this) == TYPE_NOT_CONNECTED) {
                dialogHelper.showConfirmDialog(this, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.no_connected_network));
            } else {
                login(email, password);
            }
        } else if (cbAutoLogin.equals(v)) {
            if (LOGIN_TYPE.equals("auto")) {
                LOGIN_TYPE = "general";
            } else {
                LOGIN_TYPE = "auto";
            }
            SharedPreferences.Editor editor = getSharedPreferences("auto", Activity.MODE_PRIVATE).edit();
            editor.putString(getString(R.string.login_type), LOGIN_TYPE);
            editor.apply();
            cbAutoLogin.setChecked(LOGIN_TYPE.equals("auto"));
        }
    }

    /**
     * ------------------------------- category ?. 서버 통신 -------------------------------
     */
    private void login(String email, String password) {
        Call<User> callSigninGeneral = retrofitHelper.getRetrofitInterFace().signinGeneral(email, password);
        Log.d(TAG, "login - email : " + email);
        Log.d(TAG, "login - password : " + password);

        callSigninGeneral.enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NotNull Call<User> call, @NotNull Response<User> response) {
                switch (response.code()) {
                    case 400:
                        retrofitHelper.printRetrofitResponse(TAG, response);
                        dialogHelper.showConfirmDialog(LoginActivity.this, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.client_error_message));
                        break;

                    case 500:
                        retrofitHelper.printRetrofitResponse(TAG, response);
                        dialogHelper.showConfirmDialog(LoginActivity.this, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.server_error_message));
                        break;

                    case 204:
                        dialogHelper.showConfirmDialog(LoginActivity.this, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.login_fail));
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
                        USER_SESSION = userSession;
                        USER_IDX = userIdx;
                        USER_EMAIL = email;
                        USER_NAME = name;
                        PROFILE_IMAGE_URL = profileImageUrl;

                        SharedPreferences.Editor editor = getSharedPreferences("auto", Activity.MODE_PRIVATE).edit();
                        editor.putString(getString(R.string.user_session), USER_SESSION);
                        editor.putInt(getString(R.string.user_idx), USER_IDX);
                        editor.putString(getString(R.string.email), USER_EMAIL);
                        editor.putString(getString(R.string.name), USER_NAME);
                        editor.putString(getString(R.string.profile_image_url), PROFILE_IMAGE_URL);
                        editor.apply();

                        Log.d(TAG, "login - LOGIN_TYPE : " + LOGIN_TYPE);
                        Log.d(TAG, "login - USER_SESSION : " + USER_SESSION);
                        Log.d(TAG, "login - USER_IDX : " + USER_IDX);
                        Log.d(TAG, "login - USER_EMAIL : " + USER_EMAIL);
                        Log.d(TAG, "login - USER_NAME : " + USER_NAME);
                        Log.d(TAG, "login - PROFILE_IMAGE_URL : " + PROFILE_IMAGE_URL);

                        // TODO: 스낵바 메시지 안 뜸
                        methodHelper.showSnackBar(TAG, LoginActivity.this, R.string.login_success);

                        // 메인 화면으로 이동
                        Intent toMainIntent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(toMainIntent);
                        finish();
                        break;
                }
            }

            @Override
            public void onFailure(@NotNull Call<User> call, @NotNull Throwable t) {
                Log.e(TAG, "login - callSignup failed : " + t.getMessage());
                dialogHelper.showConfirmDialog(LoginActivity.this, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.network_not_stable));
            }
        });
    }

    /**
     * ------------------------------- category ?. 유틸리티 -------------------------------
     */
}