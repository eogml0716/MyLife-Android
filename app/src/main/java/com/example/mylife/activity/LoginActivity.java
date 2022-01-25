package com.example.mylife.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.mylife.R;
import com.example.mylife.item.User;
import com.example.mylife.util.DialogHelper;
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

    private TextInputLayout tilEmail, tilPassword;
    private Button btnLogin;

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
    private void setInitData() {
    }

    private void bindView() {
        /* 뷰 바인드 */
        tvMoveToSignUp = findViewById(R.id.tv_move_to_sign_up);
        tilEmail = findViewById(R.id.til_email);
        tilPassword = findViewById(R.id.til_password);
        btnLogin = findViewById(R.id.btn_login);

        /* 리스너 관련 */
        tvMoveToSignUp.setOnClickListener(this);
        btnLogin.setOnClickListener(this);
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
            String email = getTextInputLayoutString(tilEmail);
            String password = getTextInputLayoutString(tilPassword);

            if (networkConnection.checkNetworkConnection(LoginActivity.this) == TYPE_NOT_CONNECTED) {
                dialogHelper.showConfirmDialog(this, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.no_connected_network));
            } else {
                login(email, password);
            }
        }
    }

    /**
     * ------------------------------- category ?. 서버 통신 -------------------------------
     */
    private void login(String email, String password) {
        Call<User> callGeneralLogin = retrofitHelper.getRetrofitInterFace().generalLogin(email, password);
        Log.d(TAG, "login - email : " + email);
        Log.d(TAG, "login - password : " + password);

        callGeneralLogin.enqueue(new Callback<User>() {
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

                    case 200:
                        // 회원인 경우. 유저 정보를 SharedPreference에 저장한 후 메인 화면으로 이동한다.
                        // SharedPreference에 저장하는 유저 정보 : 로그인 타입, 유저 세션, 유저 인덱스, 유저 이메일, 유저 닉네임, 유저 프로필 이미지 URL
                        String userSession = response.headers().get("Set-Cookie");
                        int userIdx = response.body().getUserIdx();
                        String email = response.body().getEmail();
                        String name = response.body().getName();
                        String profileImageUrl = response.body().getProfileImageUrl();

                        // TODO: 유저 로그인 타입은 자동 로그인, 네이버 로그인, 카카오 로그인 구현하면 다시 건드리기
//                        LOGIN_TYPE = type;
                        USER_SESSION = userSession;
                        USER_IDX = userIdx;
                        USER_EMAIL = email;
                        USER_NAME = name;
                        PROFILE_IMAGE_URL = profileImageUrl;

                        SharedPreferences.Editor editor = getSharedPreferences("auto", Activity.MODE_PRIVATE).edit();
//                        editor.putInt(getString(R.string.login_type), type);
                        editor.putString(getString(R.string.user_session), userSession);
                        editor.putInt(getString(R.string.user_idx), userIdx);
                        editor.putString(getString(R.string.email), email);
                        editor.putString(getString(R.string.name), name);
                        editor.putString(getString(R.string.profile_image_url), profileImageUrl);
                        editor.apply();

                        Log.d(TAG, "login - userSession : " + userSession);
                        Log.d(TAG, "login - userIdx : " + userIdx);
                        Log.d(TAG, "login - email : " + email);
                        Log.d(TAG, "login - name : " + name);
                        Log.d(TAG, "login - profileImageUrl : " + profileImageUrl);

                        // TODO: 스낵바 메시지 안 뜸
                        showSnackBar(LoginActivity.this, R.string.login_success);

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
    private String getTextInputLayoutString(TextInputLayout textInputLayout) {
        // 공백이 아닐 때 처리할 내용
        if (textInputLayout.getEditText().getText().length() != 0) {
            Log.d(TAG, "getEditTextValue - textInputLayout.getEditText().getText() : " + textInputLayout.getEditText().getText());
            return textInputLayout.getEditText().getText().toString();
        }
        // 공백일 때 처리할 내용
        Log.e(TAG, "getEditTextValue - textInputLayout.getEditText().getText() 공백일 때 : " + textInputLayout.getEditText().getText());
        return null;
    }

    public void showSnackBar(Activity activity, int message){
        View rootView = activity.getWindow().getDecorView().findViewById(android.R.id.content);
        Snackbar.make(rootView, message, BaseTransientBottomBar.LENGTH_INDEFINITE).show();
    }
}