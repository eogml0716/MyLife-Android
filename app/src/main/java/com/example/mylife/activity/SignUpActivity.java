package com.example.mylife.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mylife.R;
import com.example.mylife.item.User;
import com.example.mylife.util.DialogHelper;
import com.example.mylife.util.NetworkConnection;
import com.example.mylife.util.RetrofitHelper;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.mylife.util.NetworkConnection.TYPE_NOT_CONNECTED;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = "SignUpActivity";
    private final RetrofitHelper retrofitHelper = RetrofitHelper.getInstance();
    private final NetworkConnection networkConnection = NetworkConnection.getInstance();
    private final DialogHelper dialogHelper = DialogHelper.getInstance();

    private TextView tvMoveToLogin;
    private TextInputLayout tilEmail, tilName, tilPassword;
    private Button btnSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        bindView();
    }

    /**
     * ------------------------------- category 0. 최초 설정 -------------------------------
     */
    private void bindView() {
        /* findViewById */
        tvMoveToLogin = findViewById(R.id.tv_move_to_login);
        tilEmail = findViewById(R.id.til_email);
        tilPassword = findViewById(R.id.til_password);
        tilName = findViewById(R.id.til_name);
        btnSignUp = findViewById(R.id.btn_sign_up);

        /* 클릭 이벤트 관련 */
        tvMoveToLogin.setOnClickListener(this);
        btnSignUp.setOnClickListener(this);
    }

    /**
     * ------------------------------- category 1. 뷰 리스너 -------------------------------
     */
    @Override
    public void onClick(View v) {
        // TODO: 구글에서는 switch문을 권장했다가 if문을 권장하는 경우가 생성되었다는데.. 왜 switch문 쓰면 if문으로 바꾸라는 메시지를 줄까?
        if (btnSignUp.equals(v)) {
            // 이메일 정규식 체크
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(getTextInputLayoutString(tilEmail)).matches()) {
                dialogHelper.showConfirmDialog(this, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.email_regex_failed));
                return;
            }

            if (getTextInputLayoutString(tilName) == null) {
                dialogHelper.showConfirmDialog(this, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.edittext_null));
                return;
            }

            // 비밀번호 정규식 체크
            if (!Pattern.matches("^(?=.*[A-Za-z])(?=.*[0-9])(?=.*[$@$!%*#?&]).{8,15}.$", getTextInputLayoutString(tilPassword))) {
                dialogHelper.showConfirmDialog(this, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.password_regex_failed));
                return;
            }

            if (networkConnection.checkNetworkConnection(SignUpActivity.this) == TYPE_NOT_CONNECTED) {
                dialogHelper.showConfirmDialog(this, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.no_connected_network));
            } else {
                signupUser();
            }
        } else if (tvMoveToLogin.equals(v)) {
            Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    /**
     * ------------------------------- category ?. 서버 통신 -------------------------------
     */
    private void signupUser() {
        String email = getTextInputLayoutString(tilEmail);
        String password = getTextInputLayoutString(tilPassword);
        String name = getTextInputLayoutString(tilName);

        Call<User> callSignup = retrofitHelper.getRetrofitInterFace().signup(email, password, name);
        Log.d(TAG, "signupUser - email : " + email);
        Log.d(TAG, "signupUser - password : " + password);
        Log.d(TAG, "signupUser - name : " + name);

        callSignup.enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NotNull Call<User> call, @NotNull Response<User> response) {
                switch (response.code()) {
                    case 400:
                        retrofitHelper.printRetrofitResponse(TAG, response);
                        dialogHelper.showConfirmDialog(SignUpActivity.this, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.client_error_message));
                        break;

                    case 500:
                        retrofitHelper.printRetrofitResponse(TAG, response);
                        dialogHelper.showConfirmDialog(SignUpActivity.this, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.server_error_message));
                        break;

                    case 200:
                        // 로그인 화면으로 이동
                        Intent toLoginIntent = new Intent(SignUpActivity.this, LoginActivity.class);
                        startActivity(toLoginIntent);
                        finish();
                        break;
                }
            }

            @Override
            public void onFailure(@NotNull Call<User> call, @NotNull Throwable t) {
                Log.e(TAG, "signupUser - callSignup failed : " + t.getMessage());
                dialogHelper.showConfirmDialog(SignUpActivity.this, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.network_not_stable));
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
}